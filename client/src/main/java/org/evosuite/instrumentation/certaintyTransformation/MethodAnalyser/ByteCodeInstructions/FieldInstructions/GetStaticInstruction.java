package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.FieldInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.GETSTATIC;

public class GetStaticInstruction extends ByteCodeInstruction {
    private final String owner;
    private final String field;
    private final String descriptor;

    public GetStaticInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber,
                                String owner, String field, String descriptor) {
        super(className, methodName, lineNumber, methodDescriptor, "GETSTATIC \"" + owner + ":" + field + "\"", instructionNumber,
                GETSTATIC);
        this.owner = owner;
        this.field = field;
        this.descriptor = descriptor;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.emptyList();
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.of(Type.getType(descriptor).getSort());
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
