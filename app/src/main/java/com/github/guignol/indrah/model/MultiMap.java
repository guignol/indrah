package com.github.guignol.indrah.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MultiMap<K, V> {
    private final Map<K, List<V>> map = new HashMap<>();

    public void clear(K key) {
        getContainer(key).clear();
    }

    public void put(K key, V value) {
        getContainer(key).add(value);
    }

    public void forEach(K key, Consumer<V> action) {
        getContainer(key).forEach(action);
    }

    @NotNull
    private List<V> getContainer(K key) {
        if (map.containsKey(key)) {
            final List<V> list = map.get(key);
            if (list != null) {
                return list;
            }
        }
        final ArrayList<V> container = new ArrayList<>();
        map.put(key, container);
        return container;
    }
}
