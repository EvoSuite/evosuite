package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.InvokeInstructions;

import org.objectweb.asm.Opcodes;

public class InvokeVirtualInstruction extends InvokeInstruction {
    public InvokeVirtualInstruction(String className, String methodName, int line,String methodDescriptor, String owner,
                                    String name,
                                    String descriptor, int instructionNumber) {
        super(className, methodName, line,methodDescriptor, INVOKATION_TYPE.INVOKEVIRTUAL, owner, name, descriptor, instructionNumber,
                Opcodes.INVOKEVIRTUAL);
    }
}
