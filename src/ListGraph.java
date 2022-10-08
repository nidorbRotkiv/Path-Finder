import java.util.*;

/**
 * Oriktad graf
 */
public class ListGraph<T> implements Graph<T> {

    private final Map<T, Set<Edge<T>>> nodes = new HashMap<>();

    @Override
    public void add(T node) {

        if (!nodes.containsKey(node)) {

            nodes.putIfAbsent(node, new HashSet<>());
        }
    }

    @Override
    public void remove(T inputNode) throws NoSuchElementException {
        if (nodes.containsKey(inputNode)) {

            // iterera över alla edges i nodens set och raderar edgen från noden om edgen har inputNode som destination

            for (Set<Edge<T>> nodesEdges : nodes.values()) {
                nodesEdges.removeIf(edge -> edge.getDestination().equals(inputNode));
            }

            nodes.remove(inputNode);

        } else {
            throw new NoSuchElementException("Noden finns ej i grafen.");
        }
    }

    public void connect(T a, T b, String name, int weight) throws NoSuchElementException, IllegalArgumentException, IllegalStateException {

        if (!nodes.containsKey(a) || !nodes.containsKey(b)) {
            throw new NoSuchElementException("Minst en av noderna existerar inte.");
        }

        if (weight < 0) {
            throw new IllegalArgumentException("Vikten är mindre än 0.");
        }

        for (Edge edge : nodes.get(a)) {
            if (edge.equals(new Edge(b, name, weight))) {
                throw new IllegalStateException("denna koppling finns redan");
            }
        }

        //   connect(b, a, name, weight);

        add(a);
        add(b);

        Set<Edge<T>> edgesOfA = nodes.get(a);
        Set<Edge<T>> edgesOfB = nodes.get(b);

        edgesOfA.add(new Edge(b, name, weight));
        edgesOfB.add(new Edge(a, name, weight));

    }

    public boolean pathExists(T a, T b) {

        if(!nodes.containsKey(a) || !nodes.containsKey(b)) {
            return false;
        }

        Set<T> visited = new HashSet<>();
        depthFirstVisitAll(a, visited);
        return visited.contains(b);
    }

    public List<Edge<T>> getAnyPath(T from, T to) {
        Map<T, T> connection = new HashMap<>();
        depthFirstConnection(from, null, connection);
        if (!connection.containsKey(to)) {
            return Collections.emptyList();
        }
        return gatherPath(from, to, connection);
    }

    public List<Edge<T>> getShortestPath(T from, T to) {
        Map<T, T> connections = new HashMap<>();
        connections.put(from, null);

        LinkedList<T> queue = new LinkedList<>();
        queue.add(from);
        while (!queue.isEmpty()) {
            T city = queue.pollFirst();
            for (Edge edge : nodes.get(city)) {
                T destination = (T) edge.getDestination();
                if (!connections.containsKey(destination)) {
                    connections.put(destination, city);
                    queue.add(destination);
                }
            }
        }

        if (!connections.containsKey(to)) {
            throw new IllegalStateException("no connection");
        }

        return gatherPath(from, to, connections);

    }

    private List<Edge<T>> gatherPath(T from, T to, Map<T, T> connection) {
        LinkedList<Edge<T>> path = new LinkedList<>();
        T current = to;
        while (!current.equals(from)) {
            T next = connection.get(current);
            Edge edge = getEdgeBetween(next, current);
            path.addFirst(edge);
            current = next;
        }
        return Collections.unmodifiableList(path);
    }

    public Edge getEdgeBetween(T next, T current) throws NoSuchElementException {

        if (!nodes.containsKey(next) || !nodes.containsKey(current)) {
            throw new NoSuchElementException("Minst en av noderna finns ej i grafen.");
        }

        for (Edge edge : nodes.get(next)) {
            if (edge.getDestination().equals(current)) {
                return edge;
            }
        }

        return null;
    }

    private void depthFirstConnection(T to, T from, Map<T, T> connection) {
        connection.put(to, from);
        for (Edge edge : nodes.get(to)) {
            if (!connection.containsKey(edge.getDestination())) {
                depthFirstConnection((T) edge.getDestination(), to, connection);
            }
        }

    }

    private void depthFirstVisitAll(T current, Set<T> visited) {
        visited.add(current);
        for (Edge edge : nodes.get(current)) {
            if (!visited.contains(edge.getDestination())) {
                depthFirstVisitAll((T) edge.getDestination(), visited);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (T node : nodes.keySet()) {
            sb.append(node).append(": ").append(nodes.get(node)).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void setConnectionWeight(T node1, T node2, int weight) throws NoSuchElementException, IllegalArgumentException {

        if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
            throw new NoSuchElementException("minst en av noderna finns ej i grafen");
        }

        if (getEdgeBetween(node2, node1) == null || getEdgeBetween(node1, node2) == null) {
            throw new IllegalArgumentException("Det finns ingen koppling mellan noderna");
        }


        getEdgeBetween(node2, node1).setWeight(weight);
        getEdgeBetween(node1, node2).setWeight(weight);


    }

    @Override
    public Set getNodes() {
        return Set.copyOf(nodes.keySet());
    }

    // TA DEN TILL HANDLEDNING - Varför <Edge<T>>?? // fUNKAR DOCK bra

    @Override
    public Collection<Edge<T>> getEdgesFrom(T node) throws NoSuchElementException {

        if (!nodes.containsKey(node)) {
            throw new NoSuchElementException("noden finns inte i avbildningen");
        }
        return Set.copyOf(nodes.get(node));

    }

    // Medveten begränsning, då det inte går att connecta igen

    @Override
    public void disconnect(T node1, T node2) throws NoSuchElementException, IllegalStateException {

        if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
            throw new NoSuchElementException("Minst en av noderna finns ej i grafen.");
        } else if (getEdgeBetween(node1, node2) == null) {
            throw new IllegalStateException("Finns ingen koppling mellan dessa noder.");
        } else {

            nodes.get(node1).remove(getEdgeBetween(node1, node2));
            nodes.get(node2).remove(getEdgeBetween(node2, node1));

        }

    }

    // borde kanske vara getShortestPath?

    @Override
    public List<Edge<T>> getPath(T from, T to) {

        if(getAnyPath(from, to).isEmpty()) {
            return null;
        }
        return getAnyPath(from, to);
    }

    public Map<T, Set<Edge<T>>> getNodesMap(){
        return nodes;
    }
}