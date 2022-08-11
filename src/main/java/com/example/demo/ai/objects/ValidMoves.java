package com.example.demo.ai.objects;

import java.util.ArrayList;

public class ValidMoves {
    ArrayList<Pos> moves;
    ArrayList<ArrayList<Pos>> control;

    public ValidMoves(ArrayList<Pos> moves) {
        this.moves = moves;
    }

    public ValidMoves(ArrayList<ArrayList<Pos>> control, ArrayList<Pos> pins) {
        this.control = control;
        this.moves = pins;
    }

    public ArrayList<ArrayList<Pos>> getControl() {
        return control;
    }

    public ArrayList<Pos> getMoves() {
        return moves;
    }

    public ArrayList<Pos> getPins() {
        return moves;
    }
}
