package org.evosuite.instrumentation.certainty_transformation.method_analyser.basic_blocks;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.FrameLayout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BasicBlockFrames {
    private final BasicBlock basicBlock;
    private final Map<BasicBlock, BasicBlockStackManipulation> manipulationMap;
    private final FrameLayout inputFrameLayout;
    private final Map<BasicBlock, FrameLayout> outputFrames;
    private boolean inputFrameChanged = false;
    private Set<BasicBlock> outputFrameChanged = new HashSet<>();

    public BasicBlockFrames(BasicBlock basicBlock,
                            Map<BasicBlock, BasicBlockStackManipulation> manipulationMap,
                            FrameLayout inputFrameLayout){
        this.basicBlock = basicBlock;
        this.manipulationMap = manipulationMap;
        this.inputFrameLayout = inputFrameLayout;
        this.outputFrames = new HashMap<>();
        for (Map.Entry<BasicBlock, BasicBlockStackManipulation> entry :
                manipulationMap.entrySet()) {
            outputFrames.put(entry.getKey(), entry.getValue().apply(inputFrameLayout));
        }
    }
}
