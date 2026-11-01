# Sokoban

Play the game on [CodinGame](https://www.codingame.com/training/hard/sokoban).

## Prerequisites

- A JDK (Java 9 or newer is recommended).
- Maven.

## Install PDDL4J

The project uses PDDL4J 4.0.0. The JAR file `pddl4j-4.0.0.jar` is provided in the repository.

Before building the project on a new computer, install this JAR in the local Maven repository by running the following command from the project root:

```bash
mvn install:install-file -Dfile=pddl4j-4.0.0.jar -DgroupId=fr.uga -DartifactId=pddl4j -Dversion=4.0.0 -Dpackaging=jar -DgeneratePom=true
```

This installation is local to the computer and only needs to be done once on each computer. The `pom.xml` already declares PDDL4J as a dependency; there is no need to modify it.

## Build

From the project root, run:

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

To choose another level without modifying the source code, pass the test filename as an argument:

```bash
java --add-opens java.base/java.lang=ALL-UNNAMED -server -Xms2048m -Xmx2048m -cp "target/sokoban-1.0-SNAPSHOT-jar-with-dependencies.jar" sokoban.SokobanMain test5.json
```

The following syntax is also supported:

```bash
java --add-opens java.base/java.lang=ALL-UNNAMED -server -Xms2048m -Xmx2048m -cp "target/sokoban-1.0-SNAPSHOT-jar-with-dependencies.jar" sokoban.SokobanMain --test test5.json
```

The test filename must correspond to a JSON file in the project's `config` directory (for example, `config/test5.json`). The code builds the path `config/<filename>` before passing it to the Codingame runner. Run the command from the project root so that the `config` directory can be found.

The commands can be run from Windows PowerShell, Linux, or macOS, provided Java, Maven, and PDDL4J have been installed as described above.

## Portability notes

- The Java code does not use a path tied to a particular Windows user or project directory.
- The PDDL domain is loaded from the Java classpath resource `/pddl/domain.pddl` and copied to a temporary file because PDDL4J expects a file path.
- The generated PDDL problem is written to a temporary file.
- The HSP planner is launched with the Java executable from the active JDK and the current Java classpath. PDDL4J is therefore found through the Maven dependency declared in `pom.xml`.
- Temporary PDDL files are deleted after the planner has finished.

The existing project structure, the Codingame dependencies, and the Maven configuration supplied for the course are kept. The `domain.pddl` file is unchanged.

The `mvn exec:java` command is not used here because the existing setup reports an issue with the `src/main/resources/view/assets` directory.
