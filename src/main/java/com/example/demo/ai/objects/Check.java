package com.example.demo.ai.objects;

import java.util.ArrayList;

public class Check {
    int by;
    long path;

    public Check(int by, long path) {
        this.by = by;
        this.path = path;
    }

    public Check clone() {
        return new Check(this.by, this.path);
    }

    public long path() {
        return path;
    }

    @Override
    public String toString() {
        return "Check{" +
                "by=" + by +
                ", path=" + path +
                '}';
    }
}
