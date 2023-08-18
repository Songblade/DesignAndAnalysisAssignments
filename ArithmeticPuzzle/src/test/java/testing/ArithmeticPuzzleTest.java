package testing;

import edu.yu.da.ArithmeticPuzzle;
import edu.yu.da.ArithmeticPuzzleBase;
import edu.yu.da.ArithmeticPuzzleBase.SolutionI;
import edu.yu.da.GeneticAlgorithmConfig;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ArithmeticPuzzleTest {
    // here, I will add all my tests
    // unfortunately, there could be any number of solutions to each puzzle.
    // So, I will not be able to expect a certain solution in my tests
    // Instead, I will need to test the solution I am given for its correctness
    // My correctness tester will go through a given solution and find the digits of each answer
    // It will make sure that each letter corresponds to a single digit, and that the answers are of the
        // appropriate length
    // I have poorly defined this
    // For each digit, it must store its current value and also all the locations it conforms to
    // But making this test suite is proving more complicated than I am willing to spend time on, so I will
        // evaluate numbers by hand
    // I will make the test suite later if I really want it

    private SolutionI displayTest(String augend, String addend, String sum) {
        int maxGens = 100;
        System.out.println("\nTesting with " + augend + " + " + addend + " = " + sum);
        ArithmeticPuzzleBase enigma = new ArithmeticPuzzle(augend, addend, sum);
        SolutionI solution = enigma.solveIt(new GeneticAlgorithmConfig(1000, maxGens,
                GeneticAlgorithmConfig.SelectionType.TOURNAMENT, 0.8, 0.7));
        System.out.println("Number of generations: " + solution.nGenerations() + " out of " + maxGens);
        System.out.println("Solution: " + solution.solution());
        if (!solution.solution().isEmpty()) {
            long augendNumber = convertLetterDigitToNumber(augend, solution.solution());
            long addendNumber = convertLetterDigitToNumber(addend, solution.solution());
            long sumNumber = convertLetterDigitToNumber(sum, solution.solution());
            System.out.println("Solution: " + augendNumber + " + " + addendNumber + " = " + sumNumber);
            assertEquals(0, augendNumber + addendNumber - sumNumber,
                    "augend + addend != solution");
        } else {
            assert false: "No solution calculated";
        }
        return solution;
    }

    private long convertLetterDigitToNumber(String word, List<Character> solution) {
        Map<Character, Byte> invertedConverter = new HashMap<>();
        for (byte i = 0; i < solution.size(); i++) {
            if (solution.get(i) != ' ') {
                invertedConverter.put(solution.get(i), i);
            }
        }
        StringBuilder newWord = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            // we add find the mapping of the letter at this index of the word and add it to the number
            newWord.append(invertedConverter.get(word.charAt(i)));
        }
        return Long.parseLong(newWord.toString());
    }

    // a + b = c, where solution can be 1 + 2 = 3 or others
    @Test
    public void oneDigitTest() {
        displayTest("A", "B", "C");
    }

    // test where a + b = cd, where solution can be 5 + 7 = 12
    @Test
    public void oneToTwoDigitsTest() {
        displayTest("A", "B", "CD");
    }

    // test where a + b = cde, where c must be 0 but the rest is unchanged from previous
    @Test
    public void firstIs0Test() {
        SolutionI solution = displayTest("A", "B", "CDE");
        assertEquals('C', solution.solution().get(0));
    }

    // test where a + b = a, where b has to be zero and a is non-zero
    @Test
    public void identityTest() {
        SolutionI solution = displayTest("A", "B", "A");
        assertEquals(0, convertLetterDigitToNumber("B", solution.solution()));
        assertEquals('B', solution.solution().get(0));
    }

    // test where a + a = a, where a must be zero
    @Test
    public void zeroTest() {
        SolutionI solution = displayTest("A", "A", "A");
        assertEquals(0, convertLetterDigitToNumber("A", solution.solution()));
        assertEquals(List.of('A', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '), solution.solution());
    }

    // test where aaa + aaaaa = a, where a still must be zero
    @Test
    public void bigZeroTest() {
        SolutionI solution = displayTest("AAA", "AAAAA", "A");
        assertEquals(0, convertLetterDigitToNumber("AAA", solution.solution()));
        assertEquals(0, convertLetterDigitToNumber("AAAAA", solution.solution()));
        assertEquals(0, convertLetterDigitToNumber("A", solution.solution()));
        assertEquals(List.of('A', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '), solution.solution());
    }

    // test where a + b = cc, which must be 5 + 6 = 11
    @Test
    public void sum11Test() {
        SolutionI solution = displayTest("A", "B", "CC");
        assertEquals(11, convertLetterDigitToNumber("CC", solution.solution()));
        assertEquals('C', solution.solution().get(1));
    }

    // test where a + a = b, which could be 1 + 1 = 2
    @Test
    public void smallDoubleTest() {
        SolutionI solution = displayTest("A", "A", "B");
        assertEquals(convertLetterDigitToNumber("B", solution.solution()),
                convertLetterDigitToNumber("A", solution.solution()) * 2);
    }

    // test where a + a = bc, which could be 6 + 6 = 12
    @Test
    public void oneToTwoDigitDoubleTest() {
        SolutionI solution = displayTest("A", "A", "BC");
        assertEquals(convertLetterDigitToNumber("BC", solution.solution()),
                convertLetterDigitToNumber("A", solution.solution()) * 2);
    }

    // test where aa + aa = cc, which could be 11 + 11 = 22
    @Test
    public void biggerDoubleTest() {
        SolutionI solution = displayTest("AA", "AA", "CC");
        assertEquals(convertLetterDigitToNumber("CC", solution.solution()),
                convertLetterDigitToNumber("AA", solution.solution()) * 2);
    }


    // test where aa + aa = bcd, which could be 66 + 66 = 132
    @Test
    public void twoToThreeDigitDoubleTest() {
        SolutionI solution = displayTest("AA", "AA", "BCD");
        assertEquals(convertLetterDigitToNumber("BCD", solution.solution()),
                convertLetterDigitToNumber("AA", solution.solution()) * 2);    }

    // test where abcde + fghij = hjbce, which could be 12345 + 67890 = 80235
    @Test
    public void bigNumberTest() {
        displayTest("ABCDE", "FGHIJ", "HJBCE");
    }

    // test where ababa + babab = ccccc, which could be 12121 + 21212 = 33333
    @Test
    public void recombinationTest() {
        displayTest("ABABA", "BABAB", "CCCCC");
    }

    // test where aaabb + cdbae = fgehhi, which could be 55566 + 78654 = 134220
    @Test
    public void evenBiggerTest() {
        displayTest("AAABB", "CDBAE", "FGEHHI");
    }

    // test where CIGHGJHCAF + DADJACJEEA = HAAHHCHHFG
    // which could be 3978708316 + 4140130551 = 8118838867
    @Test
    public void ridiculouslyBigTest() {
        displayTest("CIGHGJHCAF", "DADJACJEEA", "HAAHHCHHFG");
    }

    // test where a + b = aa, where only makes sense if a = b = 0, so returns a failed answer
    @Test
    public void impossibleCaseTest() {
        assertThrows(AssertionError.class,()->displayTest("A", "B", "AA"));
    }

    // now I need to test that an IAE is thrown if any string is empty
    @Test
    public void emptyStringIAE() {
        assertThrows(IllegalArgumentException.class, ()->new ArithmeticPuzzle("", "A", "B"));
        assertThrows(IllegalArgumentException.class, ()->new ArithmeticPuzzle("A", "", "B"));
        assertThrows(IllegalArgumentException.class, ()->new ArithmeticPuzzle("B", "A", ""));
    }
}
