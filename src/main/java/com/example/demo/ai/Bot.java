package com.example.demo.ai;

import com.example.demo.ai.objects.BestMove;
import com.example.demo.ai.objects.Check;
import com.example.demo.ai.objects.Move;
import com.example.demo.ai.objects.Pair;
import com.example.demo.ai.objects.Pin;
import com.example.demo.ai.objects.Pos;
import com.example.demo.ai.objects.Side;
import com.example.demo.ai.objects.ValidMoves;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import static com.example.demo.ai.Util.is;
import static com.example.demo.ai.Util.isBlack;
import static com.example.demo.ai.Util.offBoard;
import static com.example.demo.ai.Util.sideOf;

public class Bot {
    static int DEPTH = 4;

    static int MULT = 1;

    static boolean DEBUG = false;

    static boolean LOG = true;

    static boolean EXTRA_DEPTH = false;

    private static int nodes = 0;

    public static BestMove getMove(String fen) {
        State state = setupState(fen);
        if (LOG) {
            Util.printBoard(state, true);
        }

        // TODO: extraDepth

        long start = System.currentTimeMillis();
        MoveInfo best = bestMove(state);
        long total = System.currentTimeMillis() - start;

        System.out.println("Best: " + best.toString());

        Pos from = null;
        Pos to = null;

        BestMove res = new BestMove(null, null, '\0');

        if (best.move != null) {
            from = best.move.from();
            to = best.move.to();

            if (is(state.at(from), 'p')) {
                if (to.y() <= 0) {
                    res.setPromote(Util.PROM[to.y() * -1]);
                    to.setY(0);
                } else if (to.y() >= 7) {
                    res.setPromote(Util.PROM[to.y() - 7]);
                    to.setY(7);
                }
            }

            best.line.add(0, best.move);
        }

        res.setFrom(from);
        res.setTo(to);

        System.out.println("\nMove: " + from + " -> " + to);
        System.out.println("Score: " + Math.round(best.score * 1000) / 1000);

        StringBuilder line = new StringBuilder("Line: ");

        for (Move m : best.line) {
            line.append(Util.an(m.from())).append(" -> ").append(Util.an(m.to())).append("     ");
        }

        System.out.println(line);
        System.out.println("Nodes: " + nodes);
        System.out.println("Time: " + total + "ms\n");

        return res;
    }

    static State setupState(String fen) {
        System.out.println("FEN: " + fen);
        String[] data = fen.split(" ");
        State state = new State();
        nodes = 0;

        String[] setup = data[0].split("/");

        int r = 7, c = 0;
        for (String row : setup) {
            if (r < 0) {
                throw new IllegalStateException("Too many rows");
            }
            String[] rowData = row.split("");
            for (String item : rowData) {
                if (c > 7) {
                    throw new IllegalStateException("Too many columns in row " + r);
                }
                char itm = item.charAt(0);
                if (Util.isNumeric(item)) {
                    c += Integer.parseInt(item);
                } else {
                    state.set(new Pos(c, r), itm);
                    if (is(itm, 'k')) {
                        state.kings().set(sideOf(itm), new Pos(c, r));
                    }
                    c++;
                }
            }
            r--;
            c = 0;
        }

        state.turn(data[1].equals("w") ? Side.WHITE : Side.BLACK);

        // castle data
        if (!data[2].equals("-")) {
            setup = data[2].split("");
            for (String castle : setup) {
                char cv = castle.charAt(0);
                state.castle().get(sideOf(cv))[is(cv, 'k') ? 0 : 1] = true;
            }
        }

        // en passant
        if (!data[3].equals("-")) {
            state.enPassant(Util.coordFromAn(data[3]));
        }

        state.calcControls(state.turn());

        state.calcCheck();

        return state;
    }

