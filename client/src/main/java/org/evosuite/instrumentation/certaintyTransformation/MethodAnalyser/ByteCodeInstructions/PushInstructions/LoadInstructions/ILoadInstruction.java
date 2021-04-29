package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions.LoadInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.ILOAD;

public class ILoadInstruction extends LoadInstruction {
    public ILoadInstruction(String className, String methodName, int line, String methodDescriptor,int localVariableIndex,
                            int instructionNumber, boolean methodIsStatic) {
        super(className, methodName, line,methodDescriptor, "ILOAD", localVariableIndex, instructionNumber,
                StackTypeSet.TWO_COMPLEMENT, ILOAD, methodIsStatic);
    }
}
