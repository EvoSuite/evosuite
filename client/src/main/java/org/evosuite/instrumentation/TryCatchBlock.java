package org.evosuite.instrumentation;

import org.objectweb.asm.Label;

/**
 * Created by gordon on 03/01/2016.
 */
class TryCatchBlock {
    public TryCatchBlock(Label start, Label end, Label handler, String type) {
        this.start = start;
        this.end = end;
        this.handler = handler;
        this.type = type;
    }

    Label start;
    Label end;
    Label handler;
    String type;
}
