package edu.yu.da;


import java.util.*;

/** Defines the API for specifying and solving the FindMinyan problem (see the
 * requirements document).  Also defines an inner interface, and uses it as
 * part of the ArithmeticPuzzleI API definition.
 *
 * Students MAY NOT change the public API of this class, nor may they add ANY
 * constructor
 *
 * @author Avraham Leff
 */
public class FindMinyan {

    /*
    To do list: First, let's write Dijkstra stam.
    Wait, first let's create the actual graph.
    * */

    private final int n;
    private final List<List<Highway>> graph; // using a list of lists to avoid generic problems from list of arrays
    private final boolean[] cityMinyan;
    private int routeLength; // length of the most efficient route
    private int routeNumber; // number of most efficient routes

    /** Constructor: clients specify the number of cities involved in the
     * problem.  Cities are numbered 1...n, and for convenience, the "start" city
     * is labelled as "1", and the goal city is labelled as "n".
     *
     * @param nCities number of cities, must be greater than 1.
     */
    public FindMinyan(final int nCities) {
        if (nCities <= 1) {
            throw new IllegalArgumentException("nCities must be > 1, you gave " + nCities);
        }
        n = nCities; // I am keeping this, because it is one less than the actual data structures, and I don't
            // want to have to subtract
        // initialize empty graph
        graph = new ArrayList<>(nCities + 1); // so that I can use their numbering
        graph.add(null); // so that we can skip index 0, which goes unused
        for (int i = 1; i <= nCities; i++) {
            graph.add(new ArrayList<>());
        }

        cityMinyan = new boolean[nCities + 1];
    }

    /** Defines a highway leading (bi-directionally) between two cities, of
     * specified duration.
     *
     * @param city1 identifies a 1 <= city <= n, must differ from city2
     * @param city2 identifies a 1 <= city <= n, must differ from city1
     * @param duration the bidirectional duration of a trip between the two
     * cities on this highway, must be non-negative
     */
    public void addHighway(final int city1, final int duration, final int city2) {
        cleanHighway(city1, duration, city2);

        // we add the valid highway to the graph, for both cities
        Highway road = new Highway(city1, duration, city2);
        graph.get(city1).add(road);
        graph.get(city2).add(road);
    }

    private void cleanHighway(final int city1, final int duration, final int city2) {
        cleanCity(city1);
        cleanCity(city2);
        if (city1 == city2) {
            throw new IllegalArgumentException("Cities must be distinct, they were both " + city1);
        }
        if (duration < 0) {
            throw new IllegalArgumentException("Highway duration cannot be negative, you gave " + duration);
        }
        if (graph.get(city1).contains(new Highway(city1, duration, city2))) {
            throw new IllegalArgumentException("A highway between these cities already exists");
        }
    }

    /**
     * Represents the edges of the graph
     * Each one contains two endpoints and a weight
     */
    private static class Highway {
        private final int city1;
        private final int city2;
        private final int duration;
        private Highway(final int city1, final int duration, final int city2) {
            this.city1 = Math.min(city1, city2);
            this.city2 = Math.max(city1, city2);
            // I enforce that, at least internally, city1 < city2
            // this makes sure that certain equality and hashcode tests work as intended
            this.duration = duration;
        }

        /**
         * @param city that Highway connects to
         * @return the other city that isn't the parameter that Highway connects to
         */
        private int otherCity(int city) {
            return city1 == city? city2 : city1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Highway highway = (Highway) o;
            // does not check duration, so that highways with different durations will be caught as equal
                // in my error checking
            return city1 == highway.city1 && city2 == highway.city2;
            // if they connect the same two cities, it doesn't matter which direction
        }

        @Override
        public int hashCode() {
            return Objects.hash(city1, city2);
        }
    }

    private void cleanCity(final int city) {
        if (city < 1) {
            throw new IllegalArgumentException("City " + city + " must be a positive integer");
        }
        if (city > n) {
            throw new IllegalArgumentException("City " + city + " must be within the bound " + n);
        }
    }

