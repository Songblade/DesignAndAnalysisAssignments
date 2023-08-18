package edu.yu.da;

import java.util.*;

/* Note: This code came from GeeksForGeeks, before I (heavily) modified it
 Found here: https://www.geeksforgeeks.org/ford-fulkerson-algorithm-for-maximum-flow-problem/
 I have changed it to return an answer,
 */
public class MaxFlow {
    private final int V; // Number of vertices in graph
    private final List<Map<Integer, Integer>> residualGraph;
    private final int source;
    private final int sink;

    // the reason I am adding a constructor is that Java is very annoying about returning multiple
    // return values
    // So, each use of MaxFlow means calling the constructor
    // But you would call a separate method to get the minCut
    // This is a Sedgewickian design

    /**
     * Creates an object to run the algorithm
     * @param adjacencyList the graph we are running it on, in the form of an adjacency list
     *                      The adjacency list is organized by the index of the first node
     *                      It contains a map for each node with the destination node as key and the capacity
     *                      as value
     */
    public MaxFlow(List<Map<Integer, Integer>> adjacencyList, int source, int sink) {
        // we make the residual graph by cloning the original
        residualGraph = new ArrayList<>();
        for (Map<Integer, Integer> edgeNodes : adjacencyList) {
            residualGraph.add(new HashMap<>(edgeNodes));
        }
        V = adjacencyList.size();
        this.source = source;
        this.sink = sink;
    }

    // Returns the maximum flow from s to t in the given graph
    public int fordFulkerson() {
        // Create a residual graph and fill the residual
        // graph with given capacities in the original graph
        // as residual capacities in residual graph

        // This array is filled by BFS and to store path
        int[] parent = new int[V];

        int maxFlow = 0; // There is no flow initially

        // Augment the flow while there is path from source to sink
        while (bfs(parent)) {
            // Find minimum residual capacity of the edges along the path filled by BFS. Or we can say find the
            // maximum flow through the path found.
            int pathFlow = Integer.MAX_VALUE;
            for (int vertex = sink; vertex != source; vertex = parent[vertex]) {
                int previous = parent[vertex];
                pathFlow = Math.min(pathFlow, residualGraph.get(previous).get(vertex));
            }

            // update residual capacities of the edges and reverse edges along the path
            for (int vertex = sink; vertex != source; vertex = parent[vertex]) {
                int previous = parent[vertex];
                residualGraph.get(previous).put(vertex, residualGraph.get(previous).get(vertex) - pathFlow);
                if (!residualGraph.get(vertex).containsKey(previous)) {
                    residualGraph.get(vertex).put(previous, pathFlow);
                } else {
                    residualGraph.get(vertex).put(previous, residualGraph.get(vertex).get(previous) + pathFlow);
                }
            }

            // Add path flow to overall flow
            maxFlow += pathFlow;
        }

        // Return the overall flow
        return maxFlow;
    }

    /* Returns true if there is a path from source 's' to
      sink 't' in residual graph. Also fills parent[] to
      store the path */
    private boolean bfs(int[] parent) {
        // Create a visited array and mark all vertices as not visited
        boolean[] visited = new boolean[V];

        // Create a queue, enqueue source vertex and mark source vertex as visited
        LinkedList<Integer> queue = new LinkedList<>();
        queue.add(source);
        visited[source] = true;
        parent[source] = -1;

        // Standard BFS Loop
        while (queue.size() != 0) {
            int currentVertex = queue.poll();

            for (int vertex : residualGraph.get(currentVertex).keySet()) {
                if (!visited[vertex] && residualGraph.get(currentVertex).get(vertex) > 0) {
                    // If we find a connection to the sink node, then there is no point in BFS anymore
                    // We just have to set its parent and can return true
                    if (vertex == sink) {
                        parent[vertex] = currentVertex;
                        return true;
                    }
                    queue.add(vertex);
                    parent[vertex] = currentVertex;
                    visited[vertex] = true;
                }
            }
        }

        // We didn't reach sink in BFS starting from source, so return false
        return false;
    }

    /**
     * @return the nodes on s's side of the min cut
     */
    public List<Integer> findMinCut() {
        List<Integer> minCut = new ArrayList<>();

        boolean[] visited = new boolean[V];

        // Create a queue, enqueue source vertex and mark source vertex as visited
        LinkedList<Integer> queue = new LinkedList<>();
        queue.add(source);
        visited[source] = true;

        // Standard BFS Loop
        while (!queue.isEmpty()) {
            int currentVertex = queue.poll();

            for (int vertex : residualGraph.get(currentVertex).keySet()) {
                if (!visited[vertex] && residualGraph.get(currentVertex).get(vertex) > 0) {
                    // here, we don't care about the parent node, only what is reachable
                    // so, for every vertex, we just add it to the minCut
                    minCut.add(vertex);
                    queue.add(vertex);
                    visited[vertex] = true;
                }
            }
        }

        return minCut;
    }

}
