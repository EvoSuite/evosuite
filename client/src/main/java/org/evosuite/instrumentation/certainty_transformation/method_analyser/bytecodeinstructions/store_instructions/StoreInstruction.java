package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.store_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.variables.VariableLifetime;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.variables.VariableTable;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class StoreInstruction extends ByteCodeInstruction {
    protected final int localVariableIndex;
    private final StackTypeSet storedType;

    public StoreInstruction(String className, String methodName, int line,String methodDescriptor, String label,
                            int localVariableIndex, int instructionNumber, int storedType, int opcode) {
        super(className, methodName, line, methodDescriptor, label, instructionNumber, opcode);
        this.localVariableIndex = localVariableIndex;
        this.storedType = StackTypeSet.of(storedType);
    }

    public StoreInstruction(String className, String methodName, int line,String methodDescriptor, String label,
                            int localVariableIndex, int instructionNumber, StackTypeSet storedType, int opcode) {
        super(className, methodName, line, methodDescriptor, label, instructionNumber, opcode);
        this.localVariableIndex = localVariableIndex;
        this.storedType = storedType;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.singletonList(storedType);
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.of(Type.VOID);
    }

    public int getLocalVariableIndex() {
        return localVariableIndex;
    }

    @Override
    public List<StackTypeSet> consumedFromStack(VariableTable table) {
        Optional<VariableLifetime> any = table.getLifetimesAtLocalVariableIndex(localVariableIndex)
                .stream().filter(i -> i.isAliveAt(this, true))
                .findAny();
        return any.map(variableLifetime -> Collections.singletonList(StackTypeSet.ofMergeTypes(Type.getType(variableLifetime.getDesc())))).
                orElseGet(() -> super.consumedFromStack(table));
    }

    @Override
    public boolean writesVariable(int index){
        return localVariableIndex == index;
    }

    @Override
    public Set<Integer> writesVariables(){
        return Collections.singleton(localVariableIndex);
    }

    @Override
    public Set<Integer> readsVariables(){
        return Collections.emptySet();
    }


}
