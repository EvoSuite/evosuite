package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.unary_instructions.mixed_unary_Instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.unary_instructions.AtoB_UnaryInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.F2D;

public class F2DInstruction extends AtoB_UnaryInstruction {
    public F2DInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "F2D", instructionNumber, StackTypeSet.FLOAT, StackTypeSet.DOUBLE, F2D);
    }
}
