package com.github.guignol.indrah.mvvm.main_window;

import com.github.guignol.indrah.Colors;
import com.github.guignol.indrah.Preference;
import com.github.guignol.indrah.mvvm.commit_browser.CommitBrowserModel;
import com.github.guignol.indrah.mvvm.commit_maker.CommitMakerModel;
import com.github.guignol.indrah.mvvm.common.Directory;
import com.github.guignol.indrah.mvvm.sidebar.SideBarModel;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Optional;

public class MainWindowModel {

    final CommitMakerModel commitMakerModel;
    final CommitBrowserModel commitBrowserModel;
    private final SideBarModel sideBar;

    public MainWindowModel(@NotNull Preference preference,
                           @NotNull SideBarModel sideBar) {
        this.sideBar = sideBar;

        // 色の変更
        this.sideBar.colorSetting.onColorChanged().subscribe(prime -> {
            Colors.change(prime);
            preference.edit(data -> data.primeRGB = prime.getRGB());
        });
        // 設定を反映
        final int primeColor = Optional.ofNullable(preference.readOnly().primeRGB).orElse(sideBar.colorSetting.defaultRGB);
        this.sideBar.colorSetting.changeColor(new Color(primeColor));

        final Directory directory = this.sideBar.directory;
        // リロードを設定
        directory.setReload(this::reload);

        commitMakerModel = new CommitMakerModel(directory);
        // コミット後のリロード
        commitMakerModel.commitAction.onCommit().subscribe(eventStatus -> this.reload());
        commitBrowserModel = new CommitBrowserModel(directory, preference);
    }

    public void reload() {
        commitMakerModel.reload(true);
        commitBrowserModel.reload();
    }

    private MainWindowType type = MainWindowType.COMMIT_BROWSER; // 初期値

    Observable<MainWindowType> onWindowChangeRequested() {
        return sideBar.windowSwitcher.onRequested().map(eventStatus -> {
            type = type.next();
            return type;
        });
    }
}
