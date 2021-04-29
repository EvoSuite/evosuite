package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.IntBinaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxAtoA_BinaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

public class IntBinaryInstruction extends AxAtoA_BinaryInstruction {

    public IntBinaryInstruction(INT_BINARY_INSTRUCTION instruction, String className, String methodName,
                                int lineNUmber, String methodDescriptor,int instructionNumber, int opcode) {
        super(className, methodName, instruction.toString(), lineNUmber, methodDescriptor,instructionNumber, StackTypeSet.TWO_COMPLEMENT, opcode);
    }

    protected enum INT_BINARY_INSTRUCTION {
        IADD, ISUB, IMUL, IDIV, IREM, IAND, IOR, IXOR
    }
}
