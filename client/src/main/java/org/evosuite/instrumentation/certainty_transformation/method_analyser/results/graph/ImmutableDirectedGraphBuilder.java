package org.evosuite.instrumentation.certainty_transformation.method_analyser.results.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

public class ImmutableDirectedGraphBuilder<T extends NodeContent> extends DirectedGraphBuilder<T> {

    public ImmutableDirectedGraphBuilder() {
        super();
    }

    public ImmutableDirectedGraphBuilder(Collection<Node<T>> nodes, Collection<Edge<T>> edges) {
        super(nodes, edges);
    }

    public ImmutableDirectedGraphBuilder(DirectedGraphBuilder<T> other, Collection<Node<T>> additionalNodes,
                                         Collection<Edge<T>> additionalEdges) {
        super(other, additionalNodes, additionalEdges);
    }

    public ImmutableDirectedGraphBuilder(DirectedGraphBuilder<T> other) {
        super(other);
    }

    public ImmutableDirectedGraphBuilder(DirectedGraph<T> graph) {
        super(graph);
    }

    @Override
    public ImmutableDirectedGraphBuilder<T> addNodes(Collection<Node<T>> nodes) {
        return new ImmutableDirectedGraphBuilder<>(this, nodes, Collections.emptyList());
    }

    @Override
    protected ImmutableDirectedGraphBuilder<T> addEdgesChecked(Collection<Edge<T>> edges) {
        return new ImmutableDirectedGraphBuilder<>(this, Collections.emptyList(), edges);
    }

    @Override
    protected ImmutableDirectedGraphBuilder<T> updateEdgesChecked(Collection<Edge<T>> edges) {
        super.checkSourceAndDestinationNodes(edges);
        HashSet<Edge<T>> updatedEdges = new HashSet<>(super.edges);
        updatedEdges.removeAll(edges);
        updatedEdges.addAll(edges);
        return new ImmutableDirectedGraphBuilder<>(super.nodes, updatedEdges);
    }

    @Override
    public DirectedGraphBuilder<T> reverse() {
        return new ImmutableDirectedGraphBuilder<>(nodes, edges.stream().map(e -> new Edge<>(e.getDestination(),
                e.getSource(),e.getWeight())).collect(Collectors.toSet()));
    }
}
