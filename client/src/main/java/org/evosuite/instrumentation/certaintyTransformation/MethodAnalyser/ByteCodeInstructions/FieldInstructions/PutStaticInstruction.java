package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.FieldInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.PUTSTATIC;

public class PutStaticInstruction extends ByteCodeInstruction {
    private final String owner;
    private final String field;
    private final String desc;

    public PutStaticInstruction(String className, String methodName, int lineNumber,
                                String methodDescriptor, int instructionNumber,
                                String owner, String field, String desc) {
        super(className, methodName, lineNumber, methodDescriptor, "PUTSTATIC \"" + owner + ":" + field + "\"", instructionNumber,
                PUTSTATIC);
        this.owner = owner;
        this.field = field;
        this.desc = desc;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.singletonList(StackTypeSet.of(Type.getType(desc).getSort()));
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.VOID;
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
