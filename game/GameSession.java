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

public class GameSession {
   private final Socket playerA;
   private final Socket playerB;
   private final String answer;
   private final ExecutorService pool = Executors.newFixedThreadPool(2);

   public GameSession(Socket var1, Socket var2, String var3) {
      this.playerA = var1;
      this.playerB = var2;
      this.answer = var3;
   }

   public void start() {
      Future var1 = this.pool.submit(() -> {
         this.handlePlayer(this.playerA, this.playerB);
      });
      Future var2 = this.pool.submit(() -> {
         this.handlePlayer(this.playerB, this.playerA);
      });

      try {
         var1.get();
         var2.get();
      } catch (InterruptedException var8) {
         Thread.currentThread().interrupt();
      } catch (ExecutionException var9) {
         var9.printStackTrace();
      } finally {
         this.pool.shutdownNow();
      }
   }

   private void handlePlayer(Socket var1, Socket var2) {
      try {
         BufferedReader var3 = new BufferedReader(new InputStreamReader(var1.getInputStream()));

         try {
            PrintWriter var4 = new PrintWriter(var1.getOutputStream(), true); // Current Player's PrintWriter

            try {
               PrintWriter var5 = new PrintWriter(var2.getOutputStream(), true); // Opponent's PrintWriter

               String var6;
               try {
                  while((var6 = var3.readLine()) != null) {
                     List var7;
                     if (var6.equalsIgnoreCase(this.answer)) {
                        // **CHANGE START**

                        // WINNER'S MESSAGE: No 'correct position' or 'correct letter' feedback.
                        // Their UI should already have rendered their last guess correctly.
                        var4.println("YOU WIN!!");

                        // LOSER'S MESSAGE: Include the opponent's correct guess
                        // and the full green highlight information for their UI.
                        var7 = List.of(0, 1, 2, 3, 4); // All positions correct for the loser's display
                        var5.println("[opponent] " + var6 + "  correct position: " + String.valueOf(var7) + " correct letter: []");
                        var5.println("YOU LOSE... The answer was " + this.answer + ".");

                        // **CHANGE END**
                        break; // Game ends, exit loop
                     }

                     // Normal game progression feedback
                     var7 = GameLogic.findCorrectPositions(this.answer, var6);
                     List var8 = GameLogic.findIncludedLetters(this.answer, var6);
                     String var10001 = String.valueOf(var7);
                     var4.println("correct position: " + var10001 + " correct letter: " + String.valueOf(var8));
                     var5.println("[opponent] " + var6 + "  correct position: " + String.valueOf(var7) + " correct letter: " + String.valueOf(var8));
                  }
               } catch (Throwable var30) {
                  try {
                     var5.close();
                  } catch (Throwable var29) {
                     var30.addSuppressed(var29);
                  }

                  throw var30;
               }

               var5.close();
            } catch (Throwable var31) {
               try {
                  var4.close();
               } catch (Throwable var28) {
                  var31.addSuppressed(var28);
               }

               throw var31;
            }

            var4.close();
         } catch (Throwable var32) {
            try {
               var3.close();
            } catch (Throwable var27) {
               var32.addSuppressed(var27);
            }

            throw var32;
         }

         var3.close();
      } catch (IOException var33) {
         var33.printStackTrace();
      } finally {
         // It's safer to shut down the pool and close sockets outside the try-catch block
         // of the while loop, but within the outer try-catch for handlePlayer,
         // to ensure they are always closed.
         this.pool.shutdownNow();

         try {
            this.playerA.close();
         } catch (IOException var26) {
             // Log or handle
         }

         try {
            this.playerB.close();
         } catch (IOException var25) {
             // Log or handle
         }
      }
   }
}
