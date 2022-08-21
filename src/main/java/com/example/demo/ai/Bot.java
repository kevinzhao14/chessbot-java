package com.example.demo.ai;

import com.example.demo.ai.objects.BestMove;
import com.example.demo.ai.objects.Check;
import com.example.demo.ai.objects.Move;
import com.example.demo.ai.objects.Pin;
import com.example.demo.ai.objects.Side;
import com.example.demo.ai.objects.Static;
import com.example.demo.ai.objects.ValidMoves;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.example.demo.ai.Util.an;
import static com.example.demo.ai.Util.bit;
import static com.example.demo.ai.Util.bitmapAdd;
import static com.example.demo.ai.Util.bitmapHas;
import static com.example.demo.ai.Util.eval;
import static com.example.demo.ai.Util.is;
import static com.example.demo.ai.Util.isBlack;
import static com.example.demo.ai.Util.log;
import static com.example.demo.ai.Util.offBoard;
import static com.example.demo.ai.Util.pieceValue;
import static com.example.demo.ai.Util.pieceValueDiff;
import static com.example.demo.ai.Util.sideOf;

public class Bot {
    static int DEPTH = 5;

    static int MULT = 1;

    static boolean DEBUG = true;

    static boolean LOG = false;

    static boolean EXTRA_DEPTH = false;

    private static int nodes = 0;

    public static BestMove getMove(String fen, boolean testMode) {
        State state = setupState(fen);
        if (LOG) {
            Util.printBoard(state, true);
//            System.out.println(getValidMoves('P', 12, state));
        }


        // TODO: extraDepth
        int extraDepth = (int) Math.floor((32 - state.count()) / 14.0);
        if (EXTRA_DEPTH && extraDepth > 0) {
            DEPTH += extraDepth;
            System.out.println("Extra depth: " + DEPTH);
        }

        long start = System.nanoTime();
        MoveInfo best = bestMove(state);
        double total = (System.nanoTime() - start) / 1000000.0;

        int from = -1;
        int to = -1;

        BestMove res = new BestMove(-1, -1, '\0');

        if (best.move != null) {
            from = best.move.from();
            to = best.move.to();

            if (is(state.at(from), 'p')) {
                int r = (int) Math.floor(to / 8.0);
                if (r <= 0) {
                    res.setPromote(Util.PROM[r * -1]);
                    to -= r * 8;
                } else if (r >= 7) {
                    res.setPromote(Util.PROM[r - 7]);
                    to = 56 + to % 8;
                }
            }

            best.line.add(0, best.move);
        }

        res.setFrom(from);
        res.setTo(to);

        System.out.println("\nMove: " + from + " -> " + to);

        res.setEval(Math.round(best.score * 1000) / 1000.0);
        System.out.println("Score: " + res.getEval());


        if (EXTRA_DEPTH) {
            DEPTH -= extraDepth;
        }


        StringBuilder line = new StringBuilder("Line: ");

        for (Move m : best.line) {
            line.append(Util.an(m.from())).append(" -> ").append(Util.an(m.to())).append("     ");
        }

        System.out.println(line);
        System.out.println("Nodes: " + nodes);
        System.out.println("Time: " + total + "ms\n");

        if (testMode) {
            res.setNodes(nodes);
        }

        return res;

//        */
    }

    public static BestMove getMove(String fen) {
        return getMove(fen, false);
    }

    public static double getEval(String fen) {
        State state = setupState(fen);
        MoveInfo best = bestMove(state);

        double mod = MULT == 1 ? DEPTH : ((1 - Math.pow(MULT, DEPTH)) / (1 - MULT));
        double score = best.score / mod;
        if (LOG) {
            log(best.score, score, mod);
        }

        return score;
    }

    public static State setupState(String fen) {
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
                    int pos = r * 8 + c;
                    state.set(pos, itm);
                    if (is(itm, 'k')) {
                        state.kings().set(sideOf(itm), pos);
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
                int index = (is(cv, 'k') ? 0 : 1) + (isBlack(sideOf(cv)) ? 2 : 0);
                state.castle()[index] = true;
            }
        }

        // en passant
        if (!data[3].equals("-")) {
            state.enPassant(Util.coordFromAn(data[3]));
        }

        state.calcControls(state.turn().opp());

        state.calcCheck();

