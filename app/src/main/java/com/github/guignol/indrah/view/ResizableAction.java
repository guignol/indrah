package com.github.guignol.indrah.view;

import com.github.guignol.indrah.model.Direction;
import com.github.guignol.indrah.model.Edge;

import javax.swing.*;
import java.awt.*;

public class ResizableAction {

    static Edge onDragHorizontally(final Container parent,
                                   final Component leftComponent,
                                   final Component rightComponent,
                                   final int left,
                                   final int right) {
        final int navigationCenter = (right + left) / 2;
        final int parentSize = parent.getWidth();
        final int parentCenter = parentSize / 2;
        final Component target;
        final Dimension maximumSize;
        Edge edge;
        if (navigationCenter < parentCenter) {
            target = leftComponent;
            maximumSize = target.getMaximumSize();
            maximumSize.width = Math.round(left);
            maximumSize.width = Math.max(maximumSize.width, 0);
            rightComponent.setMaximumSize(null);
            edge = new Edge(Direction.LEFT, maximumSize.width);
        } else {
            target = rightComponent;
            maximumSize = target.getMaximumSize();
            maximumSize.width = Math.round(parentSize - right);
            maximumSize.width = Math.max(maximumSize.width, 0);
            leftComponent.setMaximumSize(null);
            edge = new Edge(Direction.RIGHT, maximumSize.width);
        }
        target.setMaximumSize(maximumSize);
        target.setPreferredSize(maximumSize);
        SwingUtilities.invokeLater(target::revalidate);
        return edge;
    }

    static Edge onDragVertically(final Container parent,
                                 final Component topComponent,
                                 final Component bottomComponent,
                                 final int top,
                                 final int bottom) {
        final int navigationalCenter = (bottom + top) / 2;
        final int parentSize = parent.getHeight();
        final int parentCenter = parentSize / 2;
        final Component target;
        final Dimension maximumSize;
        final Edge edge;
        if (navigationalCenter < parentCenter) {
            target = topComponent;
            maximumSize = target.getMaximumSize();
            maximumSize.height = Math.round(top);
            maximumSize.height = Math.max(maximumSize.height, 0);
            bottomComponent.setMaximumSize(null);
            edge = new Edge(Direction.TOP, maximumSize.height);
        } else {
            target = bottomComponent;
            maximumSize = target.getMaximumSize();
            maximumSize.height = Math.round(parentSize - bottom);
            maximumSize.height = Math.max(maximumSize.height, 0);
            topComponent.setMaximumSize(null);
            edge = new Edge(Direction.BOTTOM, maximumSize.height);
        }
        target.setMaximumSize(maximumSize);
        target.setPreferredSize(maximumSize);
        SwingUtilities.invokeLater(target::revalidate);
        return edge;
    }
}
