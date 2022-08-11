package com.example.demo.ai.objects;

public enum Side {
    WHITE, BLACK, DRAW, NONE;

    public Side opp() {
        if (this == WHITE) {
            return BLACK;
        } else if (this == BLACK) {
            return WHITE;
        } else {
            return NONE;
        }
    }
}
