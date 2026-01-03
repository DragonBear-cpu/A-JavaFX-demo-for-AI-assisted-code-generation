package demo;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DeepSeekClient {

    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private static final String MODEL = "deepseek-chat";

    public String generateJavaFxCode(String requirement) throws Exception {
        String apiKey = System.getenv("DEEPSEEK_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("未设置环境变量 DEEPSEEK_API_KEY");
        }

        String systemPrompt =
                "你是一名资深 JavaFX 工程师。"
                        + "请根据用户需求输出【可运行的 JavaFX 代码】。"
                        + "只输出 Java 代码，不要解释，不要 Markdown。";

        String jsonBody =
                "{"
                        + "\"model\":\"" + MODEL + "\","
                        + "\"messages\":["
                        + "{\"role\":\"system\",\"content\":\"" + escape(systemPrompt) + "\"},"
                        + "{\"role\":\"user\",\"content\":\"" + escape(requirement) + "\"}"
                        + "],"
                        + "\"temperature\":0.2,"
                        + "\"stream\":false"
                        + "}";

        HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(60000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        String resp = readAll(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());

        if (code < 200 || code >= 300) {
            throw new RuntimeException("请求失败，HTTP " + code + "：\n" + resp);
        }

        String content = parseContent(resp);
        return CodeExtractor.extractJava(content).trim();
    }

    private String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            return sb.toString();
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\n");
    }

    // 简易提取 choices[0].message.content（足够课程 demo）
    private String parseContent(String json) {
        if (json == null) return "";
        String key = "\"content\":\"";
        int idx = json.indexOf(key);
        if (idx < 0) return "";
        int start = idx + key.length();

        StringBuilder sb = new StringBuilder();
        boolean esc = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (esc) {
                if (c == 'n') sb.append('\n');
                else if (c == 't') sb.append('\t');
                else sb.append(c);
                esc = false;
                continue;
            }
            if (c == '\\') { esc = true; continue; }
            if (c == '"') break;
            sb.append(c);
        }
        return sb.toString();
    }
}
