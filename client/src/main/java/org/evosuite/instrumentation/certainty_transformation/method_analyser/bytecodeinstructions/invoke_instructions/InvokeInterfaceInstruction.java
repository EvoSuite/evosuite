package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.invoke_instructions;

import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;

public class InvokeInterfaceInstruction extends InvokeInstruction {
    public InvokeInterfaceInstruction(String className, String methodName, int line,String methodDescriptor, String owner, String name,
                                      String descriptor, int instructionNumber) {
        super(className, methodName, line,methodDescriptor, INVOKATION_TYPE.INVOKEINTERFACE, owner, name, descriptor,
                instructionNumber, INVOKEINTERFACE);
    }
}