        return state;
    }

    static MoveInfo bestMove(State state, int depth, boolean hasAlpha, Double alpha) {
        if (depth == 0) {
            return new MoveInfo(null, eval(state), new LinkedList<>());
        }
        HashMap<Integer, ArrayList<Integer>> valid = allValidMoves(state);

        ArrayList<Move> validMoves = new ArrayList<>();
        for (Map.Entry<Integer, ArrayList<Integer>> entry : valid.entrySet()) {
            int from = entry.getKey();
            ArrayList<Integer> tos = entry.getValue();
            for (int to : tos) {

                // move ordering heuristic
                char toPiece = state.at(to);
                double score = 0;

                // capture heuristic
                char fromPiece = state.at(from);
                if (toPiece != 0) {
                    score = pieceValue(toPiece, to) * 9 - pieceValue(fromPiece, from);
                } else {
                    int tempTo = to;
                    if (is(fromPiece, 'p')) {
                        if (to > 63) {
                            tempTo = 56 + to % 8;
                        } else if (to < 0) {
                            tempTo = (to + 1) % 8 + 7;
                        }
                    }
                    score = pieceValueDiff(fromPiece, from, tempTo);
                }


                validMoves.add(new Move(from, to, score));
            }
        }

        // Order moves
        validMoves.sort((o1, o2) -> {
            double diff = o2.score() - o1.score();
            if (diff < 0) {
                return -1;
            } else if (diff > 0) {
                return 1;
            } else {
                return 0;
            }
        });

        ArrayList<Move> best = new ArrayList<>();
        boolean useBeta = false;
        double bestScore = 0;

        for (Move move : validMoves) {
            int from = move.from();
            int to = move.to();

            nodes++;

            State simState = simMove(state, from, to);
            double score = 0;
            LinkedList<Move> line = new LinkedList<>();

            if (simState.won() == Side.NONE) {
                MoveInfo childBest = bestMove(simState, depth - 1, useBeta, bestScore);

                if (childBest.move != null) {
                    line.addAll(0, childBest.line);
                    line.add(0, childBest.move);
                    childBest.move.setLine(null);
                }

                score = childBest.score;
            } else if (simState.won() != Side.DRAW) {
                int mult = isBlack(simState.won()) ? -1 : 1;
                score = (1000 + (DEPTH - depth + 1)) * mult;
                score += depth * 100 * mult;
            }

            if (best.size() == 0 || (isBlack(state.turn()) ? score <= bestScore : score >= bestScore)) {
                if (score != bestScore) {
                    bestScore = score;
                    best.clear();
                }
                useBeta = true;
                best.add(new Move(from, to, line));

                if (hasAlpha) {
                    if (isBlack(state.turn()) && score < alpha) {
                        break;
                    } else if (!isBlack(state.turn()) && score > alpha) {
                        break;
                    }
                }
            }
        }

        Move bestRand = best.get(best.size() == 1 ? 0 : Util.rand(0, best.size()));
        if (depth == DEPTH && best.size() > 1) {
            System.out.println("\nRandomly moving out of " + best.size() + " moves");
            if (LOG) {
                for (Move move : best) {
                    System.out.println(Util.an(move.from()) + " -> " + Util.an(move.to()));
                }
            }
        }

        return new MoveInfo(bestRand, bestScore, bestRand.line());
    }

    static MoveInfo bestMove(State state) {
        return bestMove(state, DEPTH, false, 0.0);
    }

    private static HashMap<Integer, ArrayList<Integer>> allValidMoves(State state) {
        Side turn = state.turn();
        HashMap<Integer, ArrayList<Integer>> moves = new HashMap<>();

        if (state.won() != Side.NONE) {
            return moves;
        }

        for (int i = 0; i < 64; i++) {
            char piece = state.at(i);
            if (piece != 0 && sideOf(piece) == turn) {
                ValidMoves m = getValidMoves(piece, i, state);
                moves.put(i, m.moves());
            }
        }

        return moves;
    }

    private static boolean hasValidMoves(State state) {
        Side turn = state.turn();

        if (state.won() != Side.NONE) {
            return false;
        }

        for (int i = 0; i < 64; i++) {
            char piece = state.at(i);
            if (piece != 0 && sideOf(piece) == turn) {
                ValidMoves m = getValidMoves(piece, i, state);
                if (m.moves().size() > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    static ValidMoves getValidMoves(char piece, int p, State state, boolean getControl) {
        Side side = sideOf(piece);
        ArrayList<Integer> moves = new ArrayList<>();
        ArrayList<ArrayList<Integer>> control = new ArrayList<>();
        ArrayList<Pin> pins = new ArrayList<>();

        class Pather {
            void go(int p, int from, int to) {
                for (int i = from; i < to; i++) {
                    int pinPiece = -1;
                    boolean pin = false;
                    boolean kingPin = false;
                    long pinMoves = 0;
                    ArrayList<Integer> path = new ArrayList<>();

                    pinMoves = bitmapAdd(pinMoves, bit(p));

                    int pos = Util.go(p, i);
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
                            pinMoves = bitmapAdd(pinMoves, bit(pos));
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
                            new Pather().go(pos, i, i + 1);
                        }
                    }
                }
            }
        }

        switch (Character.toLowerCase((piece))) {
            case 'p':
                int dir = isBlack(side) ? -8 : 8;
                int single = p + dir;
                if (!getControl && state.at(single) == 0) {
                    moves.add(single);
                    // check for promotion
                    if (isBlack(side) ? (single <= 7) : (single >= 56)) {
                        for (int i = 1; i < 4; i++) {
                            moves.add(single + dir * i);
                        }
                    } else {
                        if (isBlack(side) ? (p >= 48) : (p <= 15)) {
                            int doub = single + dir;
                            if (state.at(doub) == 0) {
                                moves.add(doub);
                            }
                        }
                    }
                }

                int[] tpos = new int[]{-1, -1};
                int c = p % 8;
                if (c >= 1) {
                    tpos[0] = single - 1;
                }
                if (c <= 6) {
                    tpos[1] = single + 1;
                }

                for (int tp : tpos) {
                    if (tp == -1) {
                        continue;
                    }
                    char take = state.at(tp);
                    if (getControl) {
                        control.add(new ArrayList<>(Collections.singletonList(tp)));
                    } else if (state.enPassant() != -1 && tp == state.enPassant()) {
                        State sim = state.clone();
                        sim.set(tp, sim.at(p));
                        sim.remove(p);
                        sim.remove(tp - dir);
                        sim.calcControls(side.opp());
                        if (sim.isControlled(sim.kings().get(side)) == null) {
                            moves.add(tp);
                        }
                    } else if (take != 0 && sideOf(take) != side) {
                        moves.add(tp);
                        if (isBlack(side) ? (tp <= 7) : (tp >= 56)) {
                            for (int i = 1; i < 4; i++) {
                                moves.add(tp + dir * i);
                            }
                        }
                    }
                }
                break;

            case 'n':
                for (int pos : Static.N[p]) {
                    if (offBoard(pos)) {
                        continue;
                    }
                    if (getControl) {
                        ArrayList<Integer> list = new ArrayList<>();
                        list.add(pos);
                        control.add(list);
                    } else if (state.at(pos) == 0 || sideOf(state.at(pos)) != side) {
                        moves.add(pos);
                    }
                }
                break;

            case 'k':
                for (int pos : Static.K[p]) {
                    if (offBoard(pos)) {
                        continue;
                    }
                    if (getControl) {
                        ArrayList<Integer> list = new ArrayList<>();
                        list.add(pos);
                        control.add(list);
                    } else if (state.at(pos) == 0 || sideOf(state.at(pos)) != side) {
                        moves.add(pos);
                    }
                }

                // castling
                if (!getControl && state.isControlled(p) == null) {
                    boolean[] castle = state.castle();
                    int index = isBlack(side) ? 2 : 0;
                    if (castle[index] && state.at(p + 1) == 0
                    && state.at(p + 2) == 0
                    && state.isControlled(p + 1) == null) {
                        moves.add(p + 2);
                    }
                    if (castle[index + 1] && state.at(p - 1) == 0
                    && state.at(p - 2) == 0
                    && state.at(p - 3) == 0
                    && state.isControlled(p - 1) == null) {
                        moves.add(p - 2);
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
            ArrayList<Integer> checkMoves = new ArrayList<>();
            if (check != null) {
                checkMoves = check.path();
                inCheck = true;
            }

            long pinMoves = 0;
            for (Pin pin : state.pins()) {
                if (pin.pinPiece() == p) {
                    pinMoves = pin.pinMoves();
                    break;
                }
            }

            boolean isKing = is(piece, 'k');
            for (int i = 0; i < moves.size(); i++) {
                int move = moves.get(i);
                if (pinMoves != 0 && !bitmapHas(pinMoves, bit(move))) {
                    moves.remove(i);
                    i--;

                } else {
                    if (state.enPassant() != -1 && is(piece, 'p') && move == state.enPassant()) {
                        continue;
                    }

                    // king moving to a controlled square
                    if ((isKing && state.isControlled(move) != null)
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

    static ValidMoves getValidMoves(char piece, int p, State state) {
        return getValidMoves(piece, p, state, false);
    }

    private static State simMove(State state, int from, int to) {
        state = state.clone();

        char fromPiece = state.at(from);
        char toPiece = state.at(to);
        Side side = sideOf(fromPiece);

        if (is(fromPiece, 'p')) {
            // promotion
            int r = (int) Math.floor(to / 8.0);
            if (isBlack(side) && r <= 0) {
                fromPiece = Util.PROM[r * -1];
                to -= r * 8;
            } else if (!isBlack(side) && r >= 7) {
                fromPiece = Character.toUpperCase(Util.PROM[r - 7]);
                to = 56 + to % 8;
            }

            // en passant
            if (state.enPassant() != -1 && to == state.enPassant()) {
                state.remove(isBlack(side) ? (to + 8) : (to - 8));
            }
        } else if (is(fromPiece, 'k')) {
            // castling
            if (Math.abs(from - to) == 2) {
                boolean kingSide = from < to;
                int y = isBlack(side) ? 56 : 0;
                int rookPos = y + (kingSide ? 7 : 0);
                int newRookPos = y + (kingSide ? 5 : 3);

                state.set(newRookPos, isBlack(side) ? 'r' : 'R');
                state.remove(rookPos);
            }
            int index = isBlack(side) ? 2 : 0;
            state.castle()[index] = false;
            state.castle()[index + 1] = false;
            state.kings().set(side, to);

        } else if (is(fromPiece, 'r')) {
            // cancel castling
            boolean[] castle = state.castle();
            int ind = isBlack(side) ? 2 : 0;
            if (castle[ind] && from == (isBlack(side) ? 63 : 7)) {
                castle[ind] = false;
            } else if (castle[ind + 1] && from == (isBlack(side) ? 56 : 0)) {
                castle[ind + 1] = false;
            }

        } else if (toPiece != 0 && is(toPiece, 'r')) {
            // cancel castling
            boolean[] castle = state.castle();
            int ind = isBlack(side) ? 0 : 2;
            if (castle[ind] && to == (isBlack(side) ? 7 : 63)) {
                castle[ind] = false;
            } else if (castle[ind + 1] && to == (isBlack(side) ? 0 : 56)) {
                castle[ind + 1] = false;
            }
        }

        state.set(to, fromPiece);
        state.remove(from);

        // set up the next en passant
        if (is(fromPiece, 'p') && Math.abs(to - from) == 16) {
            state.enPassant(to + (isBlack(side) ? 8 : -8));
        } else {
            state.enPassant(-1);
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

    public static long[] testPerft(String fen, int min, int max) {
        if (min == max) {
            LOG = true;
        }

        State originalState = setupState(fen);
        long[] nodes = new long[max - min + 1];

        if (LOG) {
            log("pins", originalState.pins());
        }

        int n = 0;
        for (int i = min; i <= max; i++) {
            State state = originalState.clone();
            long start = System.nanoTime();

            long count = movesTest(state, i);

            double time = (System.nanoTime() - start) / 1000000.0;
            nodes[n] = count;
            n++;

            if (LOG) {
                System.out.println("----- Depth " + i);
                System.out.println("Moves: " + count);
                System.out.println("Time: " + time);
            }
        }

        return nodes;
    }

    private static long movesTest(State state, int depth) {
        int logDepth = 1;

        if (depth == 0) {
            return 1;
        }

        HashMap<Integer, ArrayList<Integer>> moves = allValidMoves(state);
        int num = 0;
        if (LOG && depth == logDepth) {
            log("moves", moves);
        }
        for (int from : moves.keySet()) {
            ArrayList<Integer> tos = moves.get(from);
            for (int to : tos) {
                State sim = simMove(state, from, to);

                long c = movesTest(sim, depth - 1);

                num += c;

                if (LOG && depth == logDepth) {
                    System.out.println(an(from) + an(to, state.at(from)) + ": " + c);
                }
            }
        }

        if (LOG && depth == logDepth) {
            System.out.println("Total: " + num);
        }

        return num;
    }

    public static void test() {

    }
}
