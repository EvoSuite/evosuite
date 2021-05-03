package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.AxB_to_A_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.AxBtoA_BinaryInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.LSHL;

public class LshlInstruction extends AxBtoA_BinaryInstruction {
    public LshlInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(className,
                methodName,
                lineNumber,methodDescriptor,
                "LSHL",
                instructionNumber,
                StackTypeSet.LONG,
                StackTypeSet.INT,
                LSHL);
    }
}
