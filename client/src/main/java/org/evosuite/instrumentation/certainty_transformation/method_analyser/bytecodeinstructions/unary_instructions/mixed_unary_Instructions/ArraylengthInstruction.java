package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.unary_instructions.mixed_unary_Instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.unary_instructions.AtoB_UnaryInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.ARRAYLENGTH;

public class ArraylengthInstruction extends AtoB_UnaryInstruction {
    public ArraylengthInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {

        super(className, methodName, lineNumber,methodDescriptor, "ARRAYLENGTH", instructionNumber, StackTypeSet.ARRAY,
                StackTypeSet.INT, ARRAYLENGTH);
    }
}
