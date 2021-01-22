package com.github.guignol.indrah.model.swing;

import com.github.guignol.indrah.model.ImageHolder;
import org.jetbrains.annotations.Nullable;

public interface ListItem {
    String item();

    boolean isHeader();

    @Nullable
    default ImageHolder imageHolder() {
        return null;
    }
}