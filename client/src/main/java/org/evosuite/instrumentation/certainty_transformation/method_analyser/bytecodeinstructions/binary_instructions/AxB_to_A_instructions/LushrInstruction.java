package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.AxB_to_A_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.AxBtoA_BinaryInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.LUSHR;

public class LushrInstruction extends AxBtoA_BinaryInstruction {
    public LushrInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className,
                methodName,
                lineNumber,methodDescriptor,
                "LUSHR",
                instructionNumber,
                StackTypeSet.LONG,
                StackTypeSet.INT,
                LUSHR);
    }
}
