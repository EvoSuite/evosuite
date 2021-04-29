package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.TryCatch;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.objectweb.asm.Label;

import java.util.Map;

public class UnresolvedTryCatchBlock {

    private final Label start;
    private final Label end;
    private final Label handler;
    private final String type;

    public UnresolvedTryCatchBlock(Label start, Label end, Label handler, String type) {
        this.start = start;
        this.end = end;
        this.handler = handler;
        this.type = type;
    }

    public TryCatchBlock resolveLabels(Map<Label, ByteCodeInstruction> map) {
        return new TryCatchBlock(map.get(start),
                map.get(end),
                map.get(handler),
                type);
    }
}
