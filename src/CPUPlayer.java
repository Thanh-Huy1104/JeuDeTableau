import java.util.ArrayList;

// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
// Vous pouvez par contre ajouter d'autres méthodes (ça devrait
// être le cas)
class CPUPlayer {

    // Contient le nombre de noeuds visités (le nombre
    // d'appel à la fonction MinMax ou Alpha Beta)
    // Normalement, la variable devrait être incrémentée
    // au début de votre MinMax ou Alpha Beta.
    private int numExploredNodes;

    private Mark max;
    private Mark min;

    // Le constructeur reçoit en paramètre le
    // joueur MAX (X ou O)
    public CPUPlayer(Mark cpu) {
        this.max = cpu;
        this.min = cpu.equals(Mark.X) ? Mark.O : Mark.X;
    }

    // Ne pas changer cette méthode
    public int getNumOfExploredNodes() {
        return numExploredNodes;
    }

    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public ArrayList<Move> getNextMoveMinMax(Board board) {
        numExploredNodes = 0;
        ArrayList<Move> bestMoves = new ArrayList<>();
        int bestScore = Integer.MIN_VALUE;
        Move[] possibleMoves = board.getPossibleMoves();

        for (Move move : possibleMoves) {
            board.play(move, this.max);
            int score = minimax(board, this.min);

            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == bestScore) {
                bestMoves.add(move);
            }

            board.undo(move);
        }

        return bestMoves;
    }

    public int minimax(Board board, Mark player) {
        numExploredNodes++;
        int evaluation = board.evaluate(this.max);

        if (evaluation != Board.CONTINUE) {
            return evaluation;
        }

        Move[] possibleMoves = board.getPossibleMoves();

        if (player == this.max) {
            int maxScore = Integer.MIN_VALUE;

            for (Move move : possibleMoves) {
                board.play(move, player);
                int score = minimax(board, this.min);
                maxScore = Math.max(maxScore, score);

                board.undo(move);
            }

            return maxScore;
        } else if (player == this.min) {
            int minScore = Integer.MAX_VALUE;

            for (Move move : possibleMoves) {
                board.play(move, player);
                int score = minimax(board, this.max);
                minScore = Math.min(minScore, score);

                board.undo(move);
            }

            return minScore;
        }

        return -1; // Il y a eu une erreur quelque part (add falloff)
    }

    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public ArrayList<Move> getNextMoveAB(Board board) {
        numExploredNodes = 0;
        ArrayList<Move> bestMoves = new ArrayList<>();
        int bestScore = Integer.MIN_VALUE;
        Move[] possibleMoves = board.getPossibleMoves();

        for (Move move : possibleMoves) {
            board.play(move, this.max);
            int score = alphaBeta(board, this.min, Integer.MIN_VALUE, Integer.MAX_VALUE);

            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == bestScore) {
                bestMoves.add(move);
            }

            board.undo(move);
        }

        return bestMoves;
    }

    public int alphaBeta(Board board, Mark player, int alpha, int beta) {
        numExploredNodes++;
        int evaluation = board.evaluate(this.max);

        if (evaluation != Board.CONTINUE) {
            return evaluation;
        }

        Move[] possibleMoves = board.getPossibleMoves();

        if (player == this.max) {
            int alphaTemp = Integer.MIN_VALUE;

            for (Move move : possibleMoves) {
                board.play(move, player);
                int score = alphaBeta(board, this.min, Math.max(alphaTemp, alpha), beta);
                alphaTemp = Math.max(alphaTemp, score);

                if (alphaTemp >= beta) {
                    board.undo(move);
                    return alphaTemp;
                }

                board.undo(move);
            }

            return alphaTemp;
        } else if (player == this.min) {
            int betaTemp = Integer.MAX_VALUE;

            for (Move move : possibleMoves) {
                board.play(move, player);
                int score = alphaBeta(board, this.max, alpha, Math.min(betaTemp, beta));
                betaTemp = Math.min(betaTemp, score);

                if (betaTemp <= alpha) {
                    board.undo(move);
                    return betaTemp;
                }

                board.undo(move);
            }

            return betaTemp;
        }

        return -1;
    }
}
