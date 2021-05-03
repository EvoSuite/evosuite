package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.unary_instructions.mixed_unary_Instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.unary_instructions.AtoB_UnaryInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.F2L;

public class F2LInstruction extends AtoB_UnaryInstruction {
    public F2LInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "F2L", instructionNumber, StackTypeSet.FLOAT, StackTypeSet.LONG, F2L);
    }
}
