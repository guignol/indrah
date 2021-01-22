package com.github.guignol.indrah.command;

import java.nio.file.Path;

public class DiffNameOnlyCommand extends Command {

    private final boolean cached;

    public DiffNameOnlyCommand(Path dir, boolean cached) {
        super(dir);
        this.cached = cached;
    }

    @Override
    protected String[] command() {
        // "git diff --name-only" + (cached ? " --cached" : "")
        if (cached) {
            return new CommandBuilder("git")
                    .add(CommandOptions.diffStandard())
                    .add("diff", "--name-only", "--cached")
                    .build();
        } else {
            return new CommandBuilder("git")
                    .add(CommandOptions.diffStandard())
                    .add("diff", "--name-only")
                    .build();
        }
    }
}
