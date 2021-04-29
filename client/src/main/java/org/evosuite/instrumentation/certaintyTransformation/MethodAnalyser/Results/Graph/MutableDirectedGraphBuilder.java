package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class MutableDirectedGraphBuilder<T extends NodeContent> extends DirectedGraphBuilder<T> {
    public MutableDirectedGraphBuilder() {
        super();
    }

    public MutableDirectedGraphBuilder(DirectedGraphBuilder<T> other) {
        super(other);
    }

    public MutableDirectedGraphBuilder(Collection<Node<T>> nodes, Collection<Edge<T>> edges) {
        super(nodes, edges);
    }

    public MutableDirectedGraphBuilder(DirectedGraphBuilder<T> other, Collection<Node<T>> additionalNodes,
                                       Collection<Edge<T>> additionalEdges) {
        super(other, additionalNodes, additionalEdges);
    }

    public MutableDirectedGraphBuilder(DirectedGraph<T> graph) {
        super(graph);
    }

    @Override
    public DirectedGraphBuilder<T> addNodes(Collection<Node<T>> nodes) {
        super.nodes.addAll(nodes);
        return this;
    }

    @Override
    protected DirectedGraphBuilder<T> addEdgesChecked(Collection<Edge<T>> edges) {
        super.edges.addAll(edges);
        return this;
    }

    @Override
    protected DirectedGraphBuilder<T> updateEdgesChecked(Collection<Edge<T>> edges) {
        super.edges.removeAll(edges);
        super.edges.addAll(edges);
        return this;
    }

    @Override
    public DirectedGraphBuilder<T> reverse() {
        Set<Edge<T>> collect = edges.stream().map(e -> new Edge<>(e.getDestination(), e.getSource(), e.getWeight())).collect(Collectors.toSet());
        edges.clear();
        edges.addAll(collect);
        return this;
    }
}
