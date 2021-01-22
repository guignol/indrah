package com.github.guignol.indrah.mvvm.dragging_arrow;

import java.awt.*;
import java.awt.geom.Path2D;

class DraggingArrow {

    final Color color;
    final Path2D path2D;

    DraggingArrow(Color color, Path2D path2D) {
        this.color = color;
        this.path2D = path2D;
    }
}
