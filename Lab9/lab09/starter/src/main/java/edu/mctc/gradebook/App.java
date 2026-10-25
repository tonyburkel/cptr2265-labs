package edu.mctc.gradebook;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

/** Demo: mvn -q compile exec:java   (or pass a roster path with -Dexec.args="data/other.csv") */
public class App {

    private static final Map<String, Double> WEIGHTS = Map.of("labs", 0.40, "quizzes", 0.20, "exams", 0.40);

    public static void main(String[] args) throws Exception {
        Path path = Path.of(args.length > 0 ? args[0] : "data/sample_roster.csv");
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:gradebook")) {
            GradebookService service = new GradebookService(new StudentRepository(conn), new RosterLoader());
            int count = service.importRoster(path);
            System.out.printf("Imported %d scores from %s%n%n", count, path);
            System.out.printf("%-6s%-18s%8s  Grade%n", "ID", "Name", "Percent");
            for (GradebookService.FinalGrade g : service.classReport(WEIGHTS, "quizzes")) {
                System.out.printf("%-6s%-18s%8.2f  %s%n", g.studentId(), g.name(), g.percent(), g.letter());
            }
        }
    }
}
