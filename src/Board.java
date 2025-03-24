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

    private int COLUMN = 9;
    private int ROW = 9;

    // Ne pas changer la signature de cette méthode
    public Board() {
        this.board = new Mark[COLUMN][ROW];
        for (int i = 0; i < COLUMN; i++) {
            for (int j = 0; j < ROW; j++) {
                board[i][j] = Mark.EMPTY;
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
        board[m.getRow()][m.getCol()] = mark;
    }

    public void undo(Move m) {
        board[m.getRow()][m.getCol()] = Mark.EMPTY;
    }

    // Ne pas changer la signature de cette méthode
    public int evaluate(Mark mark) {
        Mark opponent = (mark == Mark.X) ? Mark.O : Mark.X;

        if (getMetaWinner() == mark) return VICTORY;
        if (getMetaWinner() == opponent) return DEFEAT;

        int score = 0;

        // Strongly value control of the center meta-grid
        Mark centerSubWinner = checkSubBoardWinner(3, 3);
        if (centerSubWinner == mark) score += 100;
        else if (centerSubWinner == opponent) score -= 100;

        // Evaluate all 3x3 sub-grids
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int subScore = evaluateSubGrid(i * 3, j * 3, mark);
                score += subScore;

                Mark winner = checkSubBoardWinner(i * 3, j * 3);
                if (winner == mark) score += 60;
                else if (winner == opponent) score -= 60;

                // Bonus for controlling the center cell of sub-grid
                Mark center = board[i * 3 + 1][j * 3 + 1];
                if (center == mark) score += 10;
                else if (center == opponent) score -= 10;
            }
        }

        // Evaluate potential macro-board control
        score += 2 * evaluateMetaBoard(mark);

        return score;
    }


    private int evaluateSubGrid(int row, int col, Mark mark) {
        int score = 0;

        for (int i = 0; i < 3; i++) {
            // Check rows for potential
            score += evaluateLine(new Mark[]{board[row + i][col], board[row + i][col + 1], board[row + i][col + 2]}, mark);
            // Check columns for potential
            score += evaluateLine(new Mark[]{board[row][col + i], board[row + 1][col + i], board[row + 2][col + i]}, mark);
        }

        // Check diagonals
        score += evaluateLine(new Mark[]{board[row][col], board[row + 1][col + 1], board[row + 2][col + 2]}, mark);
        score += evaluateLine(new Mark[]{board[row][col + 2], board[row + 1][col + 1], board[row + 2][col]}, mark);

        return score;
    }

    private int evaluateLine(Mark[] line, Mark mark) {
        int countMark = 0;
        int countOpponent = 0;
        Mark opponent = (mark == Mark.X) ? Mark.O : Mark.X;

        for (Mark m : line) {
            if (m == mark) countMark++;
            else if (m == opponent) countOpponent++;
        }

        // Line Scoring
        if (countMark == 3) return 100; // winning line
        if (countMark == 2 && countOpponent == 0) return 40;
        if (countMark == 1 && countOpponent == 0) return 10;
        if (countOpponent == 2 && countMark == 0) return -35;
        if (countOpponent == 1 && countMark == 0) return -10;

        return 0;
    }



    private int evaluateMetaBoard(Mark mark) {
        Mark[][] metaBoard = new Mark[3][3];

        // Construct the meta-board (sub-grid winners)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                metaBoard[i][j] = checkSubBoardWinner(i * 3, j * 3);
            }
        }

        int score = 0;

        // Reward control of the meta-board
        for (int i = 0; i < 3; i++) {
            score += evaluateLine(new Mark[]{metaBoard[i][0], metaBoard[i][1], metaBoard[i][2]}, mark);
            score += evaluateLine(new Mark[]{metaBoard[0][i], metaBoard[1][i], metaBoard[2][i]}, mark);
        }

        // Check diagonals
        score += evaluateLine(new Mark[]{metaBoard[0][0], metaBoard[1][1], metaBoard[2][2]}, mark);
        score += evaluateLine(new Mark[]{metaBoard[0][2], metaBoard[1][1], metaBoard[2][0]}, mark);

        return score;
    }



    public Move[] getPossibleMoves(int lastRow, int lastCol) {
        ArrayList<Move> moves = new ArrayList<>();
        int subRow = (lastRow % 3) * 3;
        int subCol = (lastCol % 3) * 3;

        for (int i = subRow; i < subRow + 3; i++) {
            for (int j = subCol; j < subCol + 3; j++) {
                if (board[i][j] == Mark.EMPTY) {
                    moves.add(new Move(i, j));
                }
            }
        }

        // If sub grid is filled find else where
        if (moves.isEmpty()) {
            for (int i = 0; i < COLUMN; i++) {
                for (int j = 0; j < ROW; j++) {
                    if (board[i][j] == Mark.EMPTY) {
                        moves.add(new Move(i, j));
                    }
                }
            }
        }
        return moves.toArray(new Move[0]);
    }

    // Check if globally there is a winner
    private Mark getMetaWinner() {
        Mark[][] metaBoard = new Mark[3][3];

        // Creates a board with sub-grid winners
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                metaBoard[i][j] = checkSubBoardWinner(i * 3, j * 3);
            }
        }


        // Finds winner in new board
        return checkBoardWinner(metaBoard);
    }

    // Finds the a winnning sub-grid
    private Mark checkSubBoardWinner(int row, int col) {
        for (int i = 0; i < 3; i++) {

            // Checks each row is the same Mark
            if (board[row + i][col] == board[row + i][col + 1] &&
                    board[row + i][col] == board[row + i][col + 2] &&
                    board[row + i][col] != Mark.EMPTY) {
                return board[row + i][col];
            }

            // Checks each col is the same mark
            if (board[row][col + i] == board[row + 1][col + i] &&
                    board[row][col + i] == board[row + 2][col + i] &&
                    board[row][col + i] != Mark.EMPTY) {
                return board[row][col + i];
            }
        }

        // Checks one diagonal
        if (board[row][col] == board[row + 1][col + 1] &&
                board[row][col] == board[row + 2][col + 2] &&
                board[row][col] != Mark.EMPTY) {
            return board[row][col];
        }

        // Checks other diagonal
        if (board[row][col + 2] == board[row + 1][col + 1] &&
                board[row][col + 2] == board[row + 2][col] &&
                board[row][col + 2] != Mark.EMPTY) {
            return board[row][col + 2];
        }

        return Mark.EMPTY;
    }


    // Find winner in a sub-grid
    private Mark checkBoardWinner(Mark[][] board) {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == board[i][1] && board[i][0] == board[i][2] && board[i][0] != Mark.EMPTY) {
                return board[i][0];
            }
            if (board[0][i] == board[1][i] && board[0][i] == board[2][i] && board[0][i] != Mark.EMPTY) {
                return board[0][i];
            }
        }

        if (board[0][0] == board[1][1] && board[0][0] == board[2][2] && board[0][0] != Mark.EMPTY) {
            return board[0][0];
        }

        if (board[0][2] == board[1][1] && board[0][2] == board[2][0] && board[0][2] != Mark.EMPTY) {
            return board[0][2];
        }

        return null;
    }
}
