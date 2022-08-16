package com.example.demo.ai.objects;

import java.util.LinkedList;

public class Move {
    int from;
    int to;
    LinkedList<Move> line;
    double score;

    public Move(int from, int to, LinkedList<Move> line) {
        this.from = from;
        this.to = to;
        this.line = line;
    }

    public Move(int from, int to, double score) {
        this(from, to, null);
        this.score = score;
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    public void setLine(LinkedList<Move> line) {
        this.line = line;
    }

    public LinkedList<Move> line() {
        return line;
    }

    public double score() {
        return score;
    }

    @Override
    public String toString() {
        return "Move{" +
                from +
                " -> " + to +
                '}';
    }
}
