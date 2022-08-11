package com.example.demo.ai.objects;

public class Pair<T, U> {
    T a;
    U b;

    public Pair(T a, U b) {
        this.a = a;
        this.b = b;
    }

    public T a() {
        return a;
    }

    public U b() {
        return b;
    }
}
