package com.github.guignol.indrah.utils;

import com.github.guignol.indrah.command.*;
import com.github.guignol.indrah.model.Diff;
import com.github.guignol.indrah.model.DiffSummary;
import com.github.guignol.indrah.model.Hunk;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.guignol.indrah.command.DiffCommand.EXIT_WITH_DIFF;

public class DiffUtils {

    public static Single<List<Diff>> diffs(final Path root, final boolean cached) {
        final Single<List<DiffSummary>> diffNames = new DiffNameStatusCommand(root, cached).toSummaries();
        final Single<CommandOutput> diffDetails = new DiffDetailCommand(root, cached).toSingle();
        final Single<List<Diff>> diffs = Single.zip(diffNames, diffDetails, (summaries, output) -> parseDiff(output, summaries));
        if (cached) {
            return diffs;
        } else {
            final List<Diff> seed = new ArrayList<>();
            final Single<List<Diff>> diffsUntracked = new DiffNameUntrackedCommand(root).toSummaries()
                    .toObservable()
                    .flatMap(Observable::fromIterable)
                    .flatMap(summary -> new DiffUntrackedCommand(root, summary).toSingle()
                            .map(output -> parseDiff(output, summary)).toObservable())
                    .reduce(seed, (sum, list) -> {
                        sum.addAll(list);
                        return sum;
                    });
            return Single.zip(diffs, diffsUntracked, (diffs1, diffs2) -> {
                final List<Diff> list = new ArrayList<>();
                list.addAll(diffs1);
                list.addAll(diffs2);
                return list;
            });
        }
    }

    @NotNull
    private static List<Diff> parseDiff(@NotNull final CommandOutput output, @NotNull List<DiffSummary> summaries) {
        if (output.exitCode == EXIT_WITH_DIFF) {
            final List<List<String>> allDiffLines = StringUtils.grouping(output.standardInputs, "diff");
            return ListUtils.map(allDiffLines, (diff, index) -> {
                // TODO indexじゃなく探したほうがいいかも（でも名前があるとは限らない？？）
                final DiffSummary summary = summaries.get(index);
                return parseDiff(diff, summary);
            });
        } else {
            return new ArrayList<>();
        }
    }

    @NotNull
    private static List<Diff> parseDiff(@NotNull final CommandOutput output, @NotNull DiffSummary summary) {
        if (output.exitCode == EXIT_WITH_DIFF) {
            final List<List<String>> allDiffLines = StringUtils.grouping(output.standardInputs, "diff");
            return allDiffLines.stream()
                    .map(diff -> parseDiff(diff, summary))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    private static Diff parseDiff(@NotNull List<String> strings, @NotNull DiffSummary summary) {
        final String diffHeader = Diff.oneLineHeader(StringUtils.above(strings, "@@"));
        final List<Hunk> hunks = StringUtils.grouping(strings, "@@").stream()
                .map(lines -> {
                    final String header = lines.remove(0);
                    return new Hunk(header, lines);
                })
                .collect(Collectors.toList());
        return new Diff(summary, diffHeader, hunks);
    }
}
