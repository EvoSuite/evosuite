package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.GotoInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.JumpInstruction;
import org.objectweb.asm.Opcodes;

public class GotoInstructionPlaceholder extends UnconditionalJumpPlaceholder {
    public GotoInstructionPlaceholder(String className, String methodName, int lineNUmber, String methodDescriptor,int instructionNumber) {
        super(JumpInstruction.JUMP_TYPE.GOTO, className, methodName, lineNUmber,methodDescriptor, instructionNumber, Opcodes.GOTO);
    }

    @Override
    public GotoInstruction setDestination(ByteCodeInstruction instruction) {
        return new GotoInstruction(className,methodName,lineNumber,methodDescriptor,instructionNumber,instruction);
    }
}
