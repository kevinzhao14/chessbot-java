package com.example.demo.ai;

import com.example.demo.ai.objects.Check;
import com.example.demo.ai.objects.Group;
import com.example.demo.ai.objects.Pair;
import com.example.demo.ai.objects.Pin;
import com.example.demo.ai.objects.Side;
import com.example.demo.ai.objects.ValidMoves;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.example.demo.ai.Util.log;

public class State {
    private char[] board;
    private Side turn;
    private Side won;
    private boolean[] castle;
    private int enPassant;
    private Check check;
    private Group<Integer> kings;
    private HashMap<Integer, ArrayList<ArrayList<Integer>>> control;
    private ArrayList<Pin> pins;

    public State() {
        this.board = new char[64];
        this.turn = Side.WHITE;
        this.won = Side.NONE;
        this.castle = new boolean[] {false, false, false, false};
        this.enPassant = -1;
        this.check = null;
        this.kings = new Group<>(-1, -1);
        this.control = null;
        this.pins = null;
    }

    public char at(int pos) {
        if (pos < 0 || pos > 63) {
            return 0;
        }
        return this.board[pos];
    }

    public void set(int pos, char piece) {
        this.board[pos] = piece;
    }

    public void remove(int pos) {
        this.board[pos] = 0;
    }

    public State clone() {
        State state = new State();
        state.board = this.board.clone();
        state.turn = this.turn;
        state.won = this.won;

        state.castle = Arrays.copyOf(this.castle, 4);
        state.enPassant = this.enPassant;
        if (this.check != null) {
            state.check = this.check.clone();
        }
        state.kings = new Group<>(this.kings.white(), this.kings.black());

        HashMap<Integer, ArrayList<ArrayList<Integer>>> control = new HashMap<>();
        for (Map.Entry<Integer, ArrayList<ArrayList<Integer>>> entry : this.control.entrySet()) {
            int pos = entry.getKey();
            ArrayList<ArrayList<Integer>> currPaths = entry.getValue();
            ArrayList<ArrayList<Integer>> paths = new ArrayList<>();
            for (ArrayList<Integer> path : currPaths) {
                paths.add((ArrayList<Integer>) path.clone());
            }
            control.put(pos, paths);
        }

        state.control = control;

        state.pins = new ArrayList<>();
        for (Pin p : this.pins) {
            state.pins.add(p.clone());
        }

        return state;
    }

    public void calcControls(Side side) {
        HashMap<Integer, ArrayList<ArrayList<Integer>>> control = new HashMap<>();
        ArrayList<Pin> pins = new ArrayList<>();

        for (int i = 0; i < 64; i++) {
                char piece = this.board[i];
                if (Util.sideOf(piece) != side) {
                    continue;
                }
                ValidMoves squares = Bot.getValidMoves(piece, i, this, true);
                control.put(i, squares.control());
                pins.addAll(squares.pins());
        }

        this.control = control;
        this.pins = pins;
    }

    public ArrayList<Pair<Integer, ArrayList<Integer>>> isControlled(int pos) {
        ArrayList<Pair<Integer, ArrayList<Integer>>> pathList = new ArrayList<>();
        for (Map.Entry<Integer, ArrayList<ArrayList<Integer>>> entry : this.control.entrySet()) {
            int by = entry.getKey();
            ArrayList<ArrayList<Integer>> paths = entry.getValue();
            for (ArrayList<Integer> path : paths) {
                for (int p : path) {
                    if (p == pos) {
                        pathList.add(new Pair<>(by, path));
                    }
                }
            }
        }
        return pathList.size() > 0 ? pathList : null;
    }

    public boolean calcCheck() {
        int kingPos = this.kings.get(this.turn);
        ArrayList<Pair<Integer, ArrayList<Integer>>> isCheck = this.isControlled(kingPos);
        if (isCheck != null) {
            if (isCheck.size() == 1) {
                Pair<Integer, ArrayList<Integer>> check = isCheck.get(0);
                ArrayList<Integer> newList = new ArrayList<>(check.b());
                newList.add(0, check.a());
                this.check = new Check(check.a(), newList);
            } else {
                this.check = new Check(kingPos, new ArrayList<>());
            }
            return true;
        }
        this.check = null;
        return false;
    }

    public Side turn() {
        return turn;
    }

    public void turn(Side side) {
        this.turn = side;
    }

    public Side won() {
        return won;
    }

    public void won(Side side) {
        this.won = side;
    }

    public boolean[] castle() {
        return castle;
    }

    public int enPassant() {
        return enPassant;
    }

    public void enPassant(int pos) {
        this.enPassant = pos;
    }

    public Check check() {
        return check;
    }

    public Group<Integer> kings() {
        return kings;
    }

    public ArrayList<Pin> pins() {
        return pins;
    }

    public void pins(ArrayList<Pin> pins) {
        this.pins = pins;
    }

    @Override
    public String toString() {
        return "State{" +
                "board=" + Arrays.toString(board) +
                ", turn=" + turn +
                ", won=" + won +
                ", castle=" + Arrays.toString(castle) +
                ", enPassant=" + enPassant +
                ", check=" + check +
                ", kings=" + kings +
                ", control=" + control +
                ", pins=" + pins +
                '}';
    }
}
