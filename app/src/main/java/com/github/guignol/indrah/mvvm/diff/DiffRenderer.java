package com.github.guignol.indrah.mvvm.diff;

import com.github.guignol.indrah.Colors;
import com.github.guignol.indrah.model.ImageHolder;
import com.github.guignol.indrah.model.swing.ListItem;
import com.github.guignol.indrah.view.list.ListRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class DiffRenderer<M extends ListItem> implements ListRenderer<M> {

    @NotNull
    public Component render(JLabel label, M value, boolean isSelected, @Nullable Consumer<ImageIcon> updater) {
        final String line = value.item();
        label.setOpaque(true);
        label.setIcon(null);
        // 基本の色
        label.setBackground(Color.WHITE);
        label.setForeground(Color.BLACK);
        final ImageHolder imageHolder = value.imageHolder();
        if (imageHolder != null && updater != null) {
            final ImageIcon icon = imageHolder.lazy(imageIcon -> SwingUtilities.invokeLater(() -> updater.accept(imageIcon)));
            // このメソッド内でしかlabelを更新できないので、
            // 画像読み込み後にもう一度ここを通るようにする
            // https://stackoverflow.com/questions/26102119/jlist-lazy-load-images
            if (icon != null) {
                label.setIcon(icon);
                label.setText("");
                return label;
            }
        }

        label.setText(line);
        label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        final boolean isHeader = value.isHeader();
        label.setEnabled(!isHeader);
        if (isHeader) {
            label.setBackground(Colors.DIFF.HEADER_BACK.get());
        } else if (line.startsWith("+")) {
            if (isSelected) {
                label.setBackground(Colors.DIFF.BACK.PLUS);
            } else {
                label.setForeground(Colors.DIFF.TEXT.PLUS);
            }
        } else if (line.startsWith("-")) {
            if (isSelected) {
                label.setBackground(Colors.DIFF.BACK.MINUS);
            } else {
                label.setForeground(Colors.DIFF.TEXT.MINUS);
            }
        } else if (!line.startsWith(" ") && !line.startsWith("\\")) {
            label.setForeground(Colors.DIFF.SPECIAL);
        } else {
            if (isSelected) {
                label.setBackground(Colors.SELECTED_LITE.get());
            }
        }

        return label;
    }
}
