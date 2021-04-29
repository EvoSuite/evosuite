package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ArrayLoadInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.DALOAD;

public class DArrayLoadInstruction extends ArrayLoadInstruction {
    public DArrayLoadInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                                 int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor, "DALOAD", instructionNumber, StackTypeSet.DOUBLE,
                DALOAD);
    }
}