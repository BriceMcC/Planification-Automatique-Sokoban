package sokoban;

import java.util.Scanner;
import java.io.PrintWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Agent {
    // Chemins relatifs portables
    private static final String DOMAIN_FILE = "C:\\Users\\zirto\\Documents\\Cours\\M2\\Planification automatique\\TP1\\sokoban-master\\src\\main\\resources\\pddl\\domain.pddl";
    private static final String PROBLEM_FILE = "C:\\Users\\zirto\\Documents\\Cours\\M2\\Planification automatique\\TP1\\sokoban-master\\target\\problem.pddl";
    private static int boxCountGlobal = 0;

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // 1. Lecture du niveau initial et génération du problème PDDL
        convertCurrentLevelToPDDLProblem(input);

        // 2. Lancement du planificateur HSP
        String plan = runPlanner(DOMAIN_FILE, PROBLEM_FILE);

        if (plan.isEmpty()) {
            System.err.println("Aucun plan trouvé !");
            return;
        }

        // 3. Boucle de jeu : restituer les actions une par une au Referee
        for (int i = 0; i < plan.length(); i++) {
            System.out.println(plan.charAt(i));

            // Si ce n'est pas le dernier tour, on consomme l'entrée du tour suivant
            if (i < plan.length() - 1) {
                input.nextInt(); // playerX
                input.nextInt(); // playerY
                for (int b = 0; b < boxCountGlobal; b++) {
                    input.nextInt(); // boxX
                    input.nextInt(); // boxY
                }
            }
        }
    }

    public static void convertCurrentLevelToPDDLProblem(Scanner input) {
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

        createPDDLProblemFile(PROBLEM_FILE, width, height, boxCountGlobal, map, playerX, playerY, boxes);
    }

    public static void createPDDLProblemFile(String filename, int width, int height, int boxCount, char[][] map, int playerX, int playerY, int[][] boxes) {
        File file = new File(filename);
        file.getParentFile().mkdirs(); // Création du dossier target s'il n'existe pas

        try (PrintWriter writer = new PrintWriter(file)) {
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
                        // Déclaration des cases claires (ni joueur, ni caisse)
                        boolean isOccupied = (x == playerX && y == playerY);
                        for (int[] box : boxes) {
                            if (box[0] == x && box[1] == y) isOccupied = true;
                        }
                        if (!isOccupied) writer.println("    (clear loc-" + x + "-" + y + ")");

                        // Déclaration dynamique des adjacences jouables
                        if (y > 0 && map[y - 1][x] != '#')
                            writer.println("    (adjacent loc-" + x + "-" + y + " loc-" + x + "-" + (y - 1) + " up)");
                        if (y < height - 1 && map[y + 1][x] != '#')
                            writer.println("    (adjacent loc-" + x + "-" + y + " loc-" + x + "-" + (y + 1) + " down)");
                        if (x > 0 && map[y][x - 1] != '#')
                            writer.println("    (adjacent loc-" + x + "-" + y + " loc-" + (x - 1) + "-" + y + " left)");
                        if (x < width - 1 && map[y][x + 1] != '#')
                            writer.println("    (adjacent loc-" + x + "-" + y + " loc-" + (x + 1) + "-" + y + " right)");
                    }
                }
            }
            writer.println("  )");

            // --- BUT ---
            writer.println("  (:goal (and");
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Les cibles sont signalées par '*' d'après le paramétrage du Board
                    if (map[y][x] == '*') {
                        writer.println("    (box-at loc-" + x + "-" + y + ")");
                    }
                }
            }
            writer.println("  ))");
            writer.println(")");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String runPlanner(String domainFilename, String problemFilename) {
        StringBuilder planStr = new StringBuilder();
        try {
            // Chemins absolus vers ton projet
            String basePath = "C:\\Users\\zirto\\Documents\\Cours\\M2\\Planification automatique\\TP1\\sokoban-master\\";
            String pddl4jJar = basePath + "pddl4j-4.0.0.jar";
            String domPath = basePath + "src\\main\\resources\\pddl\\domain.pddl";
            String probPath = basePath + "target\\problem.pddl";

            // CORRECTION : statespace.HSP (sans le .hsp.)
            ProcessBuilder pb = new ProcessBuilder(
                    "java", "-cp", pddl4jJar,
                    "fr.uga.pddl4j.planners.statespace.HSP",
                    domPath, probPath
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            String line;

            System.err.println("\n--- LOGS PDDL4J ---");
            while ((line = reader.readLine()) != null) {
                System.err.println(line);
                String lineLower = line.toLowerCase();
                if (lineLower.contains("(move ") || lineLower.contains("(push ")) {
                    if (lineLower.contains("up")) planStr.append("U");
                    else if (lineLower.contains("down")) planStr.append("D");
                    else if (lineLower.contains("left")) planStr.append("L");
                    else if (lineLower.contains("right")) planStr.append("R");
                }
            }
            System.err.println("-------------------\n");

            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return planStr.toString();
    }
}