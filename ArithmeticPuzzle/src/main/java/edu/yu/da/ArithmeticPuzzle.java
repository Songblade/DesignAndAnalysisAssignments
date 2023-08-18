package edu.yu.da;

import java.util.*;

public class ArithmeticPuzzle extends ArithmeticPuzzleBase {

    private final String augend;
    private final String addend;
    private final String sum;
    private final List<Character> letterOrder;
    private SolutionI returnValue;

    /**
     * Constructor.  Specifies the arithmetic puzzle to be solved in terms of an
     * augend, addend, and sum.
     *
     * Representation: all characters are in the range A-Z, with each letter
     * represents a unique digit.  The puzzle to be solved specifies that the
     * augend and addend (each representing a number in base 10) sum to the
     * specified sum (also a number in base 10).  Each of these numbers is
     * represented with the most significant letter (digit) in position 0, next
     * most significant letter (digit) in position 1, and so on.  The numbers
     * need not be the same length: an "empty" digit is represented by the
     * "space" character.
     *
     * Addition: Augend + Addend = Sum
     *
     * @param augend string representation of the augend being added
     * @param addend string representation of the addend being added
     * @param sum string representation of the sum
     */
    public ArithmeticPuzzle(String augend, String addend, String sum) {
        super(augend, addend, sum);
        cleanInput(augend, addend, sum);
        this.augend = augend;
        this.addend = addend;
        this.sum = sum;
        Set<Character> letters = new HashSet<>();
        letterOrder = new ArrayList<>();
        getLettersInString(letters, letterOrder, augend);
        getLettersInString(letters, letterOrder, addend);
        getLettersInString(letters, letterOrder, sum);
    }

    private void cleanInput(String augend, String addend, String sum) {
        cleanWord(augend, "augend");
        cleanWord(addend, "addend");
        cleanWord(sum, "sum");
    }

    private void cleanWord(String word, String wordName) {
        if (word == null) {
            throw new IllegalArgumentException(wordName + " is null");
        }
        if (word.isBlank()) {
            throw new IllegalArgumentException(wordName + " is missing");
        }
        for (int i = 0; i < word.length(); i++) {
            char currentChar = word.charAt(i);
            if (currentChar < 'A' || currentChar > 'Z') {
                System.err.println("Warning: String " + wordName + " contains illegal character " + currentChar);
                // not throwing an error, in case Professor Leff gives us a lower case letter
            }
        }
    }

    private void getLettersInString(Set<Character> letters, List<Character> letterOrder, String word) {
        for (int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            if (!letters.contains(letter)) {
                letters.add(letter);
                letterOrder.add(letter);
            }
        }
    }

    /**
     * Returns the best Solution found by a genetic algorithm for the arithmetic
     * puzzle problem specified by the requirements document.
     *
     * @param gac contains properties needed by a genetic algorithm
     * @see GeneticAlgorithmConfig
     */
    @Override
    public SolutionI solveIt(GeneticAlgorithmConfig gac) {
        if (gac == null) {
            throw new IllegalArgumentException("Config is null");
        }
        // first, we create the initial population of Strings, represented as an array of byte arrays
        // the number of rows is the population size, while the number of columns is the number of letters
        byte[][] population = createRandomPopulation(gac.getInitialPopulationSize(), letterOrder.size());
        // new byte[gac.getInitialPopulationSize()][letterOrder.size()];
        // this gives me an initial population of zero strings
        // I could make them different, but he gave us no guidance on this, so I will leave it alone for now
        for (int generation = 1; generation <= gac.getMaxGenerations(); generation++) {
            long[] fitness = examineAllChromosomes(population, generation);
            //System.out.print("Generation " + generation + ": ");
            //printPopulation(population);
            //System.out.println("Fitness: " + Arrays.toString(fitness));
            if (fitness == null) {
                return returnValue;
            }
            // next we do selection
            // which method we call to get a new population depends on whether we are doing roulette or
            // tournament
            // either way, it replaces the old population with a new, hopefully better one
            if (gac.getSelectionType() == GeneticAlgorithmConfig.SelectionType.ROULETTE) {
                population = rouletteSelection(population, fitness);
            } else {
                population = tournamentSelection(population, fitness);
            }
            //System.out.print("Selection: ");
            //printPopulation(population);
            // next, we do crossover, where, after a random point, 2 chromosomes swap data
            // how often we do this depends on the number from the config record
            crossover(population, gac.getCrossoverProbability());
            //System.out.print("Crossover: ");
            //printPopulation(population);
            // next, we mutate random chromosomes
            mutate(population, gac.getMutationProbability());
            //System.out.print("Mutation: ");
            //printPopulation(population);

        }
        return new SolutionI() { // since we failed to find a solution, we return an empty list
            @Override
            public List<Character> solution() {
                return Collections.EMPTY_LIST;
            }
            @Override
            public String getAugend() {
                return augend;
            }
            @Override
            public String getAddend() {
                return addend;
            }
            @Override
            public String getSum() {
                return sum;
            }
            @Override
            public int nGenerations() {
                return gac.getMaxGenerations();
            }
        };
    }

