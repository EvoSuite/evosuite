package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.evosuite.instrumentation.certainty_transformation.method_analyser.MethodAnalyser.METHOD_ENTER_ORDER;

public class MethodEnter extends ByteCodeInstruction{

    public static final int METHOD_ENTER_OPCODE = -1;
    private final String descriptor;

    public MethodEnter(String className, String methodName, String methodDescriptor) {
        super(className, methodName, METHOD_ENTER_ORDER
                , methodDescriptor, "Enter Method " + String.join(":", className, methodName, methodDescriptor), METHOD_ENTER_ORDER,
                METHOD_ENTER_OPCODE);
        descriptor = methodDescriptor;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.emptyList();
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.of(Type.VOID);
    }

    @Override
    public boolean writesVariable(int index){
        return false;
    }
    @Override
    public Set<Integer> readsVariables(){
        return Collections.emptySet();
    }
    @Override
    public Set<Integer> writesVariables(){
        int length = Type.getArgumentTypes(descriptor).length;
        return IntStream.range(0, length).boxed().collect(Collectors.toSet());
    }
}
