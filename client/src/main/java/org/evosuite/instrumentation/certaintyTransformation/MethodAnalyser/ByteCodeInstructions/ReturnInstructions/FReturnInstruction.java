package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ReturnInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.FRETURN;

public class FReturnInstruction extends ReturnInstruction {
    public FReturnInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(StackTypeSet.FLOAT, className, methodName, "FRETURN", lineNumber,methodDescriptor, instructionNumber, FRETURN);
    }
}
