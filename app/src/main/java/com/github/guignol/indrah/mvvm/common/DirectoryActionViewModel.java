package com.github.guignol.indrah.mvvm.common;

import com.github.guignol.swing.binding.IViewModel;
import com.github.guignol.swing.rx.SwingScheduler;
import io.reactivex.Observable;

import java.nio.file.Path;
import java.util.List;

class DirectoryActionViewModel extends IViewModel<Directory> {

    DirectoryActionViewModel(Directory model) {
        super(model);
    }

    ///////////// From View

    void reload() {
        model.reload();
    }

    void openExplorer() {
        model.openExplorer();
    }

    void select() {
        model.select();
    }

    void selectPath(int[] indices) {
        model.select(indices);
    }

    public void removePath(int[] indices) {
        model.removePath(indices);
    }

    ///////////// To View

    Observable<List<Path>> history() {
        return model.history().observeOn(SwingScheduler.getInstance());
    }
}
