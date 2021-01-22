package com.github.guignol.indrah.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class StringUtils {

    public static final Collector<CharSequence, ?, String> WITH_LINE_BREAK = Collectors.joining(
            "\n",
            "",
            "\n");

    public static final Collector<CharSequence, ?, String> toHtml = Collectors.joining(
            "</nobr><br><nobr>",
            "<html><nobr>",
            "</nobr></html>");

    public static String scrapAndBuild(@NotNull final String string,
                                       @NotNull final String splitRegExp,
                                       @NotNull final Consumer<List<String>> editor) {
        return scrapAndBuild(string, splitRegExp, splitRegExp, editor);
    }

    public static String scrapAndBuild(@NotNull final String target,
                                       @NotNull final String splitRegExp,
                                       @NotNull final String joiner,
                                       @NotNull final Consumer<List<String>> editor) {
        final List<String> divided = ListUtils.from(target.split(splitRegExp));
        editor.accept(divided);
        return divided.stream().collect(Collectors.joining(joiner));
    }

    public static String singleQuotation(@NotNull final String original) {
        if (original.startsWith("'")) {
            return original;
        }
        return "'" + original + "'";
    }

    public static String doubleQuotation(@NotNull final String original) {
        if (original.startsWith("\"")) {
            return original;
        }
        return "\"" + original + "\"";
    }

    // nullまたは空白またはホワイトスペース
    public static boolean isBlank(@Nullable final String target) {
        return target == null || target.replaceAll("\\s", "").isEmpty();
    }

    /**
     * 指定のprefixで始まるアイテムより手前にあるSubListを返す
     */
    @NotNull
    public static List<String> above(List<String> list, final String prefix) {
        final List<String> ret = new ArrayList<>();
        for (String line : list) {
            if (line.startsWith(prefix)) {
                break;
            }
            ret.add(line);
        }
        return ret;
    }

    /**
     * 指定のprefixで始まるアイテムを開始アイテムとして、ListのListを作る
     */
    @NotNull
    public static List<List<String>> grouping(List<String> list, final String prefix) {
        final List<List<String>> ret = new ArrayList<>();
        List<String> now = null;
        for (String line : list) {
            if (line.startsWith(prefix)) {
                now = new ArrayList<>();
                ret.add(now);
            }
            if (now != null) {
                now.add(line);
            }
        }

        return ret;
    }

    public static String[] splitWithWhiteSpace(String string) {
        return string.split("\\s+");
    }
}
