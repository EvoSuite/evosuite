package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxBtoA_BinaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxAtoA_BinaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.Opcodes;

public class IshlInstruction extends AxAtoA_BinaryInstruction {
    public IshlInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, "ISHL", lineNumber,methodDescriptor, instructionNumber, StackTypeSet.INT, Opcodes.ISHL);
    }
}
