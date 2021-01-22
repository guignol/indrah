package com.github.guignol.indrah.command;

import java.nio.file.Path;

import static com.github.guignol.indrah.command.DiffCommand.OPTION_EXIT_CODE;

public class DiffDetailCommand extends Command {

    private final boolean cached;

    public DiffDetailCommand(Path dir, boolean cached) {
        super(dir);
        this.cached = cached;
    }

    @Override
    protected String[] command() {
        if (cached) {
            // git diff-index -p --cached HEAD
            // diff-indexだと、コミットが無い場合に HEADと比較できない
//            return new String[]{"git", "diff-index", "-p", OPTION_EXIT_CODE, "--cached", "HEAD"};
            return new CommandBuilder("git")
                    .add(CommandOptions.diffStandard())
                    .add("diff", "-p", OPTION_EXIT_CODE, "--cached", "-M")
                    .build();
        } else {
            // git diff-files -p
//            return new String[]{"git", "diff-files", "-p", OPTION_EXIT_CODE};
            return new CommandBuilder("git")
                    .add(CommandOptions.diffStandard())
                    .add("diff", "-p", OPTION_EXIT_CODE)
                    .build();
        }
    }
}
