package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.return_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class ReturnInstruction extends ByteCodeInstruction {
    private final StackTypeSet returnType;

    public ReturnInstruction(StackTypeSet returnType, String className, String methodName, String label, int lineNumber,String methodDescriptor,
                             int instructionNumber, int opcode) {
        super(className, methodName, lineNumber, methodDescriptor, label, instructionNumber, opcode);
        this.returnType = returnType;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.singletonList(StackTypeSet.copy(returnType));
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.of(Type.VOID);
    }

    @Override
    public List<Integer> getSuccessors() {
        return Collections.emptyList();
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
