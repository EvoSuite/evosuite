package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.push_instructions.LoadInstructions;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.StackTypeSet;

import static org.objectweb.asm.Opcodes.DLOAD;

public class DLoadInstruction extends LoadInstruction {
    public DLoadInstruction(String className, String methodName, int line,String methodDescriptor, int localVariableIndex,
                            int instructionNumber, boolean methodIsStatic) {
        super(className, methodName, line, methodDescriptor,"DLOAD", localVariableIndex, instructionNumber,
                StackTypeSet.DOUBLE, DLOAD, methodIsStatic);
    }
}
