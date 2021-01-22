package com.github.guignol.indrah.model;

public class Edge {
    public final Direction from;
    public final int distance;

    public Edge(Direction from, int distance) {
        this.from = from;
        this.distance = distance;
    }

    public static class Pair {
        public final Edge vertical;
        public final Edge horizontal;

        public Pair(Edge vertical, Edge horizontal) {
            if (vertical.from.horizontal()) {
                throw new RuntimeException();
            }
            if (horizontal.from.vertical()) {
                throw new RuntimeException();
            }
            this.vertical = vertical;
            this.horizontal = horizontal;
        }
    }
}
