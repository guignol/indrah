package com.github.guignol.indrah.view.list;

import com.github.guignol.indrah.model.ImageHolder;
import com.github.guignol.indrah.model.swing.ListItem;
import com.github.guignol.indrah.model.swing.SimpleListModel;
import com.github.guignol.swing.binding.Keys;
import com.github.guignol.swing.binding.Property;
import io.reactivex.Observable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import java.awt.event.MouseListener;

public class ListWrapperForJTable implements ListWrapper {

    private final JTable table;
    private final JScrollPane scrollPane;
    private final ListRenderer<ListItem> renderer;

    private final Runnable preRender;

    public ListWrapperForJTable(SimpleListModel<? extends ListItem> dataModel,
                                ListRenderer<ListItem> renderer) {
        this.renderer = renderer;
        this.table = new JTable();
        this.scrollPane = new JScrollPane(this.table);
        this.scrollPane.setBorder(BorderFactory.createEmptyBorder());
        this.table.setTableHeader(null);
        this.table.setShowHorizontalLines(false);
        this.table.setRowMargin(0);
        this.table.setColumnModel(new DefaultTableColumnModel() {
            @Override
            public void setColumnMargin(int newMargin) {
                super.setColumnMargin(0);
            }
        });
        this.table.setModel(new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return dataModel.getSize();
            }

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return dataModel.getElementAt(rowIndex);
            }
        });

        // テキストのsetRowHeightを先にまとめてやるためのプリレンダリング
        preRender = () -> {
            // 以下のいずれかを実行しないと、テーブルの最大行数が最初に表示した行数に依存してしまう
            // おそらく、JTable内部でrowModel = null;が必要なのだと思う
            this.table.setRowHeight(1);
//            setRowSorter(null);
//            tableChanged(new TableModelEvent(getModel()));
            final int size = dataModel.getSize();
            for (int i = 0; i < size; i++) {
                final ListItem item = dataModel.getElementAt(i);
                final ImageHolder imageHolder = item.imageHolder();
                if (imageHolder == null || !imageHolder.hasLoader()) {
                    // 大したコストではないのでキャッシュしない
                    final JLabel label = new JLabel();
                    this.renderer.render(label, item, false, null);
                    final int height = label.getPreferredSize().height;
                    // バイナリの場合はdiffを表示できないので1未満になるが、1未満でsetRowHeightできないため
                    // TODO 何か表示したほうがいいかもしれない（その場合はここじゃなくListItemの修正）
                    table.setRowHeight(i, Math.max(1, height));
                }
            }
        };
        final TableColumn tableColumn = this.table.getColumnModel().getColumn(0);
        tableColumn.setCellRenderer((table, value, isSelected, hasFocus, row, column) ->
                this.renderer.render(new JLabel(), (ListItem) value, isSelected, imageIcon -> {
                    final int iconHeight = imageIcon.getIconHeight();
                    table.setRowHeight(row, iconHeight);
                }));
    }

    @Override
    public void addMouseListener(MouseListener listener) {
        table.addMouseListener(listener);
    }

    @Override
    public void setSelectionModel(ListSelectionModel selectionModel) {
        table.setSelectionModel(selectionModel);
    }

    @Override
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    @Override
    public void updateUI() {
        preRender.run();
    }

    @Override
    public Observable<int[]> selectedIndices() {
        return Property.onSelection(table);
    }

    @Override
    public Keys.Registry getKeysRegistry() {
        return new Keys.Registry(this.table);
    }
}
