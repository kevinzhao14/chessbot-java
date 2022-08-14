package com.example.demo.ai.objects;

import java.util.ArrayList;

public class Pin {
    int pinPiece;
    ArrayList<Integer> pinMoves;

    public Pin(int pinPiece, ArrayList<Integer> pinMoves) {
        this.pinPiece = pinPiece;
        this.pinMoves = pinMoves;
    }

    public Pin clone() {
        return new Pin(this.pinPiece, (ArrayList<Integer>) this.pinMoves.clone());
    }

    public int pinPiece() {
        return pinPiece;
    }

    public ArrayList<Integer> pinMoves() {
        return pinMoves;
    }
}
