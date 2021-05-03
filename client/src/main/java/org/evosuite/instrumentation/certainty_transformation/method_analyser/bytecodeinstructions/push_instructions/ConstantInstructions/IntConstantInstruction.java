package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.push_instructions.ConstantInstructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.ICONST_0;

public class IntConstantInstruction extends ConstantInstruction<Integer> {


    public IntConstantInstruction(String className, String methodName, int lineNUmber, String methodDescriptor,int constant, int instructionNumber) {
        super(className, methodName, lineNUmber,methodDescriptor, "ICONST " + constant, instructionNumber, constant, StackTypeSet.TWO_COMPLEMENT,
                value2Opcode(constant));
    }

    private static int value2Opcode(int value) {
        if (value >= -1 && value <= 5)
            return ICONST_0 + value;
        throw new IllegalArgumentException("ICONST can only be used with values in the range of -1 to 5 (inclusive)");
    }
}
