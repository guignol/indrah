package com.github.guignol.indrah.utils;

import com.github.guignol.indrah.command.CommandOutput;
import com.github.guignol.indrah.command.FileStageCommand;
import com.github.guignol.indrah.model.*;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.guignol.indrah.command.ApplyPatchCommand.Executor;

public class StageUtils {

    private static final Predicate<String> noContextLine = line -> line.startsWith("+") || line.startsWith("-");

    public static void stage(Executor executor,
                             List<DiffLine> selectedLines,
                             boolean unstage,
                             Consumer<CommandOutput> callbackForShortcut) {
        // パッチを作らずにファイル全体をstage/unstageする場合
        if (StageUtils.doStageFile(executor.getCurrentDirectory(), selectedLines, unstage, callbackForShortcut)) {
            return;
        }
        // 変化を全く含まない場合
        if (StageUtils.nothingToDo(selectedLines)) {
            return;
        }
        final DiffLine startLine = selectedLines.get(0);
        final DiffLine endLine = ListUtils.last(selectedLines);
        final Range range = new Range(startLine.indexInHunk, endLine.indexInHunk);

        // 選択範囲のhunkはすべて同じであることを前提
        final Hunk targetHunk = startLine.hunk.copy();

        // hunk全体を選択した場合
        if (range.contains(noContextRange(targetHunk))) {
            System.out.println("hunk全体を選択");
            final String patchForHunk = startLine.diff.header + targetHunk.toString();
            executor.execute(patchForHunk, unstage, null);
            return;
        }

        if (shouldRemoveSignForNNLAEOF(startLine.diff, targetHunk, range, unstage)) {
            final String command = removeSignForNNLAEOF(startLine, range, unstage);
            executor.execute(command, unstage, null);
        } else if (UnstageUtils.needsWorkaround(startLine.diff, unstage)) {
            // ①hunkを全てunstageする
            // ②選択行の後を全てstageする
            // ③選択行の前を全てstageする
            UnstageUtils.unstageWorkaround(executor, startLine, endLine);
        } else {
            // 通常時
            final String command = PatchUtils.makePatch(
                    startLine.diff.header,
                    targetHunk,
                    range,
                    unstage
            );
            executor.execute(command, unstage, null);
        }
    }

    public static boolean doStageFile(Path root,
                                      List<DiffLine> selectedLines,
                                      boolean unstage,
                                      Consumer<CommandOutput> callback) {
        final Diff targetDiff = selectedLines.get(0).diff;
        /*
        ・新規追加された空ファイル
        ・ファイル削除
        ・リネーム
        ・バイナリ https://stackoverflow.com/questions/17152171/git-cannot-apply-binary-patch-without-full-index-line
        */
        final boolean headerOnly = targetDiff.hunks.isEmpty();
        if (headerOnly ||
                // ファイル全体を選択
                selectedLines.size() == targetDiff.hunks.stream().mapToInt(hunk -> hunk.lines.size()).sum()) {
            switch (targetDiff.summary.status) {
                case Renamed:
                    new FileStageCommand(root, unstage, targetDiff.summary.names.before(), targetDiff.summary.names.after()).call(callback);
                    break;
                default:
                    new FileStageCommand(root, unstage, targetDiff.summary.names.any()).call(callback);
                    break;
            }
            return true;
        }
        return false;
    }

    public static FileStageCommand getFileStageCommand(Path root,
                                                       boolean unstage,
                                                       List<Diff> targets) {
        final String[] files = targets.stream().map(diff -> diff.summary).flatMap(summary -> {
            switch (summary.status) {
                case Renamed:
                    return Stream.of(summary.names.before(), summary.names.after());
                default:
                    return Stream.of(summary.names.any());
            }
        }).toArray(String[]::new);
        return new FileStageCommand(root, unstage, files);
    }

    public static boolean nothingToDo(Hunk hunk, Range range) {
        if (!range.exists()) {
            return true;
        }
        List<String> selectedLines = hunk.lines.subList(range.begin, range.end + 1);
        return noLineToDo(selectedLines);
    }

    public static boolean nothingToDo(List<DiffLine> diffLines) {
        final List<String> selectedLines = diffLines
                .stream()
                .map(diffLine -> diffLine.line)
                .collect(Collectors.toList());
        return noLineToDo(selectedLines);
    }

    public static boolean noLineToDo(List<String> selectedLines) {
        return selectedLines.stream().noneMatch(noContextLine);
    }

    public static Range noContextRange(Hunk targetHunk) {
        final List<String> lines = targetHunk.lines;
        final int first = ListUtils.findIndex(lines, noContextLine);
        final int last = ListUtils.findLastIndex(lines, noContextLine);
        return new Range(first, last);
    }

    public static boolean shouldRemoveSignForNNLAEOF(Diff diff, Hunk hunk, Range selectedRange, boolean unstage) {
        return shouldRemoveSignForNNLAEOF(stageNewFile(diff), hunk, selectedRange, unstage);
    }

    public static boolean shouldRemoveSignForNNLAEOF(boolean newFile, Hunk hunk, Range selectedRange, boolean unstage) {
        // \ No newline at end of file近傍のプラスまたはマイナスの塊を全て含まない場合
        // （一部でも近傍の塊を含む場合は、\ No newline at end of fileは有意味）
        // stageの場合を例にとると、
        // 選択されないプラス行は削除されるため、その後の\ No newline at end of fileは無意味で不要となり、
        // 選択されないマイナス行は空白始まりとして残るため、その後の\ No newline at end of fileは有意味なまま
        final Range targetRange = unstage ? hunk.endEdgeMinusLines() : hunk.endEdgePlusLines();
        if (!targetRange.exists()) {
            return false;
        }
        if (!unstage && newFile) {
            // 新規ファイルのstageの場合、\ No newline at end of fileは末尾行が無ければ削除する
            // そのほうが挙動として直感的だと思う
            return !selectedRange.contains(targetRange.end);
        } else {
            return !selectedRange.containsPartially(targetRange);
        }
    }

    @NotNull
    public static String removeSignForNNLAEOF(DiffLine startLine, Range range, boolean unstage) {
        final Hunk targetHunk = startLine.hunk.copy();
        // 不要な\ No newline at end of fileを削除する
        final Range edgeRange = unstage ? targetHunk.endEdgeMinusLines() : targetHunk.endEdgePlusLines();
        final int removed = edgeRange.end + 1;
        targetHunk.lines.remove(removed);
        final Range adjusted;
        if (range.end < removed) {
            // この行の塊の前だけがstageされる場合、rangeは何も変わらない
            adjusted = range;
        } else {
            // この行の塊の後だけがstageされる場合、rangeが1つずつ減る
            adjusted = range.offset(-1);
        }
        return PatchUtils.makePatch(
                startLine.diff.header,
                targetHunk,
                adjusted,
                unstage
        );
    }

    /**
     * 新規追加ファイルかどうか
     */
    static boolean stageNewFile(Diff diff) {
        final List<String> diffHeaderLines = diff.headerLines();
        for (String headerLine : diffHeaderLines) {
            if (headerLine.startsWith(DiffHeader.Prefix.MINUS_DEV_NULL)) {
                return true;
            }
        }
        return false;
    }
}
