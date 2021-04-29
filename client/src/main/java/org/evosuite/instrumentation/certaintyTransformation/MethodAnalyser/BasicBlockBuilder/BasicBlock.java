package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.BasicBlockBuilder;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.MethodEnter;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph.ControlFlowGraph;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph.NodeContent;

import java.util.*;
import java.util.stream.Collectors;

public class BasicBlock implements NodeContent {

    private final ControlFlowGraph graph;
    private final List<ByteCodeInstruction> instructions;

    public BasicBlock(ByteCodeInstruction start, ControlFlowGraph graph) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(graph);
        if (!graph.getContents().contains(start))
            throw new IllegalArgumentException("Passed instruction is not in passed CFG");
        if (!isStartOfSuperBlock(start, graph))
            throw new IllegalArgumentException("Passed instruction is not start of basic block");
        this.graph = graph;
        isStartOfSuperBlock(start, graph);
        List<ByteCodeInstruction> mutableInstructions = new ArrayList<>();
        ByteCodeInstruction next = start;
        while (!endOfSuperBlock(next, start)) {
            mutableInstructions.add(next);
            Collection<ByteCodeInstruction> successors = graph.getSuccessors(next);
            if (successors.size() == 1) {
                next = successors.iterator().next();
            } else {
                break;
            }
        }
        instructions = Collections.unmodifiableList(mutableInstructions);
    }

    boolean endOfSuperBlock(ByteCodeInstruction next, ByteCodeInstruction start) {
        return graph.getPredecessors(next).size() > 1 && next != start;
    }

    public List<ByteCodeInstruction> getInstructions() {
        return instructions;
    }

    public ByteCodeInstruction getLastInstruction() {
        return instructions.get(instructions.size()-1);
    }

    static boolean isStartOfSuperBlock(ByteCodeInstruction start, ControlFlowGraph graph) {
        if (start instanceof MethodEnter) return true;
        Collection<ByteCodeInstruction> predecessors = graph.getPredecessors(start);
        if (predecessors.size() > 1 || graph.getSuccessors(predecessors.iterator().next()).size() > 1) return true;
        return false;
    }

    public ControlFlowGraph getGraph() {
        return graph;
    }

    @Override
    public String toString() {
        return "BasicBlock{" + "instructions=" + instructions + '}';
    }

    @Override
    public String getLabel() {
        return instructions.stream().map(ByteCodeInstruction::getLabel).collect(Collectors.joining(","));
    }

    @Override
    public int getOrder() {
        return instructions.get(0).getOrder();
    }
}
