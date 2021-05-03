package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders.UnaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.UnaryJumpInstructions.IfNullInstruction;
import org.objectweb.asm.Opcodes;

public class IfNullInstructionPlaceholder extends UnaryJumpInstructionPlaceholder {

    public IfNullInstructionPlaceholder(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(IfNullInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber,
                IfNullInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IFNULL);
    }

    @Override
    public IfNullInstruction setDestination(ByteCodeInstruction instruction) {
        return new IfNullInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
