package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.int_binary_instructions;

import org.objectweb.asm.Opcodes;

public class IAndInstruction extends IntBinaryInstruction {

    public IAndInstruction(String className, String methodName, int lineNUmber, String methodDescriptor,int instructionNumber) {
        super(INT_BINARY_INSTRUCTION.IAND, className, methodName, lineNUmber,methodDescriptor, instructionNumber, Opcodes.IAND);
    }
}
