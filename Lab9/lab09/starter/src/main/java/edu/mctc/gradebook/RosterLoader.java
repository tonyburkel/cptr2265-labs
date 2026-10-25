package edu.mctc.gradebook;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Loads a class roster with scores from a CSV file.
 *
 * This class touches the FILE SYSTEM, so it is a natural dependency to replace
 * with a mock in Lab 10 (for example with Mockito: mock(RosterLoader.class)).
 *
 * Expected CSV columns (header row required): student_id,name,category,score
 * Simple format only: fields may not contain commas or quotes.
 */
public class RosterLoader {

    private static final List<String> REQUIRED = List.of("student_id", "name", "category", "score");

    /** One line of the roster. */
    public record ScoreRecord(String studentId, String name, String category, double score) {
    }

    /**
     * Read the CSV at path.
     *
     * @throws IOException              if the file cannot be read
     * @throws IllegalArgumentException (mentioning the line number) for a missing
     *                                  column, blank field or non-numeric score
     */
    public List<ScoreRecord> load(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("file is empty: " + path);
        }
        List<String> header = Arrays.stream(lines.get(0).split(",", -1)).map(String::strip).toList();
        List<String> missing = REQUIRED.stream().filter(c -> !header.contains(c)).toList();
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("missing column(s): " + String.join(", ", missing));
        }
        int idCol = header.indexOf("student_id");
        int nameCol = header.indexOf("name");
        int catCol = header.indexOf("category");
        int scoreCol = header.indexOf("score");

        List<ScoreRecord> records = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            int lineNo = i + 1;
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(",", -1);
            if (f.length < header.size()) {
                throw new IllegalArgumentException("line " + lineNo + ": too few fields");
            }
            for (String col : REQUIRED) {
                if (f[header.indexOf(col)].isBlank()) {
                    throw new IllegalArgumentException("line " + lineNo + ": blank " + col);
                }
            }
            double score;
            try {
                score = Double.parseDouble(f[scoreCol].strip());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "line " + lineNo + ": score is not a number: '" + f[scoreCol] + "'");
            }
            records.add(new ScoreRecord(
                    f[idCol].strip(),
                    f[nameCol].strip(),
                    f[catCol].strip().toLowerCase(Locale.ROOT),
                    score));
        }
        return records;
    }
}
