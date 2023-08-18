package testing;

import edu.yu.da.FindMinyan;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MinyanTest {

    private final FindMinyan googleMaps;

    public MinyanTest() {
        googleMaps = new FindMinyan(10);
    }

    // the trivial case, where the starting city has a Minyan and goes to the end city
    @Test
    public void startMinyanTest() {
        googleMaps.addHighway(1, 2, 10);
        googleMaps.hasMinyan(1);

        googleMaps.solveIt();
        assertEquals(2, googleMaps.shortestDuration());
        assertEquals(1, googleMaps.numberOfShortestTrips());
    }

    // the above, but the end city has a Minyan
    @Test
    public void endMinyanTest() {
        googleMaps.addHighway(1, 2, 10);
        googleMaps.hasMinyan(10);

        googleMaps.solveIt();
        assertEquals(2, googleMaps.shortestDuration());
        assertEquals(1, googleMaps.numberOfShortestTrips());
    }

    // A case where the start has a Minyan, but there is a more and less efficient route to the Minyan
    @Test
    public void efficientRouteTest() {
        googleMaps.addHighway(1, 2, 2);
        googleMaps.addHighway(1, 1, 3);
        googleMaps.addHighway(2, 2, 10);
        googleMaps.addHighway(3, 2, 10);
        googleMaps.hasMinyan(1);

        googleMaps.solveIt();
        assertEquals(3, googleMaps.shortestDuration());
        assertEquals(1, googleMaps.numberOfShortestTrips());
    }

    // A case where there are a bunch of detours to ignore
    @Test
    public void ignoreDetourTest() {
        googleMaps.addHighway(1, 2, 10);
        googleMaps.addHighway(1, 2, 5);
        googleMaps.addHighway(1, 1, 4);
        googleMaps.addHighway(4, 0, 6);
        googleMaps.addHighway(7, 15, 8);
        googleMaps.hasMinyan(1);

        googleMaps.solveIt();
        assertEquals(2, googleMaps.shortestDuration());
        assertEquals(1, googleMaps.numberOfShortestTrips());
    }

    // A case where only one route has a Minyan
    @Test
    public void oneRouteMinyanTest() {
        googleMaps.addHighway(1, 2, 2);
        googleMaps.addHighway(1, 1, 3);
        googleMaps.addHighway(2, 2, 10);
        googleMaps.addHighway(3, 2, 10);
        googleMaps.hasMinyan(3);

        googleMaps.solveIt();
        assertEquals(3, googleMaps.shortestDuration());
        assertEquals(1, googleMaps.numberOfShortestTrips());
    }

    // The above, but the Minyan route is more expensive
    @Test
    public void expensiveMinyanTest() {
        googleMaps.addHighway(1, 2, 2);
        googleMaps.addHighway(1, 1, 3);
        googleMaps.addHighway(2, 2, 10);
        googleMaps.addHighway(3, 2, 10);
        googleMaps.hasMinyan(2);

        googleMaps.solveIt();
        assertEquals(4, googleMaps.shortestDuration());
        assertEquals(1, googleMaps.numberOfShortestTrips());
    }

    // The above, but the Minyan is a detour that doesn't actually go to the destination
    @Test
    public void detourMinyanTest() {
        googleMaps.addHighway(1, 2, 2);
        googleMaps.addHighway(1, 1, 3);
        googleMaps.addHighway(3, 2, 10);
        googleMaps.hasMinyan(2);

        googleMaps.solveIt();
        assertEquals(7, googleMaps.shortestDuration());
        assertEquals(1, googleMaps.numberOfShortestTrips());
    }


    //*/

    // a case with a length 0 highway detour, to show it is supported
    @Test
    public void route0LengthTest() {
        googleMaps.addHighway(1, 2, 2);
        googleMaps.addHighway(1, 0, 3);
        googleMaps.addHighway(2, 2, 10);
        googleMaps.addHighway(3, 2, 10);
        googleMaps.hasMinyan(1);

        googleMaps.solveIt();
        assertEquals(2, googleMaps.shortestDuration());
        assertEquals(1, googleMaps.numberOfShortestTrips());
    }

    // a case with multiple routes outpacing a single route
    @Test
    public void sideStreetTest() {
        googleMaps.addHighway(1, 1, 2);
        googleMaps.addHighway(2, 1, 10);
        googleMaps.addHighway(1, 3, 10);
        googleMaps.hasMinyan(1);

        googleMaps.solveIt();
        assertEquals(2, googleMaps.shortestDuration());
        assertEquals(1, googleMaps.numberOfShortestTrips());
    }

    // a case with multiple Minyanim on the same route
    @Test
    public void multiMinyanRouteTest() {
        googleMaps.addHighway(1, 2, 2);
        googleMaps.addHighway(2, 2, 10);
        googleMaps.hasMinyan(1);
        googleMaps.hasMinyan(2);
        googleMaps.hasMinyan(10);

        googleMaps.solveIt();
        assertEquals(4, googleMaps.shortestDuration());
        assertEquals(1, googleMaps.numberOfShortestTrips());
    }

    // a case with 2 Minyanim, but one has a fast route
    @Test
    public void shorterMinyanRouteTest() {
        googleMaps.addHighway(1, 2, 2);
        googleMaps.addHighway(1, 1, 3);
        googleMaps.addHighway(2, 2, 10);
        googleMaps.addHighway(3, 2, 10);
        googleMaps.hasMinyan(2);
        googleMaps.hasMinyan(3);

        googleMaps.solveIt();
        assertEquals(3, googleMaps.shortestDuration());
        assertEquals(1, googleMaps.numberOfShortestTrips());
    }

    // a case with 2 Minyanim, with the same route speed
    @Test
    public void multiEqualMinyanTest() {
        googleMaps.addHighway(1, 2, 2);
        googleMaps.addHighway(1, 2, 3);
        googleMaps.addHighway(2, 2, 10);
        googleMaps.addHighway(3, 2, 10);
        googleMaps.hasMinyan(2);
        googleMaps.hasMinyan(3);

        googleMaps.solveIt();
        assertEquals(4, googleMaps.shortestDuration());
        assertEquals(2, googleMaps.numberOfShortestTrips());
    }

    // a case with multiple equal routes, but only one Minyan city
    @Test
    public void multiEqualRouteOneMinyanTest() {
        googleMaps.addHighway(1, 2, 2);
        googleMaps.addHighway(1, 2, 3);
        googleMaps.addHighway(2, 2, 10);
        googleMaps.addHighway(3, 2, 10);
        googleMaps.hasMinyan(1);

        googleMaps.solveIt();
        assertEquals(4, googleMaps.shortestDuration());
        assertEquals(2, googleMaps.numberOfShortestTrips());
    }

    // a case with 2 equal routes to 1 Minyan and another to a third
    @Test
    public void complex3WayTest() {
        googleMaps.addHighway(1, 2, 2);
        googleMaps.addHighway(2, 2, 8);
        googleMaps.addHighway(2, 2, 9);
        googleMaps.addHighway(8, 2, 10);
        googleMaps.addHighway(9, 2, 10);
        googleMaps.addHighway(1, 2, 3);
        googleMaps.addHighway(3, 4, 10);
        googleMaps.hasMinyan(2);
        googleMaps.hasMinyan(3);
        // we have 1-2-8-10
        // we have 1-2-9-10
        // we have 1-3-10

        googleMaps.solveIt();
        assertEquals(6, googleMaps.shortestDuration());
        assertEquals(3, googleMaps.numberOfShortestTrips());
    }

    // a test where there are two paths that each have the same two Minyanim
    @Test
    public void multiEqualRouteTwoMinyanimTest() {
        googleMaps.addHighway(1, 2, 2);
        googleMaps.addHighway(1, 2, 3);
        googleMaps.addHighway(2, 2, 10);
        googleMaps.addHighway(3, 2, 10);
        googleMaps.hasMinyan(1);
        googleMaps.hasMinyan(10);

        googleMaps.solveIt();
        assertEquals(4, googleMaps.shortestDuration());
        assertEquals(2, googleMaps.numberOfShortestTrips());
    }

    // if we have multiple splits of equal length, that we get all four routes, when the minyan is at the middle
    @Test
    public void fourWayIntersectionMiddleMinyanTest() {
        // made of 1-2-4, 1-3-4, 4-5-10, and 4-6-10
        googleMaps.addHighway(1, 2, 2);
        googleMaps.addHighway(1, 2, 3);
        googleMaps.addHighway(2, 2, 4);
        googleMaps.addHighway(3, 2, 4);

        googleMaps.addHighway(4, 2, 5);
        googleMaps.addHighway(4, 2, 6);
        googleMaps.addHighway(5, 2, 10);
        googleMaps.addHighway(6, 2, 10);

        googleMaps.hasMinyan(4);

        googleMaps.solveIt();
        assertEquals(8, googleMaps.shortestDuration());
        assertEquals(4, googleMaps.numberOfShortestTrips());
    }

    // the above, but the minyan is at one end
    @Test
    public void fourWayIntersectionEndpointMinyanTest() {
        // made of 1-2-4, 1-3-4, 4-5-10, and 4-6-10
        googleMaps.addHighway(1, 2, 2);
        googleMaps.addHighway(1, 2, 3);
        googleMaps.addHighway(2, 2, 4);
        googleMaps.addHighway(3, 2, 4);

        googleMaps.addHighway(4, 2, 5);
        googleMaps.addHighway(4, 2, 6);
        googleMaps.addHighway(5, 2, 10);
        googleMaps.addHighway(6, 2, 10);

        googleMaps.hasMinyan(1);

        googleMaps.solveIt();
        assertEquals(8, googleMaps.shortestDuration());
        assertEquals(4, googleMaps.numberOfShortestTrips());
    }

    // add a case where highways go backwards
    @Test
    public void backwardsHighwayTest() {
        googleMaps.addHighway(10, 2, 1);
        googleMaps.hasMinyan(1);

        googleMaps.solveIt();
        assertEquals(2, googleMaps.shortestDuration());
        assertEquals(1, googleMaps.numberOfShortestTrips());
    }

    // a case where you have to go from a higher number to a lower one
    @Test
    public void backwardsDrivingTest() {
        googleMaps.addHighway(1, 2, 9);
        googleMaps.addHighway(9, 2, 10);
        googleMaps.hasMinyan(1);

        googleMaps.solveIt();
        assertEquals(4, googleMaps.shortestDuration());
        assertEquals(1, googleMaps.numberOfShortestTrips());
    }

    // and the microsoft paint case
    @Test
    public void complexBranchTest() {
        // cases are 1-2-4-10, 1-2-3-10, and 1-3-10
        // all edges are 1, except 1-3, which is 2
        googleMaps.addHighway(1, 1, 2);
        googleMaps.addHighway(2, 1, 4);
        googleMaps.addHighway(4, 1, 10);
        googleMaps.addHighway(2, 1, 3);
        googleMaps.addHighway(3, 1, 10);
        googleMaps.addHighway(1, 2, 3);
        googleMaps.hasMinyan(2);
        googleMaps.hasMinyan(3);

        googleMaps.solveIt();
        assertEquals(3, googleMaps.shortestDuration());
        assertEquals(3, googleMaps.numberOfShortestTrips());
    }

    // the above case, but every node is a Minyan
    @Test
    public void allMinyanimTest() {
        // cases are 1-2-4-10, 1-2-3-10, and 1-3-10
        // all edges are 1, except 1-3, which is 2
        googleMaps.addHighway(1, 1, 2);
        googleMaps.addHighway(2, 1, 4);
        googleMaps.addHighway(4, 1, 10);
        googleMaps.addHighway(2, 1, 3);
        googleMaps.addHighway(3, 1, 10);
        googleMaps.addHighway(1, 2, 3);
        for (int i = 1; i <= 10; i++) { // add a Minyan to every city
            googleMaps.hasMinyan(i);
        }

        googleMaps.solveIt();
        assertEquals(3, googleMaps.shortestDuration());
        assertEquals(3, googleMaps.numberOfShortestTrips());
    }

    // test that a 10-Minyan test with a full tree has <75ms performance
    // I don't log, but he has a better computer
    @Test
    public void allMinyanimAllReachableTest() {
        // cases are 1-2-4-10, 1-2-3-10, and 1-3-10
        // all edges are 1, except 1-3, which is 2
        // let's keep the current solutions as the only ones
        // so all new roads are duration 2
        // let's add a 1-5-6-10 path, but for a bigger price
        // and let's add a tangent 6-7-8
        // and 7-9, and 8-9, for a circle there
        // let's also add 4-5, to make things interesting
        googleMaps.addHighway(1, 1, 2);
        googleMaps.addHighway(2, 1, 4);
        googleMaps.addHighway(4, 1, 10);
        googleMaps.addHighway(2, 1, 3);
        googleMaps.addHighway(3, 1, 10);
        googleMaps.addHighway(1, 2, 3);

        googleMaps.addHighway(1, 2, 5);
        googleMaps.addHighway(5, 2, 6);
        googleMaps.addHighway(6, 2, 10);
        googleMaps.addHighway(6, 2, 7);
        googleMaps.addHighway(7, 2, 8);
        googleMaps.addHighway(8, 2, 9);
        googleMaps.addHighway(9, 2, 7);
        googleMaps.addHighway(4, 2, 5);
        for (int i = 1; i <= 10; i++) { // add a Minyan to every city
            googleMaps.hasMinyan(i);
        }

        googleMaps.solveIt();
        assertEquals(3, googleMaps.shortestDuration());
        assertEquals(3, googleMaps.numberOfShortestTrips());
    }

    // If there are no Minyanim or no way to get to them (or the destination), return 0
    @Test
    public void noMinyanimTest() {
        googleMaps.addHighway(1, 2, 10);

        googleMaps.solveIt();
        assertEquals(0, googleMaps.shortestDuration());
        assertEquals(0, googleMaps.numberOfShortestTrips());
    }

    @Test
    public void noRouteTest() {
        googleMaps.hasMinyan(1);

        googleMaps.solveIt();
        assertEquals(0, googleMaps.shortestDuration());
        assertEquals(0, googleMaps.numberOfShortestTrips());
    }

    // also return 0 if there is no way to get to the Minyan, even though 1 and n do connect
    @Test
    public void strandedMinyanTest() {
        googleMaps.addHighway(1, 2, 10);
        googleMaps.hasMinyan(3);

        googleMaps.solveIt();
        assertEquals(0, googleMaps.shortestDuration());
        assertEquals(0, googleMaps.numberOfShortestTrips());
    }

    // throw an IAE if we have the same highway added twice
    @Test
    public void duplicateHighwayTest() {
        googleMaps.addHighway(1, 2, 10);
        assertThrows(IllegalArgumentException.class, ()->googleMaps.addHighway(1, 4, 10));
    }

}
