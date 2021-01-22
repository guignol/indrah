package com.github.guignol.indrah.mvvm.commit_maker;

import com.github.guignol.indrah.mvvm.commit.CommitAction;
import com.github.guignol.indrah.mvvm.common.Directory;
import com.github.guignol.indrah.mvvm.diff.DiffListModel;
import com.github.guignol.indrah.mvvm.diff.DiffReload;
import com.github.guignol.indrah.mvvm.dragdrop.Trackable;
import com.github.guignol.indrah.mvvm.dragdrop.TrackerModel;
import com.github.guignol.indrah.mvvm.filename.NameListModel;
import com.github.guignol.indrah.utils.StageUtils;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class CommitMakerModel {

    public final CommitAction commitAction;

    private final DiffReload diffReload;

    final Holder workspace;
    final Holder index;

    public CommitMakerModel(Directory directory) {
        final Supplier<Path> getter = directory.getter();
        commitAction = new CommitAction(getter);
        diffReload = new DiffReload(getter);
        workspace = new Holder(new NameListModel(), new DiffListModel(getter, diffReload.withIndexFocused, false));
        index = new Holder(new NameListModel(), new DiffListModel(getter, diffReload.withWorkFocused, true));

        workspace.observe(diffReload.workspace);
        index.observe(diffReload.index);

        // ファイル選択の同期およびDiffへの反映
        NameListModel.syncSelection(workspace.nameListModel, index.nameListModel);
        workspace.nameListModel.showDiffs().subscribe(workspace.diffModel::update);
        index.nameListModel.showDiffs().subscribe(index.diffModel::update);

        // リロード後
        diffReload.onReload.asObservable().subscribe(eventStatus -> commitAction.fetchUser());
    }

    public void reload(boolean init) {
        if (init) {
            diffReload.withWorkFocused.run();
        } else {
            diffReload.withoutFocusChanged.run();
        }
    }

    public static class Holder {
        final NameListModel nameListModel;
        final DiffListModel diffModel;
        final TrackerModel trackerModel;

        private Holder(NameListModel nameListModel, DiffListModel diffModel) {
            this.nameListModel = nameListModel;
            this.diffModel = diffModel;
            this.trackerModel = new TrackerModel();

            // dragging
            nameListModel.onDrag()
                    .throttleLast(10, TimeUnit.MILLISECONDS)
                    .distinctUntilChanged(Trackable::equals)
                    .subscribe(trackerModel::trackOn);
            // drag end
            nameListModel.onDrop().subscribe(trackerModel::trackEnd);
            // drop
            trackerModel.onDrop().subscribe(eventStatus -> stageSelectedFiles());
        }

        private void observe(DiffReload.Holder reload) {
            reload.repository.asObservable().subscribe(nameListModel::update);
            reload.focus.asObservable().subscribe(eventStatus -> nameListModel.requestFocus());
        }

        private void stageSelectedFiles() {
            StageUtils.getFileStageCommand(diffModel.directory(), diffModel.unstage, nameListModel.getSelectedDiffs())
                    .toSingle()
                    .subscribe(output -> {
                        output.print();
                        diffModel.reload();
                    });
        }
    }
}
