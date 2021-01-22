package com.github.guignol.indrah.command;

import com.github.guignol.indrah.utils.ArrayUtils;

import java.nio.file.Path;

// 今のところ最新（HEAD）のみ
public class CommitMessageCommand extends Command {

    public CommitMessageCommand(Path dir) {
        super(dir);
    }

    @Override
    protected String[] command() {
        return ArrayUtils.of("git", "show", "-s", "--format=%B", "HEAD");
    }
}
