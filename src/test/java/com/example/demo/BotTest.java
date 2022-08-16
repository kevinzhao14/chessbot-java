package com.example.demo;

import com.example.demo.ai.Bot;
import com.example.demo.ai.Util;
import com.example.demo.ai.objects.BestMove;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.LinkedList;

public class BotTest {
    final String[] tests_black = {
            "r1bqkbnr/pppp1ppp/2n5/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R b KQkq - 3 3",
            "r1bqkb1r/2pp1ppp/p1n5/1p2p3/3Pn3/1B3N2/PPP2PPP/RNBQ1RK1 b kq - 1 7",
            "r2q1rk1/2p1bppp/p1n1b3/1p1pP3/4n3/2P2N2/PPBN1PPP/R1BQ1RK1 b - - 2 11",
            "r4rk1/2pqbbpp/p7/1p1pnp2/3Nn3/1NP2P2/PPB3PP/R1BQR1K1 b - - 0 15",
            "3r1rk1/4bbpp/p4q2/1pppn3/4P3/1NP1B3/PPB1Q1PP/R3R1K1 b - - 4 19",
            "3r1r1k/4b1pp/p4q2/1p2n3/2BNP3/4B3/PP2Q1PP/R3R1K1 b - - 0 23",
            "3r1b1k/6pp/pq6/1p6/4P3/4N3/PP2Q1PP/R5K1 b - - 0 27",
            "5r1k/6pp/p7/1pbN4/4P3/7P/qP2Q1P1/4R2K b - - 0 31",
            "5r1k/4b1p1/p3q2p/1p1NP3/4Q3/7P/1P4P1/4R2K b - - 1 35",
            "7k/5qp1/p3P2p/1p1N4/4Q2P/8/1P1b1rP1/3R2K1 b - - 0 39",
    };

    final String[] tests_white = {
            "r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 2 3",
            "r1bqkb1r/2pp1ppp/p1n5/1p2p3/B2Pn3/5N2/PPP2PPP/RNBQ1RK1 w kq b6 0 7",
            "r2q1rk1/2p1bppp/p1n1b3/1p1pP3/4n3/1BP2N2/PP1N1PPP/R1BQ1RK1 w - - 1 11",
            "r4rk1/2pqbbpp/p7/1p1pnp2/3Nn3/1NP5/PPB2PPP/R1BQR1K1 w - - 0 15",
            "3r1rk1/4bbpp/p4q2/1pppn3/4P3/1NP1B3/PPB3PP/R2QR1K1 w - - 3 19",
            "3r1r1k/4b1pp/p4q2/1p2n3/2bNP3/1B2B3/PP2Q1PP/R3R1K1 w - - 3 23",
            "3r1b1k/6pp/pq6/1p3N2/4P3/4n3/PP2Q1PP/R5K1 w - - 0 27",
            "5r1k/6pp/p7/1pbN4/4P3/8/qP2Q1PP/4R2K w - - 2 31",
            "5r1k/4b1p1/p3q2p/1p2P3/4Q3/2N4P/1P4P1/4R2K w - - 0 35",
            "7k/5qp1/p6p/1p1NP3/4Q2P/8/1P1b1rP1/3R2K1 w - - 5 39",
    };

    Perft[] perfts = {
            new Perft(2, "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - ", new long[]{
                    48, 2039, 97862
            }),
            new Perft(3, "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - ", new long[]{
                    14, 191, 2812, 43238, 674624
            }),
            new Perft(4, "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", new long[]{
                    6, 264, 9467, 422333
            }),
            new Perft(5, "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8  ", new long[]{
                    44, 1486, 62379
            }),
            new Perft(6, "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10 ", new long[]{
                    46, 2079, 89890
            })
    };

    private void testSuite(String[] suite, int count) {
        double totalTime = 0;
        long totalNodes = 0;
        LinkedList<Double> eachTimes = new LinkedList<>();
        LinkedList<Long> eachNodes = new LinkedList<>();
        int tested = 0;

        for (int i = 0; i < count; i++) {
            for (String test : suite) {
                System.out.println("> Running Test " + tested);
                long start = System.nanoTime();

                BestMove results = Bot.getMove(test, true);

                double perf = (System.nanoTime() - start) / 1000000.0;
                long nodes = results.getNodes();

                eachTimes.add(perf);
                eachNodes.add(nodes);
                totalTime += perf;
                totalNodes += nodes;
                tested++;
            }
        }

        System.out.println("----- RESULTS -----");
        System.out.println("Tests:        \t" + tested);
        System.out.println("Avg Time:     \t" + commas(round(totalTime / tested)) + "ms");
        System.out.println("Avg Nodes:    \t" + commas(round((double) totalNodes / tested)));
        System.out.println("Total Time:   \t" + (round(totalTime) / 1000) + "s");
        System.out.println("Total Nodes:  \t" + commas(round(totalNodes)));
        System.out.println("Time/1k Nodes:\t" + round(totalTime / (totalNodes / 1000.0)) + "ms");
    }

    private double round(double num) {
        return Math.round(num * 100) / 100.0;
    }

    private String commas(double x) {
        return new DecimalFormat("#,###.00").format(x);
    }

    private <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

    static class Perft {
        int id;
        String fen;
        long[] nodes;

        Perft(int id, String fen, long[] nodes) {
            this.id = id;
            this.fen = fen;
            this.nodes = nodes;
        }
    }

    @Test
    void testBlack() {
        testSuite(tests_black, 1);
    }

    @Test
    void testWhite() {
        testSuite(tests_white, 1);
    }

    @Test
    void testAll() {
        String[] all = concatenate(tests_black, tests_white);
        testSuite(all, 5);
    }

    @Test
    void testPerfts() {
        for (Perft perft : perfts) {
            System.out.println("----- " + "Running Perf Test " + perft.id + " -----");
            long[] res = Bot.testPerft(perft.fen, 1, perft.nodes.length);

            Util.log("res", res[0], res[1], res[2]);
            for (int i = 0; i < perft.nodes.length; i++) {
                System.out.println("Depth " + (i + 1) + ":");
                Assertions.assertThat(res[i]).isEqualTo(perft.nodes[i]);
                System.out.println("\tPASS");
            }
        }
    }

    @Test
    void testSinglePerft() {
        String fen = "1Q2k2r/1ppp1ppp/1b3nbN/nPB5/B1P1P3/q4N2/Pp1P2PP/R2Q1RK1 b k - 0 2";
//        String fen = perfts[2].fen;

        Bot.testPerft(fen, 1, 1);
    }

    @Test
    void testOther() {
        Bot.test();
    }

}
