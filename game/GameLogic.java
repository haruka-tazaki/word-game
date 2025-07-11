//GameLogic.java
//ヒント作成など

package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    Map<Character, Integer> answerFreq = new HashMap<>();
    for (char c : answer.toCharArray()) {
        answerFreq.put(c, answerFreq.getOrDefault(c, 0) + 1);
    }

    List<Character> result = new ArrayList<>();
    for (char c : guess.toCharArray()) {
        if (answerFreq.getOrDefault(c, 0) > 0) {
            result.add(c);
            answerFreq.put(c, answerFreq.get(c) - 1); // カウントを減らす
        }
    }
    return result;
}
}
