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

                    System.out.println("Nouvelle partie! Vous jouer X, entrez votre premier coup : ");
                    String move = null;
                    move = console.readLine();
                    Move clientMove = parseMove(move);
                    output.write(move.getBytes(), 0, move.length());
                    output.flush();

                    // Initiates new board and adds our own move
                    clientBoard.play(clientMove, Mark.X);
                    currentMark = Mark.X;
                }
                // Debut de la partie en joueur O (Awaits)
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

                    // Initiating the board and start as O
                    currentMark = Mark.O;
                }


                // Le serveur demande le prochain coup
                // Le message contient aussi le dernier coup joue.
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
                    } catch (IllegalArgumentException e) {
                        System.out.println("Mouvement invalide: " + e.getMessage());
                    }
                    // Evaluate with params of clientMove row and col
                    // If board is already full then the choice is arbitrary
                    // Move newMove = AlphaBeta
                    System.out.println("Entrez votre coup : ");
                    // Also update the board
                    String move = null;
                    move = console.readLine();

                    // Client Play
                    try {
                        Move clientMove = parseMove(move);
                        clientBoard.play(clientMove, currentMark);
                        System.out.println(clientMove);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Mouvement invalide: " + e.getMessage());
                    }
                    // move = newMove (but in bytes)
                    output.write(move.getBytes(), 0, move.length());
                    output.flush();

                }
                // Le dernier coup est invalide
                if (cmd == '4') {
                    System.out.println("Coup invalide, entrez un nouveau coup : ");
                    // Redo steps for "3"
                    String move = null;
                    move = console.readLine();

                    // Client Play
                    try {
                        Move clientMove = parseMove(move);
                        clientBoard.play(clientMove, currentMark);
                        System.out.println(clientMove);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Mouvement invalide: " + e.getMessage());
                    }
                    output.write(move.getBytes(), 0, move.length());
                    output.flush();

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
}
