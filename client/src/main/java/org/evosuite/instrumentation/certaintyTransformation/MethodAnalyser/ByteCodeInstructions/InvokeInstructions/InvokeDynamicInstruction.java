package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.InvokeInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InvokeDynamicInstruction extends InvokeInstruction {
    public InvokeDynamicInstruction(String className, String methodName, int line,String methodDescriptor, String owner, String name,
                                    String descriptor, int instructionNumber) {
        super(className, methodName, line,methodDescriptor, INVOKATION_TYPE.INVOKEDYNAMIC, owner, name, descriptor,
                instructionNumber,
                Opcodes.INVOKEDYNAMIC);
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Arrays.stream(Type.getArgumentTypes(descriptor)).map(StackTypeSet::ofMergeTypes).collect(Collectors.toList());
    }
}
