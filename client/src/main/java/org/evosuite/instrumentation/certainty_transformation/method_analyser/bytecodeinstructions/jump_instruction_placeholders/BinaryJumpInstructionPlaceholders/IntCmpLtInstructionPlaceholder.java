package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders.BinaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.BinaryJumpInstructions.IntCmpLtInstruction;
import org.objectweb.asm.Opcodes;

public class IntCmpLtInstructionPlaceholder extends BinaryJumpInstructionPlaceholder {

    public IntCmpLtInstructionPlaceholder(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(IntCmpLtInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber, methodDescriptor,
                instructionNumber,
                IntCmpLtInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IF_ICMPLT);
    }

    @Override
    public IntCmpLtInstruction setDestination(ByteCodeInstruction instruction) {
        return new IntCmpLtInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
