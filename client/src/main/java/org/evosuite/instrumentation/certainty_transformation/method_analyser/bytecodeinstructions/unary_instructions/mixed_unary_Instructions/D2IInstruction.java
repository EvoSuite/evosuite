package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.unary_instructions.mixed_unary_Instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.unary_instructions.AtoB_UnaryInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.D2I;

public class D2IInstruction extends AtoB_UnaryInstruction {
    public D2IInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor,"D2I", instructionNumber, StackTypeSet.DOUBLE, StackTypeSet.INT, D2I);
    }
}
