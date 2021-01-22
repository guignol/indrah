package com.github.guignol.indrah.model;

import com.github.guignol.indrah.utils.ListUtils;
import com.github.guignol.indrah.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.guignol.indrah.model.DiffHeader.Prefix;

public class Diff {

    public final String header;
    public final List<Hunk> hunks;

    public final DiffSummary summary;

    // 「画像の取得とキャッシュのためのハッシュ」を再計算しないためのキャッシュ
    // git diffが読み直されない限りは変わらないのでここに保存する
    public final Map<String, String> hashMap = new HashMap<>();

    public Diff(DiffSummary summary, String header, List<Hunk> hunks) {
        this.summary = summary;
        this.header = header;
        this.hunks = hunks;
    }

    public List<String> headerLines() {
        return ListUtils.from(header.split("\n"));
    }

    public static String oneLineHeader(List<String> headerLines) {
        // 改行区切り、改行終わり
        return headerLines.stream().collect(StringUtils.WITH_LINE_BREAK);
    }

    public String noHunkMessage() {
        if (hunks.isEmpty()) {
            final String line = ListUtils.find(headerLines(),
                    header -> header.startsWith(Prefix.RENAME) || header.startsWith(Prefix.DELETED) || header.startsWith(Prefix.NEW_FILE_MODE));
            if (line != null) {
                return line.split(" ")[0].toUpperCase();
            }
        }
        return "";
    }

    public String wholePatch() {
        return header + hunks.stream().map(Hunk::toString).collect(Collectors.joining());
    }

    public static boolean equals(Diff before, Diff after) {
        // Diff全体のヘッダーと各hunkのヘッダーのみ確認する
        if (!before.header.equals(after.header)) {
            return false;
        }
        for (int j = 0; j < before.hunks.size(); j++) {
            if (!before.hunks.get(j).header.equals(after.hunks.get(j).header)) {
                return false;
            }
        }
        return true;
    }

    public static boolean refersToSameFile(Diff diff0, Diff diff1) {
        return ListUtils.anyMatch(fileNames(diff0), fileNames(diff1), String::equals);
    }

    public static List<String> fileNames(Diff diff) {
        final List<String> list = new ArrayList<>();
        switch (diff.summary.status) {
            case Renamed:
                list.add(diff.summary.names.before());
                list.add(diff.summary.names.after());
                break;
            case Deleted:
                list.add(diff.summary.names.before());
                break;
            case Added:
                list.add(diff.summary.names.after());
                break;
            default:
                list.add(diff.summary.names.any());
                break;
        }
        return list;
    }
}
