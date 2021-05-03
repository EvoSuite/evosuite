package org.evosuite.instrumentation.certainty_transformation.method_analyser.results.variables;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.MethodIdentifier;

public class VariableLifetime {

    private final MethodIdentifier identifier;
    private final String name;
    private final String desc;
    private final String signature;
    private final ByteCodeInstruction start;
    private final ByteCodeInstruction end;
    private final int index;

    public VariableLifetime(MethodIdentifier identifier, String name, String desc, String signature,
                            ByteCodeInstruction start, ByteCodeInstruction end, int index) {
        this.identifier = identifier;
        this.name = name;
        this.desc = desc;
        this.signature = signature;
        this.start = start;
        this.end = end;
        this.index = index;
    }


    public MethodIdentifier getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getSignature() {
        return signature;
    }

    public ByteCodeInstruction getStart() {
        return start;
    }

    public ByteCodeInstruction getEnd() {
        return end;
    }

    public int getIndex() {
        return index;
    }

    /**
     * If the lifetime described by this object contains {@param instruction}. Since the instruction first writing this
     * variable is not contained in the lifetime, this function can be extended for also matching the previous
     * statement via the {@parameter includeDeclaring}
     *
     * @param instruction the queried instruction
     * @param includeDeclaring whether the first write is included
     * @return whether this  variable is alive at {@param instruction}
     */
    public boolean isAliveAt(ByteCodeInstruction instruction, boolean includeDeclaring) {
        int lower = start.getOrder() - (includeDeclaring ? 1 : 0);
        int upper = end != null ? end.getOrder() : Integer.MAX_VALUE;
        return lower <= instruction.getOrder() && instruction.getOrder() < upper;
    }

    @Override
    public String toString() {
        return "VariableLifetime{" +
                "name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", start=" + start.getOrder() +
                ", end=" + (end == null ? -1 : end.getOrder()) +
                ", index=" + index +
                '}';
    }
}
