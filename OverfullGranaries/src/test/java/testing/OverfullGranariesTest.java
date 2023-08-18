package testing;

import edu.yu.da.OverfullGranaries;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OverfullGranariesTest {

    // for all of these, I test both solveIt() and minCut()
    // simple case where just one X leads directly to one Y
    @Test
    public void onePipeTest() {
        String[] xs = {"A"};
        String[] ys = {"B"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "B", 200);

        assertEquals(50, storehouse.solveIt());

        assertEquals(List.of("A"), storehouse.minCut());
    }

    // the same case, but with a different capacity on the edge
    @Test
    public void unevenCapacityTest() {
        String[] xs = {"A"};
        String[] ys = {"B"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "B", 300);

        assertEquals((10000.0 / 300), storehouse.solveIt());
    }

    // a case where one X leads to two Ys
    @Test
    public void xBranchTest() {
        String[] xs = {"A"};
        String[] ys = {"B", "D"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "B", 200);
        storehouse.edgeExists("A", "D", 200);

        assertEquals(25, storehouse.solveIt());

        assertEquals(List.of("A"), storehouse.minCut());
    }

    // a case where two Xs lead to one Y
    @Test
    public void yBranchTest() {
        String[] xs = {"A", "C"};
        String[] ys = {"B"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "B", 200);
        storehouse.edgeExists("C", "B", 200);

        assertEquals(25, storehouse.solveIt());

        assertEquals(List.of("A", "C"), storehouse.minCut());
    }

    // a case where two Xs lead to two Ys but don't cross
    @Test
    public void twoLineTest() {
        String[] xs = {"A", "C"};
        String[] ys = {"B", "D"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "B", 200);
        storehouse.edgeExists("C", "D", 200);

        assertEquals(25, storehouse.solveIt());

        assertEquals(List.of("A", "C"), storehouse.minCut());
    }

    // a case where one X leads to one Y, and the other X leads to both Ys
    @Test
    public void singleCrossTest() {
        String[] xs = {"A", "C"};
        String[] ys = {"B", "D"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "B", 200);
        storehouse.edgeExists("C", "B", 200);
        storehouse.edgeExists("C", "D", 200);

        assertEquals((10000.0 / 600), storehouse.solveIt());

        assertEquals(List.of("A", "C"), storehouse.minCut());
    }

    // a case where each X leads to 2 Ys, but one also leads to a third
    @Test
    public void doubleCrossTest() {
        String[] xs = {"A", "C"};
        String[] ys = {"B", "D", "F"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "B", 200);
        storehouse.edgeExists("C", "B", 200);
        storehouse.edgeExists("A", "D", 200);
        storehouse.edgeExists("C", "D", 200);
        storehouse.edgeExists("C", "F", 200);

        assertEquals(10, storehouse.solveIt());

        assertEquals(List.of("A", "C"), storehouse.minCut());
    }

    // an ABC chain
    @Test
    public void abcChainTest() {
        String[] xs = {"A"};
        String[] ys = {"B"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "apple", 200);
        storehouse.edgeExists("apple", "B", 200);

        assertEquals(50, storehouse.solveIt());

        assertEquals(List.of("A"), storehouse.minCut());
    }

    // a case where X goes to an intermediary that splits
    @Test
    public void xAntibodyTest() {
        String[] xs = {"A"};
        String[] ys = {"B", "D"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "apple", 200);
        storehouse.edgeExists("apple", "B", 200);
        storehouse.edgeExists("apple", "D", 200);

        assertEquals(50, storehouse.solveIt());

        assertEquals(List.of("A"), storehouse.minCut());
    }

    // a case where Y goes to an intermediary that splits
    @Test
    public void yAntibodyTest() {
        String[] xs = {"A", "C"};
        String[] ys = {"B"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "apple", 200);
        storehouse.edgeExists("C", "apple", 200);
        storehouse.edgeExists("apple", "B", 200);

        assertEquals(50, storehouse.solveIt());

        assertEquals(List.of("A", "C", "apple"), storehouse.minCut());
    }

    // a case where 2 Xs lead to an intermediate that leads to 2 Ys
    @Test
    public void xCrossoverTest() {
        String[] xs = {"A", "C"};
        String[] ys = {"B", "D"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "apple", 200);
        storehouse.edgeExists("C", "apple", 200);
        storehouse.edgeExists("apple", "B", 200);
        storehouse.edgeExists("apple", "D", 200);

        assertEquals(25, storehouse.solveIt());

        assertEquals(List.of("A", "C"), storehouse.minCut());
    }

    // same as the above, but one left leg is smaller
    @Test
    public void xCrossoverWithWeakLeftLegTest() {
        String[] xs = {"A", "C"};
        String[] ys = {"B", "D"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "apple", 200);
        storehouse.edgeExists("C", "apple", 100);
        storehouse.edgeExists("apple", "B", 200);
        storehouse.edgeExists("apple", "D", 200);

        assertEquals((10000.0 / 300), storehouse.solveIt());

        assertEquals(List.of("A", "C"), storehouse.minCut());
    }

    // same as the above, but one right leg is smaller
    @Test
    public void xCrossoverWithWeakRightLegTest() {
        String[] xs = {"A", "C"};
        String[] ys = {"B", "D"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "apple", 200);
        storehouse.edgeExists("C", "apple", 200);
        storehouse.edgeExists("apple", "B", 200);
        storehouse.edgeExists("apple", "D", 100);

        assertEquals((10000.0 / 300), storehouse.solveIt());

        assertEquals(List.of("A", "C", "apple"), storehouse.minCut());
    }

    // a case where the bottleneck doesn't directly connect to X or Y
    @Test
    public void middleBottleneckTest() {
        String[] xs = {"A"};
        String[] ys = {"B"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "apple", 200);
        storehouse.edgeExists("apple", "banana", 100);
        storehouse.edgeExists("banana", "B", 200);

        assertEquals(100, storehouse.solveIt());

        assertEquals(List.of("A", "apple"), storehouse.minCut());
    }

    // a complicated case
    @Test
    public void complicatedTest() {
        String[] xs = {"A", "C"};
        String[] ys = {"B", "D", "F"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "apple", 200);
        storehouse.edgeExists("C", "apple", 100);
        storehouse.edgeExists("C", "B", 100);
        storehouse.edgeExists("apple", "banana", 100);
        storehouse.edgeExists("apple", "F", 100);
        storehouse.edgeExists("cranberry", "D", 100);
        storehouse.edgeExists("banana", "apple", 100);
        storehouse.edgeExists("banana", "D", 100);

        assertEquals((10000.0 / 300), storehouse.solveIt());

        assertEquals(List.of("A", "C", "apple"), storehouse.minCut());
    }

    // a case with a large case size, to know we can do it in a reasonable amount of time
    // Since we have an approximately n^2 algorithm, we should test that we can do a large test
    // Let's test multiple shapes
    // First, let's do a single line, with a-b-c-...-z semantics, but with numbers for ease of use
    // Because this is a line, it will only have one loop, so we should be able to do a million no problem
    @Test
    public void bigLineTest() {
        String[] xs = {"A"};
        String[] ys = {"B"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "0", 200);
        for (int i = 0; i < 1_000_000; i++) {
            storehouse.edgeExists(Integer.toString(i), Integer.toString(i + 1), 200);
        }
        storehouse.edgeExists("1000000", "B", 200);

        assertEquals(50, storehouse.solveIt());

        assertEquals(List.of("A"), storehouse.minCut());
    }

    // Then we will do a tree, which is still simple but easy to make
    // Since this is n^2, we will make the bottom layer 1000 with 1
    // We will make the layer above 100 with 10
    // We will make the layer above that 10 with 100
    // And I think those 10 will all be the roots
    @Test
    public void bigTreeTest() {
        String[] xs = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String[] ys = new String[1000];
        for (int i = 0; i < 1000; i++) {
            if (i < 10) {
                ys[i] = "00" + i;
            } else if (i < 100) {
                ys[i] = "0" + i;
            } else {
                ys[i] = "" + i;
            }
        }
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        // first, we add edges from single-digit to double-digit, with capacity 10
        // then we add edges from double-digit to triple digit
        for (char i = '0'; i <= '9'; i++) {
            for (char j = '0'; j <= '9'; j++) {
                storehouse.edgeExists("" + i, "" + i + j, 10);
                for (char k = '0'; k <= '9'; k++) {
                    storehouse.edgeExists("" + i + j, "" + i + j + k, 1);
                }
            }
        }
        assertEquals(10, storehouse.solveIt());

        assertEquals(List.of(xs), storehouse.minCut());
    }

    // Then maybe we'll figure out something more complicated
    // Hmm. How would we even have something more complicated?
    // Let's have the previous one, but: the final edges are 2 with 2. Each thing gets it from i - 1 and i (using
    // modulo to make it go around evenly). Then, the But, the previous things only get 30, so the maxFlow is 30
    @Test
    public void bigComplicatedTreeTest() {
        String[] xs = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String[] ys = new String[1000];
        for (int i = 0; i < 1000; i++) {
            if (i < 10) {
                ys[i] = "00" + i;
            } else if (i < 100) {
                ys[i] = "0" + i;
            } else {
                ys[i] = "" + i;
            }
        }
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        // first, we add edges from single-digit to double-digit, with capacity 10
        // then we add edges from double-digit to triple digit
        for (char i = '0'; i <= '9'; i++) {
            for (char j = '0'; j <= '9'; j++) {
                storehouse.edgeExists("" + i, "" + i + j, 30);
                for (char k = '0'; k <= '9'; k++) {
                    storehouse.edgeExists("" + i + j, "" + i + j + k, 2);
                    storehouse.edgeExists("" + i + j, "" + i + ((j + 1) % 10) + k, 2);
                }
            }
        }
        assertEquals((10.0/3), storehouse.solveIt());

        assertEquals(List.of(xs), storehouse.minCut());
    }

    // the next tests are for weird edge cases
    // a case where we can move all bushels quicker than 10000/hour
    @Test
    public void superFastPipePipeTest() {
        String[] xs = {"A"};
        String[] ys = {"B"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "B", 20000);

        assertEquals(0.5, storehouse.solveIt());

        assertEquals(List.of("A"), storehouse.minCut());
    }

    // a case with segments connecting two Xs or two Ys
    @Test
    public void extraneousPipesTest() {
        String[] xs = {"A", "C"};
        String[] ys = {"B", "D"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "B", 200);
        storehouse.edgeExists("C", "D", 200);
        storehouse.edgeExists("A", "C", 2000);
        storehouse.edgeExists("B", "D", 2000);

        assertEquals(25, storehouse.solveIt());

        assertEquals(List.of("A", "C"), storehouse.minCut());
    }

    // a case with segments that go to places that never actually connect to a sink
    @Test
    public void earlyTangentPipeTest() {
        String[] xs = {"A"};
        String[] ys = {"B"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "B", 200);
        storehouse.edgeExists("A", "orangutan", 20000);

        assertEquals(50, storehouse.solveIt());

        assertEquals(List.of("A", "orangutan"), storehouse.minCut());
    }

    // like above, but the segments don't show up in the min cut
    @Test
    public void lateTangentPipeTest() {
        String[] xs = {"A"};
        String[] ys = {"B"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "apple", 200);
        storehouse.edgeExists("apple", "B", 200);
        storehouse.edgeExists("apple", "orangutan", 2000);

        assertEquals(50, storehouse.solveIt());

        assertEquals(List.of("A"), storehouse.minCut());
    }

    // test for when there is no path from the source to the sink
    // we are supposed to have Double.POSITIVE_INFINITY for solveIt, and an empty list for minCut
    // this will probably start off wrong, because I haven't yet built a response
    @Test
    public void disconnectedTest() {
        String[] xs = {"A", "C"};
        String[] ys = {"B"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "C", 200);

        assertEquals(Double.POSITIVE_INFINITY, storehouse.solveIt());

        assertEquals(List.of(), storehouse.minCut());
    }

    // test that throws ISE if minCut called before solveIt
    @Test
    public void earlyMinCutISETest() {
        String[] xs = {"A"};
        String[] ys = {"B"};
        OverfullGranaries storehouse = new OverfullGranaries(xs, ys);
        storehouse.edgeExists("A", "B", 200);

        assertThrows(IllegalStateException.class, storehouse::minCut);
    }

}
