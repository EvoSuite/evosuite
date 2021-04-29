package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.UnaryJumpInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.Opcodes;

import static org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.JumpInstruction.JUMP_TYPE.IFGE;

public class IfGeInstruction extends UnaryJumpInstruction {

    public final static StackTypeSet CONSUMED_FROM_STACK_TYPE = StackTypeSet.TWO_COMPLEMENT;
    public final static JUMP_TYPE JUMP_TYPE = IFGE;

    public IfGeInstruction(String className, String methodName, int lineNUmber,String methodDescriptor,
                           int instructionNumber,
                           ByteCodeInstruction destination) {
        super(JUMP_TYPE, className, methodName, lineNUmber,methodDescriptor, instructionNumber, destination,CONSUMED_FROM_STACK_TYPE,
                Opcodes.IFGE);
    }
}
