package com.example.demo.ai;

import com.example.demo.ai.objects.Check;
import com.example.demo.ai.objects.Group;
import com.example.demo.ai.objects.Pair;
import com.example.demo.ai.objects.Pos;
import com.example.demo.ai.objects.Side;
import com.example.demo.ai.objects.ValidMoves;

import java.util.ArrayList;
import java.util.HashMap;

public class State {
    char[][] board;
    Side turn;
    Side won;
    Group<boolean[]> castle;
    Pos enPassant;
    Check check;
    Group<Pos> kings;
    Group<HashMap<Pos, ArrayList<ArrayList<Pos>>>> control;
    ArrayList<Pos> pins;

    public State() {
        this.board = new char[8][8];
        this.turn = Side.WHITE;
        this.won = Side.NONE;
        this.castle = new Group<>(new boolean[]{false, false}, new boolean[]{false, false});
        this.enPassant = null;
        this.check = null;
        this.kings = new Group<>(null, null);
        this.control = new Group<>(null, null);
        this.pins = null;
    }

    public char at(int x, int y) {
        if (y > 7 || y < 0 || x< 0 || x > 7) {
            return 0;
        }
        return this.board[y][x];
    }

    public char at(Pos pos) {
        return at(pos.x(), pos.y());
    }

    public void set(Pos pos, char piece) {
        this.board[pos.y()][pos.x()] = piece;
    }

    public void remove(Pos pos) {
        this.set(pos, (char) 0);
    }

    public State clone() {
        State state = new State();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                state.board[j][i] = this.board[j][i];
            }
        }
        state.turn = this.turn;
        state.won = this.won;

        state.castle = new Group<>(this.castle.white().clone(), this.castle.black().clone());
        state.enPassant = this.enPassant.clone();
        state.check = this.check.clone();
        state.kings = new Group<>(this.kings.white().clone(), this.kings.black().clone());

        HashMap<Pos, ArrayList<ArrayList<Pos>>> controlWhite = new HashMap<>();
        for (Pos pos : this.control.white().keySet()) {
            ArrayList<ArrayList<Pos>> paths = new ArrayList<>();
            for (ArrayList<Pos> path : this.control.white().get(pos)) {
                ArrayList<Pos> newPath = new ArrayList<>();
                for (Pos p : path) {
                    newPath.add(p.clone());
                }
                paths.add(path);
            }
            controlWhite.put(pos.clone(), paths);
        }
        HashMap<Pos, ArrayList<ArrayList<Pos>>> controlBlack = new HashMap<>();
        for (Pos pos : this.control.black().keySet()) {
            ArrayList<ArrayList<Pos>> paths = new ArrayList<>();
            for (ArrayList<Pos> path : this.control.black().get(pos)) {
                ArrayList<Pos> newPath = new ArrayList<>();
                for (Pos p : path) {
                    newPath.add(p.clone());
                }
                paths.add(path);
            }
            controlBlack.put(pos.clone(), paths);
        }

        state.control = new Group<>(controlWhite, controlBlack);

        state.pins = new ArrayList<>();
        for (Pos p : this.pins) {
            pins.add(p.clone());
        }

        return state;
    }

    public void calcControls(Side side) {
        HashMap<Pos, ArrayList<ArrayList<Pos>>> control = new HashMap<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Pos pos = new Pos(j, i);
                char piece = this.board[i][j];
                if (Util.sideOf(piece) != side) {
                    continue;
                }
                ValidMoves squares = Bot.getValidMoves(piece, pos, this, true);
                this.control.get(side).put(pos.clone(), squares.getControl());
                this.pins = squares.getPins();
            }
        }
    }

    public ArrayList<Pair<Pos, ArrayList<Pos>>> isControlled(Pos pos, Side side) {
        if (this.control.get(side) == null) {
            return null;
        }
        ArrayList<Pair<Pos, ArrayList<Pos>>> pathList = new ArrayList<>();
        for (Pos by : this.control.get(side).keySet()) {
            ArrayList<ArrayList<Pos>> paths = this.control.get(side).get(by);
            for (ArrayList<Pos> path : paths) {
                for (Pos p : path) {
                    if (p.equals(pos)) {
                        pathList.add(new Pair(by, path));
                    }
                }
            }
        }
        return pathList.size() > 0 ? pathList : null;
    }

    public boolean calcCheck() {
        Pos kingPos = this.kings.get(this.turn);
        ArrayList<Pair<Pos, ArrayList<Pos>>> isCheck = this.isControlled(kingPos, this.turn.opp());
        if (isCheck != null) {
            if (isCheck.size() == 1) {
                Pair<Pos, ArrayList<Pos>> check = isCheck.get(0);
                check.b().add(0, check.a());
                this.check = new Check(check.a(), check.b());
            } else {
                this.check = new Check(kingPos, new ArrayList<>());
            }
            return true;
        }
        return false;
    }


//    @Override
//    public Iterator<Pos> iterator() {
//        return new StateIterator<Pos>(this);
//    }
//
//    public class StateIterator<T> implements Iterator<T> {
//        int x = 0;
//        int y = 0;
//        State state;
//        char current = 0;
//
//        public StateIterator(State state) {
//            this.state = state;
//            this.next();
//        }
//
//
//        @Override
//        public boolean hasNext() {
//            return current != 0;
//        }
//
//        @Override
//        public T next() {
//            do {
//                x++;
//                if (x > 7) {
//                    y++;
//                    if (y > 7) {
//                        current = 0;
//                        return null;
//                    }
//                }
//                current = state.at(new Pos(x, y));
//            } while (current == 0);
//            return (T) new Pos(x, y);
//        }
//    }
}
