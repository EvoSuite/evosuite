package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import java.util.Collections;
import java.util.Set;

public abstract class JumpInstruction extends ByteCodeInstruction {

    protected final JUMP_TYPE jumpType;
    protected final int jmpDestination;

    public JumpInstruction(JUMP_TYPE jumpType, String className, String methodName, int lineNUmber,String methodDescriptor,
                           int instructionNumber, ByteCodeInstruction jmpDestination, int opcode) {
        super(className, methodName, lineNUmber, methodDescriptor, jumpType.toString() + " " + jmpDestination.getOrder(),
                instructionNumber, opcode);
        this.jumpType = jumpType;
        this.jmpDestination = jmpDestination.getOrder();
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.VOID;
    }

    public enum JUMP_TYPE {
        IFEQ,
        IFNE,
        IFLT,
        IFGE,
        IFGT,
        IFLE,
        IF_ICMPEQ,
        IF_ICMPNE,
        IF_ICMPLT,
        IF_ICMPGE,
        IF_ICMPGT,
        IF_ICMPLE,
        IF_ACMPEQ,
        IF_ACMPNE,
        GOTO,
        JSR,
        IFNULL,
        IFNONNULL,
        RET
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
