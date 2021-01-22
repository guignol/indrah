package com.github.guignol.indrah.mvvm.dragdrop;

import java.awt.*;

public class TrackerViewBinder {

    public static void bind(DragAndDropLayer layer, Component target, TrackerModel model) {
        // Viewの座標とサイズに基づく計算なのでViewModelやModelに判定を切り出しても嬉しく無さそうなので
        // 他のModelから呼べるように各種通知とスレッド制御だけViewModelやModelに切り出した
        final TrackerViewModel viewModel = new TrackerViewModel(model);
        final Tracker tracker = new Tracker(layer, target);
        tracker.bind(viewModel);
    }
}
