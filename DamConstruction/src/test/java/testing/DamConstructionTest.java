package testing;

import edu.yu.da.DamConstruction;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DamConstructionTest {
    // this will contain all my tests

    // tests for cost
    // test that for a single dam, it gets it right
    @Test
    public void singleDamCostTest() {
        int[] dams = {1};
        DamConstruction damRegulator = new DamConstruction(dams, 4);
        assertEquals(4, damRegulator.cost(dams));
    }

    // test for 2 dams where order doesn't matter, it gets it right both ways
    @Test
    public void doubleDamOrderIrrelevantCostTest() {
        int[] dams = {1, 3};
        DamConstruction damRegulator = new DamConstruction(dams, 4);
        assertEquals(7, damRegulator.cost(dams));

        dams[0] = 3; dams[1] = 1;
        assertEquals(7, damRegulator.cost(dams));
    }

    // test for 2 dams where order does matter, it gets it right both ways
    @Test
    public void doubleDamOrderRelevantCostTest() {
        int[] dams = {1, 2};
        DamConstruction damRegulator = new DamConstruction(dams, 4);
        assertEquals(7, damRegulator.cost(dams));

        dams[0] = 2; dams[1] = 1;
        assertEquals(6, damRegulator.cost(dams));
    }

    // test for 3 dams where order partially matters, gets it right 3 ways
    @Test
    public void tripleDamOrderPartiallyRelevantCostTest() {
        int[] dams = {1, 2, 3};
        DamConstruction damRegulator = new DamConstruction(dams, 4);
        assertEquals(9, damRegulator.cost(dams));

        dams[0] = 2; dams[1] = 1; dams[2] = 3;
        assertEquals(8, damRegulator.cost(dams));

        dams[0] = 1; dams[1] = 3; dams[2] = 2;
        assertEquals(9, damRegulator.cost(dams));
    }

    // test for 3 dams where order fully matters, it gets it right 3 ways
    @Test
    public void tripleDamOrderFullyRelevantCostTest() {
        int[] dams = {1, 2, 3};
        DamConstruction damRegulator = new DamConstruction(dams, 5);
        assertEquals(12, damRegulator.cost(dams));

        dams[0] = 2; dams[1] = 1; dams[2] = 3; // 5 + 2 + 3 = 10
        assertEquals(10, damRegulator.cost(dams));

        dams[0] = 1; dams[1] = 3; dams[2] = 2; // 5 + 4 + 2 = 11
        assertEquals(11, damRegulator.cost(dams));

        dams[0] = 3; dams[1] = 2; dams[2] = 1; // 5 + 3 + 2 = 10
        assertEquals(10, damRegulator.cost(dams));
    }

    // test for many dams, it gets it right 1 way (not doing 55!)
    // even though he's not going above 55, I'm going to a million, because my code should be able
    // to handle it
    // unfortunately, I can't get a good test that big without making it simple
    // (or automating the solution)
    @Test
    public void manyDamsCostTest() {
        int[] dams = new int[1_000_000];
        int cost = 0;
        for (int i = 0; i < 1_000_000; i++) {
            dams[i] = i + 1;
            cost += 2_000_000 - i;
        }

        DamConstruction damRegulator = new DamConstruction(dams, 2_000_000);
        assertEquals(cost, damRegulator.cost(dams));
    }

    // test that it gets it right for a 1-dam system
    @Test
    public void singleDamSolveTest() {
        int[] dams = {1};
        DamConstruction damRegulator = new DamConstruction(dams, 4);
        assertEquals(4, damRegulator.solve());
    }

    // test for 2 dams where order doesn't matter
    @Test
    public void doubleDamOrderIrrelevantSolveTest() {
        int[] dams = {1, 3};
        DamConstruction damRegulator = new DamConstruction(dams, 4);
        assertEquals(7, damRegulator.solve());
    }

    // test for 2 dams where order does matter
    @Test
    public void doubleDamOrderRelevantSolveTest() {
        int[] dams = {1, 2};
        DamConstruction damRegulator = new DamConstruction(dams, 4);
        assertEquals(6, damRegulator.solve());
    }

    // test for 3 dams where order partially matters
    @Test
    public void tripleDamOrderPartiallyRelevantSolveTest() {
        int[] dams = {1, 2, 3};
        DamConstruction damRegulator = new DamConstruction(dams, 4);
        assertEquals(8, damRegulator.solve());
    }

    // test for 3 dams where order fully matters
    @Test
    public void tripleDamOrderFullyRelevantSolveTest() {
        int[] dams = {1, 2, 3};
        DamConstruction damRegulator = new DamConstruction(dams, 5);
        assertEquals(10, damRegulator.solve());
    }

    // test for a complex 6-dam system
    // let's have number 4 first. On the right we will do 6 then 5. On the left we will do 1, 3, 2.
    // so, 4 will be exactly in the middle, so to incentivise the right behavior (I think)
    // 1, 2, 3, will be right next to 4, but with a 1 space gap to 4 and a big gap to 0
    // the gap between 6 and the end is bigger than 6 and 5
    // So, let's put 4 at 10
    // 1, 2, 3 will be at 6, 7, 8
    // 5 is at 13 and 6 at 15
    // Let's hope my order is the right one
    // first we do 4 at a cost of 20
    // 1 is a cost of 10
    // 3 is a cost of 4
    // 2 is a cost of 2
    // 6 is a cost of 10
    // 5 is a cost of 5
    // I really hope this is correct
    // |-----123-4--5-6----|
    @Test
    public void sixDamSolveTest() {
        int[] dams = {6, 7, 8, 10, 13, 15};
        DamConstruction damRegulator = new DamConstruction(dams, 20);
        assertEquals(51, damRegulator.solve());
    }

    // test for a 55-dam system, the most he wants
    @Test
    public void manyDamsSolveTest() {
        int[] dams = IntStream.rangeClosed(1, 55).toArray();
        int[] damsIncludingEnds = IntStream.rangeClosed(0, 56).toArray();
        int cost = recursivelyCalculateCost(damsIncludingEnds, 0, 56);

        DamConstruction damRegulator = new DamConstruction(dams, 56);
        assertEquals(cost, damRegulator.solve());
    }

    private int recursivelyCalculateCost(int[] dams, int left, int right) {
        if (right - left < 2) {
            return 0;
        }
        int mid = (left + right) / 2;
        return dams[right] - dams[left] + recursivelyCalculateCost(dams, left, mid) + recursivelyCalculateCost(dams, mid, right);
    }

    // test that the client does maintain ownership of the array
    @Test
    public void clientMaintainsOwnershipSolveTest() {
        int[] dams = {1, 3};
        DamConstruction damRegulator = new DamConstruction(dams, 4);
        dams[1] = 2; // if it takes this into account, it should get 6 instead
        assertEquals(7, damRegulator.solve());
    }

}
