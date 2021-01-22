package com.github.guignol.indrah.mvvm.commit_browser;

import com.github.guignol.indrah.Colors;
import com.github.guignol.indrah.mvvm.arrange.ArrangeTodo;
import com.github.guignol.indrah.mvvm.arrange.ArrangeTodoFactory;
import com.github.guignol.indrah.mvvm.arrows.ArrowsLayer;
import com.github.guignol.indrah.mvvm.figures.Figure;
import com.github.guignol.indrah.mvvm.figures.FiguresModel;
import com.github.guignol.indrah.mvvm.log.LogListFactory;
import com.github.guignol.indrah.mvvm.log.LogListModel;
import com.github.guignol.indrah.view.CrossContainer;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class CommitBrowserFactory {

    public static Component create(CommitBrowserModel model) {

        final FiguresModel figures = new FiguresModel();
        final ArrowsLayer layer = ArrowsLayer.create(figures);

        final LogListModel logList = new LogListModel(model.commitLog.asObservable());
        final ArrangeTodo arrangeTodo = new ArrangeTodo(model.commitLog.asObservable(), model.rebaseInteractive);

        Observable.combineLatest(
                model.commitLog.asObservable(),
                arrangeTodo.arranged(),
                logList.visibleCell(),
                arrangeTodo.visibleCell(),
                layer::getArrows)
        .subscribe(arrows -> {
            figures.addFigure(Figure.CLEAR);
            arrows.forEach(figures::addFigure);
        });

        // 画面構成
        CrossContainer.init(layer.getDefault(), new CrossContainer.Quadrants(
                LogListFactory.create(logList),
                ArrangeTodoFactory.create(arrangeTodo),
                dummy(),
                dummy()
        ));
        return layer.getRoot();
    }

    @NotNull
    private static JPanel dummy() {
        return new JPanel() {
            {
                setOpaque(true);
                Colors.LITE.background(this);
            }
        };
    }
}
