package com.github.guignol.indrah.mvvm.sidebar;

import com.github.guignol.indrah.Colors;
import com.github.guignol.swing.binding.ComponentHolder;
import com.github.guignol.swing.binding.Bindable;
import com.github.guignol.swing.binding.IView;

import javax.swing.*;
import java.awt.*;

public class SideBarView implements IView<SideBarViewModel>, ComponentHolder {

    @Override
    public void bind(SideBarViewModel viewModel) {
        Bindable.click(panel)
                .toViewModel(viewModel::toggle);

        viewModel.onOpen().subscribe(this::open);
    }

    private static final int MINIMUM_WIDTH = 50;
    private static final int MAXIMUM_WIDTH = 200;
    private final JPanel panel;

    SideBarView(Component directoryAction,
                Component windowSwitcher,
                Component colorSetting) {
        panel = new JPanel();
        panel.setOpaque(true);
        Colors.HEAVY.background(panel);

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(directoryAction);
        panel.add(Box.createVerticalStrut(10));
        panel.add(windowSwitcher);
        panel.add(Box.createVerticalGlue());
        panel.add(colorSetting);

        // paddingの代用
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    @Override
    public Component getComponent() {
        return panel;
    }

    private void open(boolean open) {
        for (Component child : panel.getComponents()) {
            child.setVisible(open);
        }
        if (open) {
            setWidth(panel, MAXIMUM_WIDTH);
        } else {
            setWidth(panel, MINIMUM_WIDTH);
        }
    }

    private static void setWidth(Component component, int width) {
        final Dimension preferredSize = new Dimension(width, Short.MAX_VALUE);
        component.setPreferredSize(preferredSize);
        component.setMinimumSize(preferredSize);
        component.setMaximumSize(preferredSize);
        component.revalidate();
        component.repaint();
    }
}
