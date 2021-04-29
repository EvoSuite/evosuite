package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph.GraphPrinter;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph.DirectedGraph;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph.Edge;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph.Node;

import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractGraphPrinter {

    protected DirectedGraph<?> graph;

    public AbstractGraphPrinter(DirectedGraph<?> graph){
        this.graph = graph;
    }

    protected String getEdgeString(Function<Edge<?>, String> edgeStringFunction) {
        return getEdgeString(edgeStringFunction, "\n");
    }

    protected String getEdgeString(Function<Edge<?>, String> edgeStringFunction, String join){
        return graph.getEdges()
                .stream()
                .map(edgeStringFunction)
                .collect(Collectors.joining(join));
    }

    protected String getNodeListWithLabels(Function<Node<?>, String> nodeStringFunction) {
        return getNodeListWithLabels(nodeStringFunction, "\n");
    }

    protected String getNodeListWithLabels(Function< Node<?>, String> nodeStringFunction, String join){
        return graph.getNodes().stream().map(nodeStringFunction).collect(Collectors.joining(join));
    }

    @Override
    public abstract String toString();
}
