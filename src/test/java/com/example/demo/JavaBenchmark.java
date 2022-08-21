package com.example.demo;

import org.junit.jupiter.api.Test;

import static com.example.demo.ai.Util.log;

public class JavaBenchmark {

    @Test
    void testBitshift() {
        int n = 23;

        long start = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            int x = n << 3;
        }
        double end = (System.nanoTime() - start) / 1000000.0;
        log("shift mult", end);

        start = System.nanoTime();
        for (int j = 0; j < 10000000; j++) {
            int x = n * 8;
        }
        end = (System.nanoTime() - start) / 1000000.0;
        log("mult", end);
    }
}
