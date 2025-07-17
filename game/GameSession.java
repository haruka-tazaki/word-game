package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * GameSession manages a two-player Wordle game session.
 * Ensures that on a correct guess, only one hint (full correct) is sent,
 * preventing duplicate display for the winner.
 */
public class GameSession {
    private final Socket playerA;
    private final Socket playerB;
    private final String answer;
    private final ExecutorService pool = Executors.newFixedThreadPool(2);

    public GameSession(Socket playerA, Socket playerB, String answer) {
        this.playerA = playerA;
        this.playerB = playerB;
        this.answer  = answer;
    }

    public void start() {
        Future<?> taskA = pool.submit(() -> handlePlayer(playerA, playerB));
        Future<?> taskB = pool.submit(() -> handlePlayer(playerB, playerA));
        try {
            taskA.get();
            taskB.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            pool.shutdownNow();
        }
    }

    private void handlePlayer(Socket self, Socket opponent) {
        try (
            BufferedReader in     = new BufferedReader(new InputStreamReader(self.getInputStream()));
            PrintWriter    out    = new PrintWriter(self.getOutputStream(), true);
            PrintWriter    outOpp = new PrintWriter(opponent.getOutputStream(), true)
        ) {
            String guess;
            while ((guess = in.readLine()) != null) {
                // 1) Win check first: avoid sending normal hint on correct guess
                if (guess.equalsIgnoreCase(answer)) {
                    // Send single full-correct hint
                    List<Integer> full = List.of(0,1,2,3,4);
                    out.println("correct position: " + full + " correct letter: []");
                    outOpp.println("[opponent] " + guess
                                  + "  correct position: " + full
                                  + " correct letter: []");
                    // Then send win/lose
                    out.println("YOU WIN!!   The answer was " + answer + ".");
                    outOpp.println("YOU LOSE...   The answer was " + answer + ".");
                    break;
                }

                // 2) Normal hint processing for incorrect guesses
                List<Integer> positions = GameLogic.findCorrectPositions(answer, guess);
                List<Character> included = GameLogic.findIncludedLetters(answer, guess);

                out.println("correct position: " + positions + " correct letter: " + included);
                outOpp.println("[opponent] " + guess
                              + "  correct position: " + positions
                              + " correct letter: " + included);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdownNow();
            try { playerA.close(); } catch (IOException ignored) {}
            try { playerB.close(); } catch (IOException ignored) {}
        }
    }
}