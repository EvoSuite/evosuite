package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.UnaryJumpInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.JumpInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.Opcodes;

public class IfEqInstruction extends UnaryJumpInstruction {

    public final static StackTypeSet CONSUMED_FROM_STACK_TYPE = StackTypeSet.TWO_COMPLEMENT;
    public final static JUMP_TYPE JUMP_TYPE = JumpInstruction.JUMP_TYPE.IFEQ;

    public IfEqInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                           int instructionNumber, ByteCodeInstruction destination) {
        super(JUMP_TYPE,
                className,
                methodName,
                lineNumber,methodDescriptor,
                instructionNumber,
                destination,
                CONSUMED_FROM_STACK_TYPE, Opcodes.IFEQ);
    }
}
