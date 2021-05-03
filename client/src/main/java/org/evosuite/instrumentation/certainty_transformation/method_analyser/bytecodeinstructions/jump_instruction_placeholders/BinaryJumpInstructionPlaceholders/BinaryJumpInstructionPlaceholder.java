package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders.BinaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders.ConditionalJumpInstructionPlaceholder;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.JumpInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import java.util.Arrays;
import java.util.List;

public abstract class BinaryJumpInstructionPlaceholder extends ConditionalJumpInstructionPlaceholder {

    private final StackTypeSet consumedType;

    public BinaryJumpInstructionPlaceholder(JumpInstruction.JUMP_TYPE jumpType, String className, String methodName,
                                            int lineNUmber, String methodDescriptor, int instructionNumber, StackTypeSet consumedType, int opcode) {
        super(jumpType, className, methodName, lineNUmber,methodDescriptor, instructionNumber, opcode);
        this.consumedType = consumedType;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Arrays.asList(consumedType, consumedType);
    }
}
