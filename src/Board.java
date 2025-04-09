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
                Mark winner = getLocalWinner(i, j);
                metaBoard[i / 3][j / 3] = winner;
                if (winner == mark) score += VICTORY;
                else if (winner == opponent) score -= DEFEAT;
                score += analyzeLocalBoard(i, j, mark);

                if (i == 3 && j == 3) {
                    if (winner == mark) {
                        score += 50;
                    } else if (winner == opponent) {
                        score -= 50;
                    }
                }
            }
        }

        int metaScore = evaluateMetaBoard(metaBoard, mark);
        if (metaScore == Integer.MAX_VALUE || metaScore == -Integer.MAX_VALUE) {
            return metaScore;
        }
        score += metaScore;
        score += penalizeDangerZones(metaBoard, mark);

        return score;
    }

    private int penalizeDangerZones(Mark[][] metaBoard, Mark player) {
        int penalty = 0;
        Mark foe = (player == Mark.X) ? Mark.O : Mark.X;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (metaBoard[row][col] == Mark.EMPTY && opponentThreatExists(row, col, foe)) {
                    penalty -= 50;
                }
            }
        }

        return penalty;
    }

    private boolean opponentThreatExists(int localGridRow, int localGridCol, Mark opponent) {
        int baseRow = localGridRow * 3;
        int baseCol = localGridCol * 3;

        for (int offset = 0; offset < 3; offset++) {
            if (couldBeWin(opponent,
                    board[baseRow + offset][baseCol],
                    board[baseRow + offset][baseCol + 1],
                    board[baseRow + offset][baseCol + 2])) {
                return true;
            }

            if (couldBeWin(opponent,
                    board[baseRow][baseCol + offset],
                    board[baseRow + 1][baseCol + offset],
                    board[baseRow + 2][baseCol + offset])) {
                return true;
            }
        }

        return couldBeWin(opponent,
                board[baseRow][baseCol],
                board[baseRow + 1][baseCol + 1],
                board[baseRow + 2][baseCol + 2])
                || couldBeWin(opponent,
                board[baseRow][baseCol + 2],
                board[baseRow + 1][baseCol + 1],
                board[baseRow + 2][baseCol]);
    }


    private boolean couldBeWin(Mark player, Mark first, Mark second, Mark third) {
        return (first == player && second == player && third == Mark.EMPTY)
                || (first == player && third == player && second == Mark.EMPTY)
                || (second == player && third == player && first == Mark.EMPTY);
    }



    private int analyzeLocalBoard(int row, int col, Mark mark) {
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

        // Check for immediate win or loss
        if (hasGlobalWin(metaBoard, mark)) return Integer.MAX_VALUE;
        if (hasGlobalWin(metaBoard, opponent)) return -Integer.MAX_VALUE;

        int score = 0;

        for (int index = 0; index < 3; index++) {
            int markRow = 0, oppRow = 0, markCol = 0, oppCol = 0;

            for (int j = 0; j < 3; j++) {
                // Row evaluation
                if (metaBoard[index][j] == mark) markRow++;
                else if (metaBoard[index][j] == opponent) oppRow++;

                // Column evaluation
                if (metaBoard[j][index] == mark) markCol++;
                else if (metaBoard[j][index] == opponent) oppCol++;
            }

            score += assessLine(markRow, oppRow);
            score += assessLine(markCol, oppCol);
        }

        // Diagonal evaluations
        int mainDiagMark = 0, mainDiagOpp = 0;
        int antiDiagMark = 0, antiDiagOpp = 0;

        for (int i = 0; i < 3; i++) {
            if (metaBoard[i][i] == mark) mainDiagMark++;
            else if (metaBoard[i][i] == opponent) mainDiagOpp++;

            if (metaBoard[i][2 - i] == mark) antiDiagMark++;
            else if (metaBoard[i][2 - i] == opponent) antiDiagOpp++;
        }

        score += assessLine(mainDiagMark, mainDiagOpp);
        score += assessLine(antiDiagMark, antiDiagOpp);

        return score;
    }


    private boolean hasGlobalWin(Mark[][] grid, Mark player) {
        // Check rows and columns
        for (int i = 0; i < 3; i++) {
            if (grid[i][0] == player && grid[i][1] == player && grid[i][2] == player) return true;
            if (grid[0][i] == player && grid[1][i] == player && grid[2][i] == player) return true;
        }
        // Check diagonals
        return (grid[0][0] == player && grid[1][1] == player && grid[2][2] == player)
                || (grid[0][2] == player && grid[1][1] == player && grid[2][0] == player);
    }

    private int assessLine(int playerMarks, int opponentMarks) {
        if (playerMarks == 2 && opponentMarks == 0) return 500;
        if (playerMarks == 1 && opponentMarks == 0) return 200;
        if (opponentMarks == 2 && playerMarks == 0) return -500;
        if (opponentMarks == 1 && playerMarks == 0) return -200;
        return 0;
    }


    public void setUpBoard(String serverData) {
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

    public Mark parseMark(int val) {
        return switch (val) {
            case 4 -> Mark.X;
            case 2 -> Mark.O;
            default -> Mark.EMPTY;
        };
    }

    // Checks if a local 3x3 grid is won or full
    public boolean isLocalGridNotComplete(int row, int col) {
        int startRow = row * 3;
        int startCol = col * 3;
        Mark winner = getLocalWinner(startRow, startCol);
        if (winner != Mark.EMPTY) return false;
        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                if (board[i][j] == Mark.EMPTY) return true;
            }
        }
        return false;
    }

    // Checks for a winner in a local 3x3 grid
    public Mark getLocalWinner(int row, int col) {

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
            return getAvailableMoves();
        }

        int targetGridRow = lastRow % 3;
        int targetGridCol = lastCol % 3;
        int startRow = targetGridRow * 3;
        int startCol = targetGridCol * 3;

        if (isLocalGridNotComplete(targetGridRow, targetGridCol)) {
            collectEmptyCells(startRow, startCol, validMoves);
        } else {
            for (int localRow = 0; localRow < 3; localRow++) {
                for (int localCol = 0; localCol < 3; localCol++) {
                    if (isLocalGridNotComplete(localRow, localCol)) {
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