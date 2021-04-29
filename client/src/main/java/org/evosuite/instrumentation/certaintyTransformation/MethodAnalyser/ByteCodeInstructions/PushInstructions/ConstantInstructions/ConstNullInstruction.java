package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions.ConstantInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.ACONST_NULL;

public class ConstNullInstruction extends ConstantInstruction<Object> {
    public ConstNullInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                                int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "ACONST_NULL", instructionNumber, null, StackTypeSet.AO, ACONST_NULL);
    }
}
