package com.github.guignol.indrah.command;

import java.nio.file.Path;

public class IndexInfoCommand extends CommandByName {

    public IndexInfoCommand(Path dir, String fileName) {
        super(dir, fileName);
    }

    @Override
    protected String[] command() {
        // git ls-files -s xxxx/test.txt
        return new String[]{"git", "ls-files", "-s", filePath};
    }
}
