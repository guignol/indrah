package com.github.guignol.indrah.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class CommandExecutor {

    private static class Result {
        private final CommandOutput output;
        private final Throwable throwable;

        private Result(CommandOutput output, Throwable throwable) {
            this.output = output;
            this.throwable = throwable;
        }
    }

    @Nullable
    private Consumer<CommandOutput> callback;

    void executeOnCurrentThread(@NotNull final Path workingDir,
                                @NotNull String[] command,
                                @Nullable final Consumer<CommandOutput> callback) {
        final Result result = execute(workingDir, command, callback);
        onPostExecute(result);
    }

    SwingWorker<?, Void> executeOnSwingWorker(@NotNull final Path workingDir,
                                              @NotNull final String[] command,
                                              @Nullable final Consumer<CommandOutput> callback) {
        return new SwingWorker<Result, Void>() {

            @Override
            protected Result doInBackground() {
                return CommandExecutor.this.execute(workingDir, command, callback);
            }

            @Override
            protected void done() {
                // SwingWorker::done is executed on the event dispatching thread.
                try {
                    CommandExecutor.this.onPostExecute(get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    CommandExecutor.this.onPostExecute(new Result(null, e));
                }
            }
        };
    }

    @NotNull
    private Result execute(@NotNull final Path workingDir,
                           @NotNull String[] command,
                           @Nullable final Consumer<CommandOutput> callback) {
        this.callback = callback;
//        System.out.println("execute: " + Thread.currentThread().getName());
//        final Runtime runtime = Runtime.getRuntime();
        printCommand(command);
//            final Process process = runtime.exec(command, null, workingDir.toFile());
        try {
            final Process process = getProcess(workingDir, command);
            final CommandOutput output = getOutput(process);
            return new Result(output, null);
        } catch (IOException | InterruptedException e) {
            return new Result(null, e);
        }
    }

    @NotNull
    private Process getProcess(@NotNull final Path workingDir, String[] command) throws IOException {
        final ProcessBuilder processBuilder = new ProcessBuilder().command(command).directory(workingDir.toFile());
        final Map<String, String> env = processBuilder.environment();
        this.getEnvironment().forEach(env::put);
        return processBuilder.start();
    }

    @NotNull
    protected Map<String, String> getEnvironment() {
        return new HashMap<>();
    }

    @NotNull
    protected CommandOutput getOutput(Process process) throws InterruptedException {
        final InputStreamThread input = InputStreamThread.startWith(process.getInputStream());
        final InputStreamThread error = InputStreamThread.startWith(process.getErrorStream());
        //プロセスの終了待ち
        process.waitFor();
        return new CommandOutput(process.exitValue(), input.get(), error.get());
    }

    private void onPostExecute(final Result result) {
//        System.out.println("onPostExecute: " + Thread.currentThread().getName());
        if (result.throwable != null && result.output == null) {
            result.throwable.printStackTrace();
            CommandOutput.none(callback);
        } else if (callback != null) {
            callback.accept(result.output);
        }
    }

    private static void printCommand(String[] line) {
        for (String command : line) {
            System.out.print(" " + command);
        }
        System.out.println();
    }
}
