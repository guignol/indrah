package com.github.guignol.indrah.mvvm.main_window;

import com.github.guignol.swing.binding.ComponentHolder;
import com.github.guignol.swing.binding.IView;

import javax.swing.*;
import java.awt.*;

public class MainWindow implements IView<MainWindowViewModel>, ComponentHolder {

    @Override
    public void bind(MainWindowViewModel viewModel) {
        viewModel.onWindowChangeRequested()
                .subscribe(type -> cardLayout.show(container, type.windowName));
    }

    private final JPanel container;
    private final CardLayout cardLayout;

    MainWindow() {
        container = new JPanel();
        cardLayout = new CardLayout();
        container.setLayout(cardLayout);
    }

    void add(Component component, MainWindowType type) {
        container.add(component, type.windowName);
    }

    @Override
    public Component getComponent() {
        return container;
    }
}
