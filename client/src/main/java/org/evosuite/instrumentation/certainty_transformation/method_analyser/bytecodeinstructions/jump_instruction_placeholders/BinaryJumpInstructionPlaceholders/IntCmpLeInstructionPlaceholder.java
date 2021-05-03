package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders.BinaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.BinaryJumpInstructions.IntCmpLeInstruction;
import org.objectweb.asm.Opcodes;

public class IntCmpLeInstructionPlaceholder extends BinaryJumpInstructionPlaceholder {

    public IntCmpLeInstructionPlaceholder(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(IntCmpLeInstruction.JUMP_TYPE, className, methodName, lineNUmber,methodDescriptor, instructionNumber,
                IntCmpLeInstruction.CONSUMED_FROM_STACK_TYPE,
                Opcodes.IF_ICMPLE);
    }

    @Override
    public IntCmpLeInstruction setDestination(ByteCodeInstruction instruction) {
        return new IntCmpLeInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
