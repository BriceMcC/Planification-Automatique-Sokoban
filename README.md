# Sokoban – Planification automatique avec PDDL4J

Agent Java qui résout automatiquement des niveaux de Sokoban : il lit la carte
envoyée par le moteur de jeu CodinGame, la traduit en problème PDDL, appelle le
planificateur **HSP** de PDDL4J, puis rejoue le plan obtenu coup par coup.

## Principe de fonctionnement

1. **Lecture du niveau** : au premier tour, l'agent reçoit la taille de la carte,
   le nombre de caisses, la grille, la position du joueur et celle des caisses.
2. **Génération du problème PDDL** : chaque case non murée devient un objet
   `loc-x-y`. L'état initial décrit le joueur, les caisses, les cases libres et
   les adjacences (`up`, `down`, `left`, `right`). Le but est que chaque case cible
   contienne une caisse.
3. **Planification** : le planificateur HSP est lancé dans un processus Java
   séparé, avec le domaine `domain.pddl` et le problème généré.
4. **Exécution** : pour chaque action du plan (`move` ou `push`), l'agent en
   extrait la direction (dernier paramètre) et envoie `U`, `D`, `L` ou `R` au jeu.

## Domaine PDDL

Le fichier `src/main/resources/pddl/domain.pddl` définit :

- les types `location` et `direction` ;
- les prédicats `player-at`, `box-at`, `clear` et `adjacent` ;
- l'action `move` : le joueur se déplace vers une case adjacente libre ;
- l'action `push` : le joueur pousse une caisse vers la case libre située juste
  derrière elle, dans la même direction.

## Fichiers créés ou modifiés

- `src/main/java/sokoban/Agent.java` : Implémentation de la boucle de jeu, traduction de la carte en problème PDDL dynamique, et exécution du planificateur HSP (PDDL4J).
- `src/main/java/sokoban/SokobanMain.java` : Ajout du support des arguments en ligne de commande pour faciliter le choix du niveau à tester.
- `src/main/java/com/codingame/game/Referee.java` : Augmentation du temps limite du premier tour à 24 999 ms pour laisser le maximum de temps au planificateur (la limite du moteur de jeu étant de 25 secondes).
- `src/main/resources/pddl/domain.pddl` : Définition des règles du domaine Sokoban (prédicats, actions de déplacement et de poussée).
- `pom.xml` : Ajout de la dépendance `fr.uga:pddl4j:4.0.0`.
- `pddl4j-4.0.0.jar` : Archive fournie à la racine, requise pour l'installation locale. | Configuration Maven (dépendances du jeu et de PDDL4J) |

## Prérequis

- un JDK (Java 9 ou plus recommandé) ;
- Maven.

## Installation

PDDL4J 4.0.0 n'est pas récupérée automatiquement par Maven : le JAR fourni dans
le dépôt doit d'abord être installé dans le dépôt Maven local de la machine.
Depuis la racine du projet :

```bash
mvn install:install-file -Dfile=pddl4j-4.0.0.jar -DgroupId=fr.uga -DartifactId=pddl4j -Dversion=4.0.0 -Dpackaging=jar -DgeneratePom=true
```

Cette étape n'est à faire qu'une fois par ordinateur.

## Compilation

Depuis la racine du projet :

```bash
mvn clean package
```

Le JAR exécutable est créé ici :
`target/sokoban-1.0-SNAPSHOT-jar-with-dependencies.jar`

## Exécution

Toujours lancer les commandes **depuis la racine du projet**, afin que le dossier
`config` soit trouvé.

Niveau par défaut (`test2.json`) :

```bash
java --add-opens java.base/java.lang=ALL-UNNAMED -server -Xms2048m -Xmx2048m -cp "target/sokoban-1.0-SNAPSHOT-jar-with-dependencies.jar" sokoban.SokobanMain
```

Choisir un autre niveau sans modifier le code, en passant le nom du fichier :

```bash
java --add-opens java.base/java.lang=ALL-UNNAMED -server -Xms2048m -Xmx2048m -cp "target/sokoban-1.0-SNAPSHOT-jar-with-dependencies.jar" sokoban.SokobanMain test4.json
```

La syntaxe `--test test4.json` (ou `--test=test4.json`) est aussi acceptée. Le
fichier doit exister dans le dossier `config` (par exemple `config/test4.json`).

Une fois le programme lancé, ouvrir <http://localhost:8888/test.html> dans un
navigateur pour voir le jeu. En cas d'affichage d'un ancien niveau, vérifier
qu'aucun ancien processus Java n'est encore actif sur le port 8888 et recharger
la page avec Ctrl+F5.

## Portabilité

- Aucun chemin absolu n'est utilisé dans le code.
- Le domaine PDDL est lu depuis les ressources du JAR, puis copié dans un fichier
  temporaire (PDDL4J attend un chemin de fichier). Le problème généré est lui
  aussi écrit dans un fichier temporaire.
- Les fichiers temporaires sont supprimés à la fin de l'exécution.
- HSP est lancé avec le Java du JDK actif et le classpath du programme courant.

## Limite connue

- Tout le calcul du plan se fait pendant le premier tour, dont la durée maximale
  est fixée à 24.9 s dans `Referee.java` (le moteur de jeu refuse plus de 25 s). Un
  niveau très difficile peut donc dépasser ce délai.

## Auteur

Brice MC CARTHY
