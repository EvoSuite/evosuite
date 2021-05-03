package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.double_binary_Instructions;

import org.objectweb.asm.Opcodes;

public class DRemInstruction extends DoubleBinaryInstruction {
    public DRemInstruction(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(DOUBLE_BINARY_INSTRUCTION.DREM, className, methodName, lineNUmber,methodDescriptor, instructionNumber, Opcodes.DREM);
    }
}
