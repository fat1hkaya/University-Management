package universitiymanagementsystem.datastructures;

/**
 * Pure node-based Directed Graph implementation for course prerequisites.
 * Does not use any java.util collections.
 */
public class DirectedGraph<T> {

    private static class Vertex<T> {
        T data;
        Edge<T> headEdge; // Head of the adjacency list for this vertex
        Vertex<T> nextVertex; // To keep track of all vertices in the graph

        Vertex(T data) {
            this.data = data;
        }
    }

    private static class Edge<T> {
        Vertex<T> destination;
        Edge<T> nextEdge;

        Edge(Vertex<T> destination) {
            this.destination = destination;
        }
    }

    private Vertex<T> verticesHead;
    private int vertexCount;

    public void clear() {
        verticesHead = null;
        vertexCount = 0;
    }

    public void addVertex(T data) {
        if (findVertex(data) != null) return;
        Vertex newVertex = new Vertex(data);
        newVertex.nextVertex = verticesHead;
        verticesHead = newVertex;
        vertexCount++;
    }

    public void addEdge(T fromData, T toData) {
        Vertex from = findVertex(fromData);
        Vertex to = findVertex(toData);

        if (from == null || to == null) return;

        // Check if edge already exists
        Edge curr = from.headEdge;
        while (curr != null) {
            if (curr.destination == to) return;
            curr = curr.nextEdge;
        }

        Edge newEdge = new Edge(to);
        newEdge.nextEdge = from.headEdge;
        from.headEdge = newEdge;
    }

    private Vertex<T> findVertex(T data) {
        Vertex<T> curr = verticesHead;
        while (curr != null) {
            if (curr.data.equals(data)) return curr;
            curr = curr.nextVertex;
        }
        return null;
    }

    /**
     * Checks if a course is a prerequisite for another.
     */
    public boolean isPrerequisite(T prerequisite, T course) {
        Vertex<T> start = findVertex(prerequisite);
        Vertex<T> target = findVertex(course);
        if (start == null || target == null) return false;

        return hasPath(start, target);
    }

    private boolean hasPath(Vertex<T> current, Vertex<T> target) {
        if (current == target) return true;
        
        Edge<T> edge = current.headEdge;
        while (edge != null) {
            if (hasPath(edge.destination, target)) return true;
            edge = edge.nextEdge;
        }
        return false;
    }

    /**
     * Returns a valid course sequence using a simplified Topological Sort (Kahn's algorithm logic).
     */
    @SuppressWarnings("unchecked")
    public T[] getTopologicalSort(T[] placeholder) {
        T[] result = (T[]) java.lang.reflect.Array.newInstance(placeholder.getClass().getComponentType(), vertexCount);
        
        // Calculate in-degrees
        int[] inDegree = new int[vertexCount];
        Vertex<T>[] allVertices = (Vertex<T>[]) new Vertex[vertexCount];
        Vertex<T> currV = verticesHead;
        for (int i = 0; i < vertexCount; i++) {
            allVertices[i] = currV;
            currV = currV.nextVertex;
        }

        for (int i = 0; i < vertexCount; i++) {
            Edge<T> e = allVertices[i].headEdge;
            while (e != null) {
                // Find index of destination
                for (int j = 0; j < vertexCount; j++) {
                    if (allVertices[j] == e.destination) {
                        inDegree[j]++;
                        break;
                    }
                }
                e = e.nextEdge;
            }
        }

        // Simple queue logic for topological sort
        int count = 0;
        boolean[] visited = new boolean[vertexCount];
        
        while (count < vertexCount) {
            boolean found = false;
            for (int i = 0; i < vertexCount; i++) {
                if (!visited[i] && inDegree[i] == 0) {
                    result[count++] = allVertices[i].data;
                    visited[i] = true;
                    found = true;
                    
                    // Reduce in-degree of neighbors
                    Edge<T> e = allVertices[i].headEdge;
                    while (e != null) {
                        for (int j = 0; j < vertexCount; j++) {
                            if (allVertices[j] == e.destination) {
                                inDegree[j]--;
                                break;
                            }
                        }
                        e = e.nextEdge;
                    }
                }
            }
            if (!found) break; // Cycle detected or all processed
        }
        
        return result;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    /**
     * Returns all courses as an array.
     */
    @SuppressWarnings("unchecked")
    public T[] getAllVertices(T[] placeholder) {
        T[] result = (T[]) java.lang.reflect.Array.newInstance(placeholder.getClass().getComponentType(), vertexCount);
        Vertex<T> curr = verticesHead;
        int i = 0;
        while (curr != null) {
            result[i++] = curr.data;
            curr = curr.nextVertex;
        }
        return result;
    }
}
