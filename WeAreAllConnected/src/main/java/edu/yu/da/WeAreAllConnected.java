package edu.yu.da;

import java.util.*;

public class WeAreAllConnected extends WeAreAllConnectedBase {

    // TODO: Improve lower terms (preferably without a Fibonacci heap)

    public static class Segment extends SegmentBase {

        /**
         * Constructor.
         *
         * @param x        one end of a communication segment, specified by a city id
         *                 (0..n-1)
         * @param y        one end of a communication segment, specified by a city id
         *                 (0..n-1).  You may assume that "x" differs from "y".
         * @param duration unit-less amount of time required for a message to
         *                 travel from either end of the segment to the other.  You may assume that
         */
        public Segment(int x, int y, int duration) {
            super(x, y, duration);
        }

        // note: considers x->y and y->x different
        // You might want to change that
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof SegmentBase)) return false;
            SegmentBase that = (SegmentBase) obj;
            return x == that.x && y == that.y && duration == that.duration;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, duration);
        }

        @Override
        public String toString() {
            return "Segment from " + x + " to " + y + " with duration " + duration;
        }

    }


    /**
     * Given a list of the current communication system's segments and a list of
     * possible segments that can be added to the current system, select exactly
     * one segment from the set of possibilities to be added to the current
     * communication system.  You may assume that all segments supplied by the
     * client satisfy Segment semantics.
     *
     * @param n             the ids of all cities referenced by SegmentBase instances lie in
     *                      the range 0..n-1 (inclusive).
     * @param current       the current communication system defined as a list of
     *                      segments.  The client maintains ownership of this parameter.
     * @param possibilities one possible segment will be selected from this list
     *                      to be added to the current communication system.  The client maintains
     *                      ownership of this parameter.
     * @return the segment from the set of possibilities that provides the best
     * improvement in the total duration of the current system.  Total duration
     * is defined as the sum of the durations between all pairs of cities x and y
     * in the current system.  If more than one segment qualifies, return any of
     * those possibilities.
     */
    @Override
    public SegmentBase findBest(int n, List<SegmentBase> current, List<SegmentBase> possibilities) {
        cleanInputs(n, current, possibilities);
        //System.out.println("Starting tests with " + possibilities);
        // this is an array of longs, because we are summoning int lengths
        long[][] shortestPaths;
        if (current.size() < n * n / 4) { // for a sparse graph, use dijkstra
            shortestPaths = findShortestPathsDijkstra(n, current);
            //System.out.println("Using Dijkstra");
        } else { // for a dense graph, use Floyd-Warshall, since I already wrote it
            shortestPaths = findShortestPathsFloydWarshall(n, current);
            //System.out.println("Using Floyd Warshall");
        }
        //System.out.println(Arrays.deepToString(shortestPaths));

        SegmentBase bestSegment = null;
        long bestPairwiseImprovement = 0;
        for (SegmentBase suggestion : possibilities) {
            long pairwiseImprovement = evaluateSuggestion(suggestion, shortestPaths);
            if (pairwiseImprovement >  bestPairwiseImprovement) {
                bestPairwiseImprovement = pairwiseImprovement;
                bestSegment = suggestion;
            }
        }

        return bestSegment;//*/
    }

    private void cleanInputs(int n, List<SegmentBase> current, List<SegmentBase> possibilities) {
        if (n < 3) {
            throw new IllegalArgumentException("n " + n + " is < 3");
        }
        cleanList(current, "current");
        cleanList(possibilities, "possibilities");
    }

    private void cleanList(List<SegmentBase> segmentList, String name) {
        if (segmentList == null) {
            throw new IllegalArgumentException("List " + name + " is null");
        }
        if (segmentList.isEmpty()) {
            throw new IllegalArgumentException("List " + name + " is empty");
        }
    }

    /**
     * Finds the shortest paths between all cities using Dijkstra's algorithm from each city
     * Has an order of growth of O(VElogV)
     * @param nCities number of cities in the network
     * @param segments between cities before the algorithm is run
     * @return an array that shows the length of the connection between all cities
     */
    private long[][] findShortestPathsDijkstra(int nCities, List<SegmentBase> segments) {
        // first, we create an adjacency list in O(n) time
        List<List<SegmentBase>> adjacencyList = makeAdjacencyList(nCities, segments);
        // then, we analyze it with Dijkstra for each city
        long[][] shortestDistance = new long[nCities][nCities];
        for (int city = 0; city < nCities; city++) {
            shortestDistance[city] = findSPTDijkstra(adjacencyList, city);
        }

        return shortestDistance;
    }

    /**
     * Given a list of edges, creates a graph represented by an adjacency list
     * @param nVertices number of vertices in the graph
     * @param edges list of all edges in the graph
     * @return an adjacency list
     */
    private List<List<SegmentBase>> makeAdjacencyList(int nVertices, List<SegmentBase> edges) {
        List<List<SegmentBase>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < nVertices; i++) {
            adjacencyList.add(new ArrayList<>());
        }

        for (SegmentBase connection : edges) {
            adjacencyList.get(connection.x).add(connection);
            // The second segment has x and y reversed
            adjacencyList.get(connection.y).add(new Segment(connection.y, connection.x, connection.duration));
        }

        return adjacencyList;
    }

    private static class CompareCity implements Comparable<CompareCity> {
        private final int cityID;
        private long weight;

        private CompareCity(int cityID) {
            this.cityID = cityID;
            weight = Long.MAX_VALUE;
        }

        @Override
        public int compareTo(CompareCity o) {
            return Long.compare(weight, o.weight);
        }
    }

    /**
     * Finds the shortest paths for a specific city using Dijkstra's algorithm
     * @param adjacencyList graph that the algorithm is called on
     * @param hub city whose paths we are finding
     * @return an array containing the shortest paths starting at hub
     */
    private long[] findSPTDijkstra(List<List<SegmentBase>> adjacencyList, int hub) {
        CompareCity[] cities = new CompareCity[adjacencyList.size()];
        PriorityQueue<CompareCity> queue = new PriorityQueue<>();
        // setting initial values
        for (int i = 0; i < cities.length; i++) {
            cities[i] = new CompareCity(i);
        }
        cities[hub].weight = 0;
        queue.insert(cities[hub]);

        while (!queue.isEmpty()) {
            // we relax each vertex, starting from the current one
            CompareCity city = queue.remove();
            relax(cities, queue, city, adjacencyList.get(city.cityID));

        }

        // note: might not work, because might give me this out of order
        // if this doesn't work, that's probably why
        return Arrays.stream(cities).mapToLong((x)->x.weight).toArray();
    }

    private void relax(CompareCity[] cities, PriorityQueue<CompareCity> queue, CompareCity relaxedCity, List<SegmentBase> adjacentSegments) {
        for (SegmentBase segment : adjacentSegments) {
            int destination = segment.y;
            // destination is the other city
            if (cities[destination].weight > relaxedCity.weight + segment.duration && relaxedCity.weight != Long.MAX_VALUE) {
                long originalDistance = cities[destination].weight;
                // so I can use it later to determine if this was ever set
                cities[destination].weight = relaxedCity.weight + segment.duration;
                // reheapify it, to reflect the new changes
                // or add it if it hadn't been yet
                if (originalDistance == Long.MAX_VALUE) {
                    queue.insert(cities[destination]);
                } else {
                    queue.reHeapify(cities[destination]);
                }
            }
        }
    }

    /**
     * Finds the shortest path between each city using the Floyd-Warshall algorithm
     * @param nCities number of cities
     * @param segments roads between cities
     * @return a 2D array that gives the weights of the shortest paths between cities
     */
    private long[][] findShortestPathsFloydWarshall(int nCities, List<SegmentBase> segments) {
        long[][] distance = new long[nCities][nCities];
        // Since you said we could research the algorithm, I am basing this on the Wikipedia pseudocode
        // because I wanted a reliable source
        //let dist be a |V| × |V| array of minimum distances initialized to ∞ (infinity)
        // I will initialize it to Long.MAX_VALUE, because that way, I will never have a problem of being further
        for (int cityFrom = 0; cityFrom < nCities; cityFrom++) {
            Arrays.fill(distance[cityFrom], Long.MAX_VALUE);
        }
        //for each edge (u, v) do
        //    dist[u][v] ← w(u, v)  // The weight of the edge (u, v)
        for (SegmentBase segment : segments) {
            distance[segment.x][segment.y] = segment.duration;
            distance[segment.y][segment.x] = segment.duration;
        }
        //for each vertex v do
        //    dist[v][v] ← 0
        for (int city = 0; city < nCities; city++) {
            distance[city][city] = 0;
        }
        //for k from 1 to |V|
        //    for i from 1 to |V|
        //        for j from 1 to |V|
        //            if dist[i][j] > dist[i][k] + dist[k][j]
        //                dist[i][j] ← dist[i][k] + dist[k][j]
        //            end if*/
        // I'm pretty sure those are using 1-N bounds, so I need to change it to 0-(n-1)
        for (int k = 0; k < nCities; k++) {
            for (int i = 0; i < nCities; i++) {
                for (int j = 0; j < nCities; j++) {
                    // I am putting a caveat that if distance[i][k] or distance[k][j] are
                    // infinity, we just assume that [i][j] is not greater
                    // because there is no actual infinity value for longs, and we have overflow issues
                    if (distance[i][k] != Long.MAX_VALUE && distance[k][j] != Long.MAX_VALUE &&
                            distance[i][j] > distance[i][k] + distance[k][j]) {
                        distance[i][j] = distance[i][k] + distance[k][j];
                    }
                }
            }
        }

        // this had better work, because I don't want to debug this
        // since I only sort of understand this algorithm and got it from Wikipedia
        return distance;
    }

    /**
     * Given a possible segment addition, returns the improvement caused by the segment
     * The improvement is the change in distance between all pairs of cities
     * @param possibility segment that we are considering adding to the network
     * @param shortestPaths array that gives the length of the shortest path between all cities
     * @return the improvement of the pairwise distance of the network caused by the segment
     */
    private long evaluateSuggestion(SegmentBase possibility, long[][] shortestPaths) {
        // first, we create a list of cities where city-x + possibility < city-y
        // and the same for y
        // Because all improved routes need to have an x from the first list and a y from the second
        List<Integer> xCities = new ArrayList<>();
        List<Integer> yCities = new ArrayList<>();
        for (int city = 0; city < shortestPaths.length; city++) {
            if (shortestPaths[city][possibility.x] + possibility.duration < shortestPaths[city][possibility.y]) {
                xCities.add(city);
            }
            if (shortestPaths[city][possibility.y] + possibility.duration < shortestPaths[city][possibility.x]) {
                yCities.add(city);
            }
        }
        //System.out.println("For segment " + possibility + ": ");
        //System.out.println("xCities are " + xCities);
        //System.out.println("and yCities are " + yCities);
        // now, we go through every combination of cities, and sum the differences, if any
        long improvement = 0;
        for (int leftCity : xCities) {
            for (int rightCity : yCities) {
                long newDuration = shortestPaths[leftCity][possibility.x] + possibility.duration + shortestPaths[rightCity][possibility.y];
                if (newDuration < shortestPaths[leftCity][rightCity]) {
                    improvement += shortestPaths[leftCity][rightCity] - newDuration;
                }
            }
        }
        //System.out.println("Improvement was " + improvement);
        //System.out.println();
        return improvement;
    }

}
/*
* So, let's start by figuring out the brute force solution
* We can start by checking the graph with each possibility given, calculate the score of each, and minimizing it
* Calculating it would mean doing a DFS from each node and figuring out the scores of each
* Since DFS is O(V + E), doing it from each node is O(V(V+E)) or (O(V^2 + VE))
* Doing this for each given vertex G is O(GV^2 + VEG)
* That doesn't sound too hard
*
* Now I need to figure out how to improve the solution. I feel like one way to do it is to first determine the
* connectedness graph (there is a name for this, I forget what) without any of the additions
* Then, for each addition, we calculate the changes that happen to potentially relevant nodes, by doing a DFS
* from the nodes adjacent to the addition
* But that wouldn't work, because sometimes, pairs are updated that aren't either, or even passed through either
* before the addition was made.
* So, that wouldn't work
* I still feel like there is a better way
* Professor expects our implementation to be better, but still O(n^3). I feel like there has got to be a
* O(n^2) or O(n^2lgn) way of doing it, but I don't know what
* I also didn't bring my notes home, which might not have been a good idea if I wanted ideas from them
* There are probably algorithms that would be helpful
* I know people are looking at last year's slides, so perhaps I should too
* Transitive closure! That's the name I am looking for
* Calculating the transitive closure is O(n^3), though, and we are doing this multiple times
* That doesn't make sense though, it should be O(n^2)
* I am probably misremembering it
* Looked it up, those are for dense graphs, which I have been guaranteed not to have
*
* Could I use an MST algorithm? One that calculates the minimum edges needed to connect everything?
* I'm not sure it makes sense. That one returns a sparser graph
*
* So, the brute-force probably still is find the transitive closure via V(V+E) G times
* I still feel like there is a better way
* Plus, I feel like he intends us to use Warshall's Algorithm as brute-force, which doesn't make any sense
* But, my brute force is n^3, so it is definitely good enough
* The only worry is that it could be the efficient algorithm, and thus, what he wants from my main algorithm
* But no, that doesn't sound right
* I feel like a better algorithm was in the corner of my mind, but then I forgot it
* Let's see, what was it?
* First, I calculate a transitive closure
* Then, with the transitive closure graph, I calculate what would happen by adding this edge for each edge
* It might still be GV(V+E), but it feels better
* Anyway, let's get this in more detail
* I'm pretty sure I keep coming back to an algorithm like this
* For each edge, I calculate the distance between that edge and every other edge using this graph
* This is a dense graph now, not a sparse graph
* For each connection, I have these possibilities:
* 1) This edge does not improve, and so I use the route I had before, 1 edge on the closure
* 2) This edge does improve by directly connecting the nodes, so I go through it to the new nodes
* 3) This edge improves by starting the journey. I start at this edge and then go through a transitive edge
* 4) This edge improves by ending the journey. I go through a transition edge and then this edge
* 5) This edge is in the middle of a journey. I go through 3 edges: a transition edge, this edge, and another transition edge
* I feel like I just had an idea about exploring the graph
* Where I keep track of whether or not I use a new edge while exploring
* When exploring in general, without having gone through a new edge, I give a general mark that works for all
* When exploring with a new edge, I give a special mark just for that edge. When I go back, I lose that mark
* I realized that when exploring, I could use a special technique for SPTs or the like
* But I think it makes more sense to first find the transitive closure
* In a case like this, I can't use more than one transitive edge in a row
* So, I could keep track of the number of transitive edges I have used, and only explore non-transitive edges
* So, for each vertex, I can do five stages, each of which can detect one of the possibilities I saw before:
* 1) I check each of its transition edges, at cost V.
* 2) For each of those edges, I check a suggestion edge, at cost G, for VG.
* 3) For each of those edges, I check a transition edge, at cost V, for V^2*G.
* 4) At the original vertex, I check all suggestion edges, at cost G
* 5) I check all transition edges on those suggestion edges, at cost V*G.
* But then I need to do this for every vertex, don't I? In that case, this is much worse
* So, that doesn't work at all as a possibility
* My problem is that normally, I detect I am done when at an order of growth, but here, that is impossible
* So, how do I even know when I have an improvement?
* But, I also don't think I found one.
*
* I think that what I want to do is incorporate the G into the algorithm
* I will use indentation to show where in the loop I am
* So, for each vertex V:
*   I will start a BFS from it
* Wait a second. My brute-force probably doesn't even work. Because this is a weighted graph. I can't explore
* in the order of BFS. I need to use Dijkstra or the like to find the shortest path tree.
* Then I would do that for each vertex and each suggested edge. That's GV^2lgE, not N^3, but worse
* So, I need to first figure out what he even wants for the brute force alg, and then figure out how to improve it
*
* This is really hard. I am having a hard time thinking of any N^3 algorithm, and if I found one, I wouldn't
* consider it brute force. But according to my plan, I need it not just found, but improved and implemented tonight
* This is not good. I might need an extension
*
* So, how do I even get an N^3 solution? This doesn't make any sense. It only made sense when I got confused
* between finding the shortest path and the length to it
* If I were to walk every single path between cities, that would be ridiculous. I think it is exponential
* So, I need to find the best paths and walk them. I know how to do that. It is a nlgn operation.
* Perhaps my idea of inserting all the possibilities into the graph and only following one at a time has merit
* But it doesn't, because Dijkstra doesn't work one-at-a-time
* Perhaps I could modify it to work that way?
* But no, sometimes, the cheapest way to get to there from my starting point is to go backwards first
* Perhaps we could first make a MST
* Then, we add each edge and remove it, and see how that changes things
* But that would probably require an O(n) analysis
* We would modify our thing to make it still a tree, and see how that changes things
* But that's an O(n) operation
* Making this n^3lgn again, which is too expensive
* I am clearly looking at this from the wrong perspective, but I am not very good at changing my perspective
* Maybe I should go to Maariv. I will have terrible Kavana, but I will have terrible Kavana if I wait an hour also
* Perhaps I will have a solution when I am done. I think it happened once
* Anyway, how do I make this n^3?
* It is possible that he is thinking n^3 while keeping possibilities constant
* But in that case, I have n^2lgn possibilities, which is better than he could have hoped
* So, no, that's not good
* So, I still need to figure out what I am looking for
*
* Floyd-Warshall is apparently an algorithm for weighted graphs
* But at V^3, it is still too expensive to use more than once
* I could use it to find all shortest paths, and then do other stuff after that at a lesser order of growth
* He did say that the leading term is V^3, so there could be others
* Perhaps, for each suggestion, we figure out if it improves things
* We check every combination of vertices on one side of the edge with the other side
* If it improves things, good
* So, for each suggestion, we do V^2 work, for V^2 * G work
* Finally, an algorithm that seems to work
* Let's go to Maariv now
*
* Having thought of it, yes, this works
* But, now I need to think of how to improve this
* I'm not supposed to be able to improve the order of growth, so that means that I need to improve the second term
* But first, let's code Floyd-Warshall, since I don't think I will be improving that
* Though Shlomo is proving that even classic algorithms can sometimes be improved
*
* Should I form an adjacency list, or will I never need it?
* I'm pretty sure that once I have the array of shortest paths, I will not need anything else
* But I always can form one if needed
*
* Okay, so now I need to figure out what I want to add later
* I feel like I could use sorting to take it from GV^2 to GVlgV
* But I'm not quite sure what to sort by
* So let's just outline every possibility and see if it solves the problem
*
* Let's start by making some terminology
* The suggested edge is called the cut or c, and connects b-d
* It connects the left node and the right node
* The connections of the left node are the left siblings, usually called x
* The connections of the right node are the right siblings, usually called y
*
* Let's put down things I know are unnecessary
* The epitome of ridiculousness is checking x-c-y when x==y
* Obviously, the shortest path here is no path at all
* So, let's describe situations
* The direct path between x-y here is 0
* x-c and c-y are equal, but that doesn't mean anything
* I think the real problem here is when the direct path x-y is quicker than x-c-y
* Except, wait, that's just the definition of the problem I am trying to solve
* I feel like I should be able to sort Y
* Because everything has to go through c
* So, there is no reason why I should ever have to go through the same node in both X and Y
* If X used it, Y definitely won't
* In fact, it might make sense to go through each node and figure out whether it is closer to b or d, and
* partition it accordingly
* But that won't boost the order of growth unless most end up on one side
* I feel like there is some property of the input that I am just missing
* Like, I am going through this 1D array, but as soon as I see x, I realize that I don't need y anymore
* We know that if x-y is better than x-c-y, we don't need c
* But checking each x-y for this is my current O(n^2) algorithm that I don't like
* So, I want to figure out how to improve this based on x-c and y-c, because checking those is O(n)
* Or sorting it is O(nlgn)
* Let's say that x-c is very long
* But if x-y is even longer, then it might work if y-c is short enough
* I know I am repeating myself, but x-c + y-c + c must be > x-y
* Wait a minute. This feels like something I have seen before
* But I don't think it is, because I don't just want endpoints at c and I already have the shortest paths
* The simplest case is x=c and y=d. I know that if that doesn't work, I can throw out c
* So perhaps I want to have 2 lists: one sorted by dist-b and the other by dist-d
* Then, I go through X. For each y in Y, I check if it is better until I find one that isn't
* Anything further definitely won't be better
* But is that actually definite?
* Let's say we are doing x = b, and going through each y
* We find that y is not a good match, because there is already a y-b connection that is cheaper
* But, y' is a good match, because even though d-y' > d-y, d-y' < b-y
* I am having a hard time visualizing. Let's say there are only 4 edges. We have Q, W, E, and R
* They are all connected in a line for maximum simplicity
* Let's draw it
* The cut is W-E
* x=W
* We are sorting nodes in distance from E, so E, Q, W, R
* W-E is of course improved
* W-Q is not improved, because it is a roundabout route to use the cut
* W-W is ridiculous and of course not improved
* but W-R, despite R being furthest from E, is still improved, since it is a roundabout way to get to W and
* this improves it
* So that method of sorting doesn't work
*
* Let's say that we instead do the reverse, sort Y by its distance from b and X by its distance from d
* So, X is sorted E, Q, W, R, while Y is sorted W, Q, E, R
* We start by checking E-W, which is right, except that we are doing x-b + y-d + c, so 15 + 15 = 30, bad
* We then check E-Q, which is also backwards, but might work if the other direction
* Then we check E-E, which is ridiculous, followed by E-R, also ridiculous
* We then do Q-W, which is stupid
* followed by Q-Q, nope
* Followed by Q-E, already done
* Followed by ...
* Okay, this clearly isn't working
*
* Perhaps sorted by current length? But who are we sorting? Every combination?
* I am going to stop and change into PJs and hopefully get insight
*
* I feel like I thought of an insight and then immediately forgot it
* First of all, before doing anything, I can filter, removing from the first list any vertex where x-b + c < x-d
* And, likewise, from the second list, any vertex where c + d-y < b-y
* But, that only filters, I don't think it reduces the complexity by an order of growth
* Once I do that filtering, in my simple example, X will have W and Q, while Y will have E and R
* Hmm, in this case, I perfectly divide the sample space in half, but I think that is a coincidence
* If c is big, I might actually lose cities entirely
* But regardless, x/2*x/2 still equals x^2/4, which is only a constant improvement
* Still, might this mean my sorting idea now works?
* Probably not, and I might need a bigger case, but let's check
* With the initial filter idea, the second was stupid
* W-E is first and good
* Then we have W-R which is also good
* Then we have Q-E, which is good
* Then we have Q-R, also good
* Huh, in this case, getting rid of those means all remaining combinations work
* I assume this is not a normal case
* Let's use my first complex cut case
* It's a case of adding 1-4 (4)
* Let b=1 and d=4
* So, X would include all where x-b + 4 < x-d
* X has 1 (0 + 4 < 7)
* It has 0 (1 + 4 < 8)
* It does not have 2 (2 + 4 > 5)
* In fact, this rejects everything that is not on the left
* Meanwhile, on the right (here, top):
* Y has 4 (0 + 4 < 7)
* It does not have 3 (3 + 4 > 4)
* It does not have 7 (4 + 4 > 5)
* In fact, in both these cases, it is the same
* In fact, I bet I could figure out a mathematical formula to solve this, though it might not be fun
* So, let's assume (needs to be proven) that all xs that pass the filter and all ys that pass the filter are
* efficient together
* P by Contradiction: Let's assume there was an x where x-b + c < x-d and c + d-y < b-y, but x-y > x-c-y
* I'm not sure where to go from here, never write proofs while tired
* I just realized that 8 only connecting to 6 improving in the hyperspace rail proves that there are cases where
* it still doesn't work
* I temporarily give up, but I will keep working next time. Maybe build what I have so far
* And have the computer give me the lists I am calculating by hand
*
* I still want to figure out how to make an algorithm more efficient. If not, I can try making it parallel
* That will certainly make it more efficient, beyond what he expects us to be able to get, most likely
* And I can do it combined with the filters I have already developed
* But I am pretty sure it is not what he is looking for, and I am worried that doing it wrong will make me lose points
* Either way, I should definitely check Piazza first, I saw there were more posts
* Okay, that probably doesn't mean I need to do any adjustments accordingly
* So I should just be able to make it parallel
* I still can't make the dominant term parallel, because each step depends on the previous step
* So that has to be sequential
* But that's fine, he said we wouldn't be able to improve things from an order of growth perspective
* I think I will choose to do it parallel over the edge choices
* Because if V > G, we are still limited in efficiency by V^3, no matter what we do in the second part
* But if G ~ V^2, then the second part could balloon to V^4, so it is imperative to keep it down
* That sounds wrong, though. My second part should be more efficient than my first part
* I feel like there is definitely a better way of doing this
* But I don't think there is
* First of all, I have to examine each G
* Because if I don't, it could be that I skipped a G that contains the right answer
* For each G, that is where perhaps I could improve things
* Currently, this part is GV^2, because while my filters reduce the problem size significantly, in the worst-case
* scenario, it is still V^2, only reduced by a factor of 4
* In the best case scenario, it is reduced to V because the filters get rid of everything
* I still feel like there has to be a way of sorting things so that I don't have to deal with the bad guys
* But by what?
* Let's start with a case: In the jagged graph, segment 0-2 (1) has x contain 0, 5, 6, 7, 8, and 9
* While y contains 2, 3, and 4
* Let's swap x and y, so that x has 2, 3, and 4, while y has 0, 5, 6, 7, 8, and 9
* For x=2, we get improvements at 0, 5, 6, 7, 9, and 9
* For x=3, we get improvements at 0, 5, and 6
* For x=4, we get improvements at 0
* It happens to be that these are sorted by city, but that is a coincidence
* But they are sorted by distance from D (in this case, 0)
* Where 0 has 0 distance, 5 has 3 distance, 6 and 7 have 5 distance, 8 has 7 distance, and 9 has 10 distance
* But that doesn't actually work, because there is no reason to put 6 before 7 in this model, since both are
* equally distant
* But 6 is included while 7 is excluded
* So I think that the sorting is a coincidence
* But the minimum examination is that we examine every edge that is improved (plus a constant)
* Could I give a case where things are included by an n^2 factor?
* Yes I could
* Let's say that we have a canyon separating 2 cities, and the only way across is a very expensive bridge
* I add a cheaper bridge down the valley
* Every connection across the cut now becomes cheaper, which is n^2/4
* In fact, that suggests that in the worst-case scenario, I have to examine all of those ANYWAY
* It's possible that in a general scenario, I wouldn't, though
* Perhaps for most, I don't have to examine that many, but this time, I do
* I think the reason I can't sort it is that improvement is based both on distance from the cut and on distance
* from the destination
* That's what distinguishes 6 and 7 - 3 was already closer to 7 than 6
* But I can't write a proof based on that
* So, I think that parallel is the way to go
*
* I have less than 2 hours to submit the assignment and maybe also take a shower. But I need to improve lower-
* order terms. So, a Hackerrank. Great.
*
* Okay. So, my algorithm has 2 components. The second, finding the paths through each possibility and comparing them with the paths without the
* new segment, probably can't be improved, because there could be multiple things from multiple sides. There still might be some way of sorting it, but
* even then, the number of things that are improved can be equivalent to n^2, so unless there is a clever mathematical way, I am stuck at gn^2 in the
* worst case. So, that's not what I should be focusing on. And anyway, he said that the dominant term would be n^3 no matter what we do.
*
* So, I need to find some way of optimizing the velogv component.
* */