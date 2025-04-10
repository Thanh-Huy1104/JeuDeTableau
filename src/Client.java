import java.io.*;
import java.net.*;

import javax.swing.JOptionPane;

// Class handling communication with the server and AI gameplay
class Client {
    static Socket MyClient;
    static char START_AS_X = '1';
    static char START_AS_O = '2';
    static char NEXT_MOVE = '3';
    static char INVALID_MOVE = '4';
    static char GAME_OVER = '5';
    static BufferedInputStream input;
    static BufferedOutputStream output;
    static Board board = new Board();
    static CPUPlayer cpu = new CPUPlayer(Mark.X);

    public static void main(String[] args) {
        try {
           String ip = "localhost";
            int port = 8888;

            if (args.length == 0) {
                ip = JOptionPane.showInputDialog("Entrez l'adresse IP du serveur :", "localhost");
                if (ip == null || ip.isEmpty()) ip = "localhost";
            } else {
                ip = args[0];
            }

            MyClient = new Socket(ip, port);
            input = new BufferedInputStream(MyClient.getInputStream());
            output = new BufferedOutputStream(MyClient.getOutputStream());

            while (true) {
                char cmd = (char) input.read();
                System.out.println("Commande reçue: " + cmd);

                if (cmd == START_AS_X) {
                    cpu = new CPUPlayer(Mark.X);
                    byte[] buffer = new byte[1024];
                    int size = input.available();
                    input.read(buffer, 0, size);
                    String s = new String(buffer).trim();
                    System.out.println("Plateau initial: " + s);
                    board.init(s);
                    Move firstMove = cpu.getBestLocalMove(board, 7);
                    board.play(firstMove, cpu.getMark());
                    String moveString = firstMove.toString();
                    output.write(moveString.getBytes(), 0, moveString.length());
                    output.flush();
                    System.out.println("Premier coup joué: " + moveString);
                }

                if (cmd == START_AS_O) {
                    cpu = new CPUPlayer(Mark.O);
                    System.out.println("Nouvelle partie! Vous jouer O, attendez le coup des X");
                    byte[] aBuffer = new byte[1024];

                    int size = input.available();
                    input.read(aBuffer, 0, size);
                    String s = new String(aBuffer).trim();
                    board.init(s);
                }

                if (cmd == NEXT_MOVE) {
                    byte[] buffer = new byte[16];
                    int size = input.available();
                    input.read(buffer, 0, size);
                    String lastMove = new String(buffer).trim();
                    System.out.println("Dernier coup adverse: " + lastMove);

                    if (!lastMove.equals("A0")) {
                        Move move = parseMove(lastMove);
                        board.play(move, cpu.getOpponentMark());
                    }

                    Move nextMove = cpu.getBestLocalMove(board, 7); // Depth 7 for strength
                    board.play(nextMove, cpu.getMark());
                    String moveString = nextMove.toString();
                    output.write(moveString.getBytes(), 0, moveString.length());
                    output.flush();
                    System.out.println("Coup joué: " + moveString);
                    board.displayBoard();
                }

                if (cmd == INVALID_MOVE) {
                    System.out.println("Coup invalide, rejouez:");
                    Move nextMove = cpu.getBestLocalMove(board, 7);
                    String moveString = nextMove.toString();
                    output.write(moveString.getBytes(), 0, moveString.length());
                    output.flush();
                }

                if (cmd == GAME_OVER) {
                   break;
                }
            }
        } catch (IOException e) {
            System.out.println("Erreur de connexion: " + e.getMessage());
        }
    }

    private static Move parseMove(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Format invalide. Utiliser A1 - I9.");
        }

        char colChar = input.charAt(0);
        char rowChar = input.charAt(1);

        if (colChar < 'A' || colChar > 'I' || rowChar < '1' || rowChar > '9') {
            throw new IllegalArgumentException("Hors limites. Utiliser A-I et 1-9.");
        }

        int row = rowChar - '1';
        int col = colChar - 'A';

        return new Move(row, col);
    }
}