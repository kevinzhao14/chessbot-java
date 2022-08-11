package com.example.demo.ai.objects;

import java.util.ArrayList;

public class Check {
    Pos by;
    ArrayList<Pos> path;

    public Check(Pos by, ArrayList<Pos> path) {
        this.by = by;
        this.path = path;
    }

    public Check clone() {
        ArrayList<Pos> newPath = new ArrayList<>();
        for (Pos p : this.path) {
            newPath.add(p.clone());
        }
        return new Check(this.by.clone(), newPath);
    }
}
