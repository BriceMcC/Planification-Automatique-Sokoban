package sokoban;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Agent {
    private static final String DOMAIN_RESOURCE = "/pddl/domain.pddl";
    private static final String PLANNER_MAIN_CLASS =
            "fr.uga.pddl4j.planners.statespace.HSP";

    private static int boxCountGlobal = 0;

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Path domainFile = null;
        Path problemFile = null;

        try {
            // Les ressources sont extraites dans le dossier temporaire du système.
            // Le programme ne dépend donc pas du dossier dans lequel il est lancé.
            domainFile = copyDomainResourceToTemporaryFile();
            problemFile = Files.createTempFile("sokoban-problem-", ".pddl");

            // 1. Lecture du niveau initial et génération du problème PDDL.
            convertCurrentLevelToPDDLProblem(input, problemFile.toString());

            // 2. Lancement du planificateur HSP.
            String plan = runPlanner(domainFile.toString(), problemFile.toString());

            if (plan.isEmpty()) {
                System.err.println("Aucun plan trouvé !");
                return;
            }

            // 3. Boucle de jeu : restituer les actions une par une au Referee.
            for (int i = 0; i < plan.length(); i++) {
                System.out.println(plan.charAt(i));

                // Si ce n'est pas le dernier tour, consommer l'entrée du tour suivant.
                if (i < plan.length() - 1) {
                    input.nextInt(); // playerX
                    input.nextInt(); // playerY
                    for (int b = 0; b < boxCountGlobal; b++) {
                        input.nextInt(); // boxX
                        input.nextInt(); // boxY
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur pendant la résolution du niveau Sokoban :");
            e.printStackTrace();
        } finally {
            deleteTemporaryFile(problemFile);
            deleteTemporaryFile(domainFile);
        }
    }

    /**
     * Copie le domaine PDDL embarqué dans les ressources vers un fichier temporaire.
     * PDDL4J attend un chemin de fichier et non une ressource Java.
     */
    private static Path copyDomainResourceToTemporaryFile() throws IOException {
        Path temporaryFile = Files.createTempFile("sokoban-domain-", ".pddl");
        boolean copied = false;

        try (java.io.InputStream domainStream =
                     Agent.class.getResourceAsStream(DOMAIN_RESOURCE)) {
            if (domainStream == null) {
                throw new IOException("Ressource PDDL introuvable : " + DOMAIN_RESOURCE);
            }

            Files.copy(domainStream, temporaryFile,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            copied = true;
            return temporaryFile;
        } finally {
            if (!copied) {
                Files.deleteIfExists(temporaryFile);
            }
        }
    }

    private static void deleteTemporaryFile(Path file) {
        if (file == null) {
            return;
        }

        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            System.err.println("Impossible de supprimer le fichier temporaire : " + file);
        }
    }

    public static void convertCurrentLevelToPDDLProblem(
            Scanner input, String problemFilename) throws IOException {
        int width = input.nextInt();
        int height = input.nextInt();
        boxCountGlobal = input.nextInt();
        input.nextLine();

        char[][] map = new char[height][];
        for (int y = 0; y < height; y++) {
            map[y] = input.nextLine().toCharArray();
        }

        int playerX = input.nextInt();
        int playerY = input.nextInt();

        int[][] boxes = new int[boxCountGlobal][2];
        for (int i = 0; i < boxCountGlobal; i++) {
            boxes[i][0] = input.nextInt();
            boxes[i][1] = input.nextInt();
        }

        createPDDLProblemFile(problemFilename, width, height, boxCountGlobal,
                map, playerX, playerY, boxes);
    }

    public static void createPDDLProblemFile(
            String filename, int width, int height, int boxCount,
            char[][] map, int playerX, int playerY, int[][] boxes) throws IOException {
        Path file = Paths.get(filename);
        Path parent = file.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (PrintWriter writer = new PrintWriter(
                Files.newBufferedWriter(file, StandardCharsets.UTF_8))) {
            writer.println("(define (problem sokoban-level)");
            writer.println("  (:domain sokoban)");

            // --- OBJETS ---
            writer.print("  (:objects up down left right - direction ");
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (map[y][x] != '#') {
                        writer.print("loc-" + x + "-" + y + " ");
                    }
                }
            }
            writer.println("- location)");

            // --- ÉTAT INITIAL ---
            writer.println("  (:init");
            writer.println("    (player-at loc-" + playerX + "-" + playerY + ")");
            for (int[] box : boxes) {
                writer.println("    (box-at loc-" + box[0] + "-" + box[1] + ")");
            }

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (map[y][x] != '#') {
                        // Déclaration des cases claires (ni joueur, ni caisse).
                        boolean isOccupied = (x == playerX && y == playerY);
                        for (int[] box : boxes) {
                            if (box[0] == x && box[1] == y) {
                                isOccupied = true;
                            }
                        }
                        if (!isOccupied) {
                            writer.println("    (clear loc-" + x + "-" + y + ")");
                        }

                        // Déclaration dynamique des adjacences jouables.
                        if (y > 0 && map[y - 1][x] != '#') {
                            writer.println("    (adjacent loc-" + x + "-" + y
                                    + " loc-" + x + "-" + (y - 1) + " up)");
                        }
                        if (y < height - 1 && map[y + 1][x] != '#') {
                            writer.println("    (adjacent loc-" + x + "-" + y
                                    + " loc-" + x + "-" + (y + 1) + " down)");
                        }
                        if (x > 0 && map[y][x - 1] != '#') {
                            writer.println("    (adjacent loc-" + x + "-" + y
                                    + " loc-" + (x - 1) + "-" + y + " left)");
                        }
                        if (x < width - 1 && map[y][x + 1] != '#') {
                            writer.println("    (adjacent loc-" + x + "-" + y
                                    + " loc-" + (x + 1) + "-" + y + " right)");
                        }
                    }
                }
            }
            writer.println("  )");

            // --- BUT ---
            writer.println("  (:goal (and");
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Les cibles sont signalées par '*' par le Board.
                    if (map[y][x] == '*') {
                        writer.println("    (box-at loc-" + x + "-" + y + ")");
                    }
                }
            }
            writer.println("  ))");
            writer.println(")");

            if (writer.checkError()) {
                throw new IOException("Erreur lors de l'écriture du problème PDDL : " + file);
            }
        }
    }

    public static String runPlanner(String domainFilename, String problemFilename) {
        StringBuilder planStr = new StringBuilder();

        try {
            String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            String javaExecutableName = osName.contains("win") ? "java.exe" : "java";
            String javaExecutable = Paths.get(
                    System.getProperty("java.home"), "bin", javaExecutableName
            ).toString();

            List<String> command = new ArrayList<>();
            command.add(javaExecutable);

            // --add-opens n'existe pas sous Java 8 ; il est nécessaire sur les JDK modulaires.
            if (isJavaNineOrNewer()) {
                command.add("--add-opens");
                command.add("java.base/java.lang=ALL-UNNAMED");
            }

            // PDDL4J est une dépendance Maven : on réutilise le classpath
            // du programme courant pour que le planificateur retrouve ses classes.
            String classPath = System.getProperty("java.class.path");
            if (classPath == null || classPath.trim().isEmpty()) {
                throw new IllegalStateException("Classpath Java introuvable.");
            }

            command.add("-cp");
            command.add(classPath);
            command.add(PLANNER_MAIN_CLASS);
            command.add(domainFilename);
            command.add(problemFilename);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            System.err.println("\n--- LOGS PDDL4J ---");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println(line);
                    String lineLower = line.toLowerCase(Locale.ROOT);

                    if (lineLower.contains("(move ") || lineLower.contains("(push ")) {
                        if (lineLower.contains("up")) {
                            planStr.append("U");
                        } else if (lineLower.contains("down")) {
                            planStr.append("D");
                        } else if (lineLower.contains("left")) {
                            planStr.append("L");
                        } else if (lineLower.contains("right")) {
                            planStr.append("R");
                        }
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Le planificateur PDDL4J s'est terminé avec le code : "
                        + exitCode);
            }
            System.err.println("-------------------\n");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("L'exécution du planificateur a été interrompue.");
        } catch (Exception e) {
            System.err.println("Impossible de lancer le planificateur PDDL4J :");
            e.printStackTrace();
        }

        return planStr.toString();
    }

    private static boolean isJavaNineOrNewer() {
        String version = System.getProperty("java.specification.version", "8");
        if (version.startsWith("1.")) {
            version = version.substring(2);
        }

        int separator = version.indexOf('.');
        if (separator >= 0) {
            version = version.substring(0, separator);
        }

        try {
            return Integer.parseInt(version) >= 9;
        } catch (NumberFormatException e) {
            return true;
        }
    }
}
