package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions.LoadInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.FLOAD;

public class FLoadInstruction extends LoadInstruction {
    public FLoadInstruction(String className, String methodName, int line, String methodDescriptor,int localVariableIndex,
                            int instructionNumber, boolean methodIsStatic) {
        super(className, methodName, line,methodDescriptor, "FLOAD", localVariableIndex, instructionNumber,
                StackTypeSet.FLOAT, FLOAD, methodIsStatic);
    }
}
