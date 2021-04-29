package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.UnaryJumpInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.Opcodes;

import static org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.JumpInstruction.JUMP_TYPE.IFLT;

public class IfLtInstruction extends UnaryJumpInstruction {

    public final static StackTypeSet CONSUMED_FROM_STACK_TYPE = StackTypeSet.TWO_COMPLEMENT;
    public final static JUMP_TYPE JUMP_TYPE = IFLT;

    public IfLtInstruction(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber, ByteCodeInstruction jmpDestination) {
        super(JUMP_TYPE, className, methodName, lineNUmber,methodDescriptor, instructionNumber, jmpDestination,
                CONSUMED_FROM_STACK_TYPE, Opcodes.IFLT);
    }
}
