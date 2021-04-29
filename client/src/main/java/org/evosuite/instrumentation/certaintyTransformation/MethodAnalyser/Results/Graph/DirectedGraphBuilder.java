package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public abstract class DirectedGraphBuilder<T extends NodeContent> {
    protected final Set<Node<T>> nodes;
    protected final Set<Edge<T>> edges;

    public Set<Node<T>> getNodes() {
        return nodes;
    }

    public DirectedGraphBuilder() {
        this(Collections.emptyList(), Collections.emptyList());
    }

    public DirectedGraphBuilder(DirectedGraphBuilder<T> other) {
        this.nodes = new HashSet<>(other.nodes);
        this.edges = new HashSet<>(other.edges);
    }

    public DirectedGraphBuilder(Collection<Node<T>> nodes, Collection<Edge<T>> edges) {
        this.nodes = new HashSet<>(nodes);
        this.edges = new HashSet<>(edges);
    }

    public DirectedGraphBuilder(DirectedGraphBuilder<T> other, Collection<Node<T>> additionalNodes,
                                Collection<Edge<T>> additionalEdges) {
        this(other);
        this.nodes.addAll(additionalNodes);
        this.edges.addAll(additionalEdges);
    }

    public DirectedGraphBuilder<T> addContent(T content){
        return addNode(new Node<>(content));
    }

    public DirectedGraphBuilder<T> addContents(Collection<T> contents){
        return addNodes(contents.stream().map(Node::new).collect(Collectors.toSet()));
    }

    public DirectedGraphBuilder<T> addEdge(T from, T to){
        return addEdge(from, to, 1);
    }

    public DirectedGraphBuilder<T> addEdge(Node<T> from, Node<T> to){
        return addEdge(from.getContent(), to.getContent());
    }

    public DirectedGraphBuilder<T> addEdge(T from, T to, double weight){
        Optional<Node<T>> fromOptional = nodes.stream().filter(n -> n.getContent().equals(from)).findAny();
        Optional<Node<T>> toOptional = nodes.stream().filter(n -> n.getContent().equals(to)).findAny();
        if(fromOptional.isEmpty() || toOptional.isEmpty())
            throw new IllegalArgumentException("Add nodes before adding their edges");
        return addEdge(new Edge<>(fromOptional.get(), toOptional.get(),
                weight));
    }

    public DirectedGraphBuilder(DirectedGraph<T> graph){
        this(graph.getNodes(), graph.getEdges());
    }

    public DirectedGraphBuilder<T> addNode(Node<T> node) {
        return addNodes(Collections.singleton(node));
    }

    public abstract DirectedGraphBuilder<T> addNodes(Collection<Node<T>> nodes);

    public DirectedGraphBuilder<T> addEdge(Edge<T> edge) {
        return addEdges(Collections.singleton(edge));
    }

    public DirectedGraphBuilder<T> addEdges(Collection<Edge<T>> edges){
        this.checkSourceAndDestinationNodes(edges);
        this.checkAnyEdgeContained(edges);
        return addEdgesChecked(edges);
    }

    protected abstract DirectedGraphBuilder<T> addEdgesChecked(Collection<Edge<T>> edges);

    public DirectedGraphBuilder<T> updateEdge(Edge<T> edge){
        return this.updateEdges(Collections.singleton(edge));
    }

    public DirectedGraphBuilder<T> updateEdges(Collection<Edge<T>> edges){
        this.checkSourceAndDestinationNodes(edges);
        return updateEdgesChecked(edges);
    }

    protected abstract DirectedGraphBuilder<T> updateEdgesChecked(Collection<Edge<T>> edges);

    public DirectedGraph<T> build(){
        return new DirectedGraph<>(nodes,edges);
    }

    public<R extends DirectedGraph<T>> R build(BiFunction<Collection<Node<T>>, Collection<Edge<T>>,R> constructor){
        return constructor.apply(nodes, edges);
    }

    protected void checkSourceAndDestinationNodes(Collection<Edge<T>> edges){
        boolean sourcesInGraph = edges.stream().map(Edge::getSource).allMatch(nodes::contains);
        if (!sourcesInGraph)
            throw new IllegalArgumentException("Source nodes of all Edges need to be already contained in the MethodAnalyser.Results.Graph");
        boolean destinationsInGraph = edges.stream().map(Edge::getDestination).allMatch(nodes::contains);
        if (!destinationsInGraph)
            throw new IllegalArgumentException("Destination nodes of all Edges need to be already contained in the " + "MethodAnalyser/Results/Graph");
    }

    protected void checkAnyEdgeContained(Collection<Edge<T>> edges){
        boolean anyEdgeContained = edges.stream().anyMatch(this.edges::contains);
        if(anyEdgeContained)
            throw new IllegalArgumentException("An edge is already contained, use updateEdges to update");
    }

    public Node<T> getByOrder(int order){
        List<Node<T>> collect =
                nodes.stream().filter(n -> n.getContent().getOrder() == order).collect(Collectors.toList());
        if(collect.size() > 1)
            throw new IllegalStateException("Order should be unique");
        if(collect.size() == 0)
            return null;
        return collect.get(0);
    }

    public abstract DirectedGraphBuilder<T> reverse();

}
