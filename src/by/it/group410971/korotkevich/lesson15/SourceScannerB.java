package by.it.group410971.korotkevich.lesson15;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class SourceScannerB {

    public static void main(String[] args) {
        String src = System.getProperty("user.dir") + File.separator + "src" + File.separator;

        List<FileInfo> fileInfos = new ArrayList<>();

        try {
            Files.walkFileTree(Paths.get(src), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".java")) {
                        processJavaFile(file, fileInfos, src);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileInfos.sort((f1, f2) -> {
            int sizeCompare = Integer.compare(f1.size, f2.size);
            if (sizeCompare != 0) {
                return sizeCompare;
            }
            return f1.relativePath.compareTo(f2.relativePath);
        });

        for (FileInfo info : fileInfos) {
            System.out.println(info.size + " " + info.relativePath);
        }
    }

    private static void processJavaFile(Path file, List<FileInfo> fileInfos, String src) {
        try {
            String content = readFileWithEncodingFallback(file);

            if (content.contains("@Test") || content.contains("org.junit.Test")) {
                return;
            }

            String processedContent = processContent(content);

            String absolutePath = file.toAbsolutePath().toString();
            String relativePath = absolutePath.substring(src.length());

            int size = processedContent.getBytes(StandardCharsets.UTF_8).length;

            fileInfos.add(new FileInfo(size, relativePath));

        } catch (Exception e) {
        }
    }

    private static String readFileWithEncodingFallback(Path file) throws IOException {
        Charset[] charsets = {
                StandardCharsets.UTF_8,
                StandardCharsets.ISO_8859_1,
                Charset.forName("Windows-1251"),
                Charset.forName("CP1252")
        };

        for (Charset charset : charsets) {
            try {
                return Files.readString(file, charset);
            } catch (MalformedInputException e) {
                continue;
            }
        }

        byte[] bytes = Files.readAllBytes(file);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static String processContent(String content) {
        StringBuilder result = new StringBuilder();
        boolean inMultiLineComment = false;
        boolean inString = false;
        char stringDelimiter = '"';
        char prevChar = '\0';

        int i = 0;
        int n = content.length();

        while (i < n) {
            char currentChar = content.charAt(i);

            if (!inMultiLineComment && !inString) {
                if (currentChar == '/' && i + 1 < n && content.charAt(i + 1) == '/') {
                    while (i < n && content.charAt(i) != '\n') {
                        i++;
                    }
                    if (i < n) {
                        result.append('\n');
                    }
                    i++;
                    continue;
                }

                if (currentChar == '/' && i + 1 < n && content.charAt(i + 1) == '*') {
                    inMultiLineComment = true;
                    i += 2;
                    continue;
                }

                if (currentChar == '"' || currentChar == '\'') {
                    inString = true;
                    stringDelimiter = currentChar;
                    result.append(currentChar);
                    i++;
                    continue;
                }

                if (i == 0 || content.charAt(i - 1) == '\n') {
                    if (isPackageOrImport(content, i)) {
                        while (i < n && content.charAt(i) != '\n') {
                            i++;
                        }
                        if (i < n) {
                            i++;
                        }
                        continue;
                    }
                }
            } else if (inMultiLineComment) {
                if (currentChar == '*' && i + 1 < n && content.charAt(i + 1) == '/') {
                    inMultiLineComment = false;
                    i += 2;
                    continue;
                }
                i++;
                continue;
            } else if (inString) {
                if (currentChar == stringDelimiter && prevChar != '\\') {
                    inString = false;
                }
                if (currentChar == '\\' && prevChar != '\\') {
                    prevChar = currentChar;
                    result.append(currentChar);
                    i++;
                    continue;
                }
            }

            if (!inMultiLineComment) {
                result.append(currentChar);
            }

            prevChar = currentChar;
            i++;
        }

        String processed = result.toString();

        processed = trimLowChars(processed);

        processed = removeEmptyLines(processed);

        return processed;
    }

    private static boolean isPackageOrImport(String content, int start) {
        if (start + 6 < content.length()) {
            String nextChars = content.substring(start, start + 7);
            if (nextChars.startsWith("package") &&
                    (start + 7 >= content.length() || Character.isWhitespace(content.charAt(start + 7)))) {
                return true;
            }
        }
        if (start + 5 < content.length()) {
            String nextChars = content.substring(start, start + 6);
            if (nextChars.startsWith("import") &&
                    (start + 6 >= content.length() || Character.isWhitespace(content.charAt(start + 6)))) {
                return true;
            }
        }
        return false;
    }

    private static String trimLowChars(String str) {
        if (str.isEmpty()) return str;

        int start = 0;
        int end = str.length();

        while (start < end && str.charAt(start) < 33) {
            start++;
        }

        while (end > start && str.charAt(end - 1) < 33) {
            end--;
        }

        return str.substring(start, end);
    }

    private static String removeEmptyLines(String str) {
        String[] lines = str.split("\r?\n");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                result.append(line).append("\n");
            }
        }

        if (result.length() > 0 && result.charAt(result.length() - 1) == '\n') {
            result.setLength(result.length() - 1);
        }

        return result.toString();
    }

    static class FileInfo {
        int size;
        String relativePath;

        FileInfo(int size, String relativePath) {
            this.size = size;
            this.relativePath = relativePath;
        }
    }
}