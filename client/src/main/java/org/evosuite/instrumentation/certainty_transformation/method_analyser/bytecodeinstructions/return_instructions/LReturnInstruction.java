package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.return_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.LRETURN;

public class LReturnInstruction extends ReturnInstruction {
    public LReturnInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(StackTypeSet.LONG, className, methodName, "LRETURN", lineNumber, methodDescriptor,instructionNumber, LRETURN);
    }
}
