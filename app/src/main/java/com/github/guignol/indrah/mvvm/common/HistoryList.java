package com.github.guignol.indrah.mvvm.common;

import com.github.guignol.indrah.Colors;
import com.github.guignol.indrah.model.swing.SimpleListModel;
import com.github.guignol.swing.binding.Property;
import io.reactivex.Observable;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;

class HistoryList {

    private static class Style {

        public Color background() {
            return Colors.HEAVY.get();
        }

        public Color highlighted() {
            return Colors.brighter(background(), 0.8);
        }

        private final Color removalColor = Colors.darker(Color.RED, 0.8);

        public Color removalColor() {
            return removalColor;
        }

        public MatteBorder removalBorder() {
            return BorderFactory.createMatteBorder(5, 6, 5, 5, background());
        }

        public MatteBorder removalBorder2() {
            return BorderFactory.createMatteBorder(5, 6, 5, 5, removalColor);
        }
    }

    private final Popup popup;
    public JList<Path> list = new JList<>();
    public JList<Path> removal = new JList<>();
    private int highlighted = -1;
    private int toRemove = -1;

    HistoryList(Component target, Observable<List<Path>> history) {

        final Style style = new Style();
        final int PATH_WIDTH = 400;
        final int CELL_HEIGHT = 33;
        final int REMOVAL_WIDTH = 33;

        final SimpleListModel<Path> listModel = new SimpleListModel<>();
        list.setModel(listModel);
        list.setFixedCellHeight(CELL_HEIGHT);
        removal.setModel(listModel);
        removal.setFixedCellHeight(CELL_HEIGHT);

        final Box box = Box.createHorizontalBox();
        box.add(removal);
        box.add(list);
        popup = Popup.with(box).onto(target).forSize(() -> {
            list.clearSelection();
            list.updateUI();
            removal.clearSelection();
            removal.updateUI();

            final Dimension size;
            if (listModel.isEmpty()) {
                size = new Dimension();
            } else {
                size = new Dimension(PATH_WIDTH, listModel.getSize() * CELL_HEIGHT);
            }
            list.setPreferredSize(size);
            list.setMaximumSize(size);
            list.setMinimumSize(size);
            final Dimension remover = new Dimension(size);
            remover.width = REMOVAL_WIDTH;
            removal.setPreferredSize(remover);
            removal.setMaximumSize(remover);
            removal.setMinimumSize(remover);

            size.width += REMOVAL_WIDTH;
            return size;
        });
        popup.doNotHideOn(list, removal);

        list.setBackground(Color.LIGHT_GRAY);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {
                final JLabel label;
                label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                final Path path = (Path) value;
                label.setText(path.toString());
                label.setForeground(Color.WHITE);
                if (toRemove == index) {
                    label.setBackground(style.removalColor());
                } else if (highlighted == index) {
                    label.setBackground(style.highlighted());
                } else {
                    label.setBackground(style.background());
                }
                return label;
            }
        });
        removal.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            final JLabel label = new JLabel();
            label.setOpaque(true);
            if (toRemove == index) {
                label.setBorder(style.removalBorder2());
                label.setBackground(style.background());
            } else {
                label.setBorder(style.removalBorder());
                label.setBackground(style.removalColor());
            }
            return label;
        });

        history.subscribe(data -> {
            listModel.clear();
            data.forEach(listModel::add);
            popup.reshowIfShown();
        });
        Property.onHovered(list).subscribe(highlighted -> {
            this.highlighted = highlighted;
            this.list.updateUI();
            this.removal.updateUI();
        });
        Property.onHovered(removal).subscribe(highlighted -> {
            this.toRemove = highlighted;
            this.list.updateUI();
            this.removal.updateUI();
        });
    }

    public void hideNow() {
        popup.hideNow();
    }
}
