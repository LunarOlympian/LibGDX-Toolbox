package com.github.LunarOlympian.Toolbox.tools.groups;

public class Pair<T, K> {

    T first;
    K second;

    public Pair(T first, K second) {
        this.first = first;
        this.second = second;
    }

    // Useful for pairs
    public T getFirst() {
        return first;
    }

    public K getSecond() {
        return second;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public void setSecond(K second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return first + ", " + second;
    }
}
