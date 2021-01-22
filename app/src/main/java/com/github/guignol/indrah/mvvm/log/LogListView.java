package com.github.guignol.indrah.mvvm.log;

import com.github.guignol.indrah.model.CommitLogAnnotated;
import com.github.guignol.swing.binding.ComponentHolder;
import com.github.guignol.indrah.mvvm.arrows.VisibleCells;
import com.github.guignol.indrah.view.list.UpperListContainer;
import com.github.guignol.swing.binding.Bindable;
import com.github.guignol.swing.binding.IView;
import com.github.guignol.swing.binding.Keys;
import com.github.guignol.swing.binding.Property;
import com.github.guignol.swing.processor.View;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.concurrent.TimeUnit;

@View(factoryName = "LogListFactory")
public class LogListView implements IView<LogListViewModel>, ComponentHolder {

    @Override
    public void bind(LogListViewModel viewModel) {

        final JList<CommitLogAnnotated> list = listContainer.list;

        // to ViewModel
        Bindable.view(list, Keys.COPY)
                .toViewModel(viewModel::toClipboard);

        // from ViewModel
        list.setModel(viewModel.dataModel);
        list.setSelectionModel(viewModel.selection);
        viewModel.onDataUpdated().subscribe(status -> {
            list.updateUI();
            listContainer.alignBottom();
        });

        // スクロールを検知
        Property.onChanged(listContainer.scrollPane.getViewport())
                .throttleLast(100, TimeUnit.MILLISECONDS)
                .subscribe(eventStatus -> viewModel.updateVisibleCell(VisibleCells.fromListView(list)));
    }

    private final UpperListContainer<CommitLogAnnotated> listContainer;

    LogListView() {
        listContainer = new UpperListContainer<>(CellRenderer.CELL_HEIGHT);
        listContainer.list.setCellRenderer(new CellRenderer(true));
    }

    @Override
    public Component getComponent() {
        return listContainer.getComponent();
    }

    public static class CellRenderer extends DefaultListCellRenderer {

        public static final int CELL_HEIGHT = 40;
        private static final int SIDE_MARGIN = 10;
        public static final int LONG_MARGIN = SIDE_MARGIN + 20;

        private static final Border emptyBorder = BorderFactory.createEmptyBorder(0, SIDE_MARGIN, 0, SIDE_MARGIN);
        private static final Border emptyBorder2 = BorderFactory.createEmptyBorder(0, LONG_MARGIN, 0, SIDE_MARGIN);
        private static final MatteBorder coloredBorder = BorderFactory.createMatteBorder(4, 0, 0, 0, Color.PINK);

        private final boolean original;

        public CellRenderer(boolean original) {
            this.original = original;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            final JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            CommitLogAnnotated commit = (CommitLogAnnotated) value;
            if (!original && commit.log.hasNewMessage()) {
                label.setText(commit.log.getNewMessage());
                label.setForeground(Color.RED);
            } else {
                label.setText(commit.log.message);
            }
            if (commit.lastJunction) {
                label.setBorder(new CompoundBorder(coloredBorder, emptyBorder(original)));
            } else {
                label.setBorder(emptyBorder(original));
            }
            return label;
        }

        private static Border emptyBorder(boolean original) {
            return original ? emptyBorder : emptyBorder2;
        }
    }
}
