package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.LDC;

public class LoadConstantInstruction extends ByteCodeInstruction {
    private final Object value;
    private final StackTypeSet type;

    public LoadConstantInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber,
                                   Object value, StackTypeSet type) {
        super(className, methodName, lineNumber, methodDescriptor, "LDC \"" + value + "\"", instructionNumber, LDC);
        this.value = value;
        this.type = type;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.emptyList();
    }

    @Override
    public StackTypeSet pushedToStack() {
        return type;
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
        return Collections.emptySet();
    }
}
