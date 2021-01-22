package com.github.guignol.indrah.model;

import java.util.List;
import java.util.stream.IntStream;

public class Range {

    public static final Range NONE = new Range(-1, -1);

    public final int begin;
    public final int end; // 含む

    public Range(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    public boolean exists() {
        // endは範囲内なので、beginとendが同値でも有効な範囲が存在する
        return 0 <= begin && 0 <= end && 0 <= end - begin;
    }

    public boolean contains(int target) {
        return exists() && begin <= target && target <= end;
    }

    public boolean contains(Range target) {
        return exists() && begin <= target.begin && target.end <= end;
    }

    public boolean containsPartially(Range target) {
        return  exists() && IntStream.rangeClosed(target.begin, target.end).anyMatch(this::contains);
    }

    public Range offset(int offset) {
        return new Range(begin + offset, end + offset);
    }

    public Range[] splitBy(Range range) {
        final Range less = new Range(begin, range.begin - 1);
        final Range more = new Range(range.end + 1, end);
        return new Range[]{less, more};
    }

    public <T> List<T> subList(List<T> list) {
        return list.subList(begin, end + 1);
    }

    @Override
    public String toString() {
        return begin + " , " + end;
    }
}
