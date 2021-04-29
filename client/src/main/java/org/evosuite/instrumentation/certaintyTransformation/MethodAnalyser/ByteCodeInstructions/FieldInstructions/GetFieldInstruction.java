package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.FieldInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.GETFIELD;

public class GetFieldInstruction extends ByteCodeInstruction {
    private final String owner;
    private final String field;
    private final String desc;

    public GetFieldInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                               int instructionNumber, String owner, String field, String desc) {
        super(className, methodName, lineNumber, methodDescriptor, "GETFIELD \"" + owner + ":" + field + "\"", instructionNumber, GETFIELD);
        this.owner = owner;
        this.field = field;
        this.desc = desc;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.singletonList(StackTypeSet.OBJECT);
    }

    @Override
    public StackTypeSet pushedToStack() {
        int sort = Type.getType(desc).getSort();
        if(sort == Type.BOOLEAN)
            return StackTypeSet.BOOLEAN;
        StackTypeSet of = StackTypeSet.of(sort);
        if(of.intersection(StackTypeSet.NON_BOOLEAN).isEmpty())
            return of;
        return StackTypeSet.NON_BOOLEAN;
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
