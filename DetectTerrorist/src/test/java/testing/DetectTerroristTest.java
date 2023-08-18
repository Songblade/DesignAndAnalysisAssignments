package testing;

import edu.yu.da.DetectTerrorist;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DetectTerroristTest {

    // all the complicated and confusing stuff is in what we are allowed to do to solve the problem
    // the actual problem is quite clear
    // and this is a really simple problem, it is hard to devise a lot of tests

    private void runTest(int expected, int... passengers) {
        DetectTerrorist mossad = new DetectTerrorist(passengers);
        assertEquals(expected, mossad.getTerrorist());
    }

    // I need a test that if there is a single passenger, it detects that as the terrorist
    @Test
    public void passenger1Test() {
        runTest(0, 1);
    }

    // a test that with 2 passengers, it correctly determines the terrorist
    @Test
    public void passenger2Test() {
        runTest(0, 1, 2);
    }

    // a test with 3
    @Test
    public void passenger3Test() {
        runTest(0, 1, 2, 2);
    }

    // a test with 4
    @Test
    public void passenger4Test() {
        runTest(0, 1, 2, 2, 2);
    }

    // a test with 10
    @Test
    public void passenger10Test() {
        runTest(0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2);
    }

    // a test with 15
    @Test
    public void passenger15Test() {
        runTest(0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2);
    }

    // a test with 16
    @Test
    public void passenger16Test() {
        runTest(0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2);
    }

    // the above, but the weight numbers are much bigger
    @Test
    public void bigWeightTest() {
        runTest(0, 30000, 300000, 300000, 300000, 300000, 300000, 300000, 300000, 300000, 300000, 300000, 300000, 300000, 300000, 300000, 300000);
    }

    // a test where one of the numbers is negative
    @Test
    public void negativeTerroristTest() {
        runTest(0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    // a test where both numbers are negative
    @Test
    public void allNegativeTest() {
        runTest(0, -2, -1, -1, -1, -1, -1, -1, -1, -1, -1);
    }

    // a test of 15 where the terrorist is in slot 7
    @Test
    public void slot7Test() {
        runTest(7, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2);
    }

    // a test where the terrorist is in slot 15
    @Test
    public void slot15Test() {
        runTest(14, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1);
    }

    // a test where the terrorist is in slot 4
    @Test
    public void slot4Test() {
        runTest(4, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2);
    }

    @Test
    public void largeTest() {
        int[] passengers = new int[1_000_000];
        // I want to guarantee that the algorithm takes the maximum amount of time on each search
        // If I make it a power of 2, that always happens, but I don't want to do that
        // Wait, I can just make the first one the terrorist, since I always chop off odd things at the end
        passengers[0] = -1;
        runTest(0, passengers);
    }

    // a test that with a length 13 array (to be unlucky, unless that was 14), every slot works
    // to figure out what Professor Leff's problem was
    @Test
    public void everySlotTest() {
        int[] passengers = new int[13];
        // I want to guarantee that the algorithm takes the maximum amount of time on each search
        // If I make it a power of 2, that always happens, but I don't want to do that
        // Wait, I can just make the first one the terrorist, since I always chop off odd things at the end
        for (int i = 0; i < passengers.length; i++) {
            passengers[i] = -1;
            if (i != 0) {
                passengers[i - 1] = 0;
            }
            System.out.println("Running tests with terrorist in " + i);
            runTest(i, passengers);
        }
    }


}
