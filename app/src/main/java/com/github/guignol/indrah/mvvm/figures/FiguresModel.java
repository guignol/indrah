package com.github.guignol.indrah.mvvm.figures;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class FiguresModel {

    private PublishSubject<Figure> onFigureAdded = PublishSubject.create();

    Observable<Figure> onFigureAdded() {
        return onFigureAdded.hide();
    }

    public void addFigure(final Figure figure) {
        onFigureAdded.onNext(figure);
    }

    public void clear() {
        onFigureAdded.onNext(Figure.CLEAR);
    }
}
