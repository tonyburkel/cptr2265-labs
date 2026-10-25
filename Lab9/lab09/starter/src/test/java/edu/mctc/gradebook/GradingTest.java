package edu.mctc.gradebook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Example tests to show the structure. Lab 9 asks for at least 15 test cases:
 * normal inputs, boundary values and error conditions. Add yours below.
 *
 * Run from lab09/starter:   mvn test
 * Coverage report:          target/site/jacoco/index.html
 */
class GradingTest {

    @Test
    void letterGrade_normalScore_returnsB() {
        assertEquals("B", Grading.letterGrade(85));
    }

    @Test
    void letterGrade_above100_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Grading.letterGrade(100.5));
    }

    // TODO: boundary values (exactly 93? 92.99? 0? 100?), dropLowest, gradePoints,
    //       weightedAverage, RosterLoader, StudentRepository, GradebookService ...
}