    static MoveInfo bestMove(State state, int depth, boolean hasAlpha, Double alpha) {
        if (depth == 0) {
            return new MoveInfo(null, 0, new LinkedList<>());
        }
        HashMap<Pos, ArrayList<Pos>> valid = allValidMoves(state);

        ArrayList<Pair<Move, Integer>> validMoves = new ArrayList<>();
        for (Pos from : valid.keySet()) {
            ArrayList<Pos> tos = valid.get(from);
            for (Pos to : tos) {
                char toPiece = state.at(to);
                int score = 0;
                if (toPiece != 0) {
                    score = Util.pieceValue(toPiece);
                }
                validMoves.add(new Pair<>(new Move(from, to), score));
            }
        }
        validMoves.sort((o1, o2) -> o2.b() - o1.b());

        ArrayList<Pair<Move, LinkedList<Move>>> best = new ArrayList<>();
        double bestScore = 0;

        for (Pair<Move, Integer> movePair : validMoves) {
            Move move = movePair.a();
            Pos from = move.from();
            Pos to = move.to();

            nodes++;

            State simState = simMove(state, from, to);
            double score = Util.eval(simState);
            LinkedList<Move> line = new LinkedList<>();

            if (simState.won() == Side.NONE) {
                boolean useBeta = best.size() != 0;
                double beta = (bestScore - score) / MULT;
                MoveInfo childBest = bestMove(simState, depth - 1, useBeta, beta);
                if (childBest.move != null) {
                    line.addAll(0, childBest.line);
                    line.add(0, childBest.move);
                }
                score += MULT * childBest.score;
            } else {
                score *= 1 + (double) depth / DEPTH;
            }

            if (best.size() == 0 || (isBlack(state.turn()) ? score <= bestScore : score >= bestScore)) {
                if (score != bestScore) {
                    bestScore = score;
                    best.clear();
                }
                best.add(new Pair<>(new Move(from, to), line));

                if (hasAlpha) {
                    if (isBlack(state.turn()) && score < alpha) {
                        break;
                    } else if (!isBlack(state.turn()) && score > alpha) {
                        break;
                    }
                }
            }
        }

        Pair<Move, LinkedList<Move>> bestRand = best.get(best.size() == 1 ? 0 : Util.rand(0, best.size()));
        if (depth == DEPTH && best.size() > 1) {
            System.out.println("\nRandomly moving out of " + best.size() + " moves");
            if (LOG) {
                for (Pair<Move, LinkedList<Move>> b : best) {
                    Move move = b.a();
                    System.out.println(Util.an(move.from()) + " -> " + Util.an(move.to()));
                }
            }
        }

        return new MoveInfo(bestRand.a(), bestScore, bestRand.b());
    }

    static MoveInfo bestMove(State state) {
        return bestMove(state, DEPTH, false, 0.0);
    }

    private static HashMap<Pos, ArrayList<Pos>> allValidMoves(State state) {
        Side turn = state.turn();
        HashMap<Pos, ArrayList<Pos>> moves = new HashMap<>();

        if (state.won() != Side.NONE) {
            return moves;
        }

        for (int i = 0; i < 8; i++) {
            char[] row = state.board()[i];
            for (int j = 0; j < 8; j++) {
                char piece = row[j];
                if (piece != 0) {
                    Pos pos = new Pos(j, i);
                    if (sideOf(piece) != turn) {
                        continue;
                    }
                    ValidMoves m = getValidMoves(piece, pos, state);
                    moves.put(pos, m.moves());
                }
            }
        }

        return moves;
    }

