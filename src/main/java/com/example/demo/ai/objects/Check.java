package com.example.demo.ai.objects;

import java.util.ArrayList;

public class Check {
    int by;
    ArrayList<Integer> path;

    public Check(int by, ArrayList<Integer> path) {
        this.by = by;
        this.path = path;
    }

    public Check clone() {
        return new Check(this.by, (ArrayList<Integer>) this.path.clone());
    }

    public ArrayList<Integer> path() {
        return path;
    }
}
