package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.double_binary_Instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.AxAtoA_BinaryInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

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