    private static boolean hasValidMoves(State state) {
        Side turn = state.turn();

        if (state.won() != Side.NONE) {
            return false;
        }

        for (int i = 0; i < 8; i++) {
            char[] row = state.board()[i];
            for (int j = 0; j < 8; j++) {
                char piece = row[j];
                if (piece != 0 && sideOf(piece) == turn) {
                    Pos pos = new Pos(j, i);
                    ValidMoves m = getValidMoves(piece, pos, state);
                    if (m.moves().size() > 0) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    static ValidMoves getValidMoves(char piece, Pos p, State state, boolean getControl) {
        Side side = sideOf(piece);
        ArrayList<Pos> moves = new ArrayList<>();
        ArrayList<ArrayList<Pos>> control = new ArrayList<>();
        ArrayList<Pin> pins = new ArrayList<>();
        ArrayList<Pos> temp = new ArrayList<>();

        class Pather {
            void go(Pos pos, int from, int to) {
                for (int i = from; i < to; i++) {
                    Pos pinPiece = null;
                    boolean pin = false;
                    boolean kingPin = false;
                    ArrayList<Pos> pinMoves = new ArrayList<>();
                    ArrayList<Pos> path = new ArrayList<>();

                    pinMoves.add(pos.clone());

                    pos = Util.go(pos, i);
                    while (!Util.offBoard(pos)) {
                        char ap = state.at(pos);

                        if (ap != 0) {
                            if (pin) {
                                if (is(ap, 'k') && sideOf(ap) != side) {
                                    pins.add(new Pin(pinPiece, pinMoves));
                                }

                                break;
                            }

                            if (sideOf(ap) != side) {
                                if (getControl) {
                                    path.add(pos);
                                    if (is(ap, 'k')) {
                                        kingPin = true;
                                        break;
                                    } else {
                                        pin = true;
                                        pinPiece = pos;
                                    }
                                } else {
                                    moves.add(pos);
                                    break;
                                }
                            } else {
                                if (getControl) {
                                    path.add(pos);
                                }
                                break;
                            }
                        }

                        if (!kingPin) {
                            pinMoves.add(pos);
                        }
                        if (!pin) {
                            if (getControl) {
                                path.add(pos);
                            } else {
                                moves.add(pos);
                            }
                        }

                        pos = Util.go(pos, i);
                    }

                    if (getControl) {
                        control.add(path);
                        if (kingPin) {
                            new Pather().go(pos.clone(), i, i + 1);
                        }
                    }
                }
            }
        }

        switch (Character.toLowerCase((piece))) {
            case 'p':
                int dir = isBlack(side) ? -1 : 1;
                Pos single = new Pos(p.x(), p.y() + dir);
                if (!getControl) {
                    if (state.at(single) == 0) {
                        moves.add(single);
                        if (single.x() == (isBlack(side) ? 0 : 7)) {
                            for (int i = 1; i < 4; i++) {
                                moves.add(new Pos(single.x(), single.y() + dir * i));
                            }
                        } else {
                            if (p.y() == (isBlack(side) ? 6 : 1)) {
                                Pos doub = new Pos(single.x(), single.y() + dir);
                                if (state.at(doub) == 0) {
                                    moves.add(doub);
                                }
                            }
                        }
                    }
                }

                Pos[] tpos = new Pos[2];
                if (p.x() - 1 >= 0) {
                    tpos[0] = new Pos(p.x() - 1, single.y());
                }
                if (p.x() + 1 <= 7) {
                    tpos[1] = new Pos(p.x() + 1, single.y());
                }

                for (Pos tp : tpos) {
                    if (tp == null) {
                        continue;
                    }
                    char take = state.at(tp);
                    if (getControl) {
                        ArrayList<Pos> list = new ArrayList<>();
                        list.add(tp);
                        control.add(list);
                    } else if (state.enPassant() != null && tp.equals(state.enPassant())) {
                        State sim = state.clone();
                        sim.set(tp, sim.at(p));
                        sim.remove(p);
                        sim.remove(new Pos(tp.x(), p.y()));
                        sim.calcControls(side.opp());
                        if (sim.isControlled(sim.kings().get(side), side.opp()) != null) {
                            moves.add(tp);
                        }
                    } else if (take != 0 && sideOf(take) != side) {
                        moves.add(tp);
                        if (single.y() == (isBlack(side) ? 0 : 7)) {
                            for (int i = 1; i < 4; i++) {
                                moves.add(new Pos(tp.x(), tp.y() + dir * i));
                            }
                        }
                    }
                }
                break;

            case 'n':
                temp.add(new Pos(p.x() + 2, p.y() + 1));
                temp.add(new Pos(p.x() + 2, p.y() - 1));
                temp.add(new Pos(p.x() - 2, p.y() + 1));
                temp.add(new Pos(p.x() - 2, p.y() - 1));
                temp.add(new Pos(p.x() + 1, p.y() + 2));
                temp.add(new Pos(p.x() + 1, p.y() - 2));
                temp.add(new Pos(p.x() - 1, p.y() + 2));
                temp.add(new Pos(p.x() - 1, p.y() - 2));
                for (Pos pos : temp) {
                    if (offBoard(pos)) {
                        continue;
                    }
                    if (getControl) {
                        ArrayList<Pos> list = new ArrayList<>();
                        list.add(pos);
                        control.add(list);
                    } else if (state.at(pos) == 0 || sideOf(state.at(pos)) != side) {
                        moves.add(pos);
                    }
                }
                break;

            case 'k':
                int x1 = p.x() + 1, x2 = p.x() - 1;
                int y1 = p.y() + 1, y2 = p.y() - 1;
                temp.add(new Pos(x1, y1));
                temp.add(new Pos(x1, y2));
                temp.add(new Pos(x2, y1));
                temp.add(new Pos(x2, y2));
                temp.add(new Pos(x1, p.y()));
                temp.add(new Pos(x2, p.y()));
                temp.add(new Pos(p.x(), y1));
                temp.add(new Pos(p.x(), y2));
                for (Pos pos : temp) {
                    if (offBoard(pos)) {
                        continue;
                    }
                    if (getControl) {
                        ArrayList<Pos> list = new ArrayList<>();
                        list.add(pos);
                        control.add(list);
                    } else if (state.at(pos) == 0 || sideOf(state.at(pos)) != side) {
                        moves.add(pos);
                    }
                }

                // castling
                if (!getControl && state.isControlled(p, side.opp()) != null) {
                    boolean[] castle = state.castle().get(side);
                    if (castle[0]) {
                        if (state.at(new Pos(p.x() + 1, p.y())) == 0
                                && state.at(new Pos(p.x() + 2, p.y())) == 0
                                && state.isControlled(new Pos(p.x() + 1, p.y()), side.opp()) != null) {
                            moves.add(new Pos(p.x() + 2, p.y()));
                        }
                        if (state.at(new Pos(p.x() - 1, p.y())) == 0
                                && state.at(new Pos(p.x() - 2, p.y())) == 0
                                && state.at(new Pos(p.x() - 3, p.y())) == 0
                                && state.isControlled(new Pos(p.x() - 1, p.y()), side.opp()) != null) {
                            moves.add(new Pos(p.x() - 2, p.y()));
                        }
                    }
                }
                break;

            case 'r':
                new Pather().go(p, 0, 4);
                break;
            case 'b':
                new Pather().go(p, 4, 8);
                break;
            case 'q':
                new Pather().go(p, 0, 8);
                break;
        }

        if (!getControl) {
            Check check = state.check();
            boolean inCheck = false;
            ArrayList<Pos> checkMoves = new ArrayList<>();
            if (check != null) {
                checkMoves = check.path();
                inCheck = true;
            }

            ArrayList<Pos> pinMoves = null;
            for (Pin pin : state.pins()) {
                if (pin.pinPiece().equals(p)) {
                    pinMoves = pin.pinMoves();
                    break;
                }
            }

            boolean isKing = is(piece, 'k');
            for (int i = 0; i < moves.size(); i++) {
                Pos move = moves.get(i);
                if (pinMoves != null && !pinMoves.contains(move)) {
                    moves.remove(i);
                    i--;

                    // king moving to a controlled square
                } else {
                    if (state.enPassant() != null && is(piece, 'p') && move.equals(state.enPassant())) {
                        continue;
                    }

                    if ((isKing && state.isControlled(move, side.opp()) != null)
                            || (!isKing && inCheck && !checkMoves.contains(move))) {
                        moves.remove(i);
                        i--;
                    }
                }
            }
        }

        if (getControl) {
            return new ValidMoves(control, pins);
        } else {
            return new ValidMoves(moves);
        }
    }

    static ValidMoves getValidMoves(char piece, Pos p, State state) {
        return getValidMoves(piece, p, state, false);
    }

    private static State simMove(State state, Pos from, Pos to) {
        state = state.clone();

        char fromPiece = state.at(from);
        char toPiece = state.at(to);
        Side side = sideOf(fromPiece);

        if (is(fromPiece, 'p')) {
            // promotion
            if (isBlack(side) && to.y() <= 0) {
                fromPiece = Util.PROM[to.y() * -1];
                to = new Pos(to.x(), 0);
            } else if (!isBlack(side) && to.y() >= 7) {
                fromPiece = Character.toUpperCase(Util.PROM[to.y() - 7]);
                to = new Pos(to.x(), 7);
            }

            // en passant
            if (state.enPassant() != null && to.equals(state.enPassant())) {
                state.remove(new Pos(to.x(), isBlack(side) ? 3 : 4));
            }
        } else if (is(fromPiece, 'k')) {
            // castling
            if (Math.abs(from.x() - to.x()) == 2) {
                boolean kingSide = from.x() < to.x();
                Pos rookPos = new Pos(kingSide ? 7 : 0, from.y());
                Pos newRookPos = new Pos(kingSide ? 5 : 3, from.y());

                state.set(newRookPos, isBlack(side) ? 'r' : 'R');
                state.remove(rookPos);
            }
            state.castle().set(side, new boolean[]{false, false});
            state.kings().set(side, to.clone());
        } else if (is(fromPiece, 'r')) {
            // cancel castling
            boolean[] castle = state.castle().get(side);
            if (castle[0] && from.equals(new Pos(7, isBlack(side) ? 7 : 0))) {
                castle[0] = false;
            } else if (castle[1] && from.equals(new Pos(7, isBlack(side) ? 7 : 0))) {
                castle[1] = false;
            }
        } else if (toPiece != 0 && is(toPiece, 'r')) {
            // cancel castling
            boolean[] castle = state.castle().get(side.opp());
            if (castle[0] && from.equals(new Pos(7, isBlack(side) ? 0 : 7))) {
                castle[0] = false;
            } else if (castle[1] && from.equals(new Pos(7, isBlack(side) ? 0 : 7))) {
                castle[1] = false;
            }
        }

        state.set(to, fromPiece);
        state.remove(from);

        // set up the next en passant
        if (is(fromPiece, 'p') && Math.abs(to.y() - from.y()) == 2) {
            state.enPassant(new Pos(to.x(), isBlack(side) ? 5 : 2));
        } else {
            state.enPassant(null);
        }

        state.turn(state.turn().opp());
        state.pins(null);
        state.calcControls(side);

        boolean isCheck = state.calcCheck();

        if (!hasValidMoves(state)) {
            if (isCheck) {
                state.won(side);
            } else {
                state.won(Side.DRAW);
            }
        }

        return state;
    }

    private static class MoveInfo {
        Move move;
        double score;
        LinkedList<Move> line;

        MoveInfo(Move move, double score, LinkedList<Move> line) {
            this.move = move;
            this.score = score;
            this.line = line;
        }

        @Override
        public String toString() {
            return "MoveInfo{" +
                    "move=" + move +
                    ", score=" + score +
                    ", line=" + line +
                    '}';
        }
    }
}
