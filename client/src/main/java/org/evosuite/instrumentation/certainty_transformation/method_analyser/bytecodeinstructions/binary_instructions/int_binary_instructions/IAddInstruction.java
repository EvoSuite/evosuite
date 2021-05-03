package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.int_binary_instructions;

import org.objectweb.asm.Opcodes;

public class IAddInstruction extends IntBinaryInstruction {
    public IAddInstruction(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(INT_BINARY_INSTRUCTION.IADD, className, methodName, lineNUmber,methodDescriptor, instructionNumber, Opcodes.IADD);
    }
}
