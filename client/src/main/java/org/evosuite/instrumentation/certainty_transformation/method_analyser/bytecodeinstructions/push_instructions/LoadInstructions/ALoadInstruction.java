package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.push_instructions.LoadInstructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.ALOAD;

public class ALoadInstruction extends LoadInstruction {


    public ALoadInstruction(String className, String methodName, int line,String methodDescriptor, int localVariableIndex,
                            int instructionNumber, boolean methodIsStatic) {
        super(className, methodName, line,methodDescriptor, "ALOAD", localVariableIndex, instructionNumber, StackTypeSet.AO, ALOAD, methodIsStatic);
    }


    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.AO;
    }
}
