package com.github.guignol.indrah.command;

import java.util.Arrays;
import java.util.stream.Stream;

class CommandBuilder {

    private Stream<String> commands;

    CommandBuilder(String command) {
        this.commands = Stream.of(command);
    }

    CommandBuilder(String... commands) {
        this.commands = Arrays.stream(commands);
    }

    CommandBuilder add(String... options) {
        commands = Stream.concat(commands, Arrays.stream(options));
        return this;
    }

    CommandBuilder add(Stream<String> options) {
        commands = Stream.concat(commands, options);
        return this;
    }

    String[] build() {
        return commands.toArray(String[]::new);
    }
}
