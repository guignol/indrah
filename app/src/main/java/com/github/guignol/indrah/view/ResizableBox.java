package com.github.guignol.indrah.view;

import com.github.guignol.indrah.model.Edge;
import com.github.guignol.indrah.utils.ListUtils;
import io.reactivex.Observable;

import java.awt.*;
import java.util.List;

public class ResizableBox {

    public final Container parent;
    public final Container head;
    public final Container tail;
    public final Navigator navigator;
    public final Observable<Edge> edgeObservable;

    public ResizableBox(Container parent, Container head, Container tail, Navigator navigator, Observable<Edge> edgeObservable) {
        this.parent = parent;
        this.head = head;
        this.tail = tail;
        this.navigator = navigator;
        this.edgeObservable = edgeObservable;
    }

    private void linkHorizontally(List<ResizableBox> boxList) {
        if (boxList.contains(this)) {
            boxList.remove(this);
        }
        navigator.setDispatcher((left, right) -> boxList.forEach(box -> box.onDragHorizontally(left, right)));
    }

    private void onDragHorizontally(int left, int right) {
        ResizableAction.onDragHorizontally(parent, head, tail, left, right);
    }

    private void onDragVertically(int top, int bottom) {
        ResizableAction.onDragVertically(parent, head, tail, top, bottom);
    }

    public static void linkHorizontally(ResizableBox... boxes) {
        for (ResizableBox box : boxes) {
            box.linkHorizontally(ListUtils.from(boxes));
        }
    }
}
