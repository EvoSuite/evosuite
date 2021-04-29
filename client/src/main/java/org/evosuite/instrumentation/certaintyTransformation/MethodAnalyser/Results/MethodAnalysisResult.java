package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph.ControlFlowGraph;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Variables.VariableTable;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.InstructionInputOutputFrames;

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
