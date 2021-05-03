package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instruction_placeholders.UnaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.UnaryJumpInstructions.IfEqInstruction;
import org.objectweb.asm.Opcodes;

public class IfEqInstructionPlaceholder extends UnaryJumpInstructionPlaceholder {

    public IfEqInstructionPlaceholder(String className, String methodName, int lineNUmber,String methodDescriptor,
                                      int instructionNumber) {
        super(IfEqInstruction.JUMP_TYPE, className, methodName, lineNUmber,methodDescriptor, instructionNumber,
                IfEqInstruction.CONSUMED_FROM_STACK_TYPE,
                Opcodes.IFEQ);
    }

    @Override
    public IfEqInstruction setDestination(ByteCodeInstruction instruction) {
        return new IfEqInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}
