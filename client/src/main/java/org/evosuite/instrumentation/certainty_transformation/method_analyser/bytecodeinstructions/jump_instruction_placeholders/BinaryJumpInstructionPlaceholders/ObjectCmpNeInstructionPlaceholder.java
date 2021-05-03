package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders.BinaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.BinaryJumpInstructions.ObjectCmpNeInstruction;
import org.objectweb.asm.Opcodes;

public class ObjectCmpNeInstructionPlaceholder extends BinaryJumpInstructionPlaceholder {

    public ObjectCmpNeInstructionPlaceholder(String className, String methodName, int lineNUmber,
                                             String methodDescriptor, int instructionNumber) {
        super(ObjectCmpNeInstruction.JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber,
                ObjectCmpNeInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IF_ACMPNE);
    }

    @Override
    public ObjectCmpNeInstruction setDestination(ByteCodeInstruction instruction) {
        return new ObjectCmpNeInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
