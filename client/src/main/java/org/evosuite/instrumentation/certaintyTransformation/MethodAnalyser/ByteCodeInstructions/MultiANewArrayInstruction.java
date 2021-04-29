package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MultiANewArrayInstruction extends ByteCodeInstruction {

    private final String desc;
    private final int dimensions;

    public MultiANewArrayInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                                     int instructionNumber, String desc, int dimensions) {
        super(className, methodName, lineNumber, methodDescriptor, String.format("MULTIANEWARRAY %s %s", desc, dimensions), instructionNumber,
                Opcodes.MULTIANEWARRAY);
        this.desc = desc;
        this.dimensions = dimensions;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        List<StackTypeSet> consumed = new ArrayList<>();
        for (int i = 0; i < dimensions; i++) {
            consumed.add(StackTypeSet.TWO_COMPLEMENT);
        }
        return consumed;
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.AO;
    }

    @Override
    public boolean writesVariable(int localVariableIndex) {
        return false;
    }
    @Override
    public Set<Integer> readsVariables(){
        return Collections.emptySet();
    }
    @Override
    public Set<Integer> writesVariables(){
        return Collections.emptySet();
    }
}
