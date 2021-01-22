package com.github.guignol.indrah.command;

import com.github.guignol.indrah.model.DiffStatus;
import com.github.guignol.indrah.model.DiffSummary;
import io.reactivex.Single;

import java.nio.file.Path;
import java.util.List;

public class DiffNameUntrackedCommand extends Command {

    public DiffNameUntrackedCommand(Path dir) {
        super(dir);
    }

    @Override
    protected String[] command() {
        return new CommandBuilder("git")
                .add(CommandOptions.quotePath(false))
                .add("ls-files", "-o", "--exclude-standard")
                .build();
    }

    public Single<List<DiffSummary>> toSummaries() {
        return toSingle().map(DiffNameUntrackedCommand::summaries);
    }

    private static List<DiffSummary> summaries(CommandOutput output) {
        return output.map(fileName -> DiffStatus.Added.withNames(null, fileName));
    }
}
