package com.example.demo.ai.objects;

public class Group<T> {
    T white;
    T black;

    public Group(T white, T black) {
        this.white = white;
        this.black = black;
    }

    public T get(Side side) {
        if (side == Side.WHITE) {
            return white;
        } else if (side == Side.BLACK) {
            return black;
        } else {
            return null;
        }
    }

    public void set(Side side, T value) {
        if (side == Side.WHITE) {
            this.white = value;
        } else {
            this.black = value;
        }
    }

    public T white() {
        return this.white;
    }

    public T black() {
        return this.black;
    }
}
