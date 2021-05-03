package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.UnaryJumpInstructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.ConditionalJumpInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import java.util.Collections;
import java.util.List;

public abstract class UnaryJumpInstruction extends ConditionalJumpInstruction {
    private final StackTypeSet consumedType;

    public UnaryJumpInstruction(JUMP_TYPE jumpType, String className, String methodName, int lineNUmber,String methodDescriptor,
                                int instructionNumber, ByteCodeInstruction destination, StackTypeSet consumedType,
                                int opcode) {
        super(jumpType, className, methodName, lineNUmber,methodDescriptor, instructionNumber, destination, opcode);
        this.consumedType = StackTypeSet.copy(consumedType);
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.singletonList(consumedType);
    }
}
