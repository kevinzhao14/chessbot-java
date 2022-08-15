package com.example.demo.ai.objects;

import java.util.ArrayList;

public class Pin {
    int pinPiece;
    long pinMoves;

    public Pin(int pinPiece, long pinMoves) {
        this.pinPiece = pinPiece;
        this.pinMoves = pinMoves;
    }

    public Pin clone() {
        return new Pin(this.pinPiece, this.pinMoves);
    }

    public int pinPiece() {
        return pinPiece;
    }

    public long pinMoves() {
        return pinMoves;
    }

    @Override
    public String toString() {
        return "Pin{" +
                "pinPiece=" + pinPiece +
                ", pinMoves=" + pinMoves +
                '}';
    }
}
