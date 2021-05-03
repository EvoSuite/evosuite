package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.int_binary_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.AxAtoA_BinaryInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

public class IntBinaryInstruction extends AxAtoA_BinaryInstruction {

    public IntBinaryInstruction(INT_BINARY_INSTRUCTION instruction, String className, String methodName,
                                int lineNUmber, String methodDescriptor,int instructionNumber, int opcode) {
        super(className, methodName, instruction.toString(), lineNUmber, methodDescriptor,instructionNumber, StackTypeSet.TWO_COMPLEMENT, opcode);
    }

    protected enum INT_BINARY_INSTRUCTION {
        IADD, ISUB, IMUL, IDIV, IREM, IAND, IOR, IXOR
    }
}
