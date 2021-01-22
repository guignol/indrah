package com.github.guignol.indrah.command;

import java.nio.file.Path;

public class LogCommand extends Command {

    public LogCommand(Path dir) {
        super(dir);
    }

    @Override
    protected String[] command() {
        // --parents で親を表示できるのでマージコミットも追える
        // TODO 差分取得を行う際に、取得済みの最古のコミットを添えて範囲指定する方法があるはず
        // TODO しかし、マージが挟まると面倒そう、なので
        // TODO とりあえず全件で実装する。その後、でかいレポジトリを見て実用に十分か確認する
        // git --no-pager log --parents -n 100
        return new CommandBuilder("git")
                .add("--no-pager")
                .add("log", "--parents")
//                .add("-n", "10") // TODO 確認中
                .build();
    }
}
