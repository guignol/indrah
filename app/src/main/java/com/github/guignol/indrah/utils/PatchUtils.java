package com.github.guignol.indrah.utils;

import com.github.guignol.indrah.Preference;
import com.github.guignol.indrah.model.Hunk;
import com.github.guignol.indrah.model.Range;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 以下の移植
 * apply_range_or_line
 * https://github.com/git/git/blob/6867272d5b5615bd74ec97bf35b4c4a8d9fe3a51/git-gui/lib/diff.tcl#L643
 * TCLは複数のhunkを同時に選べるようになっているが、ここでは1つのhunkのみが対象。
 * また、TCLのコードはコマンド自体を作成しているが、ここではパッチのみ作成。
 */
public class PatchUtils {

    static String makePatch(final String diffHeader,
                            final Hunk hunk,
                            final Range range,
                            final boolean unstage) {
        return makePatch(diffHeader, hunk, range, Range.NONE, unstage);
    }

    static String makePatch(final String diffHeader,
                            final Hunk hunk,
                            final Range range,
                            final Range appliedRange,
                            final boolean unstage) {
        return makePatch(diffHeader, hunk.header, hunk.lines, range, appliedRange, unstage);
    }

    /**
     * 元となるパッチのhunkのヘッダーとボディに、ユーザの範囲選択を反映したパッチ文字列を返す
     *
     * @param diffHeader   diff全体のヘッダー
     * @param hunkHeader   hunkのヘッダー
     * @param hunkLines    hunkのボディ
     * @param range        選択範囲
     * @param appliedRange 既にapply済みの範囲（同じhunkを分割applyする場合に利用する。ただし動作確認はstageのみ）
     * @param unstage      unstageかどうか
     * @return 選択範囲を反映したパッチ文字列
     */
    private static String makePatch(final String diffHeader,
                                    final String hunkHeader,
                                    final List<String> hunkLines,
                                    final Range range,
                                    final Range appliedRange,
                                    final boolean unstage) {
        // @@ -2,7 +2,7 @@
        // @@ -1 +1,2 @@
        final String hunkStart = hunkHeader.split(" ")[1].split(",")[0].split("-")[1];
        final int offset = Integer.parseInt(hunkStart);

        // There is a special situation to take care of. Consider this
        // hunk:
        //
        //    @@ -10,4 +10,4 @@
        //     context before
        //    -old 1
        //    -old 2
        //    +new 1
        //    +new 2
        //     context after
        //
        // We used to keep the context lines in the order they appear in
        // the hunk. But then it is not possible to correctly stage only
        // "-old 1" and "+new 1" - it would result in this staged text:
        //
        //    context before
        //    old 2
        //    new 1
        //    context after
        //
        // (By symmetry it is not possible to *un*stage "old 2" and "new
        // 2".)
        //
        // We resolve the problem by introducing an asymmetry, namely,
        // when a "+" line is *staged*, it is moved in front of the
        // context lines that are generated from the "-" lines that are
        // immediately before the "+" block. That is, we construct this
        // patch:
        //
        //    @@ -10,4 +10,5 @@
        //     context before
        //    +new 1
        //     old 1
        //     old 2
        //     context after
        //
        // But we do *not* treat "-" lines that are *un*staged in a
        // special way.
        //
        // With this asymmetry it is possible to stage the change "old
        // 1" -> "new 1" directly, and to stage the change "old 2" ->
        // "new 2" by first staging the entire hunk and then unstaging
        // the change "old 1" -> "new 1".
        //
        // Applying multiple lines adds complexity to the special
        // situation.  The pre_context must be moved after the entire
        // first block of consecutive staged "+" lines, so that
        // staging both additions gives the following patch:
        //
        //    @@ -10,4 +10,6 @@
        //     context before
        //    +new 1
        //    +new 2
        //     old 1
        //     old 2
        //     context after

        // This is non-empty if and only if we are _staging_ changes;
        // then it accumulates the consecutive "-" lines (after
        // converting them to context lines) in order to be moved after
        // "+" change lines.
        final PreContextBuffer pre_context = new PreContextBuffer();

        int before = 0;
        int after = 0;
        final StringBuilder patch = new StringBuilder();
        // （選択範囲外にある）applyしないためcontext lineとして扱う行のprefix
        final String prefixToContextForUnselected = unstage ? "+" : "-";
        // （選択範囲外にある）apply済みのためcontext lineとして扱う行のprefix
        final String prefixToContextForApplied = !unstage ? "+" : "-";

        for (int i = 0; i < hunkLines.size(); i++) {

            final String line = hunkLines.get(i);
            final String to_context = appliedRange.contains(i)
                    ? prefixToContextForApplied
                    : prefixToContextForUnselected;

            // 選択範囲内の、プラスまたはマイナスで始まる行
            if (range.contains(i) && (line.startsWith("-") || line.startsWith("+"))) {
                // a line to stage/unstage
                if (line.startsWith("-")) {
                    before++;
                    patch.append(pre_context.flush());
                    patch.append(line);
                    patch.append("\n");
                } else {
                    after++;
                    patch.append(line);
                    patch.append("\n");
                }
            }
            // 選択範囲かどうかに関係なく、プラスまたはマイナスで始まらない行
            else if (!line.startsWith("-") && !line.startsWith("+")) {
                // context line
                patch.append(pre_context.flush());
                patch.append(line);
                patch.append("\n");

                // Skip the "\ No newline at end of
                // file". Depending on the locale setting
                // we don't know what this line looks
                // like exactly. The only thing we do
                // know is that it starts with "\ "
                if (!line.startsWith("\\ ")) {
                    // No newline at end of fileの行以外
                    before++;
                    after++;
                }
            }
            // 選択範囲外の、プラスまたはマイナスで始まる行
            // apply先（※）には存在しているため、context lineとして扱う
            // ※stageではindex、unstageではワークスペース
            else if (line.startsWith(to_context)) {
                // turn change line into context line
                final String contextLine = " " + line.substring(1); // 2文字目以降
                if (line.startsWith("-")) {
                    pre_context.appendLine(contextLine);
                } else {
                    patch.append(contextLine);
                    patch.append("\n");
                }
                before++;
                after++;
            }
            // 選択範囲外の、プラスまたはマイナスで始まる行
            // apply先には存在しないため行を削除する
            else {
                // a change in the opposite direction of
                // to_context which is outside the range of
                // lines to apply.
                patch.append(pre_context.flush());
            }
        }
        patch.append(pre_context.flush());
        final String newHunkHeader = String.format("@@ -%d,%d +%d,%d @@\n", offset, before, offset, after);
        final String wholePatch = newHunkHeader + patch.toString();
        return diffHeader + wholePatch;
    }

    @Nullable
    public static Path writeToFile(Path repositoryRoot, String patch) {
        return writeToFile(repositoryRoot, patch, "");
    }

    @Nullable
    public static Path writeToFile(Path repositoryRoot, String patch, String suffix) {
        // TODO SourceTreeは.git内に設定ファイルを持ってる（たぶん他の場所にもある）
        // TODO テストでは別のディレクトリやPreferenceを使いたい
        final Path temp = Preference.shared().getDirectory().resolve("temp");
        // 親ディレクトリの名前が同じレポジトリだと重複するが、主にdebug目的の識別なので問題無いはず
        final String patchFileName = "patch___" + repositoryRoot.getFileName().toString() + suffix +  ".txt";
        try {
            return FileUtils.replaceFile(temp, patchFileName, patch);
        } catch (IOException e) {
            return null;
        }
    }

    private static class PreContextBuffer {

        private final List<String> lines = new ArrayList<>();

        void appendLine(String line) {
            lines.add(line);
        }

        String flush() {
            if (lines.isEmpty()) {
                return "";
            } else {
                final String collect = lines.stream().collect(StringUtils.WITH_LINE_BREAK);
                lines.clear();
                return collect;
            }
        }
    }
}
