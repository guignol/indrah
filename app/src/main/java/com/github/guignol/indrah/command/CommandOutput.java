package com.github.guignol.indrah.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandOutput {

    public static final CommandOutput NULL = new CommandOutput(-1, new ArrayList<>(), new ArrayList<>());
    public static final CommandOutput NEXT = new CommandOutput(0, new ArrayList<>(), new ArrayList<>());

    public final int exitCode;
    @NotNull
    public final List<String> standardInputs;
    @NotNull
    public final List<String> standardErrors;

    public CommandOutput(int exitCode,
                         @NotNull List<String> standardInputs,
                         @NotNull List<String> standardErrors) {
        this.exitCode = exitCode;
        this.standardInputs = standardInputs;
        this.standardErrors = standardErrors;
    }

    public void print() {
        System.out.println(toPretty());
    }

    public String toPretty() {
        final StringBuilder builder = new StringBuilder("\n").append("exit: ").append(exitCode).append("\n");
        standardInputs.forEach(line -> {
            builder.append(line);
            builder.append("\n");
        });
        standardErrors.forEach(line -> {
            builder.append(line);
            builder.append("\n");
        });
        return builder.toString();
    }

    public <T> List<T> map(Function<String, T> mapper) {
        return this.standardInputs.stream().map(mapper).collect(Collectors.toList());
    }

    public static void none(@Nullable Consumer<CommandOutput> callback) {
        if (callback != null) {
            callback.accept(CommandOutput.NULL);
        }
    }
}
