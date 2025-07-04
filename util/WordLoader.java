package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class WordLoader {
    public static String[] loadWords(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename));
        return lines.toArray(new String[0]);
    }
}
