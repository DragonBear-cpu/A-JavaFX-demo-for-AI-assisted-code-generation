package demo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeExtractor {

    private static final Pattern CODE_BLOCK =
            Pattern.compile("```(?:java)?([\\s\\S]*?)```");

    public static String extractJava(String text) {
        Matcher m = CODE_BLOCK.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return text;
    }
}
