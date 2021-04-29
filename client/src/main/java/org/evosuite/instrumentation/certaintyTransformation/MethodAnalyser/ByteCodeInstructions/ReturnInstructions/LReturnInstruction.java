package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ReturnInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.LRETURN;

public class LReturnInstruction extends ReturnInstruction {
    public LReturnInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(StackTypeSet.LONG, className, methodName, "LRETURN", lineNumber, methodDescriptor,instructionNumber, LRETURN);
    }
}
