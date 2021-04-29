package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions.ConstantInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;

public class FConstantInstruction extends ConstantInstruction<Float> {
    private final float value;

    public FConstantInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                                int instructionNumber, float value) {
        super(className, methodName, lineNumber, methodDescriptor,"FCONST " + value , instructionNumber, value, StackTypeSet.FLOAT,
                value2opcode(value));
        this.value = value;
    }

    private static int value2opcode(float value){
        if(Arrays.asList(0F,1F,2F).contains(value))
            return Opcodes.FCONST_0 + (int)value;
        throw new IllegalArgumentException("FCONST can only be used with value 0,1,2");
    }
}
