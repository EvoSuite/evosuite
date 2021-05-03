package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.unary_instructions.double_unary_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.unary_instructions.AtoA_UnaryInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

public class DoubleUnaryInstruction extends AtoA_UnaryInstruction {
    public DoubleUnaryInstruction(DOUBLE_UNARY_INSTRUCTION instruction, String className, String methodName,
                                  int lineNumber,String methodDescriptor, int instructionNumber, int opcode) {
        super(className, methodName, lineNumber,methodDescriptor, instruction.toString(), instructionNumber, StackTypeSet.DOUBLE, opcode);
    }

    protected enum DOUBLE_UNARY_INSTRUCTION {
        DNEG
    }
}
