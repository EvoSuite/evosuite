package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders.UnaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.UnaryJumpInstructions.IfNonNullInstruction;
import org.objectweb.asm.Opcodes;

public class IfNonNullInstructionPlaceholder extends UnaryJumpInstructionPlaceholder {

    public IfNonNullInstructionPlaceholder(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(IfNonNullInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber,
                IfNonNullInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IFNONNULL);
    }

    @Override
    public IfNonNullInstruction setDestination(ByteCodeInstruction instruction) {
        return new IfNonNullInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
