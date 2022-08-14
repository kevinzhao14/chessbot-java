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

import static com.example.demo.ai.Util.log;

public class State {
    private char[] board;
    private Side turn;
    private Side won;
    private Group<boolean[]> castle;
    private int enPassant;
    private Check check;
    private Group<Integer> kings;
    private Group<HashMap<Integer, ArrayList<ArrayList<Integer>>>> control;
    private ArrayList<Pin> pins;

    public State() {
        this.board = new char[64];
        this.turn = Side.WHITE;
        this.won = Side.NONE;
        this.castle = new Group<>(new boolean[]{false, false}, new boolean[]{false, false});
        this.enPassant = -1;
        this.check = null;
        this.kings = new Group<>(-1, -1);
        this.control = new Group<>(null, null); // TODO: convert to arrays
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

        state.castle = new Group<>(this.castle.white().clone(), this.castle.black().clone());
        state.enPassant = this.enPassant;
        if (this.check != null) {
            state.check = this.check.clone();
        }
        state.kings = new Group<>(this.kings.white(), this.kings.black());

        HashMap<Integer, ArrayList<ArrayList<Integer>>> controlWhite = null;
        if (this.control.white() != null) {
            controlWhite = new HashMap<>();
            for (int pos : this.control.white().keySet()) {
                ArrayList<ArrayList<Integer>> paths = new ArrayList<>();
                for (ArrayList<Integer> path : this.control.white().get(pos)) {
                    paths.add((ArrayList<Integer>) path.clone());
                }
                controlWhite.put(pos, paths);
            }

        }
        HashMap<Integer, ArrayList<ArrayList<Integer>>> controlBlack = null;
        if (this.control.black() != null) {
            controlBlack = new HashMap<>();
            for (int pos : this.control.black().keySet()) {
                ArrayList<ArrayList<Integer>> paths = new ArrayList<>();
                for (ArrayList<Integer> path : this.control.black().get(pos)) {
                    paths.add((ArrayList<Integer>) path.clone());
                }
                controlBlack.put(pos, paths);
            }
        }

        state.control = new Group<>(controlWhite, controlBlack);

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

        this.control.set(side, control);
        this.pins(pins);
    }

    public ArrayList<Pair<Integer, ArrayList<Integer>>> isControlled(int pos, Side side) {
        if (this.control.get(side) == null) {
            return null;
        }
        ArrayList<Pair<Integer, ArrayList<Integer>>> pathList = new ArrayList<>();
        for (int by : this.control.get(side).keySet()) {
            ArrayList<ArrayList<Integer>> paths = this.control.get(side).get(by);
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
        ArrayList<Pair<Integer, ArrayList<Integer>>> isCheck = this.isControlled(kingPos, this.turn.opp());
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

    public char[] board() {
        return board;
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

    public Group<boolean[]> castle() {
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

    public Group<HashMap<Integer, ArrayList<ArrayList<Integer>>>> control() {
        return control;
    }

    @Override
    public String toString() {
        return "State{" +
                "board=" + Arrays.toString(board) +
                ", turn=" + turn +
                ", won=" + won +
                ", castle=" + castle +
                ", enPassant=" + enPassant +
                ", check=" + check +
                ", kings=" + kings +
                ", control=" + control +
                ", pins=" + pins +
                '}';
    }
}
