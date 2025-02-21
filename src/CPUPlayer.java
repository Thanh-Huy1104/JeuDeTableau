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

    private Mark cpu;
    private Mark adversaire;

    // Le constructeur reçoit en paramètre le
    // joueur MAX (X ou O)
    public CPUPlayer(Mark cpu) {
        this.cpu = cpu;
        this.adversaire = cpu.equals(Mark.X) ? Mark.O: Mark.X;
    }

    // Ne pas changer cette méthode
    public int getNumOfExploredNodes() {
        return numExploredNodes;
    }

    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public ArrayList<Move> getNextMoveMinMax(Board board) {
        this.numExploredNodes = 0;

        ArrayList<Move> move_possibles = new ArrayList<Move>();
        int maxscore = Integer.MIN_VALUE;

        //Pour chaque case vide, on clone le board, on joue le coup et on appelle la fonction MiniMax
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {

                Board tempBoard = board.clone();
                if (tempBoard.getBoard()[i][j].equals(Mark.EMPTY)) {
                    Move cpuMove = new Move(i, j);
                    tempBoard.play(cpuMove, cpu);
                    int score = MiniMax(adversaire, tempBoard);

                    //On ajoute le coup à la liste si le score est plus grand que le maxscore
                    if (score > maxscore) {
                        maxscore = score;
                        move_possibles.clear();
                        move_possibles.add(cpuMove);
                    } else if (score == maxscore) {
                        move_possibles.add(cpuMove);
                    }
                }
            }
        }
        return move_possibles;
    }

    public int MiniMax(Mark joueur, Board board) {

        int scoreFinale = board.evaluate(cpu);
        this.numExploredNodes += 1;

        if (scoreFinale != 0 || board.full()) {
            return scoreFinale;
        }

        if (joueur.equals(cpu)) {
            int maxscore = Integer.MIN_VALUE;

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {

                    Board tempBoard = board.clone();

                    if (tempBoard.getBoard()[i][j].equals(Mark.EMPTY)) {

                        tempBoard.play(new Move(i, j), joueur);
                        int score = MiniMax(adversaire, tempBoard);
                        maxscore = Math.max(maxscore, score);

                    }
                }
            }
            return maxscore;
        }

        else if (joueur.equals(adversaire)) {
            int minscore = Integer.MAX_VALUE;

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {

                    Board tempBoard = board.clone();
                    if (tempBoard.getBoard()[i][j].equals(Mark.EMPTY)) {

                        tempBoard.play(new Move(i, j), joueur);
                        int score = MiniMax(cpu, tempBoard);
                        minscore = Math.min(minscore, score);

                    }
                }
            }
            return minscore;
        }
        return 0;
    }

    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public ArrayList<Move> getNextMoveAB(Board board) {
        numExploredNodes = 0;

        ArrayList<Move> move_possibles = new ArrayList<Move>();
        int maxscore = Integer.MIN_VALUE;

        //Pour chaque case vide, on clone le board, on joue le coup et on appelle la fonction MiniMaxAlphaBeta
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {

                Board tempBoard = board.clone();
                if (tempBoard.getBoard()[i][j].equals(Mark.EMPTY)) {
                    Move cpuMove = new Move(i, j);
                    tempBoard.play(cpuMove, cpu);
                    int score = MiniMaxAlphaBeta(tempBoard, adversaire, Integer.MIN_VALUE, Integer.MAX_VALUE);

                    //On ajoute le coup à la liste si le score est plus grand que le maxscore
                    if (score > maxscore) {
                        maxscore = score;
                        move_possibles.clear();
                        move_possibles.add(cpuMove);
                    } else if (score == maxscore) {
                        move_possibles.add(cpuMove);
                    }
                }
            }
        }
        return move_possibles;
    }

    public int MiniMaxAlphaBeta(Board board, Mark joueur, int alpha, int beta) {
        int scoreFinale = board.evaluate(cpu);
        this.numExploredNodes += 1;

        if (scoreFinale != 0 || board.full()) {
            return scoreFinale;
        }

        if (joueur.equals(cpu)) {
            int alphaT = Integer.MIN_VALUE;

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {

                    Board tempBoard = board.clone();
                    if (tempBoard.getBoard()[i][j].equals(Mark.EMPTY)) {

                        tempBoard.play(new Move(i, j), joueur);
                        int score = MiniMaxAlphaBeta(tempBoard, adversaire, Math.max(alphaT, alpha), beta);
                        alphaT = Math.max(alphaT, score);
                        if (alphaT >= beta) {
                            return alphaT;
                        }
                    }
                }

            }
            return alphaT;
        }
        else if(joueur.equals(adversaire)){
            int betaT = Integer.MAX_VALUE;

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {

                    Board tempBoard = board.clone();
                    if (tempBoard.getBoard()[i][j].equals(Mark.EMPTY)) {

                        tempBoard.play(new Move(i,j), joueur);
                        int score = MiniMaxAlphaBeta(tempBoard, cpu, alpha, Math.min(betaT, beta));
                        betaT = Math.min(betaT, score);
                        if (betaT <= alpha){
                            return betaT;
                        }
                    }
                }

            }
            return betaT;
        }

        return 0;
    }
}
