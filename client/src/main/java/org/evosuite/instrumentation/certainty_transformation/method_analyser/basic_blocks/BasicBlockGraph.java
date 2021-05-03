package org.evosuite.instrumentation.certainty_transformation.method_analyser.basic_blocks;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.graph.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BasicBlockGraph extends DirectedGraph<BasicBlock> {

    private BasicBlockGraph(Collection<Node<BasicBlock>> nodes, Collection<Edge<BasicBlock>> edges) {
        super(nodes, edges);
    }

    public static BasicBlockGraph build(ControlFlowGraph graph){
        DirectedGraphBuilder<BasicBlock> builder = new MutableDirectedGraphBuilder<>();
        ByteCodeInstruction entry = graph.getEntry().getContent();
        BasicBlock entryBasicBlock = new BasicBlock(entry, graph);
        List<ByteCodeInstruction> ends = new ArrayList<>();
        List<ByteCodeInstruction> entryBlockInstructions = entryBasicBlock.getInstructions();
        ends.add(entryBlockInstructions.get(entryBlockInstructions.size()-1));
        builder = builder.addContent(entryBasicBlock);
        while(!ends.isEmpty()){
            ByteCodeInstruction remove = ends.remove(0);
            for (ByteCodeInstruction successor : graph.getSuccessors(remove)) {
                if(builder.getByOrder(successor.getOrder()) == null) {
                    BasicBlock basicBlock = new BasicBlock(successor, graph);
                    builder = builder.addContent(basicBlock);
                    List<ByteCodeInstruction> instructions = basicBlock.getInstructions();
                    ends.add(instructions.get(instructions.size()-1));
                }
            }
        }
        for (Node<BasicBlock> node : builder.getNodes()) {
            for (ByteCodeInstruction successor : graph.getSuccessors(node.getContent().getLastInstruction())) {
                Node<BasicBlock> byOrder = builder.getByOrder(successor.getOrder());
                builder = builder.addEdge(node,byOrder);
            }
        }
        return builder.build(BasicBlockGraph::new);
    }
}
