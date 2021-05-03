package org.evosuite.instrumentation.certainty_transformation.boolean_transformation.boolean_to_int_instrumened_signatures;

import org.objectweb.asm.Type;

import java.util.Collections;

public class TypeVarSignatue extends SignatureType {
    private final String signature;
    public TypeVarSignatue(String signature) {
        super(null, Collections.emptyList());
        this.signature = signature;
    }

    @Override
    public SignatureType replace(Type originalType, Type replacedType) {
        return this;
    }

    @Override
    public String toString() {
        return signature;
    }
}
