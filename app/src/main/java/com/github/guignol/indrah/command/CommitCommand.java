package com.github.guignol.indrah.command;

import com.github.guignol.indrah.utils.ListUtils;
import com.github.guignol.indrah.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.stream.Stream;

public class CommitCommand extends Command {

    public static CommitCommand allowEmpty(@NotNull Path dir, @NotNull String messages) {
        return new CommitCommand(dir, messages, Mode.ALLOW_EMPTY);
    }

    public static CommitCommand newly(@NotNull Path dir, @NotNull String messages) {
        return new CommitCommand(dir, messages, Mode.NEWLY);
    }

    public static CommitCommand amend(@NotNull Path dir, @NotNull String messages) {
        return new CommitCommand(dir, messages, Mode.AMEND);
    }

    public static CommitCommand noEdit(@NotNull Path dir) {
        return new CommitCommand(dir, null, Mode.NO_EDIT);
    }

    private enum Mode {
        NEWLY, AMEND, NO_EDIT, ALLOW_EMPTY
    }

    @Nullable
    private final String[] messages;
    @NotNull
    private final Mode mode;

    private CommitCommand(@NotNull Path dir, @Nullable String messages, @NotNull Mode mode) {
        super(dir);
        this.mode = mode;
        if (messages == null || messages.isEmpty()) {
            this.messages = null;
        } else {
            this.messages = messages.split("\n");
        }
    }

    @Override
    protected String[] command() {
        final Stream<String> gitCommit = Stream.of("git", "commit");
        final Stream<String> options;
        final Stream<String> messageOption;
        if (messages == null) {
            messageOption = Stream.empty();
        } else {
            messageOption = ListUtils.from(messages).stream()
                    .flatMap(message -> Stream.of("-m", StringUtils.doubleQuotation(message)));
        }
        switch (mode) {
            case NO_EDIT:
                options = Stream.of("--amend", "--no-edit");
                break;
            default:
            case ALLOW_EMPTY:
                options = Stream.concat(Stream.of("--allow-empty"), messageOption);
                break;
            case AMEND:
                options = Stream.concat(Stream.of("--amend"), messageOption);
                break;
            case NEWLY:
                options = messageOption;
                break;
        }
        return Stream.concat(gitCommit, options).toArray(String[]::new);
    }
}