    private void printPopulation(byte[][] population) {
        for (byte[] chromosome : population) {
            for (byte gene: chromosome) {
                System.out.print(gene);
            }
            System.out.print(", ");
        }
        System.out.println();
    }

    /**
     * Creates a fully random population of chromosomes
     * Each gene is selected independently as an integer 0...9
     * @param populationSize the number of chromosomes in the population
     * @param chromosomeSize the number of genes in the chromosome
     *                       Equal to the number of letters in the problem
     *                       chromosomeSize > 0 && <= 10
     * @return a new population
     */
    private byte[][] createRandomPopulation(int populationSize, int chromosomeSize) {
        byte[][] population = new byte[populationSize][chromosomeSize];
        Random pablo = new Random();
        for (int chromosome = 0; chromosome < populationSize; chromosome++) {
            for (int gene = 0; gene < chromosomeSize; gene++) {
                population[chromosome][gene] = (byte) pablo.nextInt(10);
            }
        }
        return population;
    }

    /**
     * Checks the fitness of all chromosomes in the population
     * If I find a chromosome of fitness 0, I store it in a global variable as a SolutionI
     * @param population of chromosomes
     * @param generation currentGeneration
     * @return the fitness of each chromosome in this population
     */
    private long[] examineAllChromosomes(byte[][] population, int generation) {
        long[] fitnessArray = new long[population.length];
        for (int i = 0; i < population.length; i++) {
            fitnessArray[i] = fitnessFunction(population[i]);
            if (fitnessArray[i] == 0) {
                returnValue = getSolution(population[i], generation);
                return null;
            }
        }
        return fitnessArray;
    }

    /**
     * Given a chromosome, returns a fitness function, where 0 means it has the right answer and a high number
     * is bad
     * @param chromosome we are analyzing, contains a number of digits equal to the number of letters
     * @return a value >= 0 that represents the fitness of this chromosome
     */
    private long fitnessFunction(byte[] chromosome) {
        // first, we take the chromosome and interpret it as a solution
        // we do this by creating a mapping of letters to numbers from the chromosome
        Map<Character, Byte> letterMapping = getDigitMapping(chromosome);
        // we then run through each word, replacing creating new words with digits
        long augendNumber = Long.parseLong(convertLetterDigitToNumber(augend, letterMapping));
        long addendNumber = Long.parseLong(convertLetterDigitToNumber(addend, letterMapping));
        long sumNumber = Long.parseLong(convertLetterDigitToNumber(sum, letterMapping));
        // we then convert those words into integers and test the equality of the two halves
        return Math.abs(augendNumber + addendNumber - sumNumber);
    }

