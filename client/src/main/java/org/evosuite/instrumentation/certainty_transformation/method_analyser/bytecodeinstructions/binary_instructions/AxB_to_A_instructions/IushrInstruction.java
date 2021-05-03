package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.AxB_to_A_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.AxAtoA_BinaryInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;
import org.objectweb.asm.Opcodes;

public class IushrInstruction extends AxAtoA_BinaryInstruction {
    public IushrInstruction(String className, String methodName, int lineNUmber,String methodDescriptor,
                            int instructionNumber) {
        super(className, methodName, "IUSHR", lineNUmber,methodDescriptor, instructionNumber, StackTypeSet.INT, Opcodes.IUSHR);
    }
}
