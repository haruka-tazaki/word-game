//GameLogic.java
//ヒント作成など

package game;

import java.util.ArrayList;
import java.util.List;

public class GameLogic {

    //正解位置のインデックス一覧を返す    
    public static List<Integer> findCorrectPositions(String answer, String guess) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (answer.charAt(i) == guess.charAt(i)) {
                positions.add(i);
            }
        }
        return positions;
    }

    //正解に含まれる文字を返す
    public static List<Character> findIncludedLetters(String answer, String guess) {
        List<Character> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            char ans = answer.charAt(i);
            for (int j = 0; j < 5; j++){
                char gue = guess.charAt(j);
                if (ans == gue) {
                    list.add(gue);
                }
            }
        }
        return list;
    }

}