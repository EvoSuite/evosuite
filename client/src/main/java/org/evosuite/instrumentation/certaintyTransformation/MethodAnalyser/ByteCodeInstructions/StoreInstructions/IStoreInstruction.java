package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.StoreInstructions;


import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.ISTORE;

public class IStoreInstruction extends StoreInstruction {
    public IStoreInstruction(String className, String methodName, int line,String methodDescriptor, int localVariableIndex,
                             int instructionNumber) {
        super(className, methodName, line,methodDescriptor, "ISTORE " + localVariableIndex, localVariableIndex, instructionNumber,
                StackTypeSet.TWO_COMPLEMENT, ISTORE);
    }
}
