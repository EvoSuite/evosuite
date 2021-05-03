package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.invoke_instructions;

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;

public class InvokeSpecialInstruction extends InvokeInstruction {

    public InvokeSpecialInstruction(String className, String methodName, int line,String methodDescriptor, String owner, String name,
                                    String descriptor, int instructionNumber) {
        super(className, methodName, line,methodDescriptor, INVOKATION_TYPE.INVOKESPECIAL, owner, name, descriptor,
                instructionNumber, INVOKESPECIAL);
    }
}
