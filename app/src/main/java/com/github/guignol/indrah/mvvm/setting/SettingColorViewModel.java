package com.github.guignol.indrah.mvvm.setting;

import com.github.guignol.swing.binding.IViewModel;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

class SettingColorViewModel extends IViewModel<SettingColorModel> {

    SettingColorViewModel(SettingColorModel model) {
        super(model);
    }

    ////////// from View

    void changeColor(Color color) {
        model.changeColor(color);
    }

    ////////// to View

    List<Color> colors() {
        return Arrays.asList(model.colors);
    }
}
