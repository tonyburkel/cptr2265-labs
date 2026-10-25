package edu.mctc.gradebook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Pure grading rules: no files, no database, no network.
 *
 * These methods are the easiest place to start writing unit tests. Each one
 * has normal cases, boundary values and error conditions worth testing.
 */
public final class Grading {

    /** Minimum percentage for each letter, from highest to lowest. */
    private static final double[] MINIMUMS = {93, 90, 87, 83, 80, 77, 73, 70, 67, 60, 0};
    private static final String[] LETTERS = {"A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F"};

    private static final Map<String, Double> GRADE_POINTS = new LinkedHashMap<>();
    static {
        GRADE_POINTS.put("A", 4.0);
        GRADE_POINTS.put("A-", 3.67);
        GRADE_POINTS.put("B+", 3.33);
        GRADE_POINTS.put("B", 3.0);
        GRADE_POINTS.put("B-", 2.67);
        GRADE_POINTS.put("C+", 2.33);
        GRADE_POINTS.put("C", 2.0);
        GRADE_POINTS.put("C-", 1.67);
        GRADE_POINTS.put("D+", 1.33);
        GRADE_POINTS.put("D", 1.0);
        GRADE_POINTS.put("F", 0.0);
    }

    private Grading() {
    }

    private static void checkScore(double score) {
        if (Double.isNaN(score) || score < 0 || score > 100) {
            throw new IllegalArgumentException("score must be between 0 and 100, got " + score);
        }
    }

    /**
     * Convert a percentage (0-100 inclusive) to a letter grade.
     *
     * @throws IllegalArgumentException if the score is NaN or out of range
     */
    public static String letterGrade(double score) {
        checkScore(score);
        for (int i = 0; i < MINIMUMS.length; i++) {
            if (score >= MINIMUMS[i]) {
                return LETTERS[i];
            }
        }
        return "F"; // unreachable, kept for safety
    }

    /**
     * Return the 4.0-scale grade points for a letter grade (case-insensitive,
     * surrounding spaces ignored).
     *
     * @throws NullPointerException     if letter is null
     * @throws IllegalArgumentException if the letter is not recognized
     */
    public static double gradePoints(String letter) {
        if (letter == null) {
            throw new NullPointerException("letter must not be null");
        }
        String key = letter.strip().toUpperCase();
        Double points = GRADE_POINTS.get(key);
        if (points == null) {
            throw new IllegalArgumentException("unknown letter grade: '" + letter + "'");
        }
        return points;
    }

    /**
     * Return a new list with the n lowest scores removed. The original list is
     * not modified and the order of the remaining scores is kept.
     *
     * @throws IllegalArgumentException if n is negative, would remove every
     *                                  score, or any score is out of range
     */
    public static List<Double> dropLowest(List<Double> scores, int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must not be negative");
        }
        if (n >= scores.size()) {
            throw new IllegalArgumentException("cannot drop every score");
        }
        for (double s : scores) {
            checkScore(s);
        }
        List<Double> result = new ArrayList<>(scores);
        if (n == 0) {
            return result;
        }
        List<Double> sorted = new ArrayList<>(scores);
        Collections.sort(sorted);
        for (int i = 0; i < n; i++) {
            result.remove(sorted.get(i)); // removes the first matching value
        }
        return result;
    }

    /**
     * Compute a weighted course percentage, rounded to 2 decimals.
     *
     * <ul>
     *   <li>weights must be non-negative and add up to 1.0</li>
     *   <li>every weighted category must have at least one score</li>
     *   <li>categories in scores that have no weight are an error</li>
     * </ul>
     *
     * Example: scores {"labs": [90, 85], "exams": [78]}, weights {"labs": 0.6, "exams": 0.4}
     */
    public static double weightedAverage(Map<String, List<Double>> scoresByCategory,
                                         Map<String, Double> weights) {
        if (weights == null || weights.isEmpty()) {
            throw new IllegalArgumentException("weights must not be empty");
        }
        double weightSum = 0;
        for (double w : weights.values()) {
            if (w < 0) {
                throw new IllegalArgumentException("weights must not be negative");
            }
            weightSum += w;
        }
        if (Math.abs(weightSum - 1.0) > 1e-9) {
            throw new IllegalArgumentException("weights must add up to 1.0");
        }

        Set<String> extra = new HashSet<>(scoresByCategory.keySet());
        extra.removeAll(weights.keySet());
        if (!extra.isEmpty()) {
            throw new IllegalArgumentException("no weight given for: " + new TreeSet<>(extra));
        }

        double total = 0;
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            List<Double> scores = scoresByCategory.get(entry.getKey());
            if (scores == null || scores.isEmpty()) {
                throw new IllegalArgumentException("no scores for category: " + entry.getKey());
            }
            double sum = 0;
            for (double s : scores) {
                checkScore(s);
                sum += s;
            }
            total += entry.getValue() * (sum / scores.size());
        }
        return Math.round(total * 100.0) / 100.0;
    }
}