    /** Specifies that a minyan can be found in the specified city.
     *
     * @param city identifies a 1 <= city <= n
     */
    public void hasMinyan(final int city) {
        cleanCity(city);
        // add the Minyan
        cityMinyan[city] = true;
    }

    /** Find a solution to the FindMinyan problem based on state specified by the
     * constructor, addHighway(), and hasMinyan() API.  Clients access the
     * solution through the shortestDuration() and nShortestDurationTrips() APIs.
     */
    public void solveIt() {
        // First check which Minyanim are reachable from 1 and n
        // if this leaves an empty set, I will set 0 return values for the other methods
        // which means doing nothing, because a 0 return value is the default
        Set<Integer> minyanim = findReachableMinyanim();
        if (minyanim.isEmpty()) {
            return;
        }
        // Create a set of routes, and Dijkstra each Minyan found valid
        boolean[] exploredCities = new boolean[n+1];
        // also, set the distance to the max, so that any path will be smaller
        routeLength = Integer.MAX_VALUE;
        for (Integer minyan : minyanim) {
            City[] shortestPathTree = getShortestPathsWithMinyan(minyan);
            // now we extract the routeLength
            if (shortestPathTree[1].weight + shortestPathTree[n].weight < routeLength) {
                routeLength = shortestPathTree[1].weight + shortestPathTree[n].weight;
                routeNumber = 0; // when we return, we will add the routes from this pass
                // and we don't want to count the ones we had before
                //return distTo;
            } else if (shortestPathTree[1].weight + shortestPathTree[n].weight > routeLength) {
                continue;
                // if the total weight is more than the route length we are given, we end without doing anything,
                // because the routes we collected are inferior to what we had before
                // so we check if the next Minyan is better, if there is one
            }
            // if we have equal length routes, we may want to add them to our collection
            // but we aren't getting rid of the old routes, because they are just as valid
            routeNumber += constructRoutes(shortestPathTree, exploredCities);
            exploredCities[minyan] = true; // so we won't duplicate its routes
        }
        // Optimizations to consider: First finding the most efficient path, and seeing if it and its duplicates
            // have Minyanim
        // Or, checking if the first or last city have Minyanim, and if so, just getting the most efficient paths
    }

    /**
     * This method takes the list of Minyanim and figures out which ones can be reached from
     * both 1 and n, in O(n + h) time
     * It performs a DFS from 1. It carries along a boolean return, to determine if we have found
     * n. It also accumulates a Set along the way of all Minyanim it finds along the way
     * When we get the final return, if it is true, we return the Set. Otherwise, we return an
     * empty set.
     * @return a Set of all Minyanim reachable from 1 and n, or an empty set if the cities do not
     * connect
     */
    private Set<Integer> findReachableMinyanim() {
        Set<Integer> minyanim = new HashSet<>();
        if (cityMinyan[1]) { // since 1 is never properly visited in the recursive method, we have to look at it
            // now
            minyanim.add(1);
        }
        boolean reachesN = findReachableRecursive(new boolean[n + 1], minyanim, 1);
        return reachesN? minyanim : new HashSet<>();
    }

    /**
     * Performs DFS on the graph to determine which nodes are both Minyanim and are reachable from 1
     * @param marked determines which nodes have been visited, should be initalized when called non-recursively
     * @param minyanim that we have found so far, should be initialized when called non-recursively
     *                 But it needs to be kept in a variable, because this is how the list of Minyanim is created
     * @param currentNode we are visiting, should be initialized as 1
     * @return true if n has been found yet, false otherwise
     */
    private boolean findReachableRecursive(boolean[] marked, Set<Integer> minyanim, int currentNode) {
        boolean foundN = false;
        marked[currentNode] = true;
        for (Highway edge : graph.get(currentNode)) {
            int destination = edge.otherCity(currentNode);
            if (!marked[destination]) {
                // if this city has not yet been visited, do so now
                if (cityMinyan[destination]) {
                    // if this is a Minyan, add it to our list
                    minyanim.add(destination);
                }
                foundN = foundN || findReachableRecursive(marked, minyanim, destination);
                // so that if foundN has already been declared true, this isn't overruled
                // returns true if we have found n in a child or this is n, otherwise false
            }
        }
        return foundN || currentNode == n;
    }



