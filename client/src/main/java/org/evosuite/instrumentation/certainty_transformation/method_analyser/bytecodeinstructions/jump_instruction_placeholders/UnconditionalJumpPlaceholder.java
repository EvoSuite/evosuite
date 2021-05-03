package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.JumpInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import java.util.Collections;
import java.util.List;

public abstract class UnconditionalJumpPlaceholder extends JumpInstructionPlaceholder {
    public UnconditionalJumpPlaceholder(JumpInstruction.JUMP_TYPE jumpType, String className, String methodName,
                                        int lineNUmber, String methodDescriptor, int instructionNumber, int opcode) {
        super(jumpType, className, methodName, lineNUmber,methodDescriptor, instructionNumber, opcode);
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.emptyList();
    }
}
