package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders.UnaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders.ConditionalJumpInstructionPlaceholder;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.JumpInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import java.util.Collections;
import java.util.List;

public abstract class UnaryJumpInstructionPlaceholder extends ConditionalJumpInstructionPlaceholder {

    private final StackTypeSet consumedType;

    public UnaryJumpInstructionPlaceholder(JumpInstruction.JUMP_TYPE jumpType,
                                           String className,
                                           String methodName,
                                           int lineNUmber, String methodDescriptor,
                                           int instructionNumber,
                                           StackTypeSet consumedType, int opcode) {
        super(jumpType, className, methodName, lineNUmber,methodDescriptor, instructionNumber, opcode);
        this.consumedType = consumedType;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.singletonList(consumedType);
    }
}