    private static class City implements Comparable<City> {

        private final int cityID;
        private int weight;
        private Set<Highway> edgesTo;

        private City(int cityID) {
            this.cityID = cityID;
            weight = Integer.MAX_VALUE;
        }

        @Override
        public int compareTo(City o) {
            return this.weight - o.weight;
        }
    }

    /**
     * Finds the most efficient paths between 1 and n that go through minyan and returns them in the form of
     * the shortest path tree
     * Since this is a version of Dijsktra's algorithm, its efficiency is O(hlgn)
     * @param minyan city with a minyan that we are passing through
     * @return the city array containing the trees that can be used to find the paths
     *      or null if the paths are too long to be worth looking at
     */
    private City[] getShortestPathsWithMinyan(final int minyan) {

        City[] distTo = new City[n + 1];
        PriorityQueue<City> queue = new PriorityQueue<>();

        for (int i = 1; i <= n; i++) {
            City city = new City(i);
            distTo[i] = city; // adding each city to the array to keep track of it
            // we are not adding them to the priority queue yet, so that we won't try to relax them
        }

        // now we make the starting city with a zero distance
        distTo[minyan].weight = 0;
        queue.insert(distTo[minyan]);

        while (!queue.isEmpty()) {
            // we relax each vertex, starting from the current one
            City city = queue.remove();
            relax(distTo, queue, city);

        }
        return distTo;
    }

    /**
     * Examines each road connected to the city.
     * If it gets us to a city faster, it updates the city in the queue
     * If it gets us to a city at the same rate, it is added to that city as a duplicate road
     * There is no return, the method modifies the parameters it is given
     * @param distTo array of cities and their distances and roads. Cities may be modified
     * @param queue of cites, may need to be updated as more efficient routes to cities are found
     * @param city whose edges we are looking at
     */
    private void relax(City[] distTo, PriorityQueue<City> queue, City city) {
        // for each edge next to the city
        for (Highway road : graph.get(city.cityID)) {
            int destination = road.otherCity(city.cityID);
            // destination is the other city
            if (distTo[destination].weight > city.weight + road.duration) {
                int originalDistance = distTo[destination].weight;
                // so I can use it later to determine if this was ever set
                // by definition, the length can't approach Max distance, because that could lead to problems
                // with an int return type
                distTo[destination].weight = city.weight + road.duration;
                // now we add the road that goes to the city to the city's distTo
                // resetting it if necessary, if there is a tie among roads to reach it
                distTo[destination].edgesTo = new HashSet<>();
                distTo[destination].edgesTo.add(road);
                // reheapify it, to reflect the new changes
                // or add it if it hadn't been yet
                if (originalDistance == Integer.MAX_VALUE) {
                    queue.insert(distTo[destination]);
                } else {
                    queue.reHeapify(distTo[destination]);
                }
            } else if (distTo[destination].weight == city.weight + road.duration
                    && (distTo[city.cityID].edgesTo == null ||
                            !distTo[city.cityID].edgesTo.contains(new Highway(city.cityID, 0, destination)))) {
                // in the case where this is a second, equal road that goes here, we add it to the edge
                // but we don't add it if that same road is already going the other direction
                // I defined highway equality that they be equal no matter the direction
                if (distTo[destination].edgesTo == null) {
                    // this can only happen if we have the original city being connected via a length 0 highway
                    // where we could have an equal length road, but not have it instantiated yet
                    distTo[destination].edgesTo = new HashSet<>();
                }
                distTo[destination].edgesTo.add(road);
                // no need to reheapify, its place in the heap hasn't changed
            }
        }
    }

