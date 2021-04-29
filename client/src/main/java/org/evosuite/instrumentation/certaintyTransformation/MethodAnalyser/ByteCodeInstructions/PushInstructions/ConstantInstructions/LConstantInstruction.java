package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions.ConstantInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.LCONST_0;

public class LConstantInstruction extends ConstantInstruction<Long> {
    public LConstantInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber,
                                long value) {
        super(className, methodName, lineNumber, methodDescriptor,"LCONST " + value, instructionNumber, value,
                StackTypeSet.LONG, value2Opcode(value));
    }

    private static int value2Opcode(long value){
        if(Arrays.asList(0L,1L).contains(value))
            return LCONST_0 + (int) value;
        throw new IllegalArgumentException("LCONST can only be used with 0 or 1L as value");
    }
}
