package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.AxB_to_A_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.AxBtoA_BinaryInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.LSHR;

public class LshrInstruction extends AxBtoA_BinaryInstruction {
    public LshrInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className,
                methodName,
                lineNumber, methodDescriptor,
                "LSHR",
                instructionNumber,
                StackTypeSet.LONG,
                StackTypeSet.INT,
                LSHR);
    }
}