    /**
     * Constructs all most efficient routes from the most recent pass of Dijkstra and edits the global variables
     * routeLength and routeNumber to show it
     * I would prefer to be side-effect-free, but Java doesn't easily return multiple variables
     * @param distTo list of cities used in Dijkstra, containing the path of the routes
     * @param exploredMinyan list of cities for whom Dijkstra has already been called, which we don't need
     *                       to explore again
     */
    private int constructRoutes(City[] distTo, boolean[] exploredMinyan) {
        // we call the sub-method on 1 and n, to determine the number of routes
        // we then multiply the result to determine the total routes
        // we add that to routeNumber

        return exploreTree(distTo, exploredMinyan, new int[n + 1], 1, new boolean[n+1])
                * exploreTree(distTo, exploredMinyan, new int[n + 1], n, new boolean[n+1]);
    }

    /**
     * A recursive method that explores the distTo tree to determine the number of shortest paths
     * It constitutes a DFS through the tree, but does not examine Minyanim whose shortest paths have already
     * been examined
     * It only explores half the tree, either from 1 or n, going towards the initial Minyan that Dijsktra
     * was called on
     * @param distTo tree that is being explored
     * @param exploredMinyan an array, kept between calls, that records which Minyanim have already had
     *                       Dijkstra called on them
     * @param cityNumber an array that records the number of branches after a given node.
     *                  When this method is called non-recursively, it should be set as new int[n+1]
     * @param currentNode we are exploring as part of the DFS
     *                    When this method is called non-recursively, it should be either 1 or n
     * @param marked an array that says if we have already traversed a certain node
     *               When this method is called non-recursively, it should be set as new boolean[n+1]
     * @return the number of duplicates in this part of the distTo tree
     */
    private int exploreTree(City[] distTo, boolean[] exploredMinyan, int[] cityNumber, int currentNode, boolean[] marked) {
        if (distTo[currentNode].edgesTo == null) {
            // if we never added any nodes that go here, we reached the core Minyan
            // so now we return 1 for the starting branch
            cityNumber[currentNode] = 1;
        } else if (exploredMinyan[currentNode]) {
            // if the starting branch is a marked city, otherwise, it is never told to return
            cityNumber[currentNode] = 0;
        } else {
            for (Highway edge : distTo[currentNode].edgesTo) {
                int destination = edge.otherCity(currentNode);
                if (!exploredMinyan[destination] && !marked[destination]) {
                    // if this city has not yet been visited and evaluated, do so now
                    // if we are visting a city with a Minyan that has already been explored, any routes beyond
                    // here are duplicates
                    // so we treat it as if it didn't exist
                    // but this is not the base case, so if we check every edge, and they are all duplicates,
                    // we will end up returning 0
                    // I don't have to be worried that I will get confused that this isn't 0, because I will
                    // always rule it out because it has been explored
                    marked[destination] = true;
                    cityNumber[currentNode] += exploreTree(distTo, exploredMinyan, cityNumber, destination, marked);
                } else { // otherwise, take what we already calculated
                    cityNumber[currentNode] += cityNumber[destination];
                }
            }
        }
        return cityNumber[currentNode];
    }

    /** Returns the duration of the shortest trip satisfying the FindMinyan
     * constraints.
     *
     * @return duration of the shortest trip, undefined if client hasn't
     * previously invoked solveIt().
     */
    public int shortestDuration() {
        return routeLength;
    }

    /** Returns the number of distinct trips that satisfy the FindMinyan
     * constraints.
     *
     * @return number of minimum duration trips, undefined if client hasn't
     * previously invoked solveIt().
     */
    public int numberOfShortestTrips() {
        return routeNumber;
    }
    /**
     * An indexed priority queue made in Data Structures, and then modified to have proper efficiency
     * It should have O(lgn) order of growth for insert, remove, and reHeapify
     * @param <E> type of elements
     */
    public static class PriorityQueue<E extends Comparable<E>> {
        private E[] elements;
        private int count = 0;
        private final Map<E, Integer> indexMap; // used to keep track of each element's index

