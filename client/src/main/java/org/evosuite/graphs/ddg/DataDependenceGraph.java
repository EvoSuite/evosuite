package org.evosuite.graphs.ddg;

import org.evosuite.setup.callgraph.Graph;
import org.evosuite.utils.generic.GenericClass;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Captures potential data dependencies between methods that stem from writing to and subsequently
 * reading from the same (static) class attribute. (The idea is closely related to the concept
 * of data dependencies on the statement-level, but instead of statements, we consider methods,
 * and instead of local variables, we consider attributes of classes.)
 * <p>
 * The graph contains two kinds of vertices: method vertices representing methods, and field
 * vertices representing fields (a.k.a. attributes of classes). Method vertices are implemented
 * by the {@code MethodEntry} class, and field vertices by the {@code FieldEntry} class.
 * <p>
 * The graph is directed. The direction of the edges encodes whether a field is read from or
 * written to. For example, an edge from a method {@code foo()} to a field {@code x} denotes that
 * {@code foo()} writes {@code x}. Similarly, an edge from a field {@code y} to a method {@code bar}
 * indicates that {@code y} is read by {@code bar()}. Field vertices can only be connected to
 * method vertices and vice versa. That is, there are no direct connections between two field
 * vertices or two method vertices. (This is similar to red-black trees where a red node must only
 * be connected to a black node and vice versa. There are no connections between two nodes of the
 * same color.)
 * <p>
 * There is a potential data dependency between two methods {@code foo()} and {@code bar()} if
 * they are connected to the same field node, while one methods writes the field and the other
 * method reads the field. For example, a method {@code foo()} writing an attribute {@code x} and a
 * method {@code bar()} reading the same attribute {@code x} induces a potential data dependency
 * between {@code foo()} and {@code bar()}. A method can have data dependencies to itself.
 */
public class DataDependenceGraph {
    private final Graph<ClassMember> graph = new Graph<ClassMember>() {
        // empty
    };

    /**
     * Records the fact that the given method reads the given field.
     *
     * @param reading the method reading the field
     * @param field the field read by the method
     */
    public void methodReadsField(MethodEntry reading, FieldEntry field) {
        graph.addEdge(field, reading);
    }

    /**
     * Records the fact that the given method writes the given field.
     *
     * @param writing the method writing the field
     * @param field the field written by the method
     */
    public void methodWritesField(MethodEntry writing, FieldEntry field) {
        graph.addEdge(writing, field);
    }

    /**
     * Tells whether the given field vertex has outgoing edges.
     *
     * @param field the field vertex whose outgoing edges to check
     * @return {@code true} if there are outgoing edges, {@code false} otherwise
     */
    private boolean hasOutgoingEdge(ClassMember field) {
        return graph.getNeighborsSize(field) > 0;
    }

    /**
     * Tells whether the given field vertex has incoming edges.
     *
     * @param field the field vertex whose incoming edges to check
     * @return {@code true} if there are incoming edges, {@code false} otherwise
     */
    private boolean hasIncomingEdge(ClassMember field) {
        return graph.getReverseNeighborsSize(field) > 0;
    }

    /**
     * Returns the set of methods writing to the given field.
     *
     * @param field the field written to
     * @return the set of methods writing to the field.
     */
    public Set<MethodEntry> getWritingMethods(FieldEntry field) {
        return graph.getReverseNeighbors(field).stream()
                .map(m -> (MethodEntry) m)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the set of methods reading from the given field.
     *
     * @param field the field read from
     * @return the set of methods reading from the field.
     */
    public Set<MethodEntry> getReadingMethods(FieldEntry field) {
        return graph.getNeighbors(field).stream()
                .map(m -> (MethodEntry) m)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the set of fields the given method writes.
     *
     * @param method the method for which to determine the fields it writes to
     * @return the set of fields written by the given method
     */
    public Set<FieldEntry> getWrittenFields(MethodEntry method) {
        return graph.getNeighbors(method).stream()
                .map(m -> (FieldEntry) m)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the set of fields the given method reads.
     *
     * @param method the method for which to determine the fields it reads from
     * @return the set of fields read by the given method
     */
    public Set<FieldEntry> getReadFields(MethodEntry method) {
        return graph.getReverseNeighbors(method).stream()
                .map(m -> (FieldEntry) m)
                .collect(Collectors.toSet());
    }

    public Set<MethodEntry> getStateModifiers(GenericClass clazz) {
        final String className = clazz.getClassName();
        final Stream<ClassMember> fieldsOfClazz = graph.getVertexSet().stream()
                .filter(m -> m instanceof FieldEntry
                        && ((FieldEntry) m).getClassName().equals(className));
        return fieldsOfClazz.flatMap(f -> graph.getReverseNeighbors(f).stream())
                .map(m -> (MethodEntry) m)
                .collect(Collectors.toSet());
    }

    /**
     * Computes all combinations of possible true dependencies (i.e. write-read dependencies)
     * between the methods contained in this graph.
     *
     * @return the set of true dependencies
     */
    public TrueDataDependenceGraph computeWriteReadPairs() {
        TrueDataDependenceGraph result = new TrueDataDependenceGraph();

        // The set of field vertices with at least one incoming and one outgoing edge.
        final Set<ClassMember> fields = graph.getVertexSet().stream()
                .filter(f -> f.isField() && hasOutgoingEdge(f) && hasIncomingEdge(f))
                .collect(Collectors.toSet());

        for (ClassMember field : fields) {
            // The set of methods writing to the current field.
            final Set<MethodEntry> writingMethods = new HashSet<>();
            graph.getReverseNeighbors(field).forEach(w -> writingMethods.add(((MethodEntry) w)));

            // The set of methods reading from the current field.
            final Set<MethodEntry> readingMethods = new HashSet<>();
            graph.getNeighbors(field).forEach(r -> readingMethods.add((MethodEntry) r));

            // Adds the write-read pairs to the result graph.
            for (MethodEntry writing : writingMethods) {
                for (MethodEntry reading : readingMethods) {
                    result.addTrueDependency(writing, reading);
                }
            }
        }

        return result;
    }
}
