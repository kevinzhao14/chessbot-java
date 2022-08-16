package com.example.demo.ai.objects;

import java.util.ArrayList;

public class ValidMoves {
    ArrayList<Integer> moves;
    ArrayList<Pin> pins;
    long[] control;

    public ValidMoves(ArrayList<Integer> moves) {
        this.moves = moves;
        this.control = null;
    }

    public ValidMoves(long[] control, ArrayList<Pin> pins) {
        this.control = control;
        this.pins = pins;
    }

    public long[] control() {
        return control;
    }

    public ArrayList<Integer> moves() {
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
