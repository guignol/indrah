package com.github.guignol.indrah;

import java.nio.file.Path;

class FilePair {

    final Path root;
    final String before;
    final String after;
    final String tag;

    FilePair(Path root, String before, String after) {
        this.root = root;
        this.before = before;
        this.after = after;
        tag = root.getFileName().toString();
    }

    FilePair downTo(final String subDirName) {
        return new FilePair(root.resolve(subDirName),
                "../" + before,
                "../" + after);
    }
}
