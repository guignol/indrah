package com.github.guignol.indrah.mvvm.common;

import com.github.guignol.swing.binding.ComponentHolder;
import com.github.guignol.indrah.mvvm.sidebar.SideBarButtons;
import com.github.guignol.swing.binding.Bindable;
import com.github.guignol.swing.binding.IView;
import com.github.guignol.swing.processor.View;

import javax.swing.*;
import java.awt.*;

@View
public class WindowSwitcherView implements IView<WindowSwitcherViewModel>, ComponentHolder {

    @Override
    public void bind(WindowSwitcherViewModel viewModel) {
        Bindable.view(changeWindow)
                .toViewModel(viewModel::changeWindow);
    }

    private final JPanel panel;
    private final AbstractButton changeWindow;

    WindowSwitcherView() {
        changeWindow = SideBarButtons.init("Window切替");

        panel = SideBarButtons.getPanel(40);
        panel.add(changeWindow);
    }

    @Override
    public Component getComponent() {
        return panel;
    }
}
