package com.github.guignol.indrah.model.swing;

import com.github.guignol.indrah.model.DiffLine;

public class DiffLineForList implements ListItem {

    private final boolean isHeader;
    public final DiffLine diffLine;

    public DiffLineForList(DiffLine diffLine, boolean isHeader) {
        this.isHeader = isHeader;
        this.diffLine = diffLine;
    }

    @Override
    public String item() {
        return diffLine.line;
    }

    @Override
    public boolean isHeader() {
        return isHeader;
    }
}
