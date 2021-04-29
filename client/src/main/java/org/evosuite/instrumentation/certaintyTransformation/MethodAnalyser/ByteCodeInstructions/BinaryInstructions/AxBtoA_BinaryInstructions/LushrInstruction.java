package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxBtoA_BinaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxBtoA_BinaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.LUSHR;

public class LushrInstruction extends AxBtoA_BinaryInstruction {
    public LushrInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className,
                methodName,
                lineNumber,methodDescriptor,
                "LUSHR",
                instructionNumber,
                StackTypeSet.LONG,
                StackTypeSet.INT,
                LUSHR);
    }
}
