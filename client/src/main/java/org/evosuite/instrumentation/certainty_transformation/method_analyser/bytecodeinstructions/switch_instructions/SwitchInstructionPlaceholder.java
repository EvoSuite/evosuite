package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.switch_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class SwitchInstructionPlaceholder extends ByteCodeInstruction {


    public SwitchInstructionPlaceholder(String className, String methodName, int lineNumber,String methodDescriptor, String label,
                                        int instructionNumber, int opcode) {
        super(className, methodName, lineNumber, methodDescriptor, label, instructionNumber, opcode);
    }

    @Override
    public Collection<Integer> getSuccessors() {
        throw new UnsupportedOperationException("getSuccessors is not supported on Placeholders");
    }

    public abstract SwitchInstruction setDestinations(ByteCodeInstruction def,
                                                      List<ByteCodeInstruction> destinations);

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
