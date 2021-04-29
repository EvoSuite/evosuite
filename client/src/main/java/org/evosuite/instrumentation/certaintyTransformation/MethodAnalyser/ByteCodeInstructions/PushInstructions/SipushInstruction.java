package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import java.util.Collections;
import java.util.Set;

import static org.objectweb.asm.Opcodes.SIPUSH;

public class SipushInstruction extends PushInstruction {
    private final short value;

    public SipushInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber, short value) {
        super(className, methodName, lineNumber, methodDescriptor, "SIPUSH " + value, instructionNumber, StackTypeSet.TWO_COMPLEMENT, SIPUSH);
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
