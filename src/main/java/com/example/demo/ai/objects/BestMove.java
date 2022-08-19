package com.example.demo.ai.objects;

public class BestMove {
    int from;
    int to;
    char promote;
    long nodes;
    double eval;

    public BestMove(int from, int to, char promote) {
        this.from = from;
        this.to = to;
        this.promote = promote;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public void setPromote(char promote) {
        this.promote = promote;
    }

    public long getNodes() {
        return nodes;
    }

    public void setNodes(long nodes) {
        this.nodes = nodes;
    }

    public double getEval() {
        return eval;
    }

    public void setEval(double eval) {
        this.eval = eval;
    }

    @Override
    public String toString() {
        String str = "{";

        str += "\"from\":";
        if (from > -1) {
            str += posToArray(from);
        } else {
            str += "null";
        }

        str += ",\"to\":";
        if (to > -1) {
            str += posToArray(to);
        } else {
            str += "null";
        }

        str += ",\"eval\":";

        if (eval >= 1000 || eval <= -1000) {
            str += "\"Mate in " + (int) Math.ceil((eval % 100) / 2) + "\"";
        } else {
            str += eval;
        }

        str += ",\"options\":{";
        if (promote != 0) {
            str += "\"promote\":\"" + promote + "\"";
        }
        str += "}}";
        return str;
    }

    private String posToArray(int pos) {
        return "[" + (pos % 8) + "," + (pos / 8) + "]";
    }
}
