package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ReturnInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.DRETURN;

public class DReturnInstruction extends ReturnInstruction {
    public DReturnInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(StackTypeSet.DOUBLE, className, methodName, "DRETURN", lineNumber,methodDescriptor, instructionNumber, DRETURN);
    }
}