        public PriorityQueue() {
            this(15); // completely random number, since I don't have a better idea
        }

        public PriorityQueue(int initialCapacity) {
            elements = (E[]) new Comparable[initialCapacity + 1];
            indexMap = new HashMap<>();
        }

        /**
         * @return true for an empty priority queue, false otherwise
         */
        public boolean isEmpty() {
            return count == 0;
        }

        /**
         * Adds an element to the priority queue
         * @param x element being added
         */
        public void insert(E x) {
            // double size of array if necessary
            // I'm not changing the array, because I don't want to break anything
            // and the doubling amortizes to O(1)
            if (count >= elements.length - 1) {
                doubleArraySize();
            }
            // add x to the bottom of the heap
            elements[++count] = x;
            // whenever we edit the heap, we update the location
            indexMap.put(x, count);
            // percolate it up to maintain heap order property
            upHeap(count);
        }

        public E remove() {
            if (isEmpty()) {
                return null; // should return null if a no-op
            }
            E min = this.elements[1];
            //swap root with last, decrement count
            this.swap(1, this.count--);
            //move new root down as needed
            this.downHeap(1);
            this.elements[this.count + 1] = null; //null it to prepare for GC
            // I am removing the key from the hashmap to avoid cluttering
            indexMap.remove(min);
            return min;
        }

        public void reHeapify(E element) {
            if (element == null) {
                throw new IllegalArgumentException("element is null");
            }
            // now O(1) time to find the integer, because I used a HashMap
            // I'm not sure why I didn't do that in my initial implementation, I already knew about HashMaps
            Integer elementIndex = indexMap.get(element); // an Integer, because could be null
            if (elementIndex == null) {
                return; // should be no-op if not in heap
            }
            // upheaps and downheaps, only one (or maybe zero) will actually do anything
            // if isn't called, elementIndex won't be changed
            upHeap(elementIndex);
            downHeap(elementIndex);
        }

        private void doubleArraySize() {
            // copies all old elements into the new array, which is double the length
            // assuming I correctly understand what this is for, this was easy, it's like Python
            elements = Arrays.copyOf(elements, elements.length * 2);
        }

        /**
         * is elements[i] > elements[j]?
         */
        private boolean isGreater(int i, int j) {
            return elements[j] != null && this.elements[i].compareTo(this.elements[j]) > 0;
            // if j is null, this should return false
        }

        /**
         * swap the values stored at elements[i] and elements[j]
         */
        private void swap(int i, int j) {
            E temp = this.elements[i];
            this.elements[i] = this.elements[j];
            this.elements[j] = temp;
            indexMap.put(elements[i], j);
            indexMap.put(elements[j], i);
        }

        /**
         * while the key at index k is less than its
         * parent's key, swap its contents with its parent’s
         * @param k the initial location of the key
         */
        private int upHeap(int k) {
            while (k > 1 && this.isGreater(k / 2, k)) {
                this.swap(k, k / 2);
                k = k / 2;
            }
            return k; // the location we brought the element to
        }

        /**
         * move an element down the heap until it is less than
         * both its children or is at the bottom of the heap
         * @param k the location of the element we are pushing down
         */
        private int downHeap(int k) {
            while (2 * k <= this.count) {
                //identify which of the 2 children are smaller
                int j = 2 * k; // j will refer to the child we are swapping with
                if (j < this.count && this.isGreater(j, j + 1)) {
                    j++;
                }
                //if the current value is < the smaller child, we're done
                if (!this.isGreater(k, j)) {
                    break;
                }
                //if not, swap and continue testing
                this.swap(k, j);
                k = j;
            }
            return k; // the current location of this element
        }
    }
}