package com.example.demo.ai;

import com.example.demo.ai.objects.Dir;
import com.example.demo.ai.objects.Side;
import com.example.demo.ai.objects.Static;

import java.util.concurrent.ThreadLocalRandom;

public class Util {
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";

    private static final char[] LETTERS = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
    static final char[] PROM = {'q', 'n', 'r', 'b'};

    static void printBoard(State state, boolean pretty, boolean colored) {
        String delim = " ";
        String nldelim = "\n";
        char empty = '_';
        boolean prepend = false;

        if (pretty) {
            delim = " | ";
            nldelim = "\n ---------------------------------\n";
            empty = ' ';
            prepend = true;
        }

        StringBuilder str = new StringBuilder(prepend ? nldelim : "");
        for (int i = 7; i >= 0; i--) {
            str.append(prepend ? delim : "");
            for (int j = 0; j < 8; j++) {
                int pos = i * 8 + j;
                char op = isEmpty(state.at(pos)) ? empty : state.at(pos);
                char piece = op;
                if (colored) {
                    // TODO: colored
                }
                str.append(piece).append(delim);
            }
            str.append(nldelim);
        }
        System.out.println(str);
    }

    static boolean is(char piece, char want) {
        return Character.toLowerCase(piece) == want;
    }

    static Side sideOf(char piece) {
        if (Character.toUpperCase((piece)) == piece) {
            return Side.WHITE;
        } else {
            return Side.BLACK;
        }
    }

    static boolean isBlack(char piece) {
        return Character.toUpperCase((piece)) != piece;
    }

    static boolean isBlack(Side side) {
        return side == Side.BLACK;
    }

    static int coordFromAn(String an) {
        return (an.charAt(0) - 97) * 8 + an.charAt(1) - 48;
    }

    static double eval(State state) {
        if (state.won() != Side.NONE) {
            if (state.won() == Side.DRAW) {
                return 0;
            }
            return isBlack(state.won()) ? -1000 : 1000;
        }
        double total = 0;
        for (int i = 0; i < 64; i++) {
            char piece = state.at(i);
            if (piece == 0) {
                continue;
            }
            total += (isBlack(piece) ? -1 : 1) * pieceValue(piece, i);
        }
        return total;
    }

    static double pieceValue(char piece, int pos) {
        if (isBlack(sideOf(piece))) {
            pos = (7 - (pos / 8)) * 8 + pos % 8;
        }
        switch (Character.toLowerCase(piece)) {
            case 'p':
                return 1 + Static.BONUS_P[pos];
            case 'r':
                return 5 + Static.BONUS_R[pos];
            case 'b':
                return 3 + Static.BONUS_B[pos];
            case 'n':
                return 3 + Static.BONUS_N[pos];
            case 'q':
                return 9 + Static.BONUS_Q[pos];
            case 'k':
                return 10 + Static.BONUS_K[pos];
            default:
                return 0;
        }
    }

    static boolean offBoard(int pos) {
        return pos < 0 || pos > 63;
    }

    static int go(int from, int dir) {
        if (dir == Dir.N || dir == Dir.NE || dir == Dir.NW) {
            if (from >= 56) {
                return -1;
            }
            from += 8;
        } else if (dir == Dir.S || dir == Dir.SE || dir == Dir.SW) {
            if (from <= 7) {
                return -1;
            }
            from -= 8;
        }

        if (dir == Dir.E || dir == Dir.SE || dir == Dir.NE) {
            if (from % 8 == 7) {
                return -1;
            }
            from += 1;
        } else if (dir == Dir.W || dir == Dir.SW || dir == Dir.NW) {
            if (from % 8 == 0) {
                return -1;
            }
            from -= 1;
        }
        return from;
    }

    static int rand(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    static void printBoard(State state, boolean pretty) {
        printBoard(state, pretty, false);
    }

    static boolean isEmpty(char piece) {
        return piece == 0;
    }

    static String an(int pos, char p) {
        int r = (int) Math.floor(pos / 8.0), c = pos % 8;
        if (c < 0) {
            c += 8;
        }
        String n = Character.toString(LETTERS[c]);
        if (is(p, 'p')) {
            if (r <= 0) {
                n += "1" + PROM[r * -1];
            } else if (r >= 7) {
                n += "8" + PROM[r - 7];
            } else {
                n += (r + 1);
            }
        } else {
            n += (r + 1);
        }
        return n;
    }

    static String an(int pos) {
        return an(pos, '\0');
    }

    static boolean isNumeric(String str) {
        return str.matches("\\d+");  //match a number with optional '-' and decimal.
    }

    public static void log(Object... args) {
        StringBuilder str = new StringBuilder();
        for (Object o : args) {
            if (o == null) {
                str.append("null ");
            } else {
                str.append(o).append(" ");
            }
        }
        System.out.println(str);
    }

    static long bit(long pos) {
        return 1L << (pos - ((pos % 8) * 2 - 7));
    }

    static boolean bitmapHas(long bitmap, long has) {
        return (bitmap & has) != 0;
    }

    static long bitmapAdd(long bitmap, long bit) {
        return bitmap | bit;
    }

    static void printBitmap(long map) {
        StringBuilder str = new StringBuilder();
        long mask = 1;
        int count = 0;
        while (count < 64) {
            str.insert(0, ((map & mask) >> count) + " ");
            if (count % 8 == 7) {
                str.insert(0, "\n");
            }
            count++;
            mask = mask << 1;
        }
        System.out.println(str);
    }
}
