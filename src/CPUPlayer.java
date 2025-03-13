import java.util.ArrayList;
import java.util.Stack;

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
    private Stack<Move> moveHistory;

    // Le constructeur reçoit en paramètre le
    // joueur MAX (X ou O)
    public CPUPlayer(Mark cpu) {
        this.max = cpu;
        this.min = cpu.equals(Mark.X) ? Mark.O : Mark.X;
        this.moveHistory = new Stack<>();
    }

    // Ne pas changer cette méthode
    public int getNumOfExploredNodes() {
        return numExploredNodes;
    }

    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
//    public ArrayList<Move> getNextMoveMinMax(Board board) {
//        numExploredNodes = 0;
//        ArrayList<Move> bestMoves = new ArrayList<>();
//        int bestScore = Integer.MIN_VALUE;
//        Move[] possibleMoves = board.getPossibleMoves();
//
//        for (Move move : possibleMoves) {
//            board.play(move, this.max);
//            int score = minimax(board, this.min);
//
//            if (score > bestScore) {
//                bestScore = score;
//                bestMoves.clear();
//                bestMoves.add(move);
//            } else if (score == bestScore) {
//                bestMoves.add(move);
//            }
//
//            board.undo(move);
//        }
//
//        return bestMoves;
//    }

//    public int minimax(Board board, Mark player) {
//        numExploredNodes++;
//        int evaluation = board.evaluate(this.max);
//
//        if (evaluation != Board.CONTINUE) {
//            return evaluation;
//        }
//
//        Move[] possibleMoves = board.getPossibleMoves();
//
//        if (player == this.max) {
//            int maxScore = Integer.MIN_VALUE;
//
//            for (Move move : possibleMoves) {
//                board.play(move, player);
//                int score = minimax(board, this.min);
//                maxScore = Math.max(maxScore, score);
//
//                board.undo(move);
//            }
//
//            return maxScore;
//        } else if (player == this.min) {
//            int minScore = Integer.MAX_VALUE;
//
//            for (Move move : possibleMoves) {
//                board.play(move, player);
//                int score = minimax(board, this.max);
//                minScore = Math.min(minScore, score);
//
//                board.undo(move);
//            }
//
//            return minScore;
//        }
//
//        return -1; // Il y a eu une erreur quelque part (add falloff)
//    }

    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public ArrayList<Move> getNextMoveAB(Board board) {
        numExploredNodes = 0;
        ArrayList<Move> bestMoves = new ArrayList<>();
        int bestScore = Integer.MIN_VALUE;
        Move lastMove = moveHistory.isEmpty() ? new Move(4, 4) : moveHistory.peek();
        Move[] possibleMoves = board.getPossibleMoves(lastMove.getRow(), lastMove.getCol());
        int maxDepth = 10;
        for (Move move : possibleMoves) {
            board.play(move, this.max);
            int score = alphaBeta(board, this.min, Integer.MIN_VALUE, Integer.MAX_VALUE, move.getRow(), move.getCol(), 0, maxDepth);
            board.undo(move);

            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == bestScore) {
                bestMoves.add(move);
            }
        }
        if (bestMoves.get(0) != null) {
            moveHistory.push(bestMoves.get(0));
        }
        return bestMoves;
    }

    public Move getLastMove() {
        return moveHistory.isEmpty() ? null : moveHistory.peek();
    }

    public Move undoMove() {
        return moveHistory.pop();
    }

    public void storeMove(Move move) {
        moveHistory.push(move);
    }

    public boolean isHistoryEmpty() {
        return moveHistory.isEmpty();
    }

    public int alphaBeta(Board board, Mark player, int alpha, int beta, int lastRow, int lastCol, int depth, int maxDepth) {
        numExploredNodes++;

        // Stop searching if we reach max depth
        if (depth >= maxDepth) {
            return board.evaluate(this.max);  // Use heuristic evaluation at max depth
        }

        int evaluation = board.evaluate(this.max);
        if (evaluation == Board.VICTORY || evaluation == Board.DEFEAT) {
            return evaluation;
        }

        Move[] possibleMoves = board.getPossibleMoves(lastRow, lastCol);

        if (player == this.max) {
            int bestValue = Integer.MIN_VALUE;

            for (Move move : possibleMoves) {
                board.play(move, player);
                int score = alphaBeta(board, this.min, alpha, beta, move.getRow(), move.getCol(), depth + 1, maxDepth);
                board.undo(move);

                bestValue = Math.max(bestValue, score);
                alpha = Math.max(alpha, bestValue);

                if (alpha >= beta) {
                    break; // Beta cutoff
                }
            }
            return bestValue;
        } else {
            int bestValue = Integer.MAX_VALUE;

            for (Move move : possibleMoves) {
                board.play(move, player);
                int score = alphaBeta(board, this.max, alpha, beta, move.getRow(), move.getCol(), depth + 1, maxDepth);
                board.undo(move);

                bestValue = Math.min(bestValue, score);
                beta = Math.min(beta, bestValue);

                if (beta <= alpha) {
                    break;
                }
            }
            return bestValue;
        }
    }
}
