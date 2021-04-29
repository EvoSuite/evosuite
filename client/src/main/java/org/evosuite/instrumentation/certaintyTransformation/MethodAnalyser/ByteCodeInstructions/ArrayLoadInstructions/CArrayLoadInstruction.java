package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ArrayLoadInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.CALOAD;

public class CArrayLoadInstruction extends ArrayLoadInstruction {
    public CArrayLoadInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor,"CALOAD", instructionNumber, StackTypeSet.CHAR, CALOAD);
    }
}
