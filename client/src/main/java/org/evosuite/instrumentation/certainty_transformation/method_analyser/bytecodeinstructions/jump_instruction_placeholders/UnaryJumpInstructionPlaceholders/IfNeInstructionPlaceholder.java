package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders.UnaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.UnaryJumpInstructions.IfNeInstruction;
import org.objectweb.asm.Opcodes;

public class IfNeInstructionPlaceholder extends UnaryJumpInstructionPlaceholder {
    public IfNeInstructionPlaceholder(String className, String methodName, int lineNUmber,String descriptor,
                                      int instructionNumber) {
        super(IfNeInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,descriptor,
                instructionNumber,
                IfNeInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IFNE);
    }

    @Override
    public IfNeInstruction setDestination(ByteCodeInstruction instruction) {
        return new IfNeInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
