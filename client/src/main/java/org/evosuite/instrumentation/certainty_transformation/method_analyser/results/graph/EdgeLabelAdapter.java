package org.evosuite.instrumentation.certainty_transformation.method_analyser.results.graph;

public class EdgeLabelAdapter<T extends NodeContent> extends Edge<T> {

    private final String label;
    private final Edge<T> adaptee;

    public EdgeLabelAdapter(Edge<T> adaptee, String label){
        super(adaptee.getSource(),adaptee.getDestination(), adaptee.getWeight());
        this.adaptee = adaptee;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public Node<T> getSource() {
        return adaptee.getSource();
    }

    @Override
    public Node<T> getDestination() {
        return adaptee.getDestination();
    }

    @Override
    public double getWeight() {
        return adaptee.getWeight();
    }

    @Override
    public String toString() {
        return adaptee.toString();
    }

    @Override
    public boolean equals(Object o) {
        return adaptee.equals(o);
    }

    @Override
    public int hashCode() {
        return adaptee.hashCode();
    }
}
