package com.github.guignol.indrah.view;

import com.github.guignol.swing.binding.Property;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.function.Function;

public class AutoResizeLayer {

    private final JLayeredPane layeredPane;
    private final JPanel defaultPanel;
    // intとIntegerで処理が異なるので注意
    private Integer modalLayer = JLayeredPane.MODAL_LAYER;

    public AutoResizeLayer() {
        defaultPanel = new JPanel();
        layeredPane = new JLayeredPane();
        layeredPane.add(defaultPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                final Dimension size = layeredPane.getSize();
                defaultPanel.setSize(size);

                layeredPane.revalidate();
                layeredPane.repaint();
            }
        });
    }

    public Container getRoot() {
        return layeredPane;
    }

    public Container getDefault() {
        return defaultPanel;
    }

    public void addPalette(Component palette) {
        layeredPane.add(palette, JLayeredPane.PALETTE_LAYER, 0);
    }

    public void redraw() {
        layeredPane.invalidate();
        layeredPane.repaint();
    }

    public void remove(Component component) {
        layeredPane.remove(component);
    }

    public void addModal(Component modal) {
        layeredPane.add(modal, modalLayer++, 0);
    }

    public void addModal(Component modal, Function<Dimension, Dimension> relativeSize) {
        layeredPane.add(modal, modalLayer++);
        Property.onEvent(layeredPane)
                .subscribe(event -> {
                    final Dimension parentSize = layeredPane.getSize();
                    if (relativeSize == null) {
                        modal.setSize(parentSize);
                    } else {
                        final Dimension relative = relativeSize.apply(parentSize);
                        modal.setSize(relative);
                    }
                });
    }
}
