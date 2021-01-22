package com.github.guignol.indrah.utils;

public class IndexUtils {

    public static void storeBackup() {
        // TODO 行ごとのstage/unstageは複数コマンドを発行するから、コマンド単位で保存すると微妙かも
        // TODO 逆に、複数コマンドを順番に見れるのは楽しそう
        // TODO いずれにせよ、コマンド単位でやるなら、複数の履歴を持つ必要がある
    }

    public static void clearBackup() {

    }

    public static void undo() {
        // TODO .git/indexの上書きと履歴管理
    }

    public static void redo() {

    }
}
