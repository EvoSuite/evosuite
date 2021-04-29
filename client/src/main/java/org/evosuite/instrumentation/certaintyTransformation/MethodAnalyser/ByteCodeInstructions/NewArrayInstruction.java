package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.NEWARRAY;

public class NewArrayInstruction extends ByteCodeInstruction {
    private final int type;

    public NewArrayInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber,
                               int opcodeType) {
        super(className, methodName, lineNumber, methodDescriptor, "NEWARRAY " + opcodeType, instructionNumber, NEWARRAY);
        this.type = opcodeType;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.singletonList(StackTypeSet.INT);
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.ARRAY;
    }

    @Override
    public boolean writesVariable(int index){
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
