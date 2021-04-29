package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph;

import java.io.Serializable;
import java.util.Objects;

public class Edge<T extends NodeContent> implements Serializable {
    protected final Node<T> source;
    protected final Node<T> destination;
    protected final double weight;


    public Edge(Node<T> source, Node<T> destination, double weight) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(destination);
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public Node<T> getSource() {
        return source;
    }

    public Node<T> getDestination() {
        return destination;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "Edge{" + "source=" + source.getContent().getOrder() + ", destination=" + destination.getContent().getOrder() + ", weight=" + weight + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;

        Edge<?> edge = (Edge<?>) o;

        if (Double.compare(edge.getWeight(), getWeight()) != 0) return false;
        if (getSource() != null ? !getSource().equals(edge.getSource()) : edge.getSource() != null) return false;
        return getDestination() != null ? getDestination().equals(edge.getDestination()) : edge.getDestination() == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getSource() != null ? getSource().hashCode() : 0;
        result = 31 * result + (getDestination() != null ? getDestination().hashCode() : 0);
        temp = Double.doubleToLongBits(getWeight());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
