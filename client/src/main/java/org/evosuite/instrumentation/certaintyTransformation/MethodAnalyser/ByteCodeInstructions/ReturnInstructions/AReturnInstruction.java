package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ReturnInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.ARETURN;

public class AReturnInstruction extends ReturnInstruction {

    public AReturnInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(StackTypeSet.AO, className, methodName, "ARETURN", lineNumber, methodDescriptor, instructionNumber,
                ARETURN);
    }

}