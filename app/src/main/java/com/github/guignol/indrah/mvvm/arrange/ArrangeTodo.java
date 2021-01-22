package com.github.guignol.indrah.mvvm.arrange;

import com.github.guignol.indrah.model.CommitLog;
import com.github.guignol.indrah.model.CommitLogAnnotated;
import com.github.guignol.indrah.model.CommitLogHistory;
import com.github.guignol.indrah.mvvm.ValueHolder;
import com.github.guignol.indrah.mvvm.arrows.VisibleCells;
import com.github.guignol.indrah.mvvm.rebase.RebaseInteractive;
import com.github.guignol.indrah.mvvm.rebase.RebaseTodo;
import com.github.guignol.indrah.mvvm.rebase.RewriteMessage;
import com.github.guignol.indrah.utils.ListUtils;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrangeTodo {

    private final RebaseInteractive rebaseInteractive;
    final RewriteMessage rewriteMessage;

    public ArrangeTodo(Observable<CommitLogHistory> log, RebaseInteractive rebaseInteractive) {
        onDataUpdated = ValueHolder.createDefault(CommitLogHistory.empty());
        log.subscribe(onDataUpdated::put);

        this.rebaseInteractive = rebaseInteractive;
        this.rewriteMessage = new RewriteMessage(() -> {
            // TODO 何もしてないのに表示が更新されている（フォーカスが戻ってくるから？）
        });
    }

    // TODO 並び変えられた最古のコミットまでのリスト（ついでにメッセージ変更されているものも？）
    private final PublishSubject<Map<String, Integer>> arranged = PublishSubject.create();

    public Observable<Map<String, Integer>> arranged() {
        return arranged.hide();
    }

    void maybeArranged(List<CommitLogAnnotated> maybeArranged) {
        final Map<String, Integer> indices = new HashMap<>();
        final CommitLogHistory original = onDataUpdated.get();
        for (int i = 0; i < maybeArranged.size(); i++) {
            final String before = original.annotated.get(i).log.commit;
            final String after = maybeArranged.get(i).log.commit;
            if (!before.equals(after)) {
                // TODO これだとズレただけのやつが分からないが、それは描画側で考える（オフセットが同じのが続けば、とか）
                indices.put(after, i);
            }
        }
        arranged.onNext(indices);
    }

    //////////////////////

    private final PublishSubject<VisibleCells> visibleCell = PublishSubject.create();

    public Observable<VisibleCells> visibleCell() {
        return visibleCell.hide();
    }

    void updateVisibleCell(VisibleCells cell) {
        visibleCell.onNext(cell);
    }

    //////////////////////

    private final ValueHolder<CommitLogHistory> onDataUpdated;

    Observable<CommitLogHistory> onDataUpdated() {
        return onDataUpdated.observable();
    }

    void interactiveRebase(List<CommitLog> targets, String ontoCommit) {
        // マージコミットが存在しないか確認する
        if (CommitLog.hasMergeCommit(targets)) {
            // TODO アラート（ただし、ここは通らないはず）
            System.out.println("マージコミットがあるのでinteractive rebaseは行わない");
            return;
        }
        if (targets.isEmpty()) {
            System.out.println("たぶん最新のコミット");
            return;
        }
        // TODO 失敗した場合もリロードで上書きされる
        rebaseInteractive.start(new RebaseTodo(ListUtils.reversed(targets), ontoCommit));
    }

    void editMessage(CommitLog commitLog) {
        // TODO コミット用のUIを流用したほうがいいのでは？でもmodalにはしたい
        rewriteMessage.edit(commitLog);
    }
}
