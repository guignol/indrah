package com.github.guignol.indrah.mvvm.rebase;

import com.github.guignol.indrah.model.CommitLog;
import com.github.guignol.swing.binding.IViewModel;
import com.github.guignol.swing.rx.SwingScheduler;
import io.reactivex.Observable;

class RewriteMessageDialogViewModel extends IViewModel<RewriteMessage> {

    RewriteMessageDialogViewModel(RewriteMessage model) {
        super(model);
    }

    /////////// From View

    void close() {
        model.close();
    }

    /////////// To View

    Observable<CommitLog> onMessageEdit() {
        return model.onMessageEdit().observeOn(SwingScheduler.getInstance());
    }

}
