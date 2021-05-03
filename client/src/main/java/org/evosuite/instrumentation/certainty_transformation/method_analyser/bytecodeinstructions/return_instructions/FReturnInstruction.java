package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.return_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.FRETURN;

public class FReturnInstruction extends ReturnInstruction {
    public FReturnInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(StackTypeSet.FLOAT, className, methodName, "FRETURN", lineNumber,methodDescriptor, instructionNumber, FRETURN);
    }
}
