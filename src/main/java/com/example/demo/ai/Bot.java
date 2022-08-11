package com.example.demo.ai;

import com.example.demo.ai.objects.Pos;
import org.springframework.beans.factory.annotation.Value;

public class Bot {
    @Value("${ai.depth}")
    int DEPTH;

    @Value("${ai.mult}")
    int MULT;

    @Value("${ai.debug}")
    boolean DEBUG;

    @Value("${ai.log}")
    boolean LOG;

    @Value("${ai.extradepth}")
    boolean EXTRA_DEPTH;


    private int nodes = 0;

    public BestMove getMove(String fen) {

    }

    private State setupState(String fen) {
    }


    class BestMove {
        Pos from;
        Pos to;
        String promote;
    }
}
