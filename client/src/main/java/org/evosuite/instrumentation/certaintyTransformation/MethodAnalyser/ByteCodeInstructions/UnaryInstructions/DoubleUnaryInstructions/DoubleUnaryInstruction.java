package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.DoubleUnaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.AtoA_UnaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

public class DoubleUnaryInstruction extends AtoA_UnaryInstruction {
    public DoubleUnaryInstruction(DOUBLE_UNARY_INSTRUCTION instruction, String className, String methodName,
                                  int lineNumber,String methodDescriptor, int instructionNumber, int opcode) {
        super(className, methodName, lineNumber,methodDescriptor, instruction.toString(), instructionNumber, StackTypeSet.DOUBLE, opcode);
    }

    protected enum DOUBLE_UNARY_INSTRUCTION {
        DNEG
    }
}
