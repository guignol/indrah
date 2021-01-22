package com.github.guignol.indrah.mvvm.setting;

import com.github.guignol.swing.binding.ComponentHolder;
import com.github.guignol.swing.binding.Bindable;
import com.github.guignol.swing.binding.IView;
import com.github.guignol.swing.processor.View;

import javax.swing.*;
import java.awt.*;

@View
public class SettingColorView implements IView<SettingColorViewModel>, ComponentHolder {

    @Override
    public void bind(SettingColorViewModel viewModel) {
        viewModel.colors().forEach(color -> {
            final JButton button = new JButton();
            button.setBackground(color);
            panel.add(button);

            Bindable.view(button)
                    .toViewModel(() -> viewModel.changeColor(color));
        });
    }

    private final JPanel panel;

    SettingColorView() {
        panel = new JPanel();
        panel.setOpaque(true);
        panel.setLayout(new GridLayout(1, 0));
        final Dimension size = new Dimension(Short.MAX_VALUE, 40);
        panel.setPreferredSize(size);
        panel.setMaximumSize(size);
//        Colors.LITE.background(panel);
    }

    @Override
    public Component getComponent() {
        return panel;
    }
}
