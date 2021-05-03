package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.float_binary_instructions;

import org.objectweb.asm.Opcodes;

public class FRemInstruction extends FloatBinaryInstruction {
    public FRemInstruction(String className, String methodName, int lineNUmber,String methodDescriptor,
                           int instructionNumber) {
        super(FLOAT_BINARY_INSTRUCTION.FREM, className, methodName, lineNUmber,methodDescriptor, instructionNumber, Opcodes.FREM);
    }
}
