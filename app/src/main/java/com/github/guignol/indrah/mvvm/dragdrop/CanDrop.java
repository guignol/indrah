package com.github.guignol.indrah.mvvm.dragdrop;

import java.awt.*;

class CanDrop {
    final boolean yes;
    final Rectangle target;
    final Trackable atRoot;

    CanDrop(boolean yes, Rectangle target, Trackable atRoot) {
        this.yes = yes;
        this.target = target;
        this.atRoot = atRoot;
    }
}
