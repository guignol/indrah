package com.github.guignol.indrah.mvvm.sidebar;

import com.github.guignol.swing.binding.IViewModel;
import com.github.guignol.swing.rx.SwingScheduler;
import io.reactivex.Observable;

class SideBarViewModel extends IViewModel<SideBarModel> {

    SideBarViewModel(SideBarModel model) {
        super(model);
    }

    ////// from View

    void toggle() {
        model.toggle();
    }

    ////// to View

    Observable<Boolean> onOpen() {
        return model.onOpen().observeOn(SwingScheduler.getInstance());
    }
}
