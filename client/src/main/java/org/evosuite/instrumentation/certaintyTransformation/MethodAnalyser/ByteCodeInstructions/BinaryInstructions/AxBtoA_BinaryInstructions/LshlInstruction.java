package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxBtoA_BinaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxBtoA_BinaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.LSHL;

public class LshlInstruction extends AxBtoA_BinaryInstruction {
    public LshlInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(className,
                methodName,
                lineNumber,methodDescriptor,
                "LSHL",
                instructionNumber,
                StackTypeSet.LONG,
                StackTypeSet.INT,
                LSHL);
    }
}
