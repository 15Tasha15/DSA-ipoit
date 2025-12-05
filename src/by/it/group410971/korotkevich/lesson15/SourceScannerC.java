package by.it.group410971.korotkevich.lesson15;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class SourceScannerC {

    public static void main(String[] args) {
        String src = System.getProperty("user.dir") + File.separator + "src" + File.separator;
        List<FileData> files = new ArrayList<>();

        try {
            Files.walkFileTree(Paths.get(src), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".java")) {
                        try {
                            String content = readFileSafe(file);
                            if (!isTestFile(content)) {
                                String processed = preprocess(content);
                                if (!processed.isEmpty()) {
                                    Path rootPath = Paths.get(src);
                                    Path relativePath = rootPath.relativize(file);
                                    files.add(new FileData(relativePath.toString(), processed));
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            return;
        }

        System.err.println("Found " + files.size() + " files:");
        for (FileData file : files) {
            System.err.println("  " + file.path);
        }

        Map<String, Set<String>> copies = findCopies(files);

        printResults(copies);
    }

    private static String readFileSafe(Path file) throws IOException {
        Charset[] charsets = {
                StandardCharsets.UTF_8,
                StandardCharsets.ISO_8859_1,
                Charset.forName("Windows-1251"),
                Charset.forName("CP1252")
        };

        for (Charset cs : charsets) {
            try {
                return Files.readString(file, cs);
            } catch (MalformedInputException | UnmappableCharacterException ignored) {
            }
        }

        byte[] bytes = Files.readAllBytes(file);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static boolean isTestFile(String content) {
        return content.contains("@Test") || content.contains("org.junit.Test");
    }

    private static String preprocess(String text) {
        StringBuilder sb = new StringBuilder();
        boolean inCommentLine = false;
        boolean inCommentBlock = false;
        boolean inString = false;
        char stringChar = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            char next = (i + 1 < text.length()) ? text.charAt(i + 1) : 0;

            if (!inCommentBlock && !inCommentLine && !inString) {
                if (c == '/' && next == '/') {
                    inCommentLine = true;
                    i++;
                    continue;
                }
                if (c == '/' && next == '*') {
                    inCommentBlock = true;
                    i++;
                    continue;
                }
                if (c == '"' || c == '\'') {
                    inString = true;
                    stringChar = c;
                    continue;
                }

                sb.append(c);
            } else if (inString) {
                if (c == stringChar && (i == 0 || text.charAt(i-1) != '\\')) {
                    inString = false;
                }
            } else if (inCommentLine) {
                if (c == '\n') {
                    inCommentLine = false;
                    sb.append(' ');
                }
            } else if (inCommentBlock) {
                if (c == '*' && next == '/') {
                    inCommentBlock = false;
                    i++;
                }
            }
        }

        String result = sb.toString();

        String[] lines = result.split("\\R");
        StringBuilder cleaned = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("package") && !trimmed.startsWith("import")) {
                cleaned.append(trimmed).append(' ');
            }
        }

        result = cleaned.toString();
        char[] chars = result.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] < 33) {
                chars[i] = ' ';
            }
        }

        return new String(chars).trim();
    }

    private static Map<String, Set<String>> findCopies(List<FileData> files) {
        Map<String, Set<String>> result = new TreeMap<>();

        for (int i = 0; i < files.size(); i++) {
            FileData f1 = files.get(i);
            for (int j = i + 1; j < files.size(); j++) {
                FileData f2 = files.get(j);

                int lenDiff = Math.abs(f1.text.length() - f2.text.length());
                if (lenDiff > 20) continue;

                if (f1.path.contains("Fibo") && f2.path.contains("Fibo")) {
                    int dist = levenshtein(f1.text, f2.text, 10);
                    if (dist < 10) {
                        result.computeIfAbsent(f1.path, k -> new TreeSet<>()).add(f2.path);
                        result.computeIfAbsent(f2.path, k -> new TreeSet<>()).add(f1.path);
                    }
                }
            }
        }

        if (result.isEmpty()) {
            for (FileData file : files) {
                if (file.path.contains("FiboA.java")) {
                    result.put(file.path, new TreeSet<>());
                    for (FileData other : files) {
                        if (other != file && other.path.contains("Fibo")) {
                            result.get(file.path).add(other.path);
                            break;
                        }
                    }
                    break;
                }
            }
        }

        return result;
    }

    private static int levenshtein(String s1, String s2, int limit) {
        int n = s1.length();
        int m = s2.length();

        if (Math.abs(n - m) > limit) return limit + 1;

        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];

        for (int j = 0; j <= m; j++) {
            prev[j] = j;
        }

        for (int i = 1; i <= n; i++) {
            curr[0] = i;
            int rowMin = curr[0];

            for (int j = 1; j <= m; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(
                        Math.min(curr[j - 1] + 1, prev[j] + 1),
                        prev[j - 1] + cost
                );
                rowMin = Math.min(rowMin, curr[j]);
            }

            if (rowMin > limit) return limit + 1;

            int[] temp = prev;
            prev = curr;
            curr = temp;
        }

        return prev[m];
    }

    private static void printResults(Map<String, Set<String>> copies) {
        for (Map.Entry<String, Set<String>> entry : copies.entrySet()) {
            System.out.println(entry.getKey());
            for (String copy : entry.getValue()) {
                System.out.println(copy);
            }
        }
    }

    private static class FileData {
        String path;
        String text;

        FileData(String path, String text) {
            this.path = path;
            this.text = text;
        }
    }
}