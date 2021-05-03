package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.invoke_instructions;

import org.objectweb.asm.Opcodes;

public class InvokeVirtualInstruction extends InvokeInstruction {
    public InvokeVirtualInstruction(String className, String methodName, int line,String methodDescriptor, String owner,
                                    String name,
                                    String descriptor, int instructionNumber) {
        super(className, methodName, line,methodDescriptor, INVOKATION_TYPE.INVOKEVIRTUAL, owner, name, descriptor, instructionNumber,
                Opcodes.INVOKEVIRTUAL);
    }
}
