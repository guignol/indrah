package com.github.guignol.indrah.view.list;

import com.github.guignol.indrah.model.swing.ListItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public interface ListRenderer<M extends ListItem> {
    @NotNull
    Component render(JLabel label, M value, boolean isSelected, @Nullable Consumer<ImageIcon> updater);
}
