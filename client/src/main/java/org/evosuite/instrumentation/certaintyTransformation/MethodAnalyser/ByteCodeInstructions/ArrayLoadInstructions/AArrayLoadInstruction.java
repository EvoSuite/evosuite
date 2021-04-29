package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ArrayLoadInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.AALOAD;

public class AArrayLoadInstruction extends ArrayLoadInstruction {

    public AArrayLoadInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor, "AALOAD", instructionNumber, StackTypeSet.AO,
                AALOAD);
    }
}
