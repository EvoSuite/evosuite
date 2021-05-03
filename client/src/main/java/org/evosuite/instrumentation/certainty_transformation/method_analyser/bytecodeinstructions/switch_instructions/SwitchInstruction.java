package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.switch_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class SwitchInstruction extends ByteCodeInstruction {

    public SwitchInstruction(String className, String methodName, int lineNumber,String methodDescriptor, String label, int instructionNumber, int opcode) {
        super(className, methodName, lineNumber, methodDescriptor, label, instructionNumber, opcode);
    }

    public abstract List<ByteCodeInstruction> getDestinations();

    public abstract ByteCodeInstruction getDefault();

    @Override
    public abstract Collection<Integer> getSuccessors();

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
