package com.github.guignol.indrah.mvvm.setting;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import java.awt.*;

public class SettingColorModel {

    Color[] colors = {
            new Color(0xe06666),
            new Color(0x1f9999),
            new Color(19, 122, 199),
            new Color(64, 64, 64),
    };

    public int defaultRGB = colors[2].getRGB();

    private PublishSubject<Color> onColorChanged = PublishSubject.create();

    public void changeColor(Color color) {
        onColorChanged.onNext(color);
    }

    public Observable<Color> onColorChanged() {
        return onColorChanged.hide();
    }
}
