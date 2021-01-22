package com.github.guignol.indrah.model.swing;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SimpleListModel<T> extends AbstractListModel<T> {

    private final List<T> list = new ArrayList<>();

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public T getElementAt(int i) {
        return list.get(i);
    }

    public boolean add(T diff) {
        return list.add(diff);
    }

    public void add(int index, T diff) {
        list.add(index, diff);
    }

    public void set(int index, T diff) {
        list.set(index, diff);
    }

    public void remove(int index) {
        list.remove(index);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public void clear() {
        list.clear();
    }

    public List<T> subList(int fromIndex, int toIndex) {
        // 選択範囲
        return list.subList(fromIndex, toIndex);
    }

    public Stream<T> stream() {
        return list.stream();
    }
}
