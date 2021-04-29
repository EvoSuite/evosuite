package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import java.util.Collections;
import java.util.Set;

import static org.objectweb.asm.Opcodes.BIPUSH;

public class BipushInstruction extends PushInstruction {
    private final byte value;

    public BipushInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber,
                             byte value) {
        super(className, methodName, lineNumber,methodDescriptor, "BIPUSH " + value, instructionNumber, StackTypeSet.TWO_COMPLEMENT, BIPUSH);
        this.value = value;
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
