import java.util.ArrayList;

// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
// Vous pouvez par contre ajouter d'autres méthodes (ça devrait 
// être le cas)
class Board {
    private Mark[][] board;

    int[][][] winningPositions = {
            {{0, 0}, {0, 1}, {0, 2}},
            {{1, 0}, {1, 1}, {1, 2}},
            {{2, 0}, {2, 1}, {2, 2}},
            {{0, 0}, {1, 0}, {2, 0}},
            {{0, 1}, {1, 1}, {2, 1}},
            {{0, 2}, {1, 2}, {2, 2}},
            {{0, 0}, {1, 1}, {2, 2}},
            {{0, 2}, {1, 1}, {2, 0}}
    };

    private int COLUMN = 3;
    private int ROW = 3;

    // Ne pas changer la signature de cette méthode
    public Board() {
        this.board = new Mark[3][3];
        for (int i = 0; i < COLUMN; i++) {
            for (int j = 0; j < ROW; j++) {
                board[i][j] = Mark.EMPTY;
            }
        }
    }

    public Mark[][] getBoard() {
        return board;
    }

    public Mark getMark(int row, int col) {
        return board[row][col];
    }

    public Board(Mark[][] board) {
        this.board = board;
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
        int row = m.getRow();
        int col = m.getCol();

        board[row][col] = Mark.EMPTY;
    }


    // retourne  100 pour une victoire
    //          -100 pour une défaite
    //           0   pour un match nul
    // Ne pas changer la signature de cette méthode
    public int evaluate(Mark mark) {

        Mark adversaire = mark.equals(Mark.X) ? Mark.O : Mark.X;

        int score = 0;

        // Verifier victoire pour les lignes
        for (Mark[] row : board) {

            // Verifie pour le joueur
            if (rowWin(row, mark)) {
                return 100;
            }

            // Verifie pour l'adversaire
            if (rowWin(row, adversaire)) {
                return -100;
            }
        }

        // Verifier victoire pour les colonnes
        for (int i = 0; i < 3; i++) {
            Mark[] column = { board[0][i], board[1][i], board[2][i] };

            // Verifie pour le joueur
            if (rowWin(column, mark)) {
                return 100;
            }

            // Verifie pour l'adversaire
            if (rowWin(column, adversaire)) {
                return -100;
            }
        }

        // Verifier victoire pour les diagonales
        Mark[] diag1 = { board[0][0], board[1][1], board[2][2] };
        Mark[] diag2 = { board[0][2], board[1][1], board[2][0] };

        // Verifie pour le joueur
        if (rowWin(diag1, mark) || rowWin(diag2, mark)) {
            return 100;
        }

        // Verifie pour l'adversaire
        if (rowWin(diag1, adversaire) || rowWin(diag2, adversaire)) {
            return -100;
        }

        return score;

    }

    public boolean rowWin(Mark[] row, Mark player) {

        boolean win = true;

        for (Mark mark : row) {
            if (!mark.equals(player))
                return false;
        }

        return win;

    }

    public Board clone() {
        Board newBoard = new Board();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                newBoard.getBoard()[i][j] = board[i][j];
            }
        }
        return newBoard;
    }

    public boolean full() {

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {

                if (board[i][j].equals(Mark.EMPTY)) {
                    return false;
                }
            }
        }
        return true;
    }
}
