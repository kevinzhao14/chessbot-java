package com.example.demo.ai.objects;

public class Pos {
    private int y;
    private int x;

    public Pos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public Pos clone() {
        return new Pos(x, y);
    }

    public boolean equals(Pos other) {
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }

    public int[] toArray() {
        return new int[]{x, y};
    }
}
