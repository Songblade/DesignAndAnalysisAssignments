package edu.yu.da;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

/** Defines the API for specifying and solving the DamConstruction problem (see
 * the requirements document).
 * Students MAY NOT change the public API of this class, nor may they add ANY
 * constructor.
 *
 * @author Avraham Leff
 */

public class DamConstruction {

    /*
     * @TODO Write tests for solve()
     * @TODO Write Big-O measurement test for solve() and cost()
     * @TODO Write cost()
     * @TODO Write solve()
     * @TODO Consider learning logging
     */

    final int riverEnd;
    final int[] damLocations;

    /** Constructor
     *
     * @param Y y-positions specifying dam locations, sorted by ascending
     * y-values.  Client maintains ownership of this parameter.  Y must contain
     * at least one element.
     * @param riverEnd the y-position of the river's end (a dam was previously
     * constructed both at this position and at position 0 and no evaluation will be
     * made of their construction cost): all values in Y are both greater than 0
     * and less than riverEnd.
     * @note students need not verify correctness of either parameter.  On the
     * other hand, for your own sake, I suggest that you add these (easy to do)
     * "sanity checks".
     */
    public DamConstruction(final int[] Y, final int riverEnd) {
        if (riverEnd < Y.length + 1) {
            throw new IllegalArgumentException("riverEnd " + riverEnd + " must leave space for at least " + Y.length + " dams");
        }
        cleanY(Y, riverEnd);
        this.riverEnd = riverEnd;
        damLocations = new int[Y.length + 2];
        damLocations[0] = 0;
        damLocations[Y.length + 1] = riverEnd;
        System.arraycopy(Y, 0, damLocations, 1, Y.length);
        // index 0 corresponds to dam 0, already built
        // indices 1...damLoc.length correspond to damLoc[i - 1]
        // index damLoc.length + 1 corresponds to dam riverEnd
    } // constructor

    /**
     * Makes sure that the dams given are all valid and in different positions, throws IAE otherwise
     * @param dams that were given as input
     * @param end of the river, with the dam already built
     */
    private void cleanY(int[] dams, int end) {
        cleanArray(dams, "Y");
        Set<Integer> damsSeen = new HashSet<>(dams.length); // so we can make sure there are no duplicate dams
        for (int dam : dams) {
            if (dam <= 0) {
                throw new IllegalArgumentException("Dam " + dam + " must have a positive location");
            }
            if (dam >= end) {
                throw new IllegalArgumentException("Dam " + dam + " must be before the river's end at " + end);
            }
            if (damsSeen.contains(dam)) {
                throw new IllegalArgumentException("Dam " + dam + " is in the input twice");
            }
            damsSeen.add(dam);
        }
    }

    private void cleanArray(int[] damArray, String name) {
        if (damArray == null) {
            throw new IllegalArgumentException("Dam " + name + " is null");
        }
        if (damArray.length == 0) {
            throw new IllegalArgumentException("Dam " + name + " is empty");
        }
    }

    /** Solves the DamConstruction problem, returning the minimum possible cost
     * of evaluating the environmental impact of dam construction over all
     * possible construction sequences.
     *
     * @return the minimum possible evaluation cost.
     */
    public int solve() {
        int[][] costTable = new int[damLocations.length][damLocations.length];
        // this is used for my dynamic programming
        // the first index is the left end of the river, the second index the right, where l < r
        // uses the same indices as damLocations
        // I don't need to fill in cases of 0 or 1, because I would be filling them in as 0 anyway, which is
            // already the default value
        // now we fill in for offsets starting at 2 and increasing to length
        for (int offset = 2; offset < damLocations.length; offset++) {
            // we fill in (i, i + offset) for each i  < length - offset
            for (int leftEnd = 0; leftEnd < damLocations.length - offset; leftEnd++) {
                int leftDam = leftEnd; // used so the lambda doesn't throw a fit
                int rightDam = leftEnd + offset;
                // the triple loop. We check all dams between leftEnd and leftEnd + offset, seeing which is cheapest
                costTable[leftEnd][leftEnd + offset] = IntStream.range(leftEnd + 1, leftEnd + offset)
                        .map(i->costTable[leftDam][i] + costTable[i][rightDam])
                        .min().getAsInt()
                        + damLocations[rightDam] - damLocations[leftDam]; // will not have a null point, because this will work every time
                // I could have made this parallel, but it will complicate the writeup
            }
        }

        // for a length 5 array, last index is 4
        // with offset of 3, last index we care about is (1, 4)
        // which is length - offset - 1

        return costTable[0][damLocations.length - 1];
    }

    /** Returns the cost of applying the dam evaluation decisions in the
     * specified order against the dam locations and river end state supplied to
     * the constructor.
     *
     * @param evaluationSequence elements of the Y parameter supplied in the
     * constructor, possibly rearranged such that the ith element represents the
     * y-position that is to be the ith dam evaluated for the WPA.  Thus: if Y =
     * {2, 4, 6}, damDecisions may be {4, 6, 2}: this method will return the cost
     * of evaluating the entire set of y-positions when dam evaluation is done
     * first for position "4", then for position "6", finally for position "2".
     * @return the cost of dam evaluation for the entire sequence of dam
     * positions when performed in the specified order.
     * @note This method is conceptually a static method because it doesn't
     * depend on the optimal solution produced by solve().  OTOH: the
     * implementation does require access to both the Y array and "river end"
     * information supplied to the constructor.
     * @note the implementation of this method is (almost certainly) not the
     * dynamic programming algorithm used in solve().  This method is part of the
     * API to stimulate your thinking as you work through this assignment and to
     * exercise your software engineering muscles.
     */
    public int cost(final int[] evaluationSequence) {
        cleanArray(evaluationSequence, "evaluationSequence");
        // to solve this, I will keep a tree set of all the dams examined so far (starting with 0 and end)
        // when I examine a dam, I will find its floor and ceiling and find the difference between them, adding it
        // efficiency is O(nlgn)
        int cost = 0;
        TreeSet<Integer> damsExamined = new TreeSet<>();
        damsExamined.add(0);
        damsExamined.add(riverEnd);

        for (int damLocation : evaluationSequence) {
            cost += damsExamined.ceiling(damLocation) - damsExamined.floor(damLocation);
            // there will be no null pointer exception, because all ints in evaluationSequence are > 0 and < riverEnd
            damsExamined.add(damLocation);
        }

        return cost;
    }
} // class
