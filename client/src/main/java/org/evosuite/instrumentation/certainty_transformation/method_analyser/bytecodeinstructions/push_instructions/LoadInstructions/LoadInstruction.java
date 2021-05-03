package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.push_instructions.LoadInstructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.push_instructions.PushInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.variables.VariableLifetime;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.variables.VariableTable;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class LoadInstruction extends PushInstruction {

    protected final int localVariableIndex;
    protected final boolean methodIsStatic;


    public LoadInstruction(String className, String methodName, int line, String methodDescriptor, String label, int localVariableIndex,
                           int instructionNumber, StackTypeSet loadedType, int opcode, boolean methodIsStatic) {
        super(className, methodName, line,methodDescriptor, String.join(" ", label, Integer.toString(localVariableIndex)),
                instructionNumber, loadedType, opcode);
        this.localVariableIndex = localVariableIndex;
        this.methodIsStatic = methodIsStatic;
    }

    public int getLocalVariableIndex() {
        return localVariableIndex;
    }

    @Override
    public StackTypeSet pushedToStack(VariableTable variableTable) {
        Set<VariableLifetime> lifetimesAtLocalVariableIndex =
                variableTable.getLifetimesAtLocalVariableIndex(localVariableIndex).stream()
                        .filter(l -> l.isAliveAt(this, false))
                        .collect(Collectors.toSet());
        int sort = -1;
        Type t = null;
        if (lifetimesAtLocalVariableIndex.size() > 1) {
            throw new IllegalStateException("Found more than one Variable at index " + localVariableIndex);
        } else if(lifetimesAtLocalVariableIndex.size() == 0){
            Type[] argumentTypes = Type.getArgumentTypes(methodDescriptor);
            int off = methodIsStatic ? 0 : 1;
            for (int i = 0; i < argumentTypes.length; i++) {
                Type argumentType = argumentTypes[i];
                if(i + off == localVariableIndex){
                    sort = argumentType.getSort();
                    t = argumentType;
                    break;
                }
                off += argumentType.getSize() -1;
            }
        }else {
            t = Type.getType(lifetimesAtLocalVariableIndex.iterator().next().getDesc());
            sort = t.getSort();
        }
        if(sort == -1){
            return this.pushedToStack();
        }
        if(sort == Type.BOOLEAN)
            return StackTypeSet.BOOLEAN;
        StackTypeSet of = StackTypeSet.ofMergeTypes(t);
        if(of.intersection(StackTypeSet.NON_BOOLEAN).isEmpty())
            return of;
        return StackTypeSet.NON_BOOLEAN;
    }

    @Override
    public Set<Integer> readsVariables(){
        return Collections.singleton(localVariableIndex);
    }
    @Override
    public Set<Integer> writesVariables(){
        return Collections.emptySet();
    }
}
