package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ReturnInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.IRETURN;

public class IReturnInstruction extends ReturnInstruction {
    @Deprecated
    public IReturnInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(StackTypeSet.TWO_COMPLEMENT, className, methodName, "IRETRUN", lineNumber, methodDescriptor,instructionNumber, IRETURN);
    }

    public IReturnInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber, StackTypeSet
                               returnTypes){
        super(returnTypes, className, methodName, "IRETURN", lineNumber,methodDescriptor, instructionNumber, IRETURN);
    }
}
