package com.github.guignol.indrah;

import com.github.guignol.indrah.command.*;
import com.github.guignol.indrah.model.Diff;
import com.github.guignol.indrah.model.DiffLine;
import com.github.guignol.indrah.model.Task;
import com.github.guignol.indrah.utils.DiffUtils;
import com.github.guignol.indrah.utils.ListUtils;
import com.github.guignol.indrah.utils.PatchUtils;
import com.github.guignol.indrah.utils.StageUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class GitTask {

    static Task<CommandOutput> init(@NotNull Path root,
                                    @NotNull Consumer<CommandOutput> assertFunc) {
        return resolver -> {
            // レポジトリを初期化
            Command.fromOneLine(root, "git init")
                    .call(output -> {
                        assertFunc.accept(output);
                        resolver.resolve(output);
                    });
        };
    }

    static Task<CommandOutput> add(@NotNull Path root,
                                   @Nullable String relativePath,
                                   @NotNull Consumer<CommandOutput> assertFunc) {
        return resolver -> {
            // ファイル丸ごとstageする
            new FileStageCommand(root, false, relativePath)
                    .call(output -> {
                        assertFunc.accept(output);
                        resolver.resolve(output);
                    });
        };
    }

    static Task<CommandOutput> reset(@NotNull Path root,
                                     @Nullable String relativePath,
                                     @NotNull Consumer<CommandOutput> assertFunc) {
        return resolver -> {
            // ファイル丸ごとunstageする
            new FileStageCommand(root, true, relativePath)
                    .call(output -> {
                        assertFunc.accept(output);
                        resolver.resolve(output);
                    });
        };
    }

    interface LineSelector extends Function<List<DiffLine>, List<DiffLine>> {
    }

    static Task<CommandOutput> stage(@NotNull Path root,
                                     @NotNull final LineSelector selector,
                                     @Nullable Consumer<CommandOutput> assertFunc) {
        return apply(false, root, diff -> true, selector, assertFunc);
    }

    static Task<CommandOutput> stage(@NotNull Path root,
                                     @NotNull final Predicate<Diff> filter,
                                     @NotNull final LineSelector selector,
                                     @Nullable Consumer<CommandOutput> assertFunc) {
        return apply(false, root, filter, selector, assertFunc);
    }

    static Task<CommandOutput> unstage(@NotNull Path root,
                                       @NotNull final LineSelector selector,
                                       @Nullable Consumer<CommandOutput> assertFunc) {
        return apply(true, root, diff -> true, selector, assertFunc);
    }

    static Task<CommandOutput> unstage(@NotNull Path root,
                                       @NotNull final Predicate<Diff> filter,
                                       @NotNull final LineSelector selector,
                                       @Nullable Consumer<CommandOutput> assertFunc) {
        return apply(true, root, filter, selector, assertFunc);
    }

    static Task<CommandOutput> apply(final boolean unstage,
                                     @NotNull Path root,
                                     @NotNull final Predicate<Diff> filter,
                                     @NotNull final LineSelector selector,
                                     @Nullable Consumer<CommandOutput> assertFunc) {
        return resolver -> {
            // diffの取得
            DiffUtils.diffs(root, unstage).subscribe(data -> {
                final Diff diff = ListUtils.find(data, filter);
                final List<DiffLine> allDiffLines = diff.hunks
                        .stream()
                        .flatMap(hunkUnit -> ListUtils.map(hunkUnit.lines,
                                (line, index) -> new DiffLine(diff, hunkUnit, index, line)).stream()
                        )
                        .collect(Collectors.toList());
                // stageまたはunstage
                final List<DiffLine> selectedLines = selector.apply(allDiffLines);
                if (assertFunc == null) {
                    // assertFuncが無い場合はdry-run扱いで、allDiffLinesを渡して終了
//                        resolver.resolve(CommandOutput.NULL);
                    return;
                }

                StageUtils.stage(
                        new ApplyPatchCommand.Executor() {
                            @Override
                            public void execute(String patch, boolean unstage, @Nullable Consumer<CommandOutput> callback) {
                                final Path patchFile = PatchUtils.writeToFile(root, patch);
                                new ApplyPatchCommand(root, patchFile, unstage)
                                        .call(output -> {
                                            if (callback != null) {
                                                callback.accept(output);
                                            }
                                            assertFunc.accept(output);
                                            resolver.resolve(output);
                                        });
                            }

                            @NotNull
                            @Override
                            public Path getCurrentDirectory() {
                                return root;
                            }
                        },
                        selectedLines,
                        unstage,
                        assertFunc);
            });
        };
    }

    static Task<CommandOutput> commit(@NotNull Path root,
                                      @NotNull Consumer<CommandOutput> assertFunc) {
        return resolver -> {
            // コミット
            CommitCommand.newly(root, "initial commit")
                    .call(output -> {
                        assertFunc.accept(output);
                        resolver.resolve(output);
                    });
        };
    }
}
