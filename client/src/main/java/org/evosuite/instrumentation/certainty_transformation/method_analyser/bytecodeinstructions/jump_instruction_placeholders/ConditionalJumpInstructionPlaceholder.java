package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.JumpInstruction;

public abstract class ConditionalJumpInstructionPlaceholder extends JumpInstructionPlaceholder {
    public ConditionalJumpInstructionPlaceholder(JumpInstruction.JUMP_TYPE jumpType, String className,
                                                 String methodName, int lineNUmber, String methodDescriptor, int instructionNumber, int opcode) {
        super(jumpType, className, methodName, lineNUmber, methodDescriptor, instructionNumber, opcode);
    }
}
