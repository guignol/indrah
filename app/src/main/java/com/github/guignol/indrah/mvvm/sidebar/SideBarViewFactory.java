package com.github.guignol.indrah.mvvm.sidebar;

import com.github.guignol.indrah.mvvm.common.DirectoryActionViewFactory;
import com.github.guignol.indrah.mvvm.common.WindowSwitcherViewFactory;
import com.github.guignol.indrah.mvvm.setting.SettingColorViewFactory;

import java.awt.*;

public class SideBarViewFactory {

    public static Component create(SideBarModel model) {
        final SideBarViewModel viewModel = new SideBarViewModel(model);
        final SideBarView view = new SideBarView(
                DirectoryActionViewFactory.create(model.directory),
                WindowSwitcherViewFactory.create(model.windowSwitcher),
                SettingColorViewFactory.create(model.colorSetting));
        view.bind(viewModel);
        return view.getComponent();
    }
}
