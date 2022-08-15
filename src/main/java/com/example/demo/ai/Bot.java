package com.example.demo.ai;

import com.example.demo.ai.objects.*;

import java.util.*;

import static com.example.demo.ai.Util.an;
import static com.example.demo.ai.Util.is;
import static com.example.demo.ai.Util.isBlack;
import static com.example.demo.ai.Util.log;
import static com.example.demo.ai.Util.offBoard;
import static com.example.demo.ai.Util.sideOf;

public class Bot {
    static int DEPTH = 4;

    static int MULT = 1;

    static boolean DEBUG = false;

    static boolean LOG = false;

    static boolean EXTRA_DEPTH = true;

    private static int nodes = 0;

    public static BestMove getMove(String fen, boolean testMode) {
        State state = setupState(fen);
        if (LOG) {
            Util.printBoard(state, true);
//            System.out.println(getValidMoves('P', 12, state));
        }


//        return new BestMove(null, null, '\0'); /*

        // TODO: extraDepth

        long start = System.currentTimeMillis();
        MoveInfo best = bestMove(state);
        long total = System.currentTimeMillis() - start;

        int from = -1;
        int to = -1;

        BestMove res = new BestMove(-1, -1, '\0');

        if (best.move != null) {
            from = best.move.from();
            to = best.move.to();

            if (is(state.at(from), 'p')) {
                int r = (int) Math.floor(from / 8.0);
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
        System.out.println("Score: " + Math.round(best.score * 1000) / 1000);

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
                state.castle().get(sideOf(cv))[is(cv, 'k') ? 0 : 1] = true;
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
            return new MoveInfo(null, 0, new LinkedList<>());
        }
        HashMap<Integer, ArrayList<Integer>> valid = allValidMoves(state);

        ArrayList<Pair<Move, Integer>> validMoves = new ArrayList<>();
        for (Map.Entry<Integer, ArrayList<Integer>> entry : valid.entrySet()) {
            int from = entry.getKey();
            ArrayList<Integer> tos = entry.getValue();
            for (int to : tos) {
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
        boolean useBeta = false;
        double bestScore = 0;

        for (Pair<Move, Integer> movePair : validMoves) {
            Move move = movePair.a();
            int from = move.from();
            int to = move.to();

            nodes++;

            State simState = simMove(state, from, to);
            double score = Util.eval(simState);
            LinkedList<Move> line = new LinkedList<>();

            if (simState.won() == Side.NONE) {
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
                    useBeta = true;
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
                    ArrayList<Integer> pinMoves = new ArrayList<>();
                    ArrayList<Integer> path = new ArrayList<>();

                    pinMoves.add(p);

                    int pos = Util.go(p, i);
                    while (!Util.offBoard(pos)) {
//                    if (getControl && p.equals(new Pos(4, 1))) log("p", i, pos, path, control);
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
                if (!getControl) {
                    if (state.at(single) == 0) {
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
                        if (sim.isControlled(sim.kings().get(side), side.opp()) == null) {
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
                if (!getControl && state.isControlled(p, side.opp()) == null) {
                    boolean[] castle = state.castle().get(side);
                    if (castle[0]) {
                        if (state.at(p + 1) == 0
                                && state.at(p + 2) == 0
                                && state.isControlled(p + 1, side.opp()) == null) {
                            moves.add(p + 2);
                        }
                    }
                    if (castle[1]) {
                        if (state.at(p - 1) == 0
                                && state.at(p - 2) == 0
                                && state.at(p - 3) == 0
                                && state.isControlled(p - 1, side.opp()) == null) {
                            moves.add(p - 2);
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
            ArrayList<Integer> checkMoves = new ArrayList<>();
            if (check != null) {
                checkMoves = check.path();
                inCheck = true;
            }

            ArrayList<Integer> pinMoves = null;
            for (Pin pin : state.pins()) {
                if (pin.pinPiece() == p) {
                    pinMoves = pin.pinMoves();
                    break;
                }
            }

            boolean isKing = is(piece, 'k');
            for (int i = 0; i < moves.size(); i++) {
                int move = moves.get(i);
                if (pinMoves != null && !pinMoves.contains(move)) {
                    moves.remove(i);
                    i--;

                } else {
                    if (state.enPassant() != -1 && is(piece, 'p') && move == state.enPassant()) {
                        continue;
                    }

                    // king moving to a controlled square
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
            state.castle().set(side, new boolean[]{false, false});
            state.kings().set(side, to);

        } else if (is(fromPiece, 'r')) {
            // cancel castling
            boolean[] castle = state.castle().get(side);
            if (castle[0] && from == (isBlack(side) ? 63 : 7)) {
                castle[0] = false;
            } else if (castle[1] && from == (isBlack(side) ? 56 : 0)) {
                castle[1] = false;
            }

        } else if (toPiece != 0 && is(toPiece, 'r')) {
            // cancel castling
            boolean[] castle = state.castle().get(side.opp());
            if (castle[0] && to == (isBlack(side) ? 7 : 63)) {
                castle[0] = false;
            } else if (castle[1] && to == (isBlack(side) ? 0 : 56)) {
                castle[1] = false;
            }
        }

        if (to < 0) log("to", to, state.at(from), fromPiece);
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
        State originalState = setupState(fen);
        long[] nodes = new long[max - min + 1];

        int n = 0;
        for (int i = min; i <= max; i++) {
            State state = originalState.clone();
//            long start = System.nanoTime();

            long count = movesTest(state, i);

//            double time = (System.nanoTime() - start) / 1000000.0;
            nodes[n] = count;
            n++;

//            System.out.println("----- Depth " + i);
//            System.out.println("Moves: " + count);
//            System.out.println("Time: " + time);
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
