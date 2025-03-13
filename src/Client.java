import java.io.*;
import java.net.*;


class Client {

    static Socket MyClient;
    static BufferedInputStream input;
    static BufferedOutputStream output;
    static int[][] board = new int[9][9];
    static Board CPUBoard = new Board();
    static Mark currentMark = Mark.EMPTY;
    static CPUPlayer cpu = null;
    static char START_AS_X = '1';
    static char START_AS_O = '2';
    static char NEXT_MOVE = '3';
    static char INVALID_MOVE = '4';
    static char GAME_OVER = '5';
    static String MINIMAX = "minimax";
    static String ALPHA_BETA = "alphabeta";
    static String Strategy = ALPHA_BETA;

    public static void main(String[] args) {
        try {
            MyClient = new Socket("localhost", 8888);
            input = new BufferedInputStream(MyClient.getInputStream());
            output = new BufferedOutputStream(MyClient.getOutputStream());
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                char cmd = 0;
                cmd = (char) input.read();
                System.out.println(cmd);

                // Debut de la partie en joueur X
                if (cmd == START_AS_X) {
                    readGameState();
                    handleStartAsX();
                }
                // Debut de la partie en joueur O (Awaits)
                if (cmd == START_AS_O) {
                    readGameState();
                    handleStartAsO();
                }

                // Le serveur demande le prochain coup
                if (cmd == NEXT_MOVE) {
                    handleNextMove();
                }
                // Le dernier coup est invalide
                if (cmd == INVALID_MOVE) {
                    handleInvalidMove();
                }
                // La partie est terminée
                if (cmd == GAME_OVER) {
                    String s = readMessage();
                    System.out.println("Partie Terminé. Le dernier coup joué est: " + s.substring(0, 3));
                    String move = null;
                    move = console.readLine();
                    output.write(move.getBytes(), 0, move.length());
                    output.flush();
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }

    }

    private static void handleStartAsX() throws IOException {
        System.out.println("Nouvelle partie! Vous jouer X, c'est à vous de commencer");
        // TODO: Implement the logic for the first move
        Move firstMove = new Move(4, 4);
        sendMove(firstMove);
        CPUBoard.play(firstMove, Mark.X);
        currentMark = Mark.X;
        cpu = new CPUPlayer(currentMark);
    }

    private static void handleStartAsO() throws IOException {
        System.out.println("Nouvelle partie! Vous jouer O, attendez le coup des X");
        currentMark = Mark.O;
        cpu = new CPUPlayer(currentMark);
    }

    private static void handleInvalidMove() throws IOException {
        System.out.println("Coup invalide, recalcul en cours...");

        if (!cpu.isHistoryEmpty()) {
            Move invalidMove = cpu.undoMove();
            System.out.println("Retrait du coup invalide : " + invalidMove);
        }

        Move bestMove;
        do {
            if (Strategy.equals(MINIMAX)) {
                bestMove = cpu.getNextMoveMinMax(CPUBoard).get(0);
            } else {
                bestMove = cpu.getNextMoveAB(CPUBoard).get(0);
            }
        } while (!isValidMove(bestMove));

        CPUBoard.play(bestMove, currentMark);
        cpu.storeMove(bestMove);
        sendMove(bestMove);
    }

    private static void handleNextMove() throws IOException {
        //Reads the last move from the server
        String s = readMessage();
        System.out.println("Dernier coup :" + s.substring(0, 3));

        Move opponentMove = parseMove(s);
        CPUBoard.play(opponentMove, getOpponent(currentMark));

        cpu.storeMove(opponentMove);
        Move bestMove;
        if (Strategy.equals(MINIMAX)) {
            bestMove = cpu.getNextMoveMinMax(CPUBoard).get(0);
        } else {
            bestMove = cpu.getNextMoveAB(CPUBoard).get(0);
        }
        CPUBoard.play(bestMove, currentMark);

        sendMove(bestMove);
    }

    private static void sendMove(Move move) throws IOException {
        System.out.println("AI joue: " + move);
        output.write(move.toString().getBytes(), 0, move.toString().length());
        output.flush();
    }

    private static String readMessage() throws IOException {
        byte[] buffer = new byte[16];
        int size = input.available();
        input.read(buffer, 0, size);
        return new String(buffer);
    }

    private static void readGameState() throws IOException {
        byte[] aBuffer = new byte[1024];

        int size = input.available();
        input.read(aBuffer, 0, size);
        String s = new String(aBuffer).trim();
        System.out.println(s);
        String[] boardValues;
        boardValues = s.split(" ");
        int x = 0, y = 0;
        for (int i = 0; i < boardValues.length; i++) {
            board[x][y] = Integer.parseInt(boardValues[i]);
            x++;
            if (x == 9) {
                x = 0;
                y++;
            }
        }
    }

    private static Move parseMove(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Format invalide. Utiliser A1 - I9.");
        }

        char colChar = input.charAt(1);
        char rowChar = input.charAt(2);

        if (colChar < 'A' || colChar > 'I' || rowChar < '1' || rowChar > '9') {
            throw new IllegalArgumentException("Hors limites. Utiliser A-I et 1-9.");
        }

        int row = rowChar - '1';
        int col = colChar - 'A';

        return new Move(row, col);
    }

    private static Mark getOpponent(Mark mark) {
        return mark == Mark.X ? Mark.O : Mark.X;
    }

    private static boolean isValidMove(Move move) {
        return move != null && move.getRow() >= 0 && move.getRow() < 9 && move.getCol() >= 0 && move.getCol() < 9;
    }
}
