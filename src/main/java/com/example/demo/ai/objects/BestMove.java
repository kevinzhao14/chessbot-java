package com.example.demo.ai.objects;

public class BestMove {
    int from;
    int to;
    char promote;
    long nodes;

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
