package com.github.LunarOlympian.Toolbox.tools.groups;

public class Trio<T, K, I> {
    // Literally just pair but with 3
    T first = null;
    K second = null;
    I third = null;

    public Trio(T first, K second, I third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    // Used with the add method
    public Trio() {

    }

    // Useful for pairs
    public T getFirst() {
        return first;
    }

    public K getSecond() {
        return second;
    }
    public I getThird() {
        return getThird();
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public void setSecond(K second) {
        this.second = second;
    }

    public void setThird(I third) {
        this.third = third;
    }

    @Override
    public String toString() {
        return first + ", " + second + ", " + third;
    }
}
