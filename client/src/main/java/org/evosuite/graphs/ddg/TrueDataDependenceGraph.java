package org.evosuite.graphs.ddg;

import org.evosuite.setup.callgraph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * This graph stores true data dependencies (i.e., write-read or read-after-write dependencies)
 * between methods. The nodes of the graph represent methods. The edges are directed. An edge
 * from a method {@code foo()} to a method {@code bar()} symbolizes a write-read dependency
 * between those two methods. In other words, {@code foo()} writes to a field that {@code bar()}
 * reads from.
 */
public class TrueDataDependenceGraph {
    private static final Logger logger = LoggerFactory.getLogger(TrueDataDependenceGraph.class);

    private final Graph<MethodEntry> graph = new Graph<MethodEntry>() { };

    /**
     * Adds a new true dependency between the given methods to the graph.
     *
     * @param writing the method writing the field
     * @param reading the method reading the field
     */
    public void addTrueDependency(MethodEntry writing, MethodEntry reading) {
        if (logger.isDebugEnabled()) {
            logger.debug("adding true dependency: " + writing + " -> " + reading);
        }

        graph.addEdge(writing, reading);
    }

    /**
     * Returns the set of writing methods that have a true dependency to the given reading method.
     *
     * @param reading the method for which to return the true dependencies
     * @return the true dependencies to the given method
     */
    public Set<MethodEntry> getWritingMethodsFor(MethodEntry reading) {
        if (!graph.containsVertex(reading)) {
            if (logger.isWarnEnabled()) {
                logger.warn("Method " + reading + " not registered during dependence analysis");
            }

            return Collections.emptySet();
        } else {
            return graph.getReverseNeighbors(reading);
        }
    }

    public Set<MethodEntry> getReadingMethodsFor(MethodEntry writing) {
        if (!graph.containsVertex(writing)) {
            if (logger.isWarnEnabled()) {
                logger.warn("Method " + writing + " not registered during dependence analysis");
            }

            return Collections.emptySet();
        } else {
            return graph.getNeighbors(writing);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<MethodEntry, Set<MethodEntry>> edges : graph.getEdges().entrySet()) {
            final MethodEntry writing = edges.getKey();
            final Set<MethodEntry> readings = edges.getValue();
            for (MethodEntry reading : readings) {
                sb.append(writing).append(" -> ").append(reading).append("\n");
            }
        }
        return sb.toString();
    }
}
