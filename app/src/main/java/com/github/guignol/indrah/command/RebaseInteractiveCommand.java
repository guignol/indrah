package com.github.guignol.indrah.command;

import com.github.guignol.indrah.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class RebaseInteractiveCommand extends Command {

    private final String targetCommit;
    private final String sequenceEditor;

    public RebaseInteractiveCommand(Path dir, String port, String targetCommit, String sequenceEditor) {
        super(dir, new CommandExecutor() {
            @NotNull
            @Override
            protected Map<String, String> getEnvironment() {
                return new HashMap<String, String>() {
                    {
                        this.put("rebase_server_port", port);
                    }
                };
            }
        });
        this.targetCommit = targetCommit;
        this.sequenceEditor = StringUtils.singleQuotation(sequenceEditor);
    }

    @Override
    protected String[] command() {
        // 分岐のあるコミットでrebase -iすると、困った感じになる
        // --preserve-merges --interactive https://git-scm.com/docs/git-rebase#_bugs
        // SourceTreeは--preserve-mergesオプションは無い
        return new CommandBuilder("git")
                .add(CommandOptions.diffStandard()) // according to SourceTree
                .add("-c", "sequence.editor=" + sequenceEditor)
                .add("-c", "core.editor=" + sequenceEditor)
                .add("rebase", "-i", targetCommit)
                .build();
    }
}
