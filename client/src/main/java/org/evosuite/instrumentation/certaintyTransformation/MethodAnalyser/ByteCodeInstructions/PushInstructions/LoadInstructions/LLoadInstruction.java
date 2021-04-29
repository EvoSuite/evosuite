package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions.LoadInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.LLOAD;

public class LLoadInstruction extends LoadInstruction {
    public LLoadInstruction(String className, String methodName, int line,String methodDescriptor, int localVariableIndex,
                            int instructionNumber, boolean methodIsStatic) {
        super(className, methodName, line,methodDescriptor, "LLOAD", localVariableIndex, instructionNumber,
                StackTypeSet.LONG, LLOAD, methodIsStatic);
    }
}
