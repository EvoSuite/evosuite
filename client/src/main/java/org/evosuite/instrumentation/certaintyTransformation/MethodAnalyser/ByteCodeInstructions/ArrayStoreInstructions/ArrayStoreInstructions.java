package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ArrayStoreInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class ArrayStoreInstructions extends ByteCodeInstruction {
    private final StackTypeSet type;

    public ArrayStoreInstructions(String className, String methodName, int lineNumber,String methodDescriptor, String label,
                                  int instructionNumber, Integer type, int opcode) {
        super(className, methodName, lineNumber, methodDescriptor, label, instructionNumber, opcode);
        this.type = StackTypeSet.of(type);
    }

    public ArrayStoreInstructions(String className, String methodName, int lineNumber,String methodDescriptor, String label,
                                  int instructionNumber, StackTypeSet type, int opcode) {
        super(className, methodName, lineNumber, methodDescriptor, label, instructionNumber, opcode);
        this.type = type;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Arrays.asList(StackTypeSet.of(Type.ARRAY),
                StackTypeSet.INT,
                type);
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.VOID;
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
