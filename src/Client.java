import java.io.*;
import java.net.*;


class Client {
    public static void main(String[] args) {

        Socket MyClient;
        BufferedInputStream input;
        BufferedOutputStream output;
        int[][] board = new int[9][9];
        Board clientBoard = new Board();
        Mark currentMark = Mark.EMPTY;

        try {
            MyClient = new Socket("localhost", 8888);

            input = new BufferedInputStream(MyClient.getInputStream());
            output = new BufferedOutputStream(MyClient.getOutputStream());
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            while (1 == 1) {
                char cmd = 0;

                cmd = (char) input.read();
                System.out.println(cmd);
                // Debut de la partie en joueur X
                if (cmd == '1') {
                    byte[] aBuffer = new byte[1024];

                    int size = input.available();
                    //System.out.println("size " + size);
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

                    Move move = new Move(4,4);
                    output.write(move.toString().getBytes(), 0, move.toString().length());
                    output.flush();

                    // Initiates new board and adds our own move
                    clientBoard.play(move, Mark.X);
                    currentMark = Mark.X;
                }
                // Debut de la partie en joueur O (Awaits)
                // Nothing to add here
                if (cmd == '2') {
                    System.out.println("Nouvelle partie! Vous jouer O, attendez le coup des X");
                    byte[] aBuffer = new byte[1024];

                    int size = input.available();
                    //System.out.println("size " + size);
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

                    // Start as O
                    currentMark = Mark.O;
                }


                // Le serveur demande le prochain coup
                // Le message contient aussi le dernier coup joue.
                // This is where the main code will be
                if (cmd == '3') {
                    byte[] aBuffer = new byte[16];

                    int size = input.available();
                    System.out.println("size :" + size);
                    input.read(aBuffer, 0, size);

                    String s = new String(aBuffer);
                    System.out.println("Dernier coup :" + s);
                    // Read opponent move && Convert s to a Move class
                    try {
                        Move clientMove = parseMove(s);
                        clientBoard.play(clientMove, getOpponent(currentMark));
                        System.out.println(clientMove);
                        CPUPlayer cpu = new CPUPlayer(currentMark);
                        cpu.storeMove(clientMove);
                        Move bestMove = cpu.getNextMoveAB(clientBoard).get(0);
                        clientBoard.play(bestMove, currentMark);
                        System.out.println(bestMove);
                        output.write(bestMove.toString().getBytes(), 0, bestMove.toString().length());
                        output.flush();
                    } catch (IllegalArgumentException e) {
                        System.out.println("Mouvement invalide: " + e.getMessage());
                    }
                }
                // Le dernier coup est invalide
                if (cmd == '4') {
                    System.out.println("Coup invalide, recalcul en cours...");

                    try {
                        CPUPlayer cpu = new CPUPlayer(currentMark);

                        // 🔹 Remove the last move from history since it's invalid
                        if (!cpu.isHistoryEmpty()) {
                            Move invalidMove = cpu.undoMove();
                            System.out.println("Retrait du coup invalide : " + invalidMove);
                        }

                        Move bestMove;
                        do {
                            bestMove = cpu.getNextMoveAB(clientBoard).get(0);  // Retry AI move
                        } while (!isValidMove(bestMove));  // Keep trying if move is invalid

                        clientBoard.play(bestMove, currentMark);
                        cpu.storeMove(bestMove); // Store the valid move

                        System.out.println("AI joue un nouveau coup : " + bestMove);
                        output.write(bestMove.toString().getBytes(), 0, bestMove.toString().length());
                        output.flush();
                    } catch (IllegalArgumentException e) {
                        System.out.println("Mouvement invalide: " + e.getMessage());
                    }
                }
                // La partie est terminée
                if (cmd == '5') {
                    byte[] aBuffer = new byte[16];
                    int size = input.available();
                    input.read(aBuffer, 0, size);
                    String s = new String(aBuffer);
                    System.out.println("Partie Terminé. Le dernier coup joué est: " + s);
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

    public static Move parseMove(String input) {
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

    public static Mark getOpponent(Mark mark) {
        return mark == Mark.X ? Mark.O : Mark.X;
    }

    private static boolean isValidMove(Move move) {
        return move != null && move.getRow() >= 0 && move.getRow() < 9 && move.getCol() >= 0 && move.getCol() < 9;
    }
}
