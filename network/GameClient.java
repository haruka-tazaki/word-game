package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import ui.GameUI;
import util.WordLoader;
import javax.swing.SwingUtilities;

public class GameClient {
    private final String host;
    private final int port;
    private final Set<String> validWords;
    private volatile boolean gameEnded = false;
    
    private final GameUI ui;
    private PrintWriter out;
    private String lastGuess;

    public GameClient(String host, int port, GameUI ui) {
        this.host = host;
        this.port = port;
        this.ui = ui;
        this.validWords = loadWordSet("wordles.txt");
    }

    public void start() {
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            this.out = new PrintWriter(socket.getOutputStream(), true);
            ui.setStatus("Connected to server. Waiting for opponent...");

            String line;
            while ((line = in.readLine()) != null && !gameEnded) {
                final String currentLine = line;

                if (currentLine.startsWith("[opponent]")) {
                    String[] parts = currentLine.substring(11).split(" ", 2);
                    String opponentGuess = parts[0];
                    String opponentHint = parts.length > 1 ? parts[1] : "";
                    ui.updateOpponentGrid(opponentGuess, opponentHint);
                
                } else if (currentLine.startsWith("YOU WIN")) {
                    gameEnded = true;
                    // 正解したので、UIが理解できる「完璧なヒント」を作成して渡す
                    String perfectHint = "correct position: [0, 1, 2, 3, 4]";
                    ui.updateMyGrid(lastGuess, perfectHint); 
                    // その後、サーバーからのメッセージでゲーム終了を通知する
                    ui.setGameEnded(currentLine);
                    
                } else if (currentLine.startsWith("YOU LOSE")) {
                    gameEnded = true;
                    ui.setGameEnded(currentLine);

                } else if (currentLine.startsWith("correct position")) {
                    ui.updateMyGrid(lastGuess, currentLine);
                } else {
                    ui.setStatus(currentLine);
                }
            }
        } catch (IOException e) {
            if (!gameEnded) {
                ui.setGameEnded("Connection to server lost.");
            }
        }
        // 不要かつ問題の原因となるfinallyブロックは削除しました
    }
    
    public void sendGuess(String guess) {
        if (gameEnded) return;

        if (guess == null || guess.length() != 5) {
            ui.setStatus("Please enter exactly 5 letters.");
            return;
        }

        if (!validWords.contains(guess)) {
            ui.setStatus("'" + guess + "' is not in the word list.");
            return;
        }
        
        this.lastGuess = guess;
        out.println(guess);
        ui.setStatus("Guess sent. Waiting for hint...");
    }

    private Set<String> loadWordSet(String filename) {
        try {
            String[] words = WordLoader.loadWords(filename);
            return new HashSet<>(Arrays.asList(words));
        } catch (IOException e) {
            System.err.println("Failed to load words.txt: " + e.getMessage());
            ui.setGameEnded("Error: Could not load word file.");
            return new HashSet<>();
        }
    }

    public static void main(String[] args) {
        String host = (args.length > 0) ? args[0] : "localhost";
        int port = (args.length > 1) ? Integer.parseInt(args[1]) : 3000;
        
        SwingUtilities.invokeLater(() -> new GameUI(host, port));
    }
}