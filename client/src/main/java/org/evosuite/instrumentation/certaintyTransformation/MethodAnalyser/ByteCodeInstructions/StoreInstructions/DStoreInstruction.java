package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.StoreInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.DSTORE;

public class DStoreInstruction extends StoreInstruction {
    public DStoreInstruction(String className, String methodName, int line, String methodDescriptor,int localVariableIndex, int instructionNumber) {
        super(className, methodName, line, methodDescriptor,"DSTORE " + localVariableIndex, localVariableIndex, instructionNumber,
                StackTypeSet.DOUBLE, DSTORE);
    }
}
