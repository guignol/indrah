package com.github.guignol.indrah;

import com.github.guignol.indrah.mvvm.Application;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        /////////////// 考え中
        // TODO user.nameとuser.emailの設定
        /////////////// 未完成
        // TODO 変更の破棄（UIをどうするか。ファイル単位の破棄。テストを追加する）
        // TODO 削除やリネームがファイル一覧で分かるとうれしい
        // TODO 変更ありリネームのテストを追加する
        /////////////// バグ
        // TODO コミット履歴が空だと初期化されない
        // TODO 破棄した後にファイル名表示が不正
        // TODO 新規ファイルの場合？1行だけ残ると？破棄できない
        // TODO リネームあるいはディレクトリ変更したファイルの一部unstageがうまくいってない
        // TODO diffヘッダーの初回クリックを受けつけないときがある
        /////////////// 未実装
        // TODO 文字サイズを可変にしたい
        // TODO シェルで開く（OS毎に別コマンドを叩くっぽいのでアレ）
        // TODO フォント
        // TODO submoduleはすぐには対応できないはずだけど、勉強してまず最低限必要なことを考える
        // TODO
        SwingUtilities.invokeLater(() -> new Application(Preference.shared()).start());
    }
}
