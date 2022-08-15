package com.example.demo.ai.objects;

public class Move {
    Pos from;
    Pos to;

    public Move(Pos from, Pos to) {
        this.from = from;
        this.to = to;
    }

    public Pos from() {
        return from;
    }

    public Pos to() {
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
