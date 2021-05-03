package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.return_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.DRETURN;

public class DReturnInstruction extends ReturnInstruction {
    public DReturnInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(StackTypeSet.DOUBLE, className, methodName, "DRETURN", lineNumber,methodDescriptor, instructionNumber, DRETURN);
    }
}
