package com.github.guignol.indrah.mvvm.common;

import com.github.guignol.swing.binding.IViewModel;

class WindowSwitcherViewModel extends IViewModel<WindowSwitcher> {

    WindowSwitcherViewModel(WindowSwitcher model) {
        super(model);
    }

    ///////////// From View

    void changeWindow() {
        model.changeWindow();
    }
}
