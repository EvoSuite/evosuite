package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.JumpInstruction;

public abstract class ConditionalJumpInstructionPlaceholder extends JumpInstructionPlaceholder {
    public ConditionalJumpInstructionPlaceholder(JumpInstruction.JUMP_TYPE jumpType, String className,
                                                 String methodName, int lineNUmber, String methodDescriptor, int instructionNumber, int opcode) {
        super(jumpType, className, methodName, lineNUmber, methodDescriptor, instructionNumber, opcode);
    }
}
