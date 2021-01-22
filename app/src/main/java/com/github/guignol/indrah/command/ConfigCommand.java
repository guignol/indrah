package com.github.guignol.indrah.command;

import com.github.guignol.indrah.utils.StringUtils;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigCommand {

    public static ConfigCommand with(Path dir) {
        return new ConfigCommand(dir);
    }

    @NotNull
    private static Function<CommandOutput, String> mapper = output -> {
        if (output.exitCode == 0) {
            return output.standardInputs.get(0);
        } else {
            return "";
        }
    };

    private final Path dir;

    private ConfigCommand(Path dir) {
        this.dir = dir;
    }

    public Single<String> global(@NotNull final String key) {
        return getCommand("--global", key)
                .toSingle().map(mapper);
    }

    public Single<String> global(@NotNull final String key, @NotNull final String value) {
        return getCommand("--global", key, StringUtils.doubleQuotation(value))
                .toSingle().map(mapper);
    }

    public Single<String> local(@NotNull final String key) {
        return getCommand("--local", key)
                .toSingle().map(mapper);
    }

    public Single<String> local(@NotNull final String key, @NotNull final String value) {
        return getCommand("--local", key, StringUtils.doubleQuotation(value))
                .toSingle().map(mapper);
    }

    private Command getCommand(@NotNull String... options) {
        return new Command(dir) {
            @Override
            protected String[] command() {
                final List<String> commands = new ArrayList<>();
                commands.add("git");
                commands.add("config");
                commands.addAll(Arrays.asList(options));
                return commands.toArray(new String[0]);
            }
        };
    }
}
