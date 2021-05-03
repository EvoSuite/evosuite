package org.evosuite.instrumentation.certainty_transformation.method_analyser.results.graph;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Graph which nodes contain content. The edges of the graph are stored in an edge list.
 *
 * @param <T> The type of the content. Must implement NodeContent.
 */
public class DirectedGraph<T extends NodeContent> implements Serializable {
    private Set<Node<T>> nodes;
    private Set<Edge<T>> edges;
    // private AdjacencyMatrix<T> edges;

    private static class AdjacencyMatrix<T extends NodeContent>{
        private final Map<Node<T>, Set<Edge<T>>> successors;
        private final Map<Node<T>, Set<Edge<T>>> predecessors;
        public AdjacencyMatrix(Collection<Edge<T>> edges, Collection<Node<T>> nodes){
            successors = new HashMap<>();
            predecessors = new HashMap<>();
            nodes.forEach(n -> {
                successors.put(n, new HashSet<>());
                predecessors.put(n, new HashSet<>());
            });
            edges.forEach(e -> {
                successors.get(e.getSource()).add(e);
                predecessors.get(e.getDestination()).add(e);
            });
            //System.out.println("Finished");
        }

        public Set<Node<T>> getSuccessors(Node<T> node){
            return successors.get(node).stream().map(Edge::getDestination).collect(Collectors.toSet());
        }

        public Set<Node<T>> getPredecessors(Node<T> node){
            return predecessors.get(node).stream().map(Edge::getSource).collect(Collectors.toSet());
        }

        public Optional<Edge<T>> getEdgeIfPresent(Node<T> from, Node<T> to){
            return successors.get(from).stream().filter(e -> e.getDestination().equals(to)).findAny();
        }

        public Set<Edge<T>> asSet(){
            return successors.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
        }
    }

    /**
     * ArrayList of Edges describing a path from a source to a destination.
     *
     * @param <T> NodeContent
     */
    public static class Path<T extends NodeContent> extends ArrayList<Edge<T>> {
        public Path(int initialCapacity) {
            super(initialCapacity);
        }

        public Path() {
        }

        public Path(Collection<? extends Edge<T>> c) {
            super(c);
        }

        public Node<T> getSourceNode() {
            return this.get(0).getSource();
        }

        public T getSource() {
            Node<T> source = getSourceNode();
            return source.getContent();
        }

        public Node<T> getDestinationNode() {
            return this.get(this.size() - 1).getDestination();
        }

        public T getDestination() {
            Node<T> destination = getDestinationNode();
            return destination.getContent();
        }

        public Stream<T> streamOfElements(){
            return getContents(Collectors.toList()).stream().map(Node::getContent);
        }

        public double distance() {
            return stream().mapToDouble(Edge::getWeight).sum();
        }

        public Path<T> extend(Path<T> other) {
            Path<T> extended = new Path<>(this);
            extended.addAll(other);
            return extended;
        }

        public Set<Node<T>> getContents(){
            Collector<Node<T>, ?, Set<Node<T>>> nodeSetCollector = Collectors.toSet();
            return getContents(nodeSetCollector);
        }

        public<X extends Collection<Node<T>>> X getContents(Collector<Node<T>, ?, X> nodeCollector){
            X collect = stream().map(Edge::getSource).collect(nodeCollector);
            collect.add(get(size()-1).getDestination());
            return collect;
        }

    }

    public T getByOrder(int order){
        return getContents().stream().filter(c -> c.getOrder() == order).findFirst().orElse(null);
    }

    public DirectedGraph(Collection<Node<T>> nodes, Collection<Edge<T>> edges) {
        this.nodes = new HashSet<>(nodes);
        this.edges = new HashSet<>(edges);
        //this.edges = new AdjacencyMatrix<>(edges,nodes);
    }

    protected Optional<Node<T>> getNodeIfPresent(T content) {
        return nodes.stream().filter(t -> t.getContent().equals(content)).findFirst();
    }

    public Collection<Node<T>> getNodes() {
        return new HashSet<>(nodes);
    }

    public Collection<Edge<T>> getEdges() {
        return new HashSet<>(edges);
        // return edges.asSet();
    }

    public Collection<Node<T>> getSuccessors(Node<T> node) {
        // return edges.getSuccessors(node);
        return edges.stream().filter(e -> e.getSource().equals(node)).map(Edge::getDestination).collect(Collectors
         .toSet());
    }

    public Collection<Node<T>> getPredecessors(Node<T> node) {
        //return edges.getPredecessors(node);
        return edges.stream().filter(e -> e.getDestination().equals(node)).map(Edge::getSource).collect(Collectors
         .toSet());
    }

    public Collection<T> getPredecessors(T content) {
        Node<T> node = getNodeOfContent(content);
        Collection<Node<T>> predecessors = this.getPredecessors(node);
        return predecessors.stream().map(Node::getContent).collect(Collectors.toList());
    }

    public Collection<T> getSuccessors(T content) {
        Node<T> node = getNodeOfContent(content);
        Collection<Node<T>> successors = this.getSuccessors(node);
        return successors.stream().map(Node::getContent).collect(Collectors.toList());
    }

    public Collection<T> getContents() {
        return nodes.stream().map(Node::getContent).collect(Collectors.toList());
    }

    private Node<T> getNodeOfContent(T content) {
        Optional<Node<T>> first = nodes.stream().filter(n -> n.getContent().equals(content)).findFirst();
        if (first.isEmpty())
            throw new IllegalArgumentException("No node of the graph contains: " + content);
        return first.get();
    }

    public Optional<Edge<T>> getEdgeIfPresent(Node<T> from, Node<T> to) {
        // return edges.getEdgeIfPresent(from,to);
        return edges.stream().filter(e -> e.source.equals(from)).filter(e -> e.destination.equals(to)).findFirst();
    }

