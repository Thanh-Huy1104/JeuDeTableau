import java.util.ArrayList;

class Board {
    private Mark[][] board;
    private final int SIZE = 9;
    public int lastRow = -1;
    public int lastCol = -1;
    public static final int VICTORY = 100;
    public static final int DEFEAT = -100;

    public Board() {
        board = new Mark[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = Mark.EMPTY;
            }
        }
    }

    public void play(Move m, Mark mark) {
        if (board[m.getRow()][m.getCol()] == Mark.EMPTY) {
            board[m.getRow()][m.getCol()] = mark;
            lastRow = m.getRow();
            lastCol = m.getCol();
        }
    }

    public void undo(Move m) {
        board[m.getRow()][m.getCol()] = Mark.EMPTY;
    }

    public int evaluate(Mark mark) {
        int score = 0;
        Mark opponent = (mark == Mark.X) ? Mark.O : Mark.X;
        Mark[][] metaBoard = new Mark[3][3];

        for (int i = 0; i < SIZE; i += 3) {
            for (int j = 0; j < SIZE; j += 3) {
                Mark winner = checkSubBoardWinner(i, j);
                metaBoard[i / 3][j / 3] = winner;
                if (winner == mark) score += VICTORY;
                else if (winner == opponent) score -= DEFEAT;
                score += evaluateSubBoard(i, j, mark);

                if (i == 3 && j == 3) { // Center board
                    if (winner == mark) {
                        score += 50; // Higher priority for winning the center board
                    } else if (winner == opponent) {
                        score -= 50; // Penalize if the opponent wins the center board
                    }
                }
            }
        }

        int metaScore = evaluateMetaBoard(metaBoard, mark);
        if (metaScore == Integer.MAX_VALUE || metaScore == -Integer.MAX_VALUE) {
            return metaScore;
        }
        score += metaScore;
        score += redirectOpponent(metaBoard, mark);

        return score;
    }

    private int redirectOpponent(Mark[][] localWinners, Mark mark) {
        int score = 0;
        Mark opponent = (mark == Mark.X) ? Mark.O : Mark.X;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (localWinners[i][j] == Mark.EMPTY) {
                    // Penalize sending the opponent to a board they can easily win
                    if (canOpponentWin(i, j, opponent)) {
                        score -= 50;
                    }
                }
            }
        }
        return score;
    }

    private boolean canOpponentWin(int localRow, int localCol, Mark opponent) {
        int startRow = localRow * 3;
        int startCol = localCol * 3;
        // Check rows, columns, and diagonals for potential wins
        for (int i = 0; i < 3; i++) {
            if (board[startRow + i][startCol] == opponent &&
                    board[startRow + i][startCol + 1] == opponent &&
                    board[startRow + i][startCol + 2] == Mark.EMPTY) {
                return true;
            }
            if (board[startRow][startCol + i] == opponent &&
                    board[startRow + 1][startCol + i] == opponent &&
                    board[startRow + 2][startCol + i] == Mark.EMPTY) {
                return true;
            }
        }

        if (board[startRow][startCol] == opponent &&
                board[startRow + 1][startCol + 1] == opponent &&
                board[startRow + 2][startCol + 2] == Mark.EMPTY) {
            return true;

        }

        if (board[startRow][startCol + 2] == opponent &&
                board[startRow + 1][startCol + 1] == opponent &&
                board[startRow + 2][startCol] == Mark.EMPTY) {
            return true;
        }
        return false;

    }

    private int evaluateSubBoard(int row, int col, Mark mark) {
        int score = 0;
        Mark opponent = mark == Mark.X ? Mark.O : Mark.X;

        for (int i = 0; i < 3; i++) {
            int rowCountMark = 0;
            int rowCountOpponent = 0;
            int colCountMark = 0;
            int colCountOpponent = 0;
            for (int j = 0; j < 3; j++) {
                if (board[row + i][col + j] == mark) rowCountMark++;
                else if (board[row + i][col + j] == opponent) rowCountOpponent++;
                if (board[row + j][col + i] == mark) colCountMark++;
                else if (board[row + j][col + i] == opponent) colCountOpponent++;
            }
            if (rowCountMark == 2 && rowCountOpponent == 0) score += 20;
            if (colCountMark == 2 && colCountOpponent == 0) score += 20;
            if (rowCountOpponent == 2 && rowCountMark == 0) score -= 20;
            if (colCountOpponent == 2 && colCountMark == 0) score -= 20;
        }

        // Check diagonals
        int diag1Mark = 0, diag1Opp = 0, diag2Mark = 0, diag2Opp = 0;
        for (int i = 0; i < 3; i++) {
            if (board[row + i][col + i] == mark) diag1Mark++;
            else if (board[row + i][col + i] == opponent) diag1Opp++;
            if (board[row + i][col + 2 - i] == mark) diag2Mark++;
            else if (board[row + i][col + 2 - i] == opponent) diag2Opp++;
        }
        if (diag1Mark == 2 && diag1Opp == 0) score += 50;
        if (diag2Mark == 2 && diag2Opp == 0) score += 50;
        if (diag1Opp == 2 && diag1Mark == 0) score -= 50;
        if (diag2Opp == 2 && diag2Mark == 0) score -= 50;

        return score;
    }

    public int evaluateMetaBoard(Mark[][] metaBoard, Mark mark) {
        Mark opponent = (mark == Mark.X) ? Mark.O : Mark.X;

        // Check for global win or loss
        for (int i = 0; i < 3; i++) {
            if (metaBoard[i][0] == mark && metaBoard[i][1] == mark && metaBoard[i][2] == mark) {
                return Integer.MAX_VALUE;
            }
            if (metaBoard[0][i] == mark && metaBoard[1][i] == mark && metaBoard[2][i] == mark) {
                return Integer.MAX_VALUE;
            }
            if (metaBoard[i][0] == opponent && metaBoard[i][1] == opponent && metaBoard[i][2] == opponent) {
                return -Integer.MAX_VALUE;
            }
            if (metaBoard[0][i] == opponent && metaBoard[1][i] == opponent && metaBoard[2][i] == opponent) {
                return -Integer.MAX_VALUE;
            }
        }
        if (metaBoard[0][0] == mark && metaBoard[1][1] == mark && metaBoard[2][2] == mark) {
            return Integer.MAX_VALUE;
        }
        if (metaBoard[0][2] == mark && metaBoard[1][1] == mark && metaBoard[2][0] == mark) {
            return Integer.MAX_VALUE;
        }
        if (metaBoard[0][0] == opponent && metaBoard[1][1] == opponent && metaBoard[2][2] == opponent) {
            return -Integer.MAX_VALUE;
        }
        if (metaBoard[0][2] == opponent && metaBoard[1][1] == opponent && metaBoard[2][0] == opponent) {
            return -Integer.MAX_VALUE;
        }

        // Evaluate potential global wins
        int score = 0;
        for (int i = 0; i < 3; i++) {
            int rowMark = 0, rowOpp = 0, colMark = 0, colOpp = 0;
            for (int j = 0; j < 3; j++) {
                if (metaBoard[i][j] == mark) rowMark++;
                else if (metaBoard[i][j] == opponent) rowOpp++;
                if (metaBoard[j][i] == mark) colMark++;
                else if (metaBoard[j][i] == opponent) colOpp++;
            }
            if (rowMark == 2 && rowOpp == 0) score += 500; // Two local wins, one empty
            if (colMark == 2 && colOpp == 0) score += 500;
            if (rowOpp == 2 && rowMark == 0) score -= 500; // Opponent has two
            if (colOpp == 2 && colMark == 0) score -= 500;

            if (rowMark == 2 && rowOpp == 0) {
                // Two local boards already won
                score += 500;
            } else if (rowMark == 1 && rowOpp == 0) {
                // Only one local board in row i, but no opponent boards there
                score += 200;
            } else if (rowOpp == 2 && rowMark == 0) {
                score -= 500;
            } else if (rowOpp == 1 && rowMark == 0) {
                score -= 200;
            }


            // Column
            if (colMark == 2 && colOpp == 0) {
                score += 500;
            } else if (colMark == 1 && colOpp == 0) {
                score += 200;
            } else if (colOpp == 2 && colMark == 0) {
                score -= 500;
            } else if (colOpp == 1 && colMark == 0) {
                score -= 200;
            }
        }
        int diag1Mark = 0, diag1Opp = 0;
        int diag2Mark = 0, diag2Opp = 0;
        for (int i = 0; i < 3; i++) {
            // Main diagonal: (0,0), (1,1), (2,2)
            if (metaBoard[i][i] == mark) diag1Mark++;
            else if (metaBoard[i][i] == opponent) diag1Opp++;

            // Anti-diagonal: (0,2), (1,1), (2,0)
            if (metaBoard[i][2 - i] == mark) diag2Mark++;
            else if (metaBoard[i][2 - i] == opponent) diag2Opp++;
        }

        if (diag1Mark == 2 && diag1Opp == 0) {
            score += 500;
        } else if (diag1Mark == 1 && diag1Opp == 0) {
            score += 200;
        } else if (diag1Opp == 2 && diag1Mark == 0) {
            score -= 500;
        } else if (diag1Opp == 1 && diag1Mark == 0) {
            score -= 200;
        }

        if (diag2Mark == 2 && diag2Opp == 0) {
            score += 500;
        } else if (diag2Mark == 1 && diag2Opp == 0) {
            score += 200;
        } else if (diag2Opp == 2 && diag2Mark == 0) {
            score -= 500;
        } else if (diag2Opp == 1 && diag2Mark == 0) {
            score -= 200;
        }

        return score;
    }

    public void init(String serverData) {
        String[] values = serverData.split(" ");
        int x = 0, y = 0;
        for (String val : values) {
            board[x][y] = parseMark(Integer.parseInt(val));
            x++;
            if (x == SIZE) {
                x = 0;
                y++;
            }
        }
    }

    // Parses server integer values into Mark enum
    public Mark parseMark(int val) {
        if (val == 4) return Mark.X;
        else if (val == 2) return Mark.O;
        else return Mark.EMPTY;
    }

    // Checks if a local 3x3 grid is won or full
    public boolean isLocalGridComplete(int row, int col) {
        int startRow = row * 3;
        int startCol = col * 3;
        Mark winner = checkSubBoardWinner(startRow, startCol);
        if (winner != Mark.EMPTY) return true;
        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                if (board[i][j] == Mark.EMPTY) return false;
            }
        }
        return true;
    }

    // Checks for a winner in a local 3x3 grid
    public Mark checkSubBoardWinner(int row, int col) {

        //Check rows
        for (int i = row; i < row + 3; i++) {
            if (board[i][col] != Mark.EMPTY && board[i][col] == board[i][col + 1] && board[i][col] == board[i][col + 2]) {
                return board[i][col];
            }
        }

        //Check columns
        for (int j = col; j < col + 3; j++) {
            if (board[row][j] != Mark.EMPTY && board[row][j] == board[row + 1][j] && board[row][j] == board[row + 2][j]) {
                return board[row][j];
            }
        }

        //Check diagonal
        if (board[row][col] != Mark.EMPTY && board[row][col] == board[row + 1][col + 1] && board[row][col] == board[row + 2][col + 2]) {
            return board[row][col];
        }

        //Check diagonal
        if (board[row][col + 2] != Mark.EMPTY && board[row][col + 2] == board[row + 1][col + 1] && board[row][col + 2] == board[row + 2][col]) {
            return board[row][col + 2];
        }
        return Mark.EMPTY;
    }

    // Returns all available moves across the entire board
    public ArrayList<Move> getAvailableMoves() {
        ArrayList<Move> moves = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == Mark.EMPTY) {
                    moves.add(new Move(i, j));
                }
            }
        }
        return moves;
    }

    public ArrayList<Move> checkSubGridMoves() {
        ArrayList<Move> validMoves = new ArrayList<>();

        if (lastRow == -1 || lastCol == -1) {
            // First move: all moves are allowed
            return getAvailableMoves();
        }

        // Determine subgrid to play in based on last move
        int targetGridRow = lastRow % 3;
        int targetGridCol = lastCol % 3;
        int startRow = targetGridRow * 3;
        int startCol = targetGridCol * 3;

        if (!isLocalGridComplete(targetGridRow, targetGridCol)) {
            // Target subgrid is valid, return moves only in that 3x3 area
            collectEmptyCells(startRow, startCol, validMoves);
        } else {
            // Subgrid is full or won, player can choose from all other playable subgrids
            for (int localRow = 0; localRow < 3; localRow++) {
                for (int localCol = 0; localCol < 3; localCol++) {
                    if (!isLocalGridComplete(localRow, localCol)) {
                        collectEmptyCells(localRow * 3, localCol * 3, validMoves);
                    }
                }
            }
        }

        return validMoves;
    }

    private void collectEmptyCells(int startRow, int startCol, ArrayList<Move> moves) {
        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                if (board[i][j] == Mark.EMPTY) {
                    moves.add(new Move(i, j));
                }
            }
        }
    }

    public void displayBoard() {
        System.out.println("\n  A B C | D E F | G H I");
        for (int i = SIZE - 1; i >= 0; i--) {
            if ((i + 1) % 3 == 0 && i != SIZE - 1) {
                System.out.println("  ------+-------+------");
            }

            System.out.print((i + 1) + " ");
            for (int j = 0; j < SIZE; j++) {
                if (j % 3 == 0 && j != 0) {
                    System.out.print("| ");
                }

                char symbol;
                switch (board[i][j]) {
                    case X:
                        symbol = 'X';
                        break;
                    case O:
                        symbol = 'O';
                        break;
                    default:
                        symbol = '.';
                }

                System.out.print(symbol + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}