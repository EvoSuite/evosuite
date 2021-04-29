package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.IntUnaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.AtoA_UnaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

public class IntUnaryInstruction extends AtoA_UnaryInstruction {
    private final INT_UNARY_INSTRUCTION instruction;

    public IntUnaryInstruction(INT_UNARY_INSTRUCTION instruction, String className, String methodName, int lineNumber
            ,String methodDescriptor, int instructionNumber, int opcode) {
        super(className, methodName, lineNumber, methodDescriptor, instruction.toString(), instructionNumber, StackTypeSet.INT, opcode);
        this.instruction = instruction;
    }

    protected enum INT_UNARY_INSTRUCTION {
        INEG
    }
}