    public Optional<Edge<T>> getEdgeIfPresent(T from, T to) {
        return getEdgeIfPresent(getNodeOfContent(from), getNodeOfContent(to));
    }

    public Edge<T> getEdge(Node<T> from, Node<T> to) {
        Optional<Edge<T>> edgeIfPresent = getEdgeIfPresent(from, to);
        if (edgeIfPresent.isPresent())
            return edgeIfPresent.get();
        throw new IllegalArgumentException("No Edge found from " + from + " to " + to);
    }

    public double getEdgeWeight(T from, T to) {
        return getEdgeWeight(getNodeOfContent(from), getNodeOfContent(to));
    }

    public double getEdgeWeight(Node<T> from, Node<T> to) {
        return getEdgeIfPresent(from, to).orElse(new Edge<>(from, to, -1)).getWeight();
    }


    public <R> Map<R, List<T>> groupBy(Function<T, R> mapper) {
        return groupBy(mapper, Comparator.comparingInt(T::getOrder));
    }

    public <R> Map<R, List<T>> groupBy(Function<T, R> mapper, Comparator<T> comparator) {
        return getNodes().stream().map(Node::getContent).sorted(comparator).collect(Collectors.groupingBy(mapper));
    }

    public Optional<Path<T>> dijkstra(T from, T to) {
        Node<T> fromNode = getNodeOfContent(from);
        Node<T> toNode = getNodeOfContent(to);

        // initialising Phase
        Collection<Node<T>> remainingNodes = getNodes();
        Map<Node<T>, Double> distances = new HashMap<>();
        Map<Node<T>, Node<T>> predecessor = new HashMap<>();
        remainingNodes.forEach(n -> {
            distances.put(n, Double.MAX_VALUE);
            predecessor.put(n, null);
        });
        distances.put(fromNode, 0d);

        // Predecessor calculation
        while (!remainingNodes.isEmpty()) {
            Node<T> min = remainingNodes.stream().min(Comparator.comparingDouble(distances::get)).get();
            if (distances.get(min) == Double.MAX_VALUE)
                break;
            remainingNodes.remove(min);
            getSuccessors(min).stream().filter(remainingNodes::contains).forEach(s -> {
                double edgeWeight = distances.get(min) + getEdgeWeight(min, s);
                if (edgeWeight < distances.get(s)) {
                    distances.put(s, edgeWeight);
                    predecessor.put(s, min);
                }
            });
            if (min.equals(toNode))
                break;
        }

        // create shortest path
        if (predecessor.get(toNode) == null)
            return Optional.empty();
        Path<T> shortestPath = new Path<>();
        shortestPath.add(getEdgeIfPresent(predecessor.get(toNode), toNode).orElseGet(() -> {
            throw new IllegalStateException("");
        }));
        while (!shortestPath.getSource().equals(from)) {
            Node<T> source = shortestPath.getSourceNode();
            shortestPath.add(0, getEdgeIfPresent(predecessor.get(source), source).orElseGet(() -> {
                throw new IllegalStateException("");
            }));
        }
        return Optional.of(shortestPath);
    }

    /**
     * Builds all acyclic paths from {@param from} to {@param to}.
     *
     * @param from the source node of the return paths.
     * @param to the destination node of the return paths.
     * @param ignore nodes that should not be contained in the result paths
     * @return all acyclic paths from {@param from} to {@param to} that does not contain any node in {@param ignore}
     */
    private Set<Path<T>> buildAllAcyclicPathsFromTo(Node<T> from, Node<T> to, Set<Node<T>> ignore) {
        // if from equals to, we return an empty path, that will be extended by another context of this method
        if (from.equals(to))
            return Collections.singleton(new Path<>());
        // Generate the next ignore set
        HashSet<Node<T>> nextIgnore = new HashSet<>(ignore);
        nextIgnore.add(from);
        // Build the paths.
        // 1. Get all successors
        // 2. Filter, so that only the ones not in ignore remain.
        // 3. Map them to their corresponding edge(source=from, destination=successor)
        // 4. Recursive call from the destination node with the extended ignore
        // 5. Extend every resulting path with the path that lead to its construction.
        // 6. Flatten the Stream<List<Path>> to Stream<Path>
        // 7. Collect them and return.
        return
                getSuccessors(from)
                        .stream()
                        .filter(n -> !ignore.contains(n))
                        .map(n -> new Path<>(Collections.singletonList(getEdge(from, n))))
                        .map(path ->
                                buildAllAcyclicPathsFromTo(path.getDestinationNode(), to, nextIgnore).
                                        stream().
                                        map(path::extend)
                                        .collect(Collectors.toSet()))
                        .flatMap(Collection::stream).collect(Collectors.toSet());

    }

    public Set<Path<T>> getAllAcyclicPathsFromTo(Node<T> from, Node<T> to) {
        return buildAllAcyclicPathsFromTo(from, to, Collections.emptySet());
    }

    public Set<Path<T>> getAllAcyclicPathsFromTo(T from, T to) {
        return getAllAcyclicPathsFromTo(getNodeOfContent(from), getNodeOfContent(to));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DirectedGraph)) return false;

        DirectedGraph<?> that = (DirectedGraph<?>) o;

        if (getNodes() != null ? !getNodes().equals(that.getNodes()) : that.getNodes() != null) return false;
        return getEdges() != null ? getEdges().equals(that.getEdges()) : that.getEdges() == null;
    }

    @Override
    public int hashCode() {
        int result = getNodes() != null ? getNodes().hashCode() : 0;
        result = 31 * result + (getEdges() != null ? getEdges().hashCode() : 0);
        return result;
    }
}
