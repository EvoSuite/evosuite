package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.BinaryJumpInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.JumpInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.Opcodes;

public class ObjectCmpNeInstruction extends BinaryJumpInstruction {

    public final static StackTypeSet CONSUMED_FROM_STACK_TYPE = StackTypeSet.OBJECT;
    public final static JUMP_TYPE JUMP_TYPE = JumpInstruction.JUMP_TYPE.IF_ACMPNE;

    public ObjectCmpNeInstruction(String className, String methodName, int lineNUmber,String methodDescriptor,
                                  int instructionNumber, ByteCodeInstruction destination) {
        super(JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber,
                destination,
                CONSUMED_FROM_STACK_TYPE, Opcodes.IF_ACMPNE);
    }
}
