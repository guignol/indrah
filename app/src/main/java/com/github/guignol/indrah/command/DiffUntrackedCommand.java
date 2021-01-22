package com.github.guignol.indrah.command;

import com.github.guignol.indrah.model.DiffSummary;

import java.nio.file.Path;

public class DiffUntrackedCommand extends DiffCommand {

    /**
     * windows用のパス区切りだとpatchの出力がおかしくなり、正しくstage/unstageできない
     * （\がエスケープされて全体に引用符がつく）
     * git ls-files -o --exclude-standardの結果をそのまま使う
     * なお、indexに追加済みのファイルだと\でも正常に解決される
     */
    public DiffUntrackedCommand(Path dir, DiffSummary summary) {
        super(dir, summary);
    }

    @Override
    protected String[] command() {
        // git diff --no-index /dev/null [untracked file names]
//        System.out.println(untracked);
        return new CommandBuilder("git")
                .add(CommandOptions.diffStandard())
                .add("diff", OPTION_EXIT_CODE, "--no-index", "/dev/null", filePath)
                .build();
    }
}
