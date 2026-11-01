# Sokoban

Play the game on [CodinGame](https://www.codingame.com/training/hard/sokoban).

## Prerequisites

- A JDK (Java 9 or newer is recommended).
- Maven.

PDDL4J is provided in the repository as `pddl4j-4.0.0.jar`. It is copied into the application resources during the Maven build, so no manual installation in the local Maven repository is needed.

## Build

Run from the root of the project:

```bash
mvn clean package
```

The executable JAR is created at:

`target/sokoban-1.0-SNAPSHOT-jar-with-dependencies.jar`

## Run

When no test is specified, the game uses `test2.json`:

```bash
java --add-opens java.base/java.lang=ALL-UNNAMED -server -Xms2048m -Xmx2048m -cp "target/sokoban-1.0-SNAPSHOT-jar-with-dependencies.jar" sokoban.SokobanMain
```

Choose another level without changing the source code:

```bash
java --add-opens java.base/java.lang=ALL-UNNAMED -server -Xms2048m -Xmx2048m -cp "target/sokoban-1.0-SNAPSHOT-jar-with-dependencies.jar" sokoban.SokobanMain test5.json
```

The following syntax also works:

```bash
java --add-opens java.base/java.lang=ALL-UNNAMED -server -Xms2048m -Xmx2048m -cp "target/sokoban-1.0-SNAPSHOT-jar-with-dependencies.jar" sokoban.SokobanMain --test test5.json
```

The test filename must correspond to a JSON file in the project's `config` directory (for example, `config/test5.json`). The short filename is passed to the Codingame runner, which loads it from that directory.

On Windows PowerShell, the same commands work. The classpath is written with forward slashes so it does not depend on Windows-specific backslashes.

## Portability

- No Java source uses a path tied to a particular user's Windows account or project folder.
- The PDDL domain is loaded from the classpath resource `/pddl/domain.pddl`.
- The supplied PDDL4J JAR is bundled as `/lib/pddl4j-4.0.0.jar`; at runtime it is extracted to a temporary file and used to start the planner.
- The generated PDDL problem is written to a temporary file.
- Java's executable is located from the active JDK, with the correct executable name for Windows or Unix-like systems.
- Temporary files are deleted after the planner has finished.

The project keeps the existing Sokoban board, PDDL generation logic, and action extraction approach. The PDDL domain file itself is unchanged.

The `mvn exec:java` command is not used because the original setup reports an issue with the `src/main/resources/view/assets` directory.
