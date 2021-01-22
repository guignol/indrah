package com.github.guignol.indrah.model;

import com.github.guignol.indrah.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Hunk {

    public static Hunk empty() {
       return new Hunk("", new ArrayList<>(), null,null);
    }

    public boolean isEmpty() {
        return header.isEmpty() && lines.isEmpty();
    }

    public final String header;
    public final List<String> lines;

    private Range endEdgePlusLines = Range.NONE;
    private Range endEdgeMinusLines = Range.NONE;

    public Hunk(@NotNull String header,
                @NotNull List<String> lines) {
        this(header, lines, null, null);
    }

    private Hunk(@NotNull String header,
                 @NotNull List<String> lines,
                 @Nullable Range endEdgePlusLines,
                 @Nullable Range endEdgeMinusLines) {
        this.header = header;
        this.lines = lines;
        if (endEdgePlusLines == null || endEdgeMinusLines == null) {
            workaround();
        } else {
            this.endEdgePlusLines = endEdgePlusLines;
            this.endEdgeMinusLines = endEdgeMinusLines;
        }
    }

    private void workaround() {
        /*

        @@ -26,5 +26,6 @@ names-is-here
         26
         27
         28
         29
        -30
        \ No newline at end of file
        +30added

        @@ -27,4 +27,5 @@ names-is-here
         27
         28
         29
        -30
        \ No newline at end of file
        +30
        +31added

        @@ -1,10 +1,8 @@
         1
         2
        -3
        -4
         5
         6
        -7
        +7seven
         8
         9
        -10
        \ No newline at end of file
        +10ten
        \ No newline at end of file

         */

        // \ No newline at end of fileを後ろから探して、その手前にある塊の範囲を得る
        final NoNewLineEdge firstEdge = searchEndEdge(lines.size());
        assignRange(firstEdge);
        if (firstEdge.exists()) {
            final NoNewLineEdge secondEdge = searchEndEdge(firstEdge.range.begin);
            assignRange(secondEdge);
        }
        // \ No newline at end of fileはその直前の行の属性だということを忘れずに
    }

    private void assignRange(NoNewLineEdge edge) {
        if ("+".equals(edge.marker)) {
            endEdgePlusLines = edge.range;
        } else if ("-".equals(edge.marker)) {
            endEdgeMinusLines = edge.range;
        }
    }

    @NotNull
    private NoNewLineEdge searchEndEdge(final int searchStart) {
        // \ No newline at end of fileを後ろから探す
        int nnlaeofIndex = -1;
        for (int i = searchStart - 1; i >= 0; i--) {
            final String line = lines.get(i);
            if (line.startsWith("\\ ")) {
                // 最初の\ No newline at end of fileを探したらbreak
                nnlaeofIndex = i;
                break;
            }
        }

        // \ No newline at end of fileが見つからなければ終了
        if (nnlaeofIndex < 0) {
            return NoNewLineEdge.NONE;
        }

        final int nearest = nnlaeofIndex - 1;
        // context line つまり スペース始まりも有り
        final String marker = lines.get(nearest).substring(0, 1);
        int top = -1;
        for (int i = nearest; i >= 0; i--) {
            final String line = lines.get(i);
            if (line.startsWith(marker)) {
                top = i;
            } else {
                // 途絶えたら終了
                break;
            }
        }
        return new NoNewLineEdge(marker, new Range(top, nearest));
    }

    public Range endEdgePlusLines() {
        return endEdgePlusLines;
    }

    public Range endEdgeMinusLines() {
        return endEdgeMinusLines;
    }

    public Hunk copy() {
        return new Hunk(header, new ArrayList<>(lines), endEdgePlusLines, endEdgeMinusLines);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hunk)) return false;

        Hunk hunk = (Hunk) o;
        if (isEmpty() || hunk.isEmpty()) {
            // 空のhunkは同じインスタンスかどうかのみ判定する
            return false;
        }

        return header.equals(hunk.header) && lines.equals(hunk.lines);
    }

    @Override
    public int hashCode() {
        int result = header.hashCode();
        result = 31 * result + lines.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return header + "\n" + lines.stream().collect(StringUtils.WITH_LINE_BREAK);
    }

    private static class NoNewLineEdge {

        private static final NoNewLineEdge NONE = new NoNewLineEdge(null, Range.NONE);

        private final String marker;
        private final Range range;

        NoNewLineEdge(String marker, Range range) {
            this.marker = marker;
            this.range = range;
        }

        boolean exists() {
            return marker != null && !marker.isEmpty() && range.exists();
        }
    }
}
