import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class CPUPlayer {
    private Mark cpu;
    private int numExploredNodes;
    private Random random;

    public CPUPlayer(Mark cpu) {
        this.cpu = cpu;
        this.numExploredNodes = 0;
        this.random = new Random();
    }

    public int getNumOfExploredNodes() {
        return numExploredNodes;
    }

    private int alphaBeta(Board board, int depth, int alpha, int beta, boolean isMax, long startTime) {
        numExploredNodes++;
        if (depth == 0 || board.checkSubGridMoves().isEmpty() || (System.currentTimeMillis() - startTime) > 2500) {
            return board.evaluate(cpu);
        }

        if (isMax) {
            int bestValue = Integer.MIN_VALUE;
            for (Move move : board.checkSubGridMoves()) {
                board.play(move, cpu);
                bestValue = Math.max(bestValue, alphaBeta(board, depth - 1, alpha, beta, false, startTime));
                board.undo(move);
                alpha = Math.max(alpha, bestValue);
                if (beta <= alpha) break;
            }
            return bestValue;
        } else {
            int bestValue = Integer.MAX_VALUE;
            Mark opponent = (cpu == Mark.X) ? Mark.O : Mark.X;
            for (Move move : board.checkSubGridMoves()) {
                board.play(move, opponent);
                bestValue = Math.min(bestValue, alphaBeta(board, depth - 1, alpha, beta, true, startTime));
                board.undo(move);
                beta = Math.min(beta, bestValue);
                if (beta <= alpha) break;
            }
            return bestValue;
        }
    }

    public Move getBestLocalMove(Board board, int maxDepth) {
        ArrayList<Move> validMoves = board.checkSubGridMoves();
        if (validMoves.isEmpty()) {
            return getFirstValidMove(board);
        }

        int bestScore = Integer.MIN_VALUE;
        Move bestMove = null;
        long startTime = System.currentTimeMillis();
        numExploredNodes = 0;

        for (Move move : validMoves) {
            board.play(move, cpu);
            int score = alphaBeta(board, maxDepth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, startTime);
            board.undo(move);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        if (bestMove == null) {
            return getFirstValidMove(board);
        }

        logMove(bestMove, bestScore);

        return bestMove;
    }

    private void logMove(Move move, int score) {
        System.out.println(String.format(
                "IA (%s) joue : %s (Score: %d, Noeuds explorés: %d)",
                cpu, move, score, numExploredNodes));
    }

    public Move getFirstValidMove(Board board) {
        List<Move> moves = board.checkSubGridMoves();

        if (moves.isEmpty()) {
            throw new IllegalStateException("Aucune case valide disponible pour jouer.");
        }

        return moves.get(random.nextInt(moves.size()));
    }


    public Mark getOpponentMark() {
        return cpu == Mark.X ? Mark.O : Mark.X;
    }

    public Mark getMark() {
        return cpu;
    }
}