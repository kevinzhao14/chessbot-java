package com.example.demo.ai;

import com.example.demo.ai.objects.Group;
import com.example.demo.ai.objects.Pos;
import com.example.demo.ai.objects.Side;

public class State {
    char[][] board;
    Side turn;
    Side won;
    Group<boolean[]> castle;
    Pos enPassant;
    Check check;

}
