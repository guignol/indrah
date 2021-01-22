package com.github.guignol.indrah.view.list;

import com.github.guignol.indrah.Colors;
import com.github.guignol.swing.binding.ComponentHolder;
import com.github.guignol.swing.binding.Property;

import javax.swing.*;
import java.awt.*;

public class UpperListContainer<T> implements ComponentHolder {

    private final Container container;
    public final JScrollPane scrollPane;
    public final JList<T> list;
    private final int cellHeight;

    public UpperListContainer(int cellHeight) {
        this.cellHeight = cellHeight;

        list = new JList<>();

        Colors.SELECTED_FILE_NAME.use(list::setSelectionBackground);
        list.setFixedCellHeight(this.cellHeight);

        scrollPane = new JScrollPane();
        scrollPane.getViewport().setView(list);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        // 横幅を狭くして横スクロール可能になるとバーの分だけ縦に長くなるが、領域は伸びず、縦スクロール可能になってしまう
        // 面倒だし不要なので横スクロールできないようにする
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        container = Box.createVerticalBox();
        container.add(Box.createVerticalGlue());
        container.add(scrollPane);

        Property.onEvent(container, Property.Event.RESIZED)
                .subscribe(event -> this.alignBottom());
    }

    public void alignBottom() {
        // 現在見えている末尾セル
//        final int lastVisibleIndex = list.getLastVisibleIndex();
        // 現在選択中の末尾セル
        final int maxSelectionIndex = list.getMaxSelectionIndex();

        final Dimension adjusted = new Dimension(scrollPane.getMaximumSize());
        adjusted.height = list.getModel().getSize() * cellHeight;
        scrollPane.setMaximumSize(adjusted);
        scrollPane.setPreferredSize(adjusted);
        // glueも更新したいのでcontainerをrevalidateする
        container.revalidate();

        // スクロール可能な場合
        if (container.getHeight() < adjusted.height) {
            // スクロール位置の変化を下からに合わせたい
            list.ensureIndexIsVisible(maxSelectionIndex);
        }
    }

    @Override
    public Component getComponent() {
        return container;
    }
}
