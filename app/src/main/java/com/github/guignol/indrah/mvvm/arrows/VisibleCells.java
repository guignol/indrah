package com.github.guignol.indrah.mvvm.arrows;

import javax.swing.*;
import java.awt.*;

public class VisibleCells {

    public static VisibleCells fromListView(JList list) {
        final int first = list.getFirstVisibleIndex();
        final int last = list.getLastVisibleIndex();
        final Rectangle rectangle = list.getCellBounds(first, last);
        return new VisibleCells(list, first, last, rectangle);
    }

    private final Component source;
    public final int firstIndex;
    public final int lastIndex;
    private final Rectangle rectangle;

    private VisibleCells(Component source, int firstIndex, int lastIndex, Rectangle rectangle) {
        this.source = source;
        this.firstIndex = firstIndex;
        this.lastIndex = lastIndex;
        this.rectangle = rectangle;
    }

    public Rectangle convert(Component destination) {
        return SwingUtilities.convertRectangle(source, rectangle, destination);
    }
}
