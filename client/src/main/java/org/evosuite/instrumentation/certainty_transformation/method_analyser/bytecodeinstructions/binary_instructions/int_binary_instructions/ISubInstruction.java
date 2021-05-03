package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.int_binary_instructions;

import org.objectweb.asm.Opcodes;

public class ISubInstruction extends IntBinaryInstruction {
    public ISubInstruction(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(INT_BINARY_INSTRUCTION.ISUB, className, methodName, lineNUmber,methodDescriptor, instructionNumber, Opcodes.ISUB);
    }
}
