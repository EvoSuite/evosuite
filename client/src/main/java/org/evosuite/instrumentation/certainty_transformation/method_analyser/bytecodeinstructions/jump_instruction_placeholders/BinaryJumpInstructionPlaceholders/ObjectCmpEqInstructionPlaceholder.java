package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders.BinaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.BinaryJumpInstructions.ObjectCmpEqInstruction;
import org.objectweb.asm.Opcodes;

public class ObjectCmpEqInstructionPlaceholder extends BinaryJumpInstructionPlaceholder {

    public ObjectCmpEqInstructionPlaceholder(String className, String methodName, int lineNUmber, String methodDescriptor,int instructionNumber) {
        super(ObjectCmpEqInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber,
                ObjectCmpEqInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IF_ACMPEQ);
    }

    @Override
    public ObjectCmpEqInstruction setDestination(ByteCodeInstruction instruction) {
        return new ObjectCmpEqInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
