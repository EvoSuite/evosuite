package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.FloatBinaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxAtoA_BinaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

public class FloatBinaryInstruction extends AxAtoA_BinaryInstruction {
    private final FLOAT_BINARY_INSTRUCTION instruction;

    public FloatBinaryInstruction(FLOAT_BINARY_INSTRUCTION instruction, String className, String methodName,
                                  int lineNUmber,String methodDescriptor, int instructionNumber, int opcode) {
        super(className, methodName, instruction.toString(), lineNUmber,methodDescriptor, instructionNumber, StackTypeSet.FLOAT,
                opcode);
        this.instruction = instruction;
    }

    protected enum FLOAT_BINARY_INSTRUCTION {
        FADD, FSUB, FMUL, FDIV, FREM
    }
}
