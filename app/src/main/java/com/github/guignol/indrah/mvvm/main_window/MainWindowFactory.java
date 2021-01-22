package com.github.guignol.indrah.mvvm.main_window;

import com.github.guignol.indrah.mvvm.commit_browser.CommitBrowserFactory;
import com.github.guignol.indrah.mvvm.commit_maker.CommitMakerFactory;

import java.awt.*;

public class MainWindowFactory {

    public static Component create(MainWindowModel model) {
        final MainWindowViewModel viewModel = new MainWindowViewModel(model);
        final MainWindow view = new MainWindow();
        view.bind(viewModel);
        view.add(CommitBrowserFactory.create(model.commitBrowserModel), MainWindowType.COMMIT_BROWSER);
        view.add(CommitMakerFactory.create(model.commitMakerModel), MainWindowType.COMMIT_MAKER);
        return view.getComponent();
    }
}
