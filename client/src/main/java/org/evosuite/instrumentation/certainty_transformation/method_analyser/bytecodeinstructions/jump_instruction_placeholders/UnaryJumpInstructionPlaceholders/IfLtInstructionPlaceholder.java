package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders.UnaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.UnaryJumpInstructions.IfLeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.UnaryJumpInstructions.IfLtInstruction;
import org.objectweb.asm.Opcodes;

public class IfLtInstructionPlaceholder extends UnaryJumpInstructionPlaceholder {

    public IfLtInstructionPlaceholder(String className, String methodName, int lineNUmber,String methodDescriptor,
                                      int instructionNumber) {
        super(IfLeInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber,
                IfLeInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IFLT);
    }

    @Override
    public IfLtInstruction setDestination(ByteCodeInstruction instruction) {
        return new IfLtInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
