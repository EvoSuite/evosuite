package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.unary_instructions.float_unary_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.unary_instructions.AtoA_UnaryInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

public class FloatUnaryInstruction extends AtoA_UnaryInstruction {
    public FloatUnaryInstruction(FLOAT_UNARY_INSTRUCTION instruction, String className, String methodName,
                                 int lineNumber,String methodDescriptor, int instructionNumber, int opcode) {
        super(className, methodName, lineNumber, methodDescriptor,instruction.toString(), instructionNumber, StackTypeSet.FLOAT, opcode);
    }

    protected enum FLOAT_UNARY_INSTRUCTION {
        FNEG
    }
}
