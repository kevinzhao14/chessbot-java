package com.example.demo.ai.objects;

public class Pos {
    int row;
    int col;

    public Pos(int col, int row) {
        this.col = col;
        this.row = row;
    }

    public int x() {
        return col;
    }

    public int y() {
        return row;
    }
}
