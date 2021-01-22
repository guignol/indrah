package com.github.guignol.indrah.mvvm.rebase;

import javax.swing.*;
import java.awt.*;

public class RewriteMessageDialogFactory {

    public static void create(RewriteMessage model, Component component) {
        final JFrame frame = (JFrame) SwingUtilities.windowForComponent(component);
        final RewriteMessageDialogViewModel viewModel = new RewriteMessageDialogViewModel(model);
        final RewriteMessageDialog view = new RewriteMessageDialog(frame);
        view.bind(viewModel);
    }
}
