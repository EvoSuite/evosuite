package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.SwitchInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.objectweb.asm.Opcodes.TABLESWITCH;

public class TableSwitchInstructionPlaceholder extends SwitchInstructionPlaceholder {

    private final int min;
    private final int max;

    public TableSwitchInstructionPlaceholder(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber, int min
            , int max) {
        super(className, methodName, lineNumber,methodDescriptor, "TABLESWITCH", instructionNumber, TABLESWITCH);
        this.min = min;
        this.max = max;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.singletonList(StackTypeSet.INT);
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.VOID;
    }

    @Override
    public Collection<Integer> getSuccessors() {
        throw new UnsupportedOperationException("getSuccessors not supported on Placeholder");
    }

    @Override
    public TableSwitchInstruction setDestinations(ByteCodeInstruction def, List<ByteCodeInstruction> destinations) {
        return new TableSwitchInstruction(className,methodName,lineNumber,methodDescriptor,instructionNumber,min,max,def,destinations);
    }
}
