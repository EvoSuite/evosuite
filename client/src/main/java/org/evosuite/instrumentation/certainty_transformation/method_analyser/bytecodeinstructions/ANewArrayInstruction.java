package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.ANEWARRAY;

public class ANewArrayInstruction extends ByteCodeInstruction {
    private final String constructedArrayType;

    public ANewArrayInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                                int instructionNumber, String constructedArrayType) {
        super(className, methodName, lineNumber, methodDescriptor, "ANEWARRAY " + constructedArrayType, instructionNumber, ANEWARRAY);
        this.constructedArrayType = constructedArrayType;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.singletonList(StackTypeSet.INT);
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.AO;
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
