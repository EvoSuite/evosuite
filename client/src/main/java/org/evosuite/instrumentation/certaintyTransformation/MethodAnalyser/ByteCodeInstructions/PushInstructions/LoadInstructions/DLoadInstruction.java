package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions.LoadInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.DLOAD;

public class DLoadInstruction extends LoadInstruction {
    public DLoadInstruction(String className, String methodName, int line,String methodDescriptor, int localVariableIndex,
                            int instructionNumber, boolean methodIsStatic) {
        super(className, methodName, line, methodDescriptor,"DLOAD", localVariableIndex, instructionNumber,
                StackTypeSet.DOUBLE, DLOAD, methodIsStatic);
    }
}
