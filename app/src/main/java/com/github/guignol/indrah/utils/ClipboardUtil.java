package com.github.guignol.indrah.utils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class ClipboardUtil {

    private static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    public static void copy(String content) {
        final StringSelection selection = new StringSelection(content);
        clipboard.setContents(selection, selection);
    }
}
