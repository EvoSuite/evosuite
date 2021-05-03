package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.push_instructions.ConstantInstructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.ACONST_NULL;

public class ConstNullInstruction extends ConstantInstruction<Object> {
    public ConstNullInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                                int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "ACONST_NULL", instructionNumber, null, StackTypeSet.AO, ACONST_NULL);
    }
}
