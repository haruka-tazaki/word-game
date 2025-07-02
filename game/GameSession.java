//ゲーム進行

package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class GameSession {
    private final Socket playerA;
    private final Socket playerB;
    private final String answer;

    //コンストラクタ
    public GameSession(Socket playerA, Socket playerB, String answer) {
        this.playerA = playerA;
        this.playerB = playerB;
        this.answer  = answer;
    }

    public void run() {
        try (
          BufferedReader inA  = new BufferedReader(new InputStreamReader(playerA.getInputStream()));
          PrintWriter    outA = new PrintWriter(playerA.getOutputStream(), true);
          BufferedReader inB  = new BufferedReader(new InputStreamReader(playerB.getInputStream()));
          PrintWriter    outB = new PrintWriter(playerB.getOutputStream(), true);
        ) {
            while (true) {//同期している(AとBはお互いに回答しないと次を打てない)
                // プレイヤーAのターン
                String guessA = inA.readLine();
                if (guessA == null || "exit".equalsIgnoreCase(guessA)) {
                    // セッション終了コマンド
                    outA.println("EXIT GAME");
                    outB.println("EXIT GAME");
                    break;
                }
                if (guessA.equalsIgnoreCase(answer)) {
                    outA.println("YOU_WIN");
                    outB.println("YOU_LOSE");
                    break;
                }
                List<Integer> posA = GameLogic.findCorrectPositions(answer, guessA);
                List<Character> charA = GameLogic.findIncludedLetters(answer, guessA);
                outA.println("correct position:" + posA + "correct letter:" + charA);

                // プレイヤーBのターン
                String guessB = inB.readLine();
                if (guessB == null || "exit".equalsIgnoreCase(guessB)) {
                    // セッション終了コマンド
                    outA.println("EXIT GAME");
                    outB.println("EXIT GAME");
                    break;
                }
                if (guessB.equalsIgnoreCase(answer)) {
                    outB.println("YOU_WIN");
                    outA.println("YOU_LOSE");
                    break;
                }
                List<Integer> posB = GameLogic.findCorrectPositions(answer, guessB);
                List<Character> charB = GameLogic.findIncludedLetters(answer, guessB);
                outB.println("correct position:" + posB + "correct letter:" + charB);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // セッション終了時にソケットを閉じる
            try { playerA.close(); } catch (IOException ignored) {}
            try { playerB.close(); } catch (IOException ignored) {}
        }
    }
}
