package edu.yu.da;


import java.util.*;

/** Defines the API for specifying and solving the OverfullGranaries problem
 * (see the requirements document).
 * Students MAY NOT change the public API of this class, nor may they add ANY
 * constructor.

 * @author Avraham Leff
 */
public class OverfullGranaries {

    /** Represents the 10_000 bushels of grain that must be moved from the
     * overfull granaries to the underfull granaries
     */
    public final static double BUSHELS_TO_MOVE = 10_000;

    // this is the graph that we use, it includes virtual edges from an imaginary source to overfull
    // and from underfull to an imaginary sink
    // 0 is the imaginary source, 1 is the sink, then comes overfull, then underfull, then everything else
    private final List<Map<Integer, Integer>> graph;
    private final Map<Integer, String> nodeToString;
    private final Map<String, Integer> stringToNode;
    private MaxFlow networkFlow;
    private boolean solutionFound;

    /** Constructor.
     *
     * @param X labelling of the overfull granaries, must contain at least one
     * element and no duplicates.  No element of X can be an element of Y.
     * @param Y labelling of the underfull granaries, must contain at least one
     * element and no duplicates.  No element of Y can be an element of X.
     */
    public OverfullGranaries(final String[] X, final String[] Y) {
        cleanArray(X, "X");
        cleanArray(Y, "Y");
        int initialCapacity = X.length + Y.length + 2;
        graph = new ArrayList<>(initialCapacity);
        nodeToString = new HashMap<>(initialCapacity - 2);
        stringToNode = new HashMap<>(initialCapacity - 2);

        // first we add every node to the graph
        for (int i = 0; i < initialCapacity; i++) {
            graph.add(new HashMap<>());
        }

        // next we set up the edges to overfull and add it to the node conversion
        for (int i = 0; i < X.length; i++) {
            graph.get(0).put(i + 2, Integer.MAX_VALUE);
            // because any amount of grain can come from the granary
            nodeToString.put(i + 2, X[i]);
            stringToNode.put(X[i], i + 2);
        }
        for (int i = 0; i < Y.length; i++) {
            int underfullIndex = i + 2 + X.length;
            graph.get(underfullIndex).put(1, Integer.MAX_VALUE);
            // because any amount of grain can enter the granary
            assert !stringToNode.containsKey(Y[i]);
            nodeToString.put(underfullIndex, Y[i]);
            stringToNode.put(Y[i], underfullIndex);
        }
    }

    private void cleanArray(String[] array, String name) {
        if (array == null) {
            throw new IllegalArgumentException("Array " + name + " is null");
        }
        if (array.length == 0) {
            throw new IllegalArgumentException("Array " + name + " is empty");
        }
    }

    /** Specifies that an edge exists from the specified src to the specified
     * dest of specified capacity.  It is legal to invoke edgeExists between
     * nodes in X, between nodes in Y, from a node in X to a node in Y, or for
     * src and dest to be hitherto unknown nodes.  The method cannot specify a
     * node in Y to be the src, nor can it specify a node in X to be the dest.
     *
     * @param src must contain at least one character
     * @param dest must contain at least one character, can't equal src
     * @param capacity must be greater than 0, and is specified implicitly to be
     * "bushels per hour"
     */
    public void edgeExists(final String src, final String dest, final int capacity) {
        cleanEdgeExists(src, dest, capacity);

        if (!stringToNode.containsKey(src)) {
            addName(src);
        }
        if (!stringToNode.containsKey(dest)) {
            addName(dest);
        }

        graph.get(stringToNode.get(src)).put(stringToNode.get(dest), capacity);
    }

    private void cleanEdgeExists(String src, String dest, int capacity) {
        if (src.equals(dest)) {
            throw new IllegalArgumentException("src and dest can't be equal, but are both " + src);
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity between " + src + " and " + dest + " must be positive, is " + capacity);
        }
        cleanString(src, "src");
        cleanString(dest, "dest");
    }

    private void cleanString(String label, String title) {
        if (label == null) {
            throw new IllegalArgumentException(title + " is null");
        }
        if (label.isEmpty()) {
            throw new IllegalArgumentException(title + " is empty");
        }
    }

    /**
     * Adds a new name to the graph and gives it a conversion
     * @param name being added to the graph
     */
    private void addName(String name) {
        stringToNode.put(name, graph.size());
        nodeToString.put(graph.size(), name);
        graph.add(new HashMap<>());
    }

    /** Solves the OverfullGranaries problem.
     *
     * @return the minimum number hours needed to achieve the goal of moving
     * BUSHELS_TO_MOVE number of bushels from the X granaries to the Y granaries
     * along the specified road map.
     * @note clients may only invoke this method after all relevant edgeExists
     * calls have been successfully invoked.
     */
    public double solveIt() {
        networkFlow = new MaxFlow(graph, 0, 1);
        int maxFlow = networkFlow.fordFulkerson();
        if (maxFlow != 0) {
            solutionFound = true;
        }
        return BUSHELS_TO_MOVE / maxFlow;
    }

    /** Return the names of all vertices in the X side of the min-cut, sorted by
     * ascending lexicographical order.
     *
     * @return only the names of the vertices in the X side of the min-cut
     * @note clients may only invoke this method after solveIt has been
     * successfully invoked.  Else throw an ISE.
     */
    public List<String> minCut() {
        if (networkFlow == null) {
            throw new IllegalStateException("minCut() called before solveIt()");
        }

        // if there is no connection between source and sink, we are supposed to return an empty list for some reason
        if (!solutionFound) {
            return new ArrayList<>();
        }

        // if I did this right, this complicated stream should result in a sorted list of the inputs
        return List.of(
                networkFlow.findMinCut()
                .parallelStream()
                .filter(nodeToString::containsKey)
                .map(nodeToString::get)
                .sorted().toArray(String[]::new));

    }

} // OverfullGranaries
