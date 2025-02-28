import java.util.ArrayList;

// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
// Vous pouvez par contre ajouter d'autres méthodes (ça devrait 
// être le cas)
class Board {

    public static final int VICTORY = 100;
    public static final int DEFEAT = -100;
    public static final int STOP = 0;
    public static final int CONTINUE = 1;
    private Mark[][] board;
    private boolean[][] sectionFilled = new boolean[3][3];
    private int COLUMN = 9;
    private int ROW = 9;

    // Ne pas changer la signature de cette méthode
    public Board() {
        this.board = new Mark[9][9];
        for (int i = 0; i < COLUMN; i++) {
            for (int j = 0; j < ROW; j++) {
                board[i][j] = Mark.EMPTY;
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                sectionFilled[i][j] = false;
            }
        }
    }

    public Board(Mark[][] board) {
        this.board = board;
    }

    public Mark[][] getBoard() {
        return board;
    }

    public Mark getMark(int row, int col) {
        return board[row][col];
    }

    // Place la pièce 'mark' sur le plateau, à la
    // position spécifiée dans Move
    //
    // Ne pas changer la signature de cette méthode
    public void play(Move m, Mark mark) {
        int row = m.getRow();
        int col = m.getCol();

        board[row][col] = mark;
    }

    public void undo(Move m) {
        board[m.getRow()][m.getCol()] = Mark.EMPTY;
    }


    // retourne  100 pour une victoire
    //          -100 pour une défaite
    //           0   pour un match nul
    // Ne pas changer la signature de cette méthode
    public int evaluate(Mark mark) {
        Mark opponent = (mark == Mark.X) ? Mark.O : Mark.X;

        for (int i = 0; i < 3; i++) {
            if (board[i][0] == mark && board[i][1] == mark && board[i][2] == mark) {
                return VICTORY;
            }
            if (board[i][0] == opponent && board[i][1] == opponent && board[i][2] == opponent) {
                return DEFEAT;
            }
            if (board[0][i] == mark && board[1][i] == mark && board[2][i] == mark) {
                return VICTORY;
            }
            if (board[0][i] == opponent && board[1][i] == opponent && board[2][i] == opponent) {
                return DEFEAT;
            }
        }

        if (board[0][0] == mark && board[1][1] == mark && board[2][2] == mark) {
            return VICTORY;
        }

        if (board[0][2] == mark && board[1][1] == mark && board[2][0] == mark) {
            return VICTORY;
        }

        if (board[0][0] == opponent && board[1][1] == opponent && board[2][2] == opponent) {
            return DEFEAT;
        }

        if (board[0][2] == opponent && board[1][1] == opponent && board[2][0] == opponent) {
            return DEFEAT;
        }

        boolean isBoardFull = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == Mark.EMPTY) {
                    isBoardFull = false;
                    break;
                }
            }
        }

        if (isBoardFull) {
            return STOP;
        }

        return CONTINUE;
    }

    public Move[] getPossibleMoves() {
        ArrayList<Move> moves = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == Mark.EMPTY) {
                    moves.add(new Move(i, j));
                }
            }
        }


        Move[] array = new Move[moves.size()];
        array = moves.toArray(array);
        return array;
    }

    public Move[] getPossibleMoves(Move move) {
        ArrayList<Move> moves = new ArrayList<>();
        int[] nextSector = getNextSector(move);

        for (int i = nextSector[0]; i < nextSector[0] + 2; i++) {
            for (int j = nextSector[1]; j < nextSector[1] + 2; j++) {
                if (board[i][j] == Mark.EMPTY) {
                    moves.add(new Move(i, j));
                }
            }
        }

        if (moves.isEmpty()) {
            sectionFilled[move.getRow()][move.getCol()] = true;
        }

        // Go through all the sections that arent filled and add them as possible moves

        Move[] array = new Move[moves.size()];
        array = moves.toArray(array);
        return array;
    }

    public static int[] getNextSector(Move m) {
        int rowIndex = m.getRow();
        int colIndex = m.getCol();

        // Determine next sector's row start (1-3, 4-6, 7-9)
        int nextSectorRow = (rowIndex % 3) * 3;

        // Determine next sector's column start (A-C, D-F, G-I)
        int nextSectorCol = (colIndex % 3) * 3;

        return new int[]{nextSectorRow, nextSectorCol};
    }
}
