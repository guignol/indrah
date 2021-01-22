package com.github.guignol.indrah.mvvm.figures;

import java.awt.*;
import java.awt.geom.Path2D;

public class Figure {

    public static Figure CLEAR = new Figure(null, null);

    final Path2D path;
    final Color color;

    public Figure(Path2D path, Color color) {
        this.path = path;
        this.color = color;
    }
}
