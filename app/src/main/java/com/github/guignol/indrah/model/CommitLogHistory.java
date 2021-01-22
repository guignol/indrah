package com.github.guignol.indrah.model;

import com.github.guignol.indrah.utils.ListUtils;
import com.github.guignol.indrah.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommitLogHistory {

    @NotNull
    public final List<CommitLogAnnotated> annotated = new ArrayList<>();

    public CommitLogHistory(@NotNull List<CommitLog> logs) {
        boolean foundLastJunction = false;
        for (CommitLog log : logs) {
            if (!foundLastJunction && log.isMergeCommit()) {
                this.annotated.add(new CommitLogAnnotated(log, true));
                foundLastJunction = true;
            } else {
                this.annotated.add(new CommitLogAnnotated(log, false));
            }
        }
    }

    public static CommitLogHistory empty() {
        return new CommitLogHistory(new ArrayList<>());
    }

    public static CommitLogHistory parse(List<String> standardInputs) {
        if (standardInputs.isEmpty()) {
            return empty();
        }
        /*
        commit 9eba911f0486aa726dccf208eccc4bd665e34e98 b69e7ed4d906275be1f6c2d7f97691e695bf7782
        Author: i am <17764785+guignol@users.noreply.github.com>
        Date:   Thu Dec 28 21:04:38 2017 +0900
           add git log command
         */
        final List<CommitLog> logs = StringUtils.grouping(standardInputs, "commit ").stream().map(lines -> {
            // commit [自分のhash] [親のhash] [親のhash] ...
            final String[] firstLine = StringUtils.splitWithWhiteSpace(lines.get(0));
            // コミットメッセージの直前の空行
            final int blankIndex = ListUtils.findIndex(lines, String::isEmpty);
            // コミットメッセージ
            // メッセージの前のタブを制御できるが、古いgitには無いオプション
            // https://git-scm.com/docs/git-log#git-log---no-expand-tabs
            final String messages = lines.subList(blankIndex + 1, lines.size())
                    .stream()
                    .map(s -> {
                        // もしかするとバージョンによって違うかも
                        final String prefix = "    ";
                        if (s.startsWith(prefix)) {
                            return s.substring(prefix.length());
                        }
                        return s;
                    })
                    .collect(Collectors.joining("\n"));
            final List<String> parents = Arrays.asList(firstLine).subList(2, firstLine.length);
            return new CommitLog(firstLine[1], messages, parents);
        }).collect(Collectors.toList());

        return new CommitLogHistory(logs);
    }
}
