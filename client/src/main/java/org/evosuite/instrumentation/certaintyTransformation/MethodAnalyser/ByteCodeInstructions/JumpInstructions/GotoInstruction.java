package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.objectweb.asm.Opcodes;

public class GotoInstruction extends UnconditionalJumpInstruction {

    public final static JUMP_TYPE JUMP_TYPE = JumpInstruction.JUMP_TYPE.GOTO;

    public GotoInstruction(String className, String methodName, int lineNUmber,String methodDescriptor,
                           int instructionNumber, ByteCodeInstruction destination) {
        super(JUMP_TYPE, className, methodName, lineNUmber,methodDescriptor, instructionNumber, destination, Opcodes.GOTO);
    }
}
