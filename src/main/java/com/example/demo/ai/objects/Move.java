package com.example.demo.ai.objects;

public class Move {
    int from;
    int to;

    public Move(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    @Override
    public String toString() {
        return "Move{" +
                from +
                " -> " + to +
                '}';
    }
}
