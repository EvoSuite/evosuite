package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders.UnaryJumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.UnaryJumpInstructions.IfLeInstruction;
import org.objectweb.asm.Opcodes;

public class IfLeInstructionPlaceholder extends UnaryJumpInstructionPlaceholder {

    public IfLeInstructionPlaceholder(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(IfLeInstruction.JUMP_TYPE, className, methodName, lineNUmber,methodDescriptor, instructionNumber,
                IfLeInstruction.CONSUMED_FROM_STACK_TYPE, Opcodes.IFLE);
    }

    @Override
    public IfLeInstruction setDestination(ByteCodeInstruction instruction) {
        return new IfLeInstruction(className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                instruction);
    }
}