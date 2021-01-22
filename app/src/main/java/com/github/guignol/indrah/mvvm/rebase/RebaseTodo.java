package com.github.guignol.indrah.mvvm.rebase;

import com.github.guignol.indrah.model.CommitLog;
import com.github.guignol.indrah.utils.FileUtils;
import com.github.guignol.indrah.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RebaseTodo {

    private final List<CommitLog> targets;
    public final String ontoCommit;

    public RebaseTodo(List<CommitLog> targets, String ontoCommit) {
        this.targets = targets;
        this.ontoCommit = ontoCommit;
    }

    public Consumer<Path> pathHandler() {
        return path -> {
            final EditType editType = EditType.from(path);
            switch (editType) {
                case TODO:
                    editTodo(path);
                    break;
                case MESSAGE:
                    editMessage(path);
                    break;
                case UNKNOWN:
                    break;
            }
        };
    }

    private void editTodo(@NotNull Path path) {
        try {
            System.out.println("before reorder:");
            final List<TodoLine> todoList = new ArrayList<>();
            final List<String> etc = new ArrayList<>();
            Files.readAllLines(path).forEach(line -> {
                final TodoLine todo = this.editTodoLine(TodoLine.from(line));
                if (todo.commitHash == null) {
                    etc.add(todo.line);
                } else {
                    System.out.println(todo.line);
                    todoList.add(todo);
                }
            });
            if (todoList.size() != targets.size()) {
                System.out.println("failed reorder");
                return;
            }
            final Stream<String> reOrdered = reOrderTodo(todoList);
            if (reOrdered == null) {
                System.out.println("failed reorder");
                return;
            }
            System.out.println();
            System.out.println("after reorder:");
            final String content = Stream.concat(reOrdered.peek(System.out::println), etc.stream())
                    .collect(Collectors.joining("\n"));
            FileUtils.replaceFile(path, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    private TodoLine editTodoLine(TodoLine todoLine) {
        final String defaultCommand = "pick ";
        if (todoLine.line.startsWith(defaultCommand)) {
            final CommitLog commitLog = search(todoLine.commitHash);
            if (commitLog != null) {
                // # Commands:
                // # p, pick = use commit
                // # r, reword = use commit, but edit the commit message
                // # e, edit = use commit, but stop for amending
                // # s, squash = use commit, but meld into previous commit
                // # f, fixup = like "squash", but discard this commit's log message
                // # x, exec = run command (the rest of the line) using shell
                // # d, drop = remove commit
                if (commitLog.hasNewMessage()) {
                    final String newLine = "r " + todoLine.line.substring(defaultCommand.length());
                    return TodoLine.from(newLine);
                }
            }
        }
        return todoLine;
    }

    @Nullable
    private CommitLog search(String commitPrefix) {
        for (CommitLog target : targets) {
            if (target.commit.startsWith(commitPrefix)) {
                return target;
            }
        }
        return null;
    }

    @Nullable
    private static String searchFromTodo(CommitLog commitLog, List<TodoLine> todoList) {
        for (final TodoLine todo : todoList) {
            final String hash = todo.commitHash;
            if (hash == null) {
                return null;
            }
            if (commitLog.commit.startsWith(hash)) {
                return todo.line;
            }
        }
        return null;
    }

    @Nullable
    private Stream<String> reOrderTodo(List<TodoLine> todoList) {
        final Stream.Builder<String> builder = Stream.builder();
        for (CommitLog commitLog : targets) {
            final String todo = searchFromTodo(commitLog, todoList);
            if (todo == null) {
                return null;
            }
            builder.accept(todo);
        }
        return builder.build();
    }

    private void editMessage(@NotNull Path path) {
        try {
            final String mark = "# Next command to do";
            final String mark2 = "# Next commands to do";
            final String mark3 = "# No commands remaining";
            final List<String> lines = Files.readAllLines(path);
            for (int i = 0; i < lines.size(); i++) {
                final String s = lines.get(i);
                if (s.startsWith(mark)
                        || s.startsWith(mark2)
                        || s.startsWith(mark3)) {
                    ///////////////////////////////////////////////////// example
                    // # Last commands done (2 commands done):
                    // #    p d18465c reformatting
                    // #    r 51756d0 updated instructions
                    // # Next commands to do (2 remaining commands):
                    // #    r 3babf57 Update README.md
                    // #    r 20e8dc2 added a license
                    ///////////////////////////////////////////////////// example
                    // # Last commands done (3 commands done):
                    // #    r 51756d0 updated instructions
                    // #    r 3babf57 Update README.md
                    // # Next command to do (1 remaining command):
                    // #    r 20e8dc2 added a license
                    ///////////////////////////////////////////////////// example
                    // # Last commands done (4 commands done):
                    // #    r 3babf57 Update README.md
                    // #    r 20e8dc2 added a license
                    // # No commands remaining.
                    final String[] line = StringUtils.splitWithWhiteSpace(lines.get(i - 1));
                    final String command = line[1];
                    final String commitHash = line[2];
                    final CommitLog commitLog = search(commitHash);
                    if (commitLog != null) {
                        // # Commands:
                        // # p, pick = use commit
                        // # r, reword = use commit, but edit the commit message
                        // # e, edit = use commit, but stop for amending
                        // # s, squash = use commit, but meld into previous commit
                        // # f, fixup = like "squash", but discard this commit's log message
                        // # x, exec = run command (the rest of the line) using shell
                        // # d, drop = remove commit
                        if (commitLog.hasNewMessage() && command.startsWith("r")) {
                            final String newMessage = commitLog.getNewMessage();
                            FileUtils.replaceFile(path, newMessage);
                            System.out.println(newMessage);
                            return;
                        }
                    }
                    break;
                }
            }
            Files.readAllLines(path).forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private enum EditType {
        TODO, MESSAGE, UNKNOWN;

        private static EditType from(@NotNull Path path) {
            if (path.endsWith(".git/rebase-merge/git-rebase-todo")) {
                return TODO;
            } else if (path.endsWith(".git/COMMIT_EDITMSG")) {
                return MESSAGE;
            } else {
                return UNKNOWN;
            }
        }
    }

    private static class TodoLine {
        @NotNull
        private final String line;
        @Nullable
        private final String commitHash;

        private TodoLine(@NotNull String line, @Nullable String commitHash) {
            this.line = line;
            this.commitHash = commitHash;
        }

        static TodoLine from(@NotNull String lineOfTodo) {
            final String commitHash;
            if (lineOfTodo.isEmpty() || lineOfTodo.startsWith("#")) {
                commitHash = null;
            } else {
                commitHash = StringUtils.splitWithWhiteSpace(lineOfTodo)[1];
            }
            return new TodoLine(lineOfTodo, commitHash);
        }
    }
}
