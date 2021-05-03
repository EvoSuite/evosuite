package org.evosuite.instrumentation.certainty_transformation.method_analyser.results.graph;

import java.io.Serializable;
import java.util.Objects;

public class Node<T extends NodeContent>  implements Serializable {
    private final T content;

    public Node(T content){
        Objects.requireNonNull(content);
        this.content = content;
    }

    public T getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Node{" + "content=" + content + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node<?> node = (Node<?>) o;

        return content.equals(node.content);
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }
}
