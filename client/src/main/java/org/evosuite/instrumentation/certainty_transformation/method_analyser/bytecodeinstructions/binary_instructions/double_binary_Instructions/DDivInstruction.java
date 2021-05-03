package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.double_binary_Instructions;

import org.objectweb.asm.Opcodes;

public class DDivInstruction extends DoubleBinaryInstruction {
    public DDivInstruction(String className, String methodName, int lineNUmber,String methodDescriptor,
                           int instructionNumber) {
        super(DOUBLE_BINARY_INSTRUCTION.DDIV, className, methodName, lineNUmber,methodDescriptor, instructionNumber, Opcodes.DDIV);
    }
}
