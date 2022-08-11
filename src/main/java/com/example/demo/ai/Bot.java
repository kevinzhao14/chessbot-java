package com.example.demo.ai;

import com.example.demo.ai.objects.Pos;
import com.example.demo.ai.objects.ValidMoves;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;

public class Bot {
    @Value("${ai.depth}")
    static int DEPTH;

    @Value("${ai.mult}")
    static int MULT;

    @Value("${ai.debug}")
    static boolean DEBUG;

    @Value("${ai.log}")
    static boolean LOG;

    @Value("${ai.extradepth}")
    static boolean EXTRA_DEPTH;


    private int nodes = 0;

    static BestMove getMove(String fen) {
        State state = setupState(fen);
        if (LOG) {
            Util.printBoard(state, true);
        }

        // TODO: extraDepth

        long start = System.currentTimeMillis();
        Move best = bestMove(state);
        long total = System.currentTimeMillis() - start;

        System.out.println("\nMove: \t" + best.move[0] + " -> " + best.move[1]);
        System.out.println("Score: \t" + Math.round(best.score * 1000) / 1000);

        Pos from = null;
        Pos to = null;

        if (best.move != null) {
            from = best.move[0];
            to = best.move[1];

        }


        return null;
    }

    static State setupState(String fen) {
        return null;
    }

    static Move bestMove(State state) {
        return null;
    }

    static ValidMoves getValidMoves(char piece, Pos p, State state, boolean control) {
        return null;
    }

    static ValidMoves getValidMoves(char piece, Pos p, State state) {
        return getValidMoves(piece, p, state, false);
    }


    class BestMove {
        Pos from;
        Pos to;
        String promote;
    }

    private class Move {
        Pos[] move;
        double score;
        ArrayList<Pos> line;
    }
}
