package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.return_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.ARETURN;

public class AReturnInstruction extends ReturnInstruction {

    public AReturnInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(StackTypeSet.AO, className, methodName, "ARETURN", lineNumber, methodDescriptor, instructionNumber,
                ARETURN);
    }

}