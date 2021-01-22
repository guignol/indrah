package com.github.guignol.indrah.command;

import com.github.guignol.indrah.model.DiffStatus;
import com.github.guignol.indrah.model.DiffSummary;
import com.github.guignol.indrah.utils.ListUtils;
import io.reactivex.Single;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public class DiffNameStatusCommand extends Command {

    public static final String SEP = "\t";

    private final boolean cached;

    public DiffNameStatusCommand(Path dir, boolean cached) {
        super(dir);
        this.cached = cached;
    }

    @Override
    protected String[] command() {
        // "git diff --name-only" + (cached ? " --cached" : "")
        if (cached) {
            return new CommandBuilder("git")
                    .add(CommandOptions.diffStandard())
                    .add("diff", "--name-status", "--cached", "-M")
                    .build();
        } else {
            return new CommandBuilder("git")
                    .add(CommandOptions.diffStandard())
                    .add("diff", "--name-status")
                    .build();
        }
    }

    public Single<List<DiffSummary>> toSummaries() {
        return toSingle().map(DiffNameStatusCommand::summaries);
    }

    private static List<DiffSummary> summaries(CommandOutput output) {
        return ListUtils.map(output.standardInputs, (item, index) -> DiffNameStatusCommand.summary(item));
    }

    private static DiffSummary summary(@NotNull String line) {
        final DiffStatus status = DiffStatus.from(line);
        final String[] split = line.split(DiffNameStatusCommand.SEP);
        switch (status) {
            case Renamed:
                return status.withNames(split[1], split[2]);
            case Added:
                return status.withNames(null, split[1]);
            case Deleted:
                return status.withNames(split[1], null);
            case Modified:
            default:
                return status.withNames(split[1], split[1]);
        }
    }
}
