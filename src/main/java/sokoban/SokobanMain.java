package sokoban;

import com.codingame.gameengine.runner.SoloGameRunner;

public class SokobanMain {
    private static final String DEFAULT_TEST_CASE = "test2.json";

    public static void main(String[] args) {
        String testCase = getTestCase(args);

        SoloGameRunner gameRunner = new SoloGameRunner();
        gameRunner.setAgent(Agent.class);
        String testCasePath = "config/" + testCase;
        gameRunner.setTestCase(testCasePath);

        System.out.println("Test Sokoban sélectionné : " + testCasePath);
        gameRunner.start();
    }

    /**
     * Accepte :
     * - aucun argument : utilise le test par défaut ;
     * - test3.json ;
     * - --test test3.json ;
     * - --test=test3.json.
     */
    private static String getTestCase(String[] args) {
        if (args == null || args.length == 0) {
            return DEFAULT_TEST_CASE;
        }

        String testCase;
        if (args.length == 1 && args[0].startsWith("--test=")) {
            testCase = args[0].substring("--test=".length());
        } else if (args.length == 1) {
            testCase = args[0];
        } else if (args.length == 2 && "--test".equals(args[0])) {
            testCase = args[1];
        } else {
            throw new IllegalArgumentException(
                    "Usage : SokobanMain [test.json | --test test.json]"
            );
        }

        // On n'accepte qu'un nom de fichier, pas un chemin fourni par l'utilisateur.
        if (!testCase.matches("[A-Za-z0-9_-]+\\.json")) {
            throw new IllegalArgumentException(
                    "Nom de test invalide : " + testCase
                            + ". Exemple : test3.json"
            );
        }

        return testCase;
    }
}
