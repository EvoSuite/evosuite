package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.field_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.PUTFIELD;

public class PutFieldInstruction extends ByteCodeInstruction {
    private final String owner;
    private final String field;
    private final String desc;

    public PutFieldInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                               int instructionNumber, String owner, String field, String desc) {
        super(className, methodName, lineNumber, methodDescriptor, "PUTFIELD \"" + owner + ":" + field + "\"", instructionNumber, PUTFIELD);
        this.owner = owner;
        this.field = field;
        this.desc = desc;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Arrays.asList(StackTypeSet.OBJECT, StackTypeSet.of(Type.getType(desc).getSort()));
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
