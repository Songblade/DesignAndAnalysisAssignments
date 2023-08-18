package edu.yu.da;

import java.util.Arrays;

/** Defines the API for specifying and solving the DetectTerrorist problem (see
 * the requirements document).
 *
 * Students MAY NOT change the public API of this class, nor may they add ANY
 * constructor.
 *
 * @author Avraham Leff
 */
public class DetectTerrorist {

    private final int[] passengers;
    private final int terrorist;

    /** Constructor: represents passengers to be detected as an array in which
     * the ith value is the weight of the ith passenger.  After the constructor
     * completes, clients can invoke getTerrorist() for a O(1) lookup cost.
     *
     * @param passengers an array of passenger weights, indexed 0...n-1.  All
     * passengers that are not terrorists have the same weight: that weight is
     * greater than the weight of the terrorist.  Exactly one passenger is a
     * terrorist.
     */
    public DetectTerrorist(final int[] passengers) {
        this.passengers = passengers; // so I don't have to pass it as a parameter everywhere
        // first, let's clean the array
        cleanPassengers();
        // we call the recursive method from the constructor
        terrorist = findTerrorist(0, passengers.length - 1);
        // I am making it a return to avoid side effects
    }   // constructor

    /**
     * Checks that the input is valid
     */
    private void cleanPassengers() {
        // we will have to assume that it has all passengers with equal weight bags except the terrorist
        // but detecting that is prohibitive, so I will leave that to the Mossad
        // but I can make sure it isn't empty
        if (passengers == null) {
            throw new IllegalArgumentException("passengers is null");
        } else if (passengers.length == 0) {
            throw new IllegalArgumentException("passengers is empty");
        }
    }

    /**
     * A recursive method to find the terrorist
     * Called where leftPointer = 0 and rightPointer = length - 1
     * @param leftPointer the leftmost part of the array where the terrorist might be
     * @param rightPointer the rightmost part of the array where the terrorist might be
     * @return the index of the terrorist in the array
     */
    private int findTerrorist(int leftPointer, int rightPointer) {
        if (leftPointer == rightPointer) {
            // the base case, where the pointers intersect, we have our terrorist
            return leftPointer; // the terrorist index
        } // if we are still here, then the pointers are not equal, and we have a recursive case
        if ((leftPointer + rightPointer) % 2 == 1) {
            // this means that there is an even number of passengers between the pointers
            // so we can just divide the pointers in half without worrying about it
            int middlePointer = (leftPointer + rightPointer) / 2; // the last index of the first subset
            // the first index of the second subset is equal to middlePointer + 1
            // I calculate it outside the method so I can use it again
            int comparison = compareSubsets(leftPointer, middlePointer, rightPointer);
            assert comparison != 0; // because if so, it means that the terrorist doesn't exist
            // we recursively call the algorithm on the smaller side, because that side has the terrorist
            return comparison < 0? findTerrorist(leftPointer, middlePointer)
                    : findTerrorist(middlePointer + 1, rightPointer);
        } else { // we have the more complicated case of an odd number of elements
            // so we chop off the end and compare the two halves
            // if they are equal, we instead return the last element of the array, which made it odd
            int middlePointer = (leftPointer + rightPointer) / 2 - 1; // -1, because the subsets are shifted one left
            int comparison = compareSubsets(leftPointer, middlePointer, rightPointer - 1);
            if (comparison < 0) {
                return findTerrorist(leftPointer, middlePointer);
            } else if (comparison > 0) {
                return findTerrorist(middlePointer + 1, rightPointer - 1);
            } else {
                return rightPointer; // because the terrorist is on the right
            }
        }
    }

    /**
     * Weighs two equal-length subsets of the array. O(n), but don't tell that to Mossad
     * It might be more efficient, because I do the O(n) work in stream().parallel()
     * @param leftPointer the first index of the first subset
     * @param middlePointer the last index of the first subset
     * @param rightPointer the last index of the second subset
     * leftPointer <= middlePointer < rightPointer
     * @return a negative number if the left is smaller, a positive number if the right is smaller,
     *         and 0 if they are equal
     */
    private int compareSubsets(int leftPointer, int middlePointer, int rightPointer) {
        // I sum up the weights of each subarray
        int leftWeight = Arrays.stream(passengers, leftPointer, middlePointer + 1).sum();
        int rightWeight = Arrays.stream(passengers, middlePointer + 1, rightPointer + 1).sum();
        return leftWeight - rightWeight;
    }

    /** Returns the index of the passenger who has been determined to be a
     * terrorist.
     *
     * @return the index of the terrorist element.
     */
    public int getTerrorist() {
        return terrorist;
    }

    // the algorithm is obvious and trivial, being essentially binary search
    // only it is n, because we have to compute the weights of each half at each stage for our compareTo
    // we drop half of the array at each stage
    // and if we pretended that weighing everything was O(1), then this would be lgn
    // the big problem with this is that we have no guarantee that there is a power of 2
    // But I can fix that, using the fact that all tefillin bags are equal despite the existence of Rabbeinu Tam
    // unless they are a terrorist
    // So, I start by ignoring everything after the smallest power of 2 less than the number of terrorists
    // for the parts before, I weigh each set of Tefillin bags
    // if one is bigger than the other, I recurse.
    // But if they are equal, I go back, and recall the recursive method on the stuff after the round stuff
    // I might have to do that a number of times equal to lgn, but I will eventually either find something even
    // or else reach a base case of 1, and return that that must be the terrorist, since I am guaranteed to have one



} // DetectTerrorist
