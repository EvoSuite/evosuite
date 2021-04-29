package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.GotoInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.JumpInstruction;
import org.objectweb.asm.Opcodes;

public class GotoInstructionPlaceholder extends UnconditionalJumpPlaceholder {
    public GotoInstructionPlaceholder(String className, String methodName, int lineNUmber, String methodDescriptor,int instructionNumber) {
        super(JumpInstruction.JUMP_TYPE.GOTO, className, methodName, lineNUmber,methodDescriptor, instructionNumber, Opcodes.GOTO);
    }

    @Override
    public GotoInstruction setDestination(ByteCodeInstruction instruction) {
        return new GotoInstruction(className,methodName,lineNumber,methodDescriptor,instructionNumber,instruction);
    }
}
