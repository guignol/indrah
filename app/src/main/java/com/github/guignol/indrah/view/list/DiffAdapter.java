package com.github.guignol.indrah.view.list;

import com.github.guignol.indrah.model.swing.ListItem;

import javax.swing.*;
import java.awt.*;

public class DiffAdapter<M extends ListItem> implements ListCellRenderer<M> {

    private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
    private final ListRenderer<M> renderer;

    public DiffAdapter(ListRenderer<M> renderer) {
        this.renderer = renderer;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends M> list,
                                                  M value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        final JLabel label = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        return renderer.render(label, value, isSelected, imageIcon -> list.updateUI());
    }
}
