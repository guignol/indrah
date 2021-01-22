package com.github.guignol.indrah.model;

import java.util.HashMap;
import java.util.Map;

public enum Direction {
    TOP(true),
    BOTTOM(true),
    LEFT(false),
    RIGHT(false);

    private final boolean vertical;

    Direction(boolean vertical) {
        this.vertical = vertical;
    }

    public boolean vertical() {
        return vertical;
    }

    public boolean horizontal() {
        return !vertical();
    }

    public static <T> Map<Direction, T> createMap() {
        return new HashMap<>();
    }
}
