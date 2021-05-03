package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders.UnaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.UnaryJumpInstructions.IfGeInstruction;
import org.objectweb.asm.Opcodes;

public class IfGeInstructionPlaceholder extends UnaryJumpInstructionPlaceholder {
    public IfGeInstructionPlaceholder(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(IfGeInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber,
                IfGeInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IFGE);
    }

    @Override
    public IfGeInstruction setDestination(ByteCodeInstruction instruction) {
        return new IfGeInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
