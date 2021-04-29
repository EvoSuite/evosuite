package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.DoubleBinaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.AxAtoA_BinaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

public class DoubleBinaryInstruction extends AxAtoA_BinaryInstruction {
    private final DOUBLE_BINARY_INSTRUCTION instruction;

    public DoubleBinaryInstruction(DOUBLE_BINARY_INSTRUCTION instruction, String className, String methodName,
                                   int lineNUmber,String methodDescriptor, int instructionNumber, int opcode) {
        super(className, methodName, instruction.toString(), lineNUmber,methodDescriptor, instructionNumber, StackTypeSet.DOUBLE,
                opcode);
        this.instruction = instruction;
    }

    public enum DOUBLE_BINARY_INSTRUCTION {
        DADD, DSUB, DMUL, DDIV, DREM
    }
}
