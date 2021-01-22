package com.github.guignol.indrah.command;

import com.github.guignol.indrah.model.DiffSummary;

import java.nio.file.Path;

public abstract class DiffCommand extends CommandByName {

    public final DiffSummary summary;

    /**
     * Make the program exit with codes similar to diff(1).
     * That is, it exits with 1 if there were differences and 0 means no differences.
     */
    static final String OPTION_EXIT_CODE = "--exit-code";
    public static final int EXIT_WITH_DIFF = 1;
    public static final int EXIT_WITHOUT_DIFF = 0;

    DiffCommand(Path dir, DiffSummary summary) {
        super(dir, summary.names.any());
        this.summary = summary;
    }
}
