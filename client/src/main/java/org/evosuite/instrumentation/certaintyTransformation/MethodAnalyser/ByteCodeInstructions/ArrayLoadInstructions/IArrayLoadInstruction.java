package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ArrayLoadInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.IALOAD;

public class IArrayLoadInstruction extends ArrayLoadInstruction {


    public IArrayLoadInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                                 int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor,"IALOAD", instructionNumber, StackTypeSet.INT, IALOAD);
    }
}
