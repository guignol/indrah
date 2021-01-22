package com.github.guignol.indrah.mvvm.common;

import com.github.guignol.swing.binding.ComponentHolder;
import com.github.guignol.indrah.mvvm.sidebar.SideBarButtons;
import com.github.guignol.swing.binding.Bindable;
import com.github.guignol.swing.binding.IView;
import com.github.guignol.swing.processor.View;

import javax.swing.*;
import java.awt.*;

@View
public class DirectoryActionView implements IView<DirectoryActionViewModel>, ComponentHolder {

    @Override
    public void bind(DirectoryActionViewModel viewModel) {

        final HistoryList history = new HistoryList(select, viewModel.history());

        // to ViewModel
        Bindable.view(history.list)
                .toViewModel(indices -> {
                    history.hideNow();
                    viewModel.selectPath(indices);
                });
        Bindable.view(history.removal)
                .toViewModel(viewModel::removePath);
        Bindable.view(reload)
                .toViewModel(viewModel::reload);
        Bindable.view(openExplorer)
                .toViewModel(viewModel::openExplorer);
        Bindable.view(select)
                .toViewModel(() -> {
                    // TODO この直後にselectにマウスエンターでポップアップを表示すると、ポップアップにマウスエンターで閉じてしまう
                    history.hideNow();
                    viewModel.select();
                });
    }

    private final JPanel panel;
    private final AbstractButton select;
    private final AbstractButton openExplorer;
    private final AbstractButton reload;

    DirectoryActionView() {
        select = SideBarButtons.init("レポジトリ変更");
        openExplorer = SideBarButtons.init("Explorer");
        reload = SideBarButtons.init("更新");

        panel = SideBarButtons.getPanel(100);
        panel.add(select);
        panel.add(openExplorer);
        panel.add(reload);
    }

    @Override
    public Component getComponent() {
        return panel;
    }
}
