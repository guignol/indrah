package com.github.guignol.indrah.mvvm.sidebar;

import com.github.guignol.indrah.mvvm.ValueHolder;
import com.github.guignol.indrah.mvvm.common.Directory;
import com.github.guignol.indrah.mvvm.common.WindowSwitcher;
import com.github.guignol.indrah.mvvm.setting.SettingColorModel;
import io.reactivex.Observable;

public class SideBarModel {

    public final Directory directory;
    public final WindowSwitcher windowSwitcher;

    public final SettingColorModel colorSetting = new SettingColorModel();

    private final ValueHolder<Boolean> open = ValueHolder.createDefault(true);

    public SideBarModel(Directory directory, WindowSwitcher windowSwitcher) {
        this.directory = directory;
        this.windowSwitcher = windowSwitcher;
    }

    Observable<Boolean> onOpen() {
        return open.observable();
    }

    public void open() {
        open.put(true);
    }

    public void close() {
        open.put(false);
    }

    void toggle() {
        if (!open.get()) {
            open();
        } else {
            close();
        }
    }
}