    /**
     * @param chromosome whose mapping we are finding
     * @return a char->short mapping made from that chromosome
     */
    private Map<Character, Byte> getDigitMapping(byte[] chromosome) {
        // we store which numbers we have used so far, so if a number is taken, we check the next highest
        Map<Character, Byte> letterMapping = new HashMap<>();
        Set<Byte> digitsUsed = new HashSet<>();
        for (int i = 0; i < letterOrder.size(); i++) {
            byte realAllele = getRealAllele(digitsUsed, chromosome[i]);
            letterMapping.put(letterOrder.get(i), realAllele);
            digitsUsed.add(realAllele);
        }
        return letterMapping;
    }

    /**
     * @param digitsUsed set containing digits already paired to a letter
     * @param officialValue allele we are trying to interpret
     * @return officialValue if not in digitsUsed, the next highest value not in digitsUsed otherwise
     *      Uses modulus to roll over to 0 if necessary
     */
    private byte getRealAllele(Set<Byte> digitsUsed, Byte officialValue) {
        // we increase officialValue until we reach an unused value, if necessary
        while (digitsUsed.contains(officialValue)) {
            officialValue = (byte)((officialValue + 1) % 10);
        }
        return officialValue;
    }

    /**
     * @param word made of digits A-Z being converted
     * @param letterMapping of letters A-Z to digits 0-9
     * @return the integer equivalent of the word given, using the given mapping
     */
    private String convertLetterDigitToNumber(String word, Map<Character, Byte> letterMapping) {
        StringBuilder newWord = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            // we add find the mapping of the letter at this index of the word and add it to the number
            newWord.append(letterMapping.get(word.charAt(i)));
        }
        return newWord.toString();
    }

    /**
     * @param chromosome with a correct solution for the problem
     * @return a SolutionI implementation of the chromosome
     */
    private SolutionI getSolution(byte[] chromosome, int nGenerations) {
        // we do this by creating a mapping of letters to numbers from the chromosome
        Map<Character, Byte> letterMapping = getDigitMapping(chromosome);
        // we don't actually need to create the solution, that is for the user to deal with
        // now we create the solution's mapping, which is the reverse of what I care about
        List<Character> numToLetterMapping = new ArrayList<>();
        // annoyingly, there is no easy way to change an array list to a different size
        for (int i = 0; i < 10; i++) {
            numToLetterMapping.add(' ');
        }
        for (Character letter : letterMapping.keySet()) {
            numToLetterMapping.set(letterMapping.get(letter), letter);
        }

        return new SolutionI() {
            @Override
            public List<Character> solution() {
                return numToLetterMapping;
            }
            @Override
            public String getAugend() {
                return augend;
            }
            @Override
            public String getAddend() {
                return addend;
            }
            @Override
            public String getSum() {
                return sum;
            }
            @Override
            public int nGenerations() {
                return nGenerations;
            }
        };
    }

    /**
     * Given an old population, creates a new population of the same size
     * @param oldPopulation from the previous generation
     * @return a new population made from the previous generation
     */
    private byte[][] rouletteSelection(byte[][] oldPopulation, long[] fitness) {
        byte[][] newPopulation = new byte[oldPopulation.length][oldPopulation[0].length];
        // first, we figure out the probability for each
        // we do this by passing through the population twice
        // (this could have been joined with the find fitness step if I knew I was using roulette)
        // first we create the roulette wheel
        TreeMap<Long, byte[]> rouletteWheel = createRouletteWheel(oldPopulation, fitness);
        // when we roll that chromosome, we will make a deep copy of it to newPopulation
        PrimitiveIterator.OfLong wheelHand = new Random().longs(oldPopulation.length, 0, rouletteWheel.lastKey()).iterator();
        for (int i = 0; i < newPopulation.length; i++) {
            newPopulation[i] = rouletteWheel.ceilingEntry(wheelHand.next()).getValue().clone();
            // takes the next random number, finds which entry that corresponds to, and adds a clone of that
            // entry to the slot in newPopulation
        }

        return newPopulation;
    }

    /**
     * Creates a roulette wheel for roulette selection
     * @param oldPopulation that will be added to the new population
     * @param fitness of each member of oldPopulation
     * @return a roulette wheel containing a mapping of cumulative negative fitness to members of oldPopulation
     * Where cheaper members have more space on the wheel, but every member has some space
     */
    private TreeMap<Long, byte[]> createRouletteWheel(byte[][] oldPopulation, long[] fitness) {
        // first, we find the max fitness of the array
        long maxFitness = 0;
        for (long score : fitness) {
            if (score > maxFitness) {
                maxFitness = score;
            }
        }
        //int maxFitness = Arrays.stream(fitness).parallel().max().getAsInt();
        // I'm not using streams, just in case it breaks Leff's build
        // then, we make a treemap of, going through the array, previous + max - this + 1
        TreeMap<Long, byte[]> rouletteWheel = new TreeMap<>();
        long previous = 0;
        for (int i = 0; i < oldPopulation.length; i++) {
            previous = previous + maxFitness - fitness[i] + 1;
            rouletteWheel.put(previous, oldPopulation[i]);
        }
        return rouletteWheel;
        // That way, things with minimum fitness will be more likely to be selected, while ones of high fitness
        // will be much less likely
        // the treemap will map to chromosome
    }

    /**
     * Selects a new population using tournament selection
     * Chooses two random members of the population, and adds the fitter one to the population
     * @param oldPopulation of chromosomes that we are replacing
     * @param fitness of each chromosome
     * @return a new population following tournament rules
     */
    private byte[][] tournamentSelection(byte[][] oldPopulation, long[] fitness) {
        byte[][] newPopulation = new byte[oldPopulation.length][oldPopulation[0].length];
        Random pablo = new Random();
        for (int i = 0; i < oldPopulation.length; i++) {
            List<Integer> candidates = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                candidates.add(pablo.nextInt(newPopulation.length));
            }
            newPopulation[i] = oldPopulation[runTournamentRecursive(candidates, fitness)].clone();
        }
        return newPopulation;
    }

    /**
     * Recursively computes a tournament of candidates and finds the one with the lightest value
     * @param candidates winners of the previous round, must be a power of 2
     * @param fitness of the population
     * @return the index of the winner of the tournament in oldPopulation
     */
    private int runTournamentRecursive(List<Integer> candidates, long[] fitness) {
        if (candidates.size() == 1) { // base case, find tournament winner
            return candidates.get(0);
        }
        List<Integer> nextRound = new ArrayList<>(candidates.size() / 2);
        for (int candidateIndex = 0; candidateIndex < candidates.size(); candidateIndex+=2) {
             nextRound.add(fitness[candidates.get(candidateIndex)] < fitness[candidates.get(candidateIndex + 1)]?
                    // remember, a lower fitness is better, it means a smaller difference
                    candidates.get(candidateIndex) : candidates.get(candidateIndex + 1));
        }
        return runTournamentRecursive(nextRound, fitness);
    }

    /**
     * Creates crossover for random members of the population
     * For each 2 chromosomes chosen, it cuts them at a certain point, and swaps genes before that
     * @param population crossover is being done to
     * @param crossoverChance that any pair of adjacent genes will cross over
     */
    private void crossover(byte[][] population, double crossoverChance) {
        if (crossoverChance == 0) {
            return;
        }
        // if we aren't actually doing crossover, I don't want to waste my time here
        int evenPopulation = population.length - population.length % 2;
        // we can't do crossover with the last one
        PrimitiveIterator.OfDouble pablo = new Random().doubles(evenPopulation / 2, 0, 1).iterator();
        for (int i = 0; i < evenPopulation; i += 2) {
            if (pablo.nextDouble() < crossoverChance) { // random chance for a crossover
                crossChromosomes(population[i], population[i + 1]);
            }
        }
    }

    /**
     * Finds a random location on the chromosomes, and swaps all genes before that
     * @param firstChromosome being crossed over
     * @param secondChromosome being crossed over
     */
    private void crossChromosomes(byte[] firstChromosome, byte[] secondChromosome) {
        int location = new Random().nextInt(firstChromosome.length);
        // location on the chromosome where the crossover will happen
        for (int i = 0; i < location; i++) {
            byte temp = firstChromosome[i];
            firstChromosome[i] = secondChromosome[i];
            secondChromosome[i] = temp;
        }
    }

    /**
     * Creates mutations in the population, changing one digit in each chromosome randomly selected
     * @param population of chromosomes which could be mutated
     * @param mutationChance of each chromosome being mutated
     */
    private void mutate(byte[][] population, double mutationChance) {
        // we can't do crossover with the last one
        PrimitiveIterator.OfDouble pablo = new Random().doubles(population.length, 0, 1).iterator();
        for (byte[] chromosome : population) {
            if (pablo.nextDouble() < mutationChance) { // random chance for a crossover
                mutateChromosome(chromosome);
            }
        }
    }

    /**
     * Mutates a random digit in the chromosome, replacing it with a random other digit
     * @param chromosome being mutated
     */
    private void mutateChromosome(byte[] chromosome) {
        Random pablo = new Random();
        chromosome[pablo.nextInt(chromosome.length)] = (byte) pablo.nextInt(10);
    }

    /*
    * First, I need to figure out how this works
    * I need a way of encoding data that can be read by a fitness function
    * My original idea was to either include a mapping of digits to letters or the other way around
    * But that is going to be hard to mutate or crossover, because any mutation could make the entire
    * mapping invalid, if I now have two As or two 2s
    * So, how to best represent a permutation in a way that is not actually a permutation?
    * Perhaps, when my fitness function reads the input, it will automatically translate duplicates into one bigger
    * For example, when it reads, for a 4-letter problem, 1111, it will interpret it as 1234 and solve accordingly
    * I might think of a better idea later, but for now, this is what I will do
    * They suggest using a binary string, which will make what a mutation does simpler
    * But each letter can hold 10 options, that isn't going to encode well in binary
    * I could make it 16-bit, but then most of the values will be nulls and causing problems
    * So, I need a better solution
    * I think that for now, I will keep it as an array of bytes, and mutations will be +1 (mod 10) to a random
    * location
    * While, a crossover means swapping information
    * But my problem is that currently, ones with a close similarity to the solution won't necessarily be good
    * for the actual solution
    * Let's say I have a + b = c, and am currently situated on 1 + 2 = 4
    * The difference is equal to 1
    * Which, I guess changing by 1 could reach it
    * Instead, let's give ab + bc = cd, for a case of 12 + 23 = 35 or something like that
    * Right now, we have a=1, b=2, c=6, d=5, for 12 + 26 = 65, which is obviously wrong, and is very wrong
    * Since it is so different, its cost would be 27, so it probably won't reproduce
    * But let's contrast it with a=5, b=3, c=4, d=5, for 53 + 34 = 45. Its difference is 42, which, fine, is worse
    * But how about a=2, b=3, c=4, d=5, for 23 + 34 = 45, with a difference of 12, much closer than my first case
    * despite being much further away
    * I'm not sure how to solve this, but lets first code the program, then figure out how to make a good fitness
    * function
    *
    * So, steps of my algorithm.
    * First, I create an initial population of Strings, with the length equal to the number of letters
    * The Strings, right now, are byte arrays
    * Then, for each generation before we either run out or find the answer:
    * We look at each chromosome, and run our fitness function, storing the result in a fitness array
    * Or maybe some other data structure
    * If any of the chromosomes solve the problem, we return that chromosome
    * Then, we select parents for the next generation, however we do that
    * Then, we cross over some of the parents
    * Finally, we mutate some of the next generation, replacing a number with a different one
    * We then finalize the next generation, which might involve something I'm not quite sure of yet
    * Then we repeat with the new generation
    * */

    /*
     * I need to figure out how to improve tournament selection
     * But looking through the code, I can't figure out what's wrong
     * So instead, I would like to make some sort of test that can tell me what is going wrong.
     * So, let's just make a really simple test case.
     * I have only 10 chromosomes, and it is a simple case (A + B = CD). I will only simulate 10
     * generations. I will see the difference between each mode.
     */
}
