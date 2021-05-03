package org.evosuite.instrumentation.certainty_transformation.method_analyser.results.graph;

public class UnweightedEdge<T extends NodeContent> extends Edge<T> {
    public UnweightedEdge(Node<T> source, Node<T> destination) {
        super(source, destination, 1);
    }
}
