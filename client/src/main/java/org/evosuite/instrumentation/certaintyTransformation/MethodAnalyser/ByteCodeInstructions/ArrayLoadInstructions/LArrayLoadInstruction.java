package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ArrayLoadInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.Opcodes;

public class LArrayLoadInstruction extends ArrayLoadInstruction {

    public LArrayLoadInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                                 int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor, "LALOAD", instructionNumber, StackTypeSet.LONG, Opcodes.LALOAD);
    }
}
