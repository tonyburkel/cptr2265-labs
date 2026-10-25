package edu.mctc.gradebook;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Application service that ties the loader, repository and grading rules together.
 *
 * Its dependencies are passed in through the constructor (dependency injection),
 * which makes it straightforward to substitute mocks or fakes in tests.
 */
public class GradebookService {

    /** A student's final result. */
    public record FinalGrade(String studentId, String name, double percent, String letter) {
    }

    private final StudentRepository repository;
    private final RosterLoader loader;

    public GradebookService(StudentRepository repository, RosterLoader loader) {
        this.repository = repository;
        this.loader = loader;
    }

    /** Load a roster CSV into the repository. Returns the number of scores imported. */
    public int importRoster(Path path) throws IOException, SQLException {
        List<RosterLoader.ScoreRecord> records = loader.load(path);
        for (RosterLoader.ScoreRecord r : records) {
            repository.addStudent(r.studentId(), r.name());
            repository.addScore(r.studentId(), r.category(), r.score());
        }
        return records.size();
    }

    /**
     * Compute one student's final grade.
     *
     * @param dropLowestIn optional category (may be null) whose single lowest
     *                     score is dropped before averaging, only if that
     *                     category has 2 or more scores
     * @throws NoSuchElementException if the student does not exist
     */
    public FinalGrade finalGrade(String studentId, Map<String, Double> weights, String dropLowestIn)
            throws SQLException {
        String name = repository.getStudentName(studentId)
                .orElseThrow(() -> new NoSuchElementException("unknown student: " + studentId));
        Map<String, List<Double>> scores = repository.getScores(studentId);
        if (dropLowestIn != null && scores.getOrDefault(dropLowestIn, List.of()).size() > 1) {
            scores.put(dropLowestIn, Grading.dropLowest(scores.get(dropLowestIn), 1));
        }
        double percent = Grading.weightedAverage(scores, weights);
        return new FinalGrade(studentId, name, percent, Grading.letterGrade(percent));
    }

    /** Final grades for every student, sorted by student id. */
    public List<FinalGrade> classReport(Map<String, Double> weights, String dropLowestIn)
            throws SQLException {
        List<FinalGrade> report = new ArrayList<>();
        for (String id : repository.listStudentIds()) {
            report.add(finalGrade(id, weights, dropLowestIn));
        }
        return report;
    }
}
