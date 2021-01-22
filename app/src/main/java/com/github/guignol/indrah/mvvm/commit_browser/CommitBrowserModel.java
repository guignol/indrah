package com.github.guignol.indrah.mvvm.commit_browser;

import com.github.guignol.indrah.Preference;
import com.github.guignol.indrah.mvvm.common.Directory;
import com.github.guignol.indrah.mvvm.log.LogRepository;
import com.github.guignol.indrah.mvvm.rebase.RebaseInteractive;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CommitBrowserModel {

    final LogRepository commitLog;
    final RebaseInteractive rebaseInteractive;

    public CommitBrowserModel(Directory directory, Preference preference) {
        final Path preferencePath = preference.getDirectory();
        final String sequenceEditor = Paths.get(preferencePath.toAbsolutePath().toString(),
                "rebase-proxy", "bin", "rebase-proxy").toString();
        rebaseInteractive = new RebaseInteractive(directory.getter(), sequenceEditor, this::reload);
        commitLog = new LogRepository(directory.getter());
    }

    public void reload() {
        commitLog.load();
    }
}
