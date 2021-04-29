package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.MethodEnter;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.MethodExit;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class InstructionGraph extends DirectedGraph<ByteCodeInstruction> {
    protected final ByteCodeInstruction entryNode;
    protected final ByteCodeInstruction exitNode;

    public InstructionGraph(Collection<Node<ByteCodeInstruction>> nodes, Collection<Edge<ByteCodeInstruction>> edges) {
        super(nodes, edges);
        entryNode = getFilteredNodeSet(i -> i instanceof MethodEnter).iterator().next();
        exitNode = getFilteredNodeSet(i -> i instanceof MethodExit).iterator().next();
    }

    public Node<ByteCodeInstruction> getEntry() {
        return getNodes().stream().filter(n -> n.getContent() instanceof MethodEnter).findFirst().orElseGet(() -> {
            throw new IllegalStateException("CFG has no Entry!");
        });
    }

    public Node<ByteCodeInstruction> getExit() {
        return getNodes().stream().filter(n -> n.getContent() instanceof MethodExit).findFirst().orElseGet(() -> {
            throw new IllegalStateException("CFG has no Entry!");
        });
    }
    public Collection<ByteCodeInstruction> getInstructionByOpcode(int opcode) {
        return getFilteredNodeSet(c -> c.getOpcode() == opcode);
    }

    public Collection<ByteCodeInstruction> getFilteredNodeSet(Predicate<ByteCodeInstruction> predicate) {
        return getContents().stream().filter(predicate).collect(Collectors.toList());
    }

    public List<ByteCodeInstruction> getSortedNodeSet(Comparator<ByteCodeInstruction> comparator) {
        return getSortedNodeSet(comparator, false);
    }

    public List<ByteCodeInstruction> getSortedNodeSet(Comparator<ByteCodeInstruction> comparator, boolean reversed) {
        Comparator<Node<ByteCodeInstruction>> c = (n1, n2) -> comparator.compare(n1.getContent(),
                n2.getContent()) * (reversed ? -1 : 1);
        return getNodes().stream().sorted(c).map(Node::getContent).collect(Collectors.toList());
    }
}
