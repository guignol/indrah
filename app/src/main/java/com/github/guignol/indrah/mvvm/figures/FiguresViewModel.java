package com.github.guignol.indrah.mvvm.figures;

import com.github.guignol.swing.binding.IViewModel;
import com.github.guignol.swing.rx.SwingScheduler;
import io.reactivex.Observable;

class FiguresViewModel extends IViewModel<FiguresModel> {

    FiguresViewModel(FiguresModel model) {
        super(model);
    }

    Observable<Figure> onNext() {
        return model.onFigureAdded().observeOn(SwingScheduler.getInstance());
    }
}
