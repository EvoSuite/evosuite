package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders.BinaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.BinaryJumpInstructions.IntCmpGeInstruction;
import org.objectweb.asm.Opcodes;

public class IntCmpGeInstructionPlaceholder extends BinaryJumpInstructionPlaceholder {

    public IntCmpGeInstructionPlaceholder(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(IntCmpGeInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber,
                IntCmpGeInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IF_ICMPGE);
    }

    @Override
    public IntCmpGeInstruction setDestination(ByteCodeInstruction instruction) {
        return new IntCmpGeInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
