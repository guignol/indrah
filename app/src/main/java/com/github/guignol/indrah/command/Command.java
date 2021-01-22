package com.github.guignol.indrah.command;

import io.reactivex.Single;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class Command {

    public static Command fromOneLine(Path dir, String oneLine, String... rest) {
        return new Command(dir) {
            @Override
            protected String[] command() {
                // 例えばgit commit -m "initial commit"を正しく区切れないが、機能する。
                // しかし、パスを扱う場合に空白が挟まると問題になるので、そういう引数はrestに詰める
                return Stream.of(oneLine.split(" "), rest).flatMap(Arrays::stream).toArray(String[]::new);
            }
        };
    }

    private static final SwingWorkerDialog WORKER_DIALOG = new SwingWorkerDialog();

    public static void initDialog(JFrame owner) {
        WORKER_DIALOG.init(owner);
    }

    private final Path currentDir;

    private final CommandExecutor executor;

    protected Command(Path dir) {
        this.currentDir = dir;
        this.executor = new CommandExecutor();
    }

    protected Command(Path dir, final CommandExecutor executor) {
        this.currentDir = dir;
        this.executor = executor;
    }

    abstract protected String[] command();

    protected boolean isValid() {
        return currentDir != null && Files.exists(currentDir);
    }

    public void call() {
        call(null);
    }

    public void call(@Nullable Consumer<CommandOutput> callback) {
        if (!isValid()) {
            printCommand(command());
            CommandOutput.none(callback);
            return;
        }

        new Worker(WORKER_DIALOG, executor).start(currentDir, command(), callback);
    }

    public Single<CommandOutput> toSingle() {
        return Single.create(e -> call(e::onSuccess));
    }

    private static void printCommand(String[] line) {
        for (String command : line) {
            System.out.print(" " + command);
        }
        System.out.println();
    }

    static class Worker {

        private final CommandExecutor executor;
        private final SwingWorkerDialog workerDialog;

        Worker(SwingWorkerDialog workerDialog, CommandExecutor executor) {
            this.workerDialog = workerDialog;
            this.executor = executor;
        }

        void start(@NotNull final Path workingDir,
                   @NotNull final String[] command,
                   @Nullable Consumer<CommandOutput> callback) {
            //        System.out.println("start: " + Thread.currentThread().getName());
            if (workerDialog.hasOwner()) {
                // workerDialogがある場合はEDTでコールバックを呼ぶ
                final SwingWorker<?, Void> worker = executor.executeOnSwingWorker(workingDir, command, callback);
                worker.addPropertyChangeListener(workerDialog.getListener());
                worker.execute();
                // executeの後に呼ばないとリスナーが効かない
                workerDialog.show();
            } else {
                // TODO 分けないほうがいいのでは？
                // TODO でも、呼び元と同じスレッドでコールバック呼ぶの難しい？
                executor.executeOnCurrentThread(workingDir, command, callback);
            }
        }
    }
}
