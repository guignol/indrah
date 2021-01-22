package com.github.guignol.indrah.command;

import java.nio.file.Path;

public abstract class CommandByName extends Command {

    // 相対パス
    final String filePath;

    public CommandByName(Path dir, String filePath) {
        super(dir);
        this.filePath = filePath;
    }

    @Override
    protected boolean isValid() {
        return super.isValid() && filePath != null && !filePath.isEmpty();
    }
}
