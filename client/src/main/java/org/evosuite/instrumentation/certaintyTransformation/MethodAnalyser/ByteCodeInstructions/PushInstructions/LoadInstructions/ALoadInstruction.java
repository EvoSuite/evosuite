package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.PushInstructions.LoadInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.ALOAD;

public class ALoadInstruction extends LoadInstruction {


    public ALoadInstruction(String className, String methodName, int line,String methodDescriptor, int localVariableIndex,
                            int instructionNumber, boolean methodIsStatic) {
        super(className, methodName, line,methodDescriptor, "ALOAD", localVariableIndex, instructionNumber, StackTypeSet.AO, ALOAD, methodIsStatic);
    }


    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.AO;
    }
}
