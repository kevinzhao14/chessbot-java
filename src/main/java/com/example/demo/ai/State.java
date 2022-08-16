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

import static com.example.demo.ai.Util.bit;
import static com.example.demo.ai.Util.bitmapAdd;
import static com.example.demo.ai.Util.bitmapHas;
import static com.example.demo.ai.Util.log;

public class State {
    private char[] board;
    private Side turn;
    private Side won;
    private Group<boolean[]> castle;
    private int enPassant;
    private Check check;
    private Group<Integer> kings;
    private Group<HashMap<Integer, long[]>> control;
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

//        boolean[] cw = this.castle.white();
//        boolean[] cb = this.castle.black();
//        state.castle = new Group<>(new boolean[]{cw[0], cw[1]}, new boolean[]{cb[0], cb[1]});
        state.castle = new Group<>(this.castle.white().clone(), this.castle.black().clone());
        state.enPassant = this.enPassant;
        if (this.check != null) {
            state.check = this.check.clone();
        }
        state.kings = new Group<>(this.kings.white(), this.kings.black());

        HashMap<Integer, long[]> controlWhite = null;
        if (this.control.white() != null) {
            controlWhite = new HashMap<>();
            for (Map.Entry<Integer, long[]> entry : this.control.white().entrySet()) {
                int pos = entry.getKey();
                long[] currPaths = entry.getValue();
                controlWhite.put(pos, Arrays.copyOf(currPaths, currPaths.length));
//                controlWhite.put(pos, currPaths.clone());
            }

        }
        HashMap<Integer, long[]> controlBlack = null;
        if (this.control.black() != null) {
            controlBlack = new HashMap<>();
            for (Map.Entry<Integer, long[]> entry : this.control.black().entrySet()) {
                int pos = entry.getKey();
                long[] currPaths = entry.getValue();
                controlBlack.put(pos, Arrays.copyOf(currPaths, currPaths.length));
//                controlBlack.put(pos, currPaths.clone());
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
        HashMap<Integer, long[]> control = new HashMap<>();
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

    public ArrayList<Pair<Integer, Long>> isControlled(int pos, Side side) {
        if (this.control.get(side) == null) {
            return null;
        }
        ArrayList<Pair<Integer, Long>> pathList = new ArrayList<>();
        for (Map.Entry<Integer, long[]> entry : this.control.get(side).entrySet()) {
            int by = entry.getKey();
            long[] paths = entry.getValue();
            for (long path : paths) {
                if (path == 0) {
                    continue;
                }
                for (int i = 0; i < 64; i++) {
                    if (!bitmapHas(path, bit(i))) {
                        continue;
                    }
                    if (i == pos) {
                        pathList.add(new Pair<>(by, path));
                    }
                }
            }
        }
        return pathList.size() > 0 ? pathList : null;
    }

    public boolean calcCheck() {
        int kingPos = this.kings.get(this.turn);
        ArrayList<Pair<Integer, Long>> isCheck = this.isControlled(kingPos, this.turn.opp());
        if (isCheck != null) {
            if (isCheck.size() == 1) {
                Pair<Integer, Long> check = isCheck.get(0);
                long newMap = bitmapAdd(check.b(), bit(check.a()));
                this.check = new Check(check.a(), newMap);
            } else {
                this.check = new Check(kingPos, 0);
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

    public Group<HashMap<Integer, long[]>> getControl() {
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
