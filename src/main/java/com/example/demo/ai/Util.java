package com.example.demo.ai;

import com.example.demo.ai.objects.Dir;
import com.example.demo.ai.objects.Pos;
import com.example.demo.ai.objects.Side;

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

        char[][] board = state.board();
        StringBuilder str = new StringBuilder(prepend ? nldelim : "");
        for (int i = 7; i >= 0; i--) {
            char[] row = board[i];
            str.append(prepend ? delim : "");
            for (int j = 0; j < 8; j++) {
                char op = isEmpty(row[j]) ? empty : row[j];
                char piece = op;
                Pos pos = new Pos(j, i);
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

    static Pos coordFromAn(String an) {
        return new Pos(an.charAt(0) - 97, an.charAt(1) - 48);
    }

    static double eval(State state) {
        if (state.won() != Side.NONE) {
            if (state.won() == Side.DRAW) {
                return 0;
            }
            return pieceValue('k') * (isBlack(state.won()) ? -1 : 1);
        }
        double total = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                char piece = state.board()[j][i];
                total += (isBlack(piece) ? -1 : 1) * pieceValue(piece);
            }
        }
        return total;
    }

    static int pieceValue(char piece) {
        switch (Character.toLowerCase(piece)) {
            case 'p':
                return 1;
            case 'r':
                return 5;
            case 'b':
                return 3;
            case 'n':
                return 3;
            case 'q':
                return 9;
            case 'k':
                return 1000;
            default:
                return 0;
        }
    }

    static boolean offBoard(Pos pos) {
        return pos.x() < 0 || pos.x() > 7 || pos.y() < 0 || pos.y() > 7;
    }

    static Pos go(Pos from, int dir) {
        Pos to = from.clone();
        if (dir == Dir.N || dir == Dir.NE || dir == Dir.NW) {
            to.setY(to.y() + 1);
        } else if (dir == Dir.S || dir == Dir.SE || dir == Dir.SW) {
            to.setY(to.y() - 1);
        }

        if (dir == Dir.E || dir == Dir.SE || dir == Dir.NE) {
            to.setX(to.x() + 1);
        } else if (dir == Dir.W || dir == Dir.SW || dir == Dir.NW) {
            to.setX(to.x() - 1);
        }
        return to;
    }

    static int rand(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
//        return (int) (Math.floor(Math.random() * (max - min + 1)) + min);
    }

    static void printBoard(State state, boolean pretty) {
        printBoard(state, pretty, false);
    }

    static boolean isEmpty(char piece) {
        return piece == 0;
    }

    static String an(Pos pos, char p) {
        String n = Character.toString(LETTERS[pos.x()]);
        if (is(p, 'p')) {
            if (pos.y() <= 0) {
                n += "1" + PROM[pos.y() * -1];
            } else if (pos.y() >= 7) {
                n += "8" + PROM[pos.y() - 7];
            }
        } else {
            n += (pos.y() + 1);
        }
        return n;
    }

    static String an(Pos pos) {
        return an(pos, '\0');
    }

    static boolean isNumeric(String str) {
        return str.matches("\\d+");  //match a number with optional '-' and decimal.
    }
}
