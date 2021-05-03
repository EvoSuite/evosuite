package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.INSTANCEOF;

public class InstanceOfInstruction extends ByteCodeInstruction {
    private final String type;

    public InstanceOfInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber,
                                 String type) {
        super(className, methodName, lineNumber, methodDescriptor, "INSTANCEOF " + type, instructionNumber, INSTANCEOF);
        this.type = type;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.singletonList(StackTypeSet.AO);
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.BOOLEAN;
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
