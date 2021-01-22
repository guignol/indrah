package com.github.guignol.indrah.mvvm.arrange;

import com.github.guignol.indrah.mvvm.rebase.RewriteMessageDialogFactory;

import java.awt.*;

public class ArrangeTodoFactory {

    public static Component create(ArrangeTodo model) {
        final ArrangeTodoListViewModel viewModel = new ArrangeTodoListViewModel(model);
        final ArrangeTodoListView view = new ArrangeTodoListView();
        view.bind(viewModel);

        final Component component = view.getComponent();
        RewriteMessageDialogFactory.create(model.rewriteMessage, component);
        return component;
    }
}
