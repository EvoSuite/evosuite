package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.UnaryJumpInstructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.JumpInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;
import org.objectweb.asm.Opcodes;

public class IfNullInstruction extends UnaryJumpInstruction {

    public static final StackTypeSet CONSUMED_FROM_STACK_TYPE = StackTypeSet.AO;
    public static final JUMP_TYPE JUMP_TYPE = JumpInstruction.JUMP_TYPE.IFNULL;

    public IfNullInstruction(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber,
                             ByteCodeInstruction destination) {

        super(JUMP_TYPE,
                className,
                methodName,
                lineNUmber,methodDescriptor,
                instructionNumber,
                destination,
                CONSUMED_FROM_STACK_TYPE, Opcodes.IFNULL);
    }
}
