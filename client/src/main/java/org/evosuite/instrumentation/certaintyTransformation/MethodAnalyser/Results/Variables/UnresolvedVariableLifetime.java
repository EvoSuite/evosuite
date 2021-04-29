package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Variables;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.MethodIdentifier;
import org.objectweb.asm.Label;

import java.util.Map;

public class UnresolvedVariableLifetime {

    private MethodIdentifier identifier;
    private final String name;
    private final String descriptor;
    private final String signature;
    private final Label start;
    private final Label end;
    private final int index;

    public UnresolvedVariableLifetime(MethodIdentifier identifier, String name, String descriptor, String signature,
                                      Label start,
                                      Label end,
                                      int index){
        this.identifier = identifier;
        this.name = name;
        this.descriptor = descriptor;
        this.signature = signature;
        this.start = start;
        this.end = end;
        this.index = index;
    }

    public VariableLifetime resolve(Map<Label, ByteCodeInstruction> label2instruction){
        return new VariableLifetime(identifier, name, descriptor, signature, label2instruction.get(start),
                label2instruction.get(end), index);
    }
}
