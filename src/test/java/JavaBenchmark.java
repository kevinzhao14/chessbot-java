import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static com.example.demo.ai.Util.bit;
import static com.example.demo.ai.Util.bitmapAdd;
import static com.example.demo.ai.Util.bitmapHas;
import static com.example.demo.ai.Util.log;

public class JavaBenchmark {

    @Test
    void testBitmapVsArrayList() {
        long start = System.nanoTime();
        for (int j = 0; j < 10000000; j++) {
            long bitmap = 0;
            for (int i = 30; i < 40; i += 3) {
                bitmap = bitmapAdd(bitmap, bit(i));
            }
            for (int i = 0; i < 64; i++) {
                if (!bitmapHas(bitmap, bit(i))) {
                    continue;
                }
                int val = i + 1;
            }
        }
        double end = (System.nanoTime() - start) / 1000000.0;
        log("bitmap", end);


        start = System.nanoTime();
        for (int j = 0; j < 10000000; j++) {
            ArrayList<Integer> list = new ArrayList<>();
            for (int i = 30; i < 40; i += 3) {
                list.add(i);
            }
            for (int val : list) {
                val += 1;
            }
        }
        end = (System.nanoTime() - start) / 1000000.0;
        log("arraylist", end);
    }

    @Test
    void testContainsVsBitmapHas() {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(57);
        list.add(58);
        list.add(59);
        list.add(60);
        list.add(0);
        list.add(2);
        list.add(5);
        list.add(9);
        list.add(14);
        list.add(20);
        list.add(21);
        list.add(22);
        list.add(23);
        long map = 8646911284552352420L;

        long start = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            for (int j = 0; j < 64; j++) {
                if (!bitmapHas(map, bit(j))) {
                    int n = j;
                }
            }
        }
        double end = (System.nanoTime() - start) / 1000000.0;
        log("map", end);


        start = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            for (int j = 0; j < 64; j++) {
                if (!list.contains(j)) {
                    int n = j;
                }
            }
        }
        end = (System.nanoTime() - start) / 1000000.0;
        log("list", end);
    }

    @Test
    void testAddListVsAddLong() {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(4);
        list.add(5);
        list.add(6);
        list.add(7);

        long map = 15L;

        long start = System.nanoTime();
        for (int i = 0; i < 10000000; i++) {
            ArrayList<Long> temp = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                temp.add(map);
            }
        }
        double end = (System.nanoTime() - start) / 1000000.0;
        log("map", end);


        start = System.nanoTime();
        for (int i = 0; i < 10000000; i++) {
            ArrayList<ArrayList<Integer>> temp = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                temp.add(list);
            }
        }
        end = (System.nanoTime() - start) / 1000000.0;
        log("list", end);
    }
}
