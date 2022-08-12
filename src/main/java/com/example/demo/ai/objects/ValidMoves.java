package com.example.demo.ai.objects;

import java.util.ArrayList;

public class ValidMoves {
    ArrayList<Pos> moves;
    ArrayList<Pin> pins;
    ArrayList<ArrayList<Pos>> control;

    public ValidMoves(ArrayList<Pos> moves) {
        this.moves = moves;
        this.control = null;
    }

    public ValidMoves(ArrayList<ArrayList<Pos>> control, ArrayList<Pin> pins) {
        this.control = control;
        this.pins = pins;
    }

    public ArrayList<ArrayList<Pos>> control() {
        return control;
    }

    public ArrayList<Pos> moves() {
        return moves;
    }

    public ArrayList<Pin> pins() {
        return pins;
    }

    @Override
    public String toString() {
        return "ValidMoves{" +
                "moves=" + moves +
                ", pins=" + pins +
                ", control=" + control +
                '}';
    }
}
