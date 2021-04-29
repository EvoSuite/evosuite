package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.LongUnaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.AtoA_UnaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

public class LongUnaryInstruction extends AtoA_UnaryInstruction {
    public LongUnaryInstruction(LONG_UNARY_INSTRUCTION instruction, String className, String methodName,
                                int lineNumber,String methodDescriptor, int instructionNumber, int opcode) {
        super(className, methodName, lineNumber,methodDescriptor, instruction.toString(), instructionNumber, StackTypeSet.LONG, opcode);
    }

    protected enum LONG_UNARY_INSTRUCTION {
        LNEG
    }
}
