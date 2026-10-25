# Lab 9 Starter: Gradebook Module (Java)

Requires **Java 21** and **Maven 3.9+** (the VS Code Extension Pack for Java includes Maven support). Use this starter for Lab 9 if you are not testing a module from your midterm project. It is designed to carry into **Lab 10** as well: it already has a file-system dependency to mock, a database for integration tests, and Mockito in the `pom.xml`.

Unzip this into the root of your `cptr2265-labs` repository so the files land in `lab09/starter/`.

## What's inside
| Path | What it is | Good for |
|---|---|---|
| `src/main/java/edu/mctc/gradebook/Grading.java` | Pure grading rules: `letterGrade`, `gradePoints`, `dropLowest`, `weightedAverage` | Lab 9 Part A unit tests (normal, boundary, error cases) |
| `.../RosterLoader.java` | `load(Path)` reads a CSV from disk | Lab 10 Part A: mock the file system with Mockito |
| `.../StudentRepository.java` | Stores students and scores in an H2 SQL database | Lab 10 Part B: integration tests with an in-memory database |
| `.../GradebookService.java` | Ties the pieces together; dependencies are passed into the constructor | Mocking and integration tests |
| `.../App.java` | A small demo that prints a class report | Seeing it run |
| `data/sample_roster.csv` | Sample data for the demo and for tests | |
| `src/test/java/edu/mctc/gradebook/GradingTest.java` | Two example JUnit 5 tests to show the structure | Add your tests here (and in new `*Test.java` files) |
| `pom.xml` | JUnit 5, Mockito, H2, and the JaCoCo coverage plugin, already configured | |

## Build and run
```
cd lab09/starter
mvn test                    # compile and run all tests, then write the coverage report
mvn -q compile exec:java    # run the demo
```
The JaCoCo coverage report (Part C) is written to `target/site/jacoco/index.html`. The demo class `App` is excluded from coverage.

## Part B ideas (strict TDD)
Write the test first and commit it failing, make it pass and commit, then refactor and commit. Pick one of these new methods for `Grading` (or propose your own):
- `classStatistics(List<Double> percentages)` returns mean, median, minimum and maximum (a small record); decide what happens with an empty list.
- `curve(List<Double> scores, double targetMean)` shifts every score so the average equals `targetMean`, capping at 100.
- `gpa(List<String> letters, List<Integer> credits)` computes a credit-weighted GPA using `gradePoints`.
- `latePenalty(double score, int daysLate)` subtracts 10% per day late, with 0 at 5 or more days.

## Looking ahead to Lab 10
- **Mocking:** `GradebookService` takes a `RosterLoader` in its constructor, so `Mockito.mock(RosterLoader.class)` can stand in for the file system (`when(loader.load(any())).thenReturn(List.of(...))`).
- **Integration tests:** in a `@BeforeEach` method, open `DriverManager.getConnection("jdbc:h2:mem:test" + System.nanoTime())`, build a real `StudentRepository`, import `data/sample_roster.csv` through the service, and check the class report. Close the connection in `@AfterEach`.
