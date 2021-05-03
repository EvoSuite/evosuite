package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.objectweb.asm.Opcodes;

public class GotoInstruction extends UnconditionalJumpInstruction {

    public final static JUMP_TYPE JUMP_TYPE = JumpInstruction.JUMP_TYPE.GOTO;

    public GotoInstruction(String className, String methodName, int lineNUmber,String methodDescriptor,
                           int instructionNumber, ByteCodeInstruction destination) {
        super(JUMP_TYPE, className, methodName, lineNUmber,methodDescriptor, instructionNumber, destination, Opcodes.GOTO);
    }
}
