package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.LongBinaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxAtoA_BinaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

public class LongBinaryInstruction extends AxAtoA_BinaryInstruction {
    private final LONG_BINARY_INSTRUCTION instruction;

    public LongBinaryInstruction(LONG_BINARY_INSTRUCTION instruction, String className, String methodName,
                                 int lineNumber,String methodDescriptor, int instructionNumber, int opcode) {
        super(className, methodName, instruction.toString(), lineNumber,methodDescriptor, instructionNumber, StackTypeSet.LONG, opcode);
        this.instruction = instruction;
    }

    protected enum LONG_BINARY_INSTRUCTION {
        LADD, LSUB, LMUL, LDIV, LREM, LAND, LOR, LXOR
    }
}
