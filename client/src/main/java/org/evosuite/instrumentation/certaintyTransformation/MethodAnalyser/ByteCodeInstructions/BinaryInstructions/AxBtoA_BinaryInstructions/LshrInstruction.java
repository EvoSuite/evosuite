package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxBtoA_BinaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxBtoA_BinaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.LSHR;

public class LshrInstruction extends AxBtoA_BinaryInstruction {
    public LshrInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className,
                methodName,
                lineNumber, methodDescriptor,
                "LSHR",
                instructionNumber,
                StackTypeSet.LONG,
                StackTypeSet.INT,
                LSHR);
    }
}
