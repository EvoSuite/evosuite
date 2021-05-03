package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.return_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.IRETURN;

public class IReturnInstruction extends ReturnInstruction {
    @Deprecated
    public IReturnInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(StackTypeSet.TWO_COMPLEMENT, className, methodName, "IRETRUN", lineNumber, methodDescriptor,instructionNumber, IRETURN);
    }

    public IReturnInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber, StackTypeSet
                               returnTypes){
        super(returnTypes, className, methodName, "IRETURN", lineNumber,methodDescriptor, instructionNumber, IRETURN);
    }
}
