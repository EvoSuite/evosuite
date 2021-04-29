package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph;

public class UnweightedEdge<T extends NodeContent> extends Edge<T> {
    public UnweightedEdge(Node<T> source, Node<T> destination) {
        super(source, destination, 1);
    }
}
