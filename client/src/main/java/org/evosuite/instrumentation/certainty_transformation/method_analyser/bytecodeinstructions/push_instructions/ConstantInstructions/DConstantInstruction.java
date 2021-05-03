package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.push_instructions.ConstantInstructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;

public class DConstantInstruction extends ConstantInstruction<Double> {

    public DConstantInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                                int instructionNumber, double value) {
        super(className, methodName, lineNumber,methodDescriptor, "DCONST " + value, instructionNumber, value,
                StackTypeSet.DOUBLE,
                valueToOpcode(value));
        this.value = value;
    }

    private static int valueToOpcode(double value){
        if(Arrays.asList(0d,1d).contains(value))
            return Opcodes.DCONST_0 + (int)value;
        throw new IllegalArgumentException("DCONST only accepts 0 and 1 as values");
    }
}
