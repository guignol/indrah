package com.github.guignol.indrah.utils;

import com.github.guignol.indrah.command.ApplyPatchCommand;
import com.github.guignol.indrah.command.CommandOutput;
import com.github.guignol.indrah.command.IndexInfoCommand;
import com.github.guignol.indrah.model.*;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class UnstageUtils {

    static boolean needsWorkaround(Diff diff, boolean unstage) {
        if (!unstage) {
            return false;
        }
        // 新規追加ファイルの部分的なunstageに失敗するため
        return StageUtils.stageNewFile(diff);
    }

    /**
     * unstageがうまくいかない場合に、すべてunstageしてから選択行以外をstageするworkaround
     * ①hunkを全てunstageする
     * ②選択行の後を全てstageする
     * ③選択行の前を全てstageする
     * ※選択行の後を先にstageするのは、\ No newline at end of fileの問題を回避するため
     * <p>
     * 一部追加するとdiffのheaderが変わる
     * <p>
     * ヘッダーのindex行のフォーマットは
     * index [変更前のハッシュ]..[変更後のハッシュ] [file mode]
     * <p>
     * [A] コミットされたファイルのハッシュ あるいは 0パディング
     * [B] stageした差分を含むハッシュ
     * [C] ワークスペースのハッシュ
     * <p>
     * 全てstageされている場合、または、何もstageされていない場合
     * index [A]..[C] [file mode]
     * 一部だけstageされている場合、
     * stageされたものは、
     * index [A]..[B] [file mode]
     * stageされてないものは、
     * index [B]..[C] [file mode]
     * <p>
     * ① [A]..[C] または [A]..[B]
     * ② [A]..['C]
     * ③ [B']..[C]
     * これだと②も失敗しそうだけど、変更前のハッシュしか見てないっぽい
     * <p>
     * また、新規ファイルの場合は以下の行があり、index行には [file mode] が無い
     * new file mode [file mode]
     * この場合、「--- a/ファイルパス」の行が「--- /dev/null」になる
     */
    static void unstageWorkaround(ApplyPatchCommand.Executor executor,
                                  DiffLine startLine,
                                  DiffLine endLine) {
        // 選択範囲は同じdiffの同じhunkに属する前提
        final Diff targetDiff = startLine.diff;
        final Hunk targetHunk = startLine.hunk.copy();
        final List<String> diffHeaderLines = targetDiff.headerLines();

        System.out.println("--------------unstageWorkaround");

        final Range selected = new Range(startLine.indexInHunk, endLine.indexInHunk);
        System.out.println("for selected lines: " + selected);
        // ①hunkを全てunstageする
        final Range range1 = new Range(0, targetHunk.lines.size() - 1);
        final Range[] unselected = range1.splitBy(selected);
        // ②選択行の後を全てstageする
        final Range range2 = unselected[1];
        // ③選択行の前を全てstageする
        final Range range3 = unselected[0];

        Task.<Boolean>chain(resolver -> {
            // ①hunkを全てunstageする
            System.out.println("① unstage: " + range1);
            final String unstageAll = PatchUtils.makePatch(
                    targetDiff.header,
                    targetHunk,
                    range1,
                    true
            );
            executor.execute(unstageAll, true, new AbortOnFailed(resolver));
        }, resolver -> {
            // ②選択行の後を全てstageする
            System.out.println("② stage: " + range2);
            // diffを全く含まない場合
            if (StageUtils.nothingToDo(targetHunk, range2)) {
                System.out.println("no diff");
                resolver.resolve(false);
                return;
            }
            final String stageAboveSelected = PatchUtils.makePatch(
                    targetDiff.header,
                    targetHunk,
                    range2,
                    false
            );
            executor.execute(stageAboveSelected, false, new AbortOnFailed(resolver));
        }, resolver -> {
            // ③選択行の前を全てstageする
            System.out.println("③ stage: " + range3);
            // diffを全く含まない場合
            if (StageUtils.nothingToDo(targetHunk, range3)) {
                System.out.println("no diff");
                return;
            }
            if (resolver.input) {
                // ②でstageした場合はいろいろ調整が必要
                // ・headerの修正
                // ・②でstageした分を考慮したpatchの作成
                resolver.resolve(true);
            } else {
                final String stageUnderSelected;
                if (StageUtils.shouldRemoveSignForNNLAEOF(true, targetHunk, range3, false)) {
                    // ②で後半部分をstageしなかった場合は、
                    // 元々のunstage対象として末尾行が含まれているということなので、
                    // ここでのstage対象には末尾行が含まれていない。
                    // したがって、通常の新規stageと同じく\ No newline at end of fileを削除する。
                    // ここでの判定は実質的に\ No newline at end of fileを含むかどうか
                    stageUnderSelected = StageUtils.removeSignForNNLAEOF(startLine, range3, false);
                } else {
                    stageUnderSelected = PatchUtils.makePatch(
                            targetDiff.header,
                            targetHunk,
                            range3,
                            false
                    );
                }
                executor.execute(stageUnderSelected, false, null);
            }
        }, resolver -> {
            // 現在のところ新規ファイルのunstageのみが対象なので、変更後のファイルパスを取得
            final String filePath = targetDiff.summary.names.after();
            // ②でstageしていれば、③は新規ファイル追加扱いにはならない
            HeaderUtils.invalidateNewLineMode(filePath, diffHeaderLines);
            // index [変更前のハッシュ]..[変更後のハッシュ] [file mode]
            HeaderUtils.validateIndexLine(
                    executor.getCurrentDirectory(),
                    filePath,
                    diffHeaderLines,
                    () -> resolver.resolve(true));
        }, resolver -> {
            final String newDiffHeader = Diff.oneLineHeader(diffHeaderLines);
            final String stageUnderSelected = PatchUtils.makePatch(
                    newDiffHeader,
                    targetHunk,
                    range3,
                    range2, // 元のhunkのうちstageしてしまったものは編集しないといけない
                    false
            );
            executor.execute(stageUnderSelected, false, null);
        });
    }

    // validate/invalidateの処理が汎用的だと保証できないのでinner class
    public static class HeaderUtils {
        /**
         * 新規ファイル扱いを書き換える
         */
        private static void invalidateNewLineMode(@NotNull String filePath, @NotNull List<String> diffHeaderLines) {
            diffHeaderLines.removeIf(line -> line.startsWith(DiffHeader.Prefix.NEW_FILE_MODE));
            // /dev/null をpathに書き換え
            ListUtils.replaceFirst(diffHeaderLines,
                    line -> line.startsWith(DiffHeader.Prefix.MINUS_DEV_NULL),
                    indexLine -> DiffHeader.Prefix.MINUS_FILE_PATH + filePath);
        }

        /**
         * index [変更前のハッシュ]..[変更後のハッシュ] [file mode]
         * の行を正しく書き換える
         */
        private static void validateIndexLine(Path currentDir,
                                              String filePath,
                                              List<String> diffHeaderLines,
                                              Runnable callback) {
            // stageされたファイルのハッシュとfile modeの取得
            // 100644 4102bb4f60b8aa307c0fed06c557ca3ce877c1dc 0	xxxx/test.txt
            new IndexInfoCommand(currentDir, filePath).call(output -> {
                final String[] fileInfo = output.standardInputs.get(0).split(" ");
                final String fileMode = fileInfo[0];
                final String stagedHash = fileInfo[1].substring(0, 7);
                ListUtils.replaceFirst(diffHeaderLines,
                        line -> line.startsWith(DiffHeader.Prefix.INDEX),
                        indexLine -> StringUtils.scrapAndBuild(indexLine, " ", indexLineItems -> {
                            final String newFromTo = StringUtils.scrapAndBuild(
                                    indexLineItems.get(1),
                                    "\\.\\.",
                                    "..",
                                    fromTo -> fromTo.set(0, stagedHash)
                            );
                            indexLineItems.clear();
                            indexLineItems.add(DiffHeader.Prefix.INDEX);
                            indexLineItems.add(newFromTo);
                            indexLineItems.add(fileMode);
                        }));
                callback.run();
            });
        }
    }

    private static class AbortOnFailed implements Consumer<CommandOutput> {
        private final Task.Resolver<Boolean> resolver;

        private AbortOnFailed(Task.Resolver<Boolean> resolver) {
            this.resolver = resolver;
        }

        @Override
        public void accept(CommandOutput output) {
            resolver.resolve(output.exitCode == 0 ? true : Task.ABORT());
        }
    }
}
