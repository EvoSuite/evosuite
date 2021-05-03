package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.invoke_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;
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
