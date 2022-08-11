package com.example.demo.ai.objects;

import java.util.ArrayList;

public class Pin {
    Pos pinPiece;
    ArrayList<Pos> pinMoves;

    public Pin(Pos pinPiece, ArrayList<Pos> pinMoves) {
        this.pinPiece = pinPiece;
        this.pinMoves = pinMoves;
    }

    public Pin clone() {
        ArrayList<Pos> newMoves = new ArrayList<>();
        for (Pos p : this.pinMoves) {
            newMoves.add(p.clone());
        }
        return new Pin(this.pinPiece.clone(), newMoves);
    }

    public Pos pinPiece() {
        return pinPiece;
    }

    public ArrayList<Pos> pinMoves() {
        return pinMoves;
    }
}
