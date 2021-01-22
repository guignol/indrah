package com.github.guignol.indrah.command;

import java.nio.file.Path;

public class HashObjectCommand extends CommandByName {

    public HashObjectCommand(Path dir, String filePath) {
        super(dir, filePath);
    }

    @Override
    protected String[] command() {
        return new String[]{"git", "hash-object", filePath};
    }
}
