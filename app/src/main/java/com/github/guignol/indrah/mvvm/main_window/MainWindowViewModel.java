package com.github.guignol.indrah.mvvm.main_window;

import com.github.guignol.swing.binding.IViewModel;
import com.github.guignol.swing.rx.SwingScheduler;
import io.reactivex.Observable;

class MainWindowViewModel extends IViewModel<MainWindowModel> {

    MainWindowViewModel(MainWindowModel model) {
        super(model);
    }

    ////// To View

    Observable<MainWindowType> onWindowChangeRequested() {
        return model.onWindowChangeRequested().observeOn(SwingScheduler.getInstance());
    }
}
