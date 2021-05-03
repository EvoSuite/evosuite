package org.evosuite.instrumentation.certainty_transformation.method_analyser.results;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.graph.ControlFlowGraph;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.variables.VariableTable;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.InstructionInputOutputFrames;

import java.util.HashMap;
import java.util.Map;

public class MethodAnalysisResult {
    private final ControlFlowGraph controlFlowGraph;
    private final VariableTable variableTable;
    private final Map<ByteCodeInstruction, InstructionInputOutputFrames> framesMap;
    private final boolean hasJumps;

    public MethodAnalysisResult(ControlFlowGraph graph, VariableTable table, Map<ByteCodeInstruction,
            InstructionInputOutputFrames> framesMap, boolean hasJumps){
        this.controlFlowGraph = graph;
        this.variableTable = table;
        if(framesMap != null)
            this.framesMap = new HashMap<>(framesMap);
        else
            this.framesMap = new HashMap<>();
        this.hasJumps = hasJumps;
    }

    public ControlFlowGraph getControlFlowGraph() {
        return controlFlowGraph;
    }

    public VariableTable getVariableTable() {
        return variableTable;
    }

    public Map<ByteCodeInstruction, InstructionInputOutputFrames> getFramesMap() {
        return new HashMap<>(framesMap);
    }

    public boolean isHasJumps() {
        return hasJumps;
    }
}
