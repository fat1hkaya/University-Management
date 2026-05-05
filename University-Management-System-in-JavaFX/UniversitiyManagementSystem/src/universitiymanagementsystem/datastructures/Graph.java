package universitiymanagementsystem.datastructures;

import java.util.*;

public class Graph<T> {

    private final Map<T, List<T>> adjacencyList;

    public Graph() {
        this.adjacencyList = new HashMap<>();
    }

    public void addVertex(T vertex) {
        adjacencyList.putIfAbsent(vertex, new ArrayList<>());
    }

    public void addEdge(T from, T to) {
        adjacencyList.putIfAbsent(from, new ArrayList<>());
        adjacencyList.putIfAbsent(to, new ArrayList<>());
        if (!adjacencyList.get(from).contains(to)) {
            adjacencyList.get(from).add(to);
        }
    }

    public boolean hasEdge(T from, T to) {
        return adjacencyList.containsKey(from) && adjacencyList.get(from).contains(to);
    }

    public List<T> getNeighbors(T vertex) {
        return adjacencyList.getOrDefault(vertex, new ArrayList<>());
    }

    public boolean hasCycleDFS() {
        Set<T> visiting = new HashSet<>();
        Set<T> visited = new HashSet<>();
        for (T node : adjacencyList.keySet()) {
            if (dfsHasCycle(node, visiting, visited)) {
                return true;
            }
        }
        return false;
    }

    private boolean dfsHasCycle(T node, Set<T> visiting, Set<T> visited) {
        if (visited.contains(node)) {
            return false;
        }
        if (visiting.contains(node)) {
            return true;
        }
        visiting.add(node);
        for (T next : adjacencyList.getOrDefault(node, Collections.emptyList())) {
            if (dfsHasCycle(next, visiting, visited)) {
                return true;
            }
        }
        visiting.remove(node);
        visited.add(node);
        return false;
    }

    public boolean wouldAddEdgeCreateCycle(T from, T to) {
        Graph<T> temp = copy();
        temp.addEdge(from, to);
        return temp.hasCycleDFS();
    }

    public List<T> topologicalSort() {
        Map<T, Integer> indegree = new HashMap<>();
        for (T node : adjacencyList.keySet()) {
            indegree.put(node, 0);
        }
        for (T node : adjacencyList.keySet()) {
            for (T neighbor : adjacencyList.get(node)) {
                indegree.put(neighbor, indegree.get(neighbor) + 1);
            }
        }

        Queue<T> queue = new ArrayDeque<>();
        for (Map.Entry<T, Integer> entry : indegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        List<T> ordered = new ArrayList<>();
        while (!queue.isEmpty()) {
            T current = queue.poll();
            ordered.add(current);
            for (T neighbor : adjacencyList.getOrDefault(current, Collections.emptyList())) {
                int newValue = indegree.get(neighbor) - 1;
                indegree.put(neighbor, newValue);
                if (newValue == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        return ordered;
    }

    public boolean isValidTopologicalSort() {
        return topologicalSort().size() == adjacencyList.size();
    }

    public Set<T> getVertices() {
        return new HashSet<>(adjacencyList.keySet());
    }

    public void clear() {
        adjacencyList.clear();
    }

    public Graph<T> copy() {
        Graph<T> copy = new Graph<>();
        for (T node : adjacencyList.keySet()) {
            copy.addVertex(node);
            for (T neighbor : adjacencyList.get(node)) {
                copy.addEdge(node, neighbor);
            }
        }
        return copy;
    }
}
