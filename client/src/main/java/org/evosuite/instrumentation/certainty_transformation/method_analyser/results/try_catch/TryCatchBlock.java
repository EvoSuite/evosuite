package org.evosuite.instrumentation.certainty_transformation.method_analyser.results.try_catch;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;

public class TryCatchBlock {
    private final ByteCodeInstruction start;
    private final ByteCodeInstruction end;

    private final ByteCodeInstruction handler;
    private final String type;

    public TryCatchBlock(ByteCodeInstruction start, ByteCodeInstruction end, ByteCodeInstruction handler, String type) {
        this.start = start;
        this.end = end;
        this.handler = handler;
        this.type = type;
    }

    public ByteCodeInstruction getStart() {
        return start;
    }

    public ByteCodeInstruction getEnd() {
        return end;
    }

    public ByteCodeInstruction getHandler() {
        return handler;
    }

    public String getType() {
        return type;
    }
}
