package org.evosuite.instrumentation.certainty_transformation.boolean_transformation.boolean_to_int_instrumened_signatures;

import org.objectweb.asm.Type;

import java.util.Collections;

public class WildcardSignature extends SignatureType {

    public WildcardSignature() {
        super(Type.getType("Ljava/lang/Object;"), Collections.emptyList());
    }

    @Override
    public SignatureType replace(Type originalType, Type replacedType) {
        return this;
    }

    @Override
    public String toString() {
        return "*";
    }
}
