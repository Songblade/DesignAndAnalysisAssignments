package testing;

import edu.yu.da.ArithmeticPuzzle;
import edu.yu.da.ArithmeticPuzzleBase;
import edu.yu.da.GeneticAlgorithmConfig;
import org.junit.jupiter.api.Test;

import java.util.PriorityQueue;

public class PuzzleExperimentsTest {
    // this will include the experiments I do to figure out which sort of things work best
    // first, I will create the repeated experiment
    // I will take one of the challenging test cases, and run it a bunch of time (say, 10), with a size limit
    // of 200 and population 1000. The score of that value is equal to the total number, with a limit of 0
    // and a maximum of 2000. We seek to minimize.
    // I will run each experiment with both Crossover and Roulette, and with mutation and crossover chances
    // running from 0 (if applicable) to 1, with the other chance controlled at 0.5.
    // I will check at increments of 0.1, though if I find a clear winning range, I may repeat with narrower ranges.
    // I will also repeat the test to make sure it is consistent enough.

    private int printScoreOfExperiment(GeneticAlgorithmConfig.SelectionType selectionType, boolean testingMutation, double probability) {
        System.out.println("Testing with type " + selectionType + " and " +
                (testingMutation? "mutation" : "crossover") + " chance of " + probability);
        ArithmeticPuzzleBase enigma = new ArithmeticPuzzle("CIGHGJHCAF", "DADJACJEEA", "HAAHHCHHFG");
        int score = 0;
        int numTries = 50;
        int numSuccesses = numTries;
        for (int i = 0; i < numTries; i++) {
            ArithmeticPuzzleBase.SolutionI solution = enigma.solveIt(new GeneticAlgorithmConfig(1000,
                    100, selectionType, testingMutation? probability : 0.5,
                    testingMutation? 0.5 : probability));
            score += solution.nGenerations();
            if (solution.nGenerations() == 100) {
                numSuccesses--;
            }
        }
        System.out.println("Score is " + score + ", average " + score / numTries + " generations");
        System.out.println("Percentage completed is " + (numSuccesses * 1.0 / numTries));
        return score / numTries;
    }

    // 2: 23, 25, 24, 25
    // Lowest average detected: 11
    // 4: 30, 30, 30, 29 (but has single digit at high mutations)
    // Lowest average detected: 6


    @Test
    public void experimentRouletteMutation() {
        int totalGenerations = 0;
        for (double probability = 0.1; probability <= 1; probability+=0.1) {
            totalGenerations += printScoreOfExperiment(GeneticAlgorithmConfig.SelectionType.ROULETTE, true, probability);
        }
        System.out.println("Average generations: " + totalGenerations / 10);
    }
    @Test
    public void experimentTournamentMutation() {
        int totalGenerations = 0;
        for (double probability = 0.1; probability <= 1; probability+=0.1) {
            totalGenerations += printScoreOfExperiment(GeneticAlgorithmConfig.SelectionType.TOURNAMENT, true, probability);
        }
        System.out.println("Average generations: " + totalGenerations / 10);
    }
    @Test
    public void experimentRouletteCrossover() {
        int totalGenerations = 0;
        for (double probability = 0.0; probability <= 1; probability+=0.1) {
            totalGenerations += printScoreOfExperiment(GeneticAlgorithmConfig.SelectionType.ROULETTE, false, probability);
        }
        System.out.println("Average generations: " + totalGenerations / 11);
    }
    @Test
    public void experimentTournamentCrossover() {
        int totalGenerations = 0;
        for (double probability = 0.0; probability <= 1; probability+=0.1) {
            totalGenerations += printScoreOfExperiment(GeneticAlgorithmConfig.SelectionType.TOURNAMENT, false, probability);
        }
        System.out.println("Average generations: " + totalGenerations / 11);
    }

    @Test
    public void testOptimalConfiguration() {
        System.out.println("Testing with type TOURNAMENT and mutation 0.9 and crossover 1.0");
        ArithmeticPuzzleBase enigma = new ArithmeticPuzzle("CIGHGJHCAF", "DADJACJEEA", "HAAHHCHHFG");
        int score = 0;
        int numTries = 50;
        int numSuccesses = numTries;
        for (int i = 0; i < numTries; i++) {
            ArithmeticPuzzleBase.SolutionI solution = enigma.solveIt(new GeneticAlgorithmConfig(1000,
                    100, GeneticAlgorithmConfig.SelectionType.TOURNAMENT, 0.9,
                    1.0));
            score += solution.nGenerations();
            if (solution.nGenerations() == 100) {
                numSuccesses--;
            }
        }
        System.out.println("Score is " + score + ", average " + score / numTries + " generations");
        System.out.println("Percentage completed is " + (numSuccesses * 1.0 / numTries));
    }

    @Test
    public void testEveryConfiguration() {
        class Setting implements Comparable<Setting> {
            final double mutateProb;
            final double crossProb;
            final int genScore;
            final int numSuccesses;

            Setting(double mutateProb, double crossProb, int genScore, int numSuccesses) {
                this.mutateProb = mutateProb;
                this.crossProb = crossProb;
                this.genScore = genScore;
                this.numSuccesses = numSuccesses;
            }

            @Override
            public int compareTo(Setting o) {
                return o.genScore - genScore;
            }
        }

        int numTries = 50;
        PriorityQueue<Setting> bestResults = new PriorityQueue<>();
        for (double mutateProb = 0.1; mutateProb <= 1.0; mutateProb += 0.1) {
            for (double crossProb = 0.0; crossProb <= 1.0; crossProb += 0.1) {
                System.out.println("Testing with type TOURNAMENT and mutation " + mutateProb + " and crossover " + crossProb);
                ArithmeticPuzzleBase enigma = new ArithmeticPuzzle("CIGHGJHCAF", "DADJACJEEA", "HAAHHCHHFG");
                int score = 0;
                int numSuccesses = numTries;
                for (int i = 0; i < numTries; i++) {
                    ArithmeticPuzzleBase.SolutionI solution = enigma.solveIt(new GeneticAlgorithmConfig(1000,
                            100, GeneticAlgorithmConfig.SelectionType.TOURNAMENT, mutateProb,
                            crossProb));
                    score += solution.nGenerations();
                    if (solution.nGenerations() == 100) {
                        numSuccesses--;
                    }
                }
                System.out.println("Score is " + score + ", average " + score / numTries + " generations");
                System.out.println("Percentage completed is " + (numSuccesses * 1.0 / numTries));
                bestResults.add(new Setting(mutateProb, crossProb, score, numSuccesses));
                if (bestResults.size() > 5) {
                    bestResults.remove(); // to keep only the best in
                }
            }
        }
        System.out.println();
        for (Setting setting : bestResults) {
            System.out.println("When tested with " + setting.mutateProb + " and crossover " + setting.crossProb + ":");
            System.out.println("Score is " + setting.genScore + ", average " + setting.genScore / numTries + " generations");
            System.out.println("Percentage completed is " + (setting.numSuccesses * 1.0 / numTries));
        }
    }
}
