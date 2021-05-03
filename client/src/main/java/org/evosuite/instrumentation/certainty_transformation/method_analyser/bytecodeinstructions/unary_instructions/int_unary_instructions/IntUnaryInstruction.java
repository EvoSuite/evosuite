package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.unary_instructions.int_unary_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.unary_instructions.AtoA_UnaryInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

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
