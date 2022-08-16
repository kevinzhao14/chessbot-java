# FEAT/Bug tracker

ID   | Type  | Description
---- | ----- | ----
1    | ~FEAT  | ~~Add castling~~
2    | ~FEAT  | ~~Add en passant~~
3    | ~FEAT  | ~~Add check verification~~
4    | ~FEAT  | ~~Add checkmates~~
5    | ~FEAT  | ~~Add stalemates~~
6    | ~FEAT  | ~~Add promotion~~
7    | OPT   | update valid moves instead of recalculating each time
8    | ~OPT   | ~~sort valid moves before searching~~
9    | ~FEAT  | ~~heatmaps - more/less eval depending the pos per piece~~
10   | ~FEAT  | ~~tests~~
11   | ~OPT   | ~~store pieces in an array as well to reduce array iterations/searching - not helpful~~
12   | OPT   | async/multithreading - call validMoves on all async and continue once all have returned?
13   | OPT   | iterative deepening
14   | OPT   | killer moves, history heuristic, quiescent search
15   | ~OPT   | ~~store board as 64 rather than 2d array~~
16   | OPT   | long methods instead of short ones that talk
17   | OPT   | return asap
18   | ~OPT   | ~~move goPath outside of getValidMoves - SLOWER~~
19   | ~OPT   | ~~change control to be one side, not a group~~
20   | OPT   | use static final instead of enum for side