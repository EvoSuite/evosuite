package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ArrayLoadInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.SALOAD;

public class SArrayLoadInstruction extends ArrayLoadInstruction {
    public SArrayLoadInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor,"SALOAD", instructionNumber, StackTypeSet.SHORT, SALOAD);
    }
}
