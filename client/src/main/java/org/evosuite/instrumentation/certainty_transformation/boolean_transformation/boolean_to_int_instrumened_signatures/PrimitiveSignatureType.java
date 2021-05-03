package org.evosuite.instrumentation.certainty_transformation.boolean_transformation.boolean_to_int_instrumened_signatures;

import org.objectweb.asm.Type;

import java.util.Collections;

public class PrimitiveSignatureType extends SignatureType{
    public PrimitiveSignatureType(Type type) {
        super(type, Collections.emptyList());
    }

    public SignatureType replace(Type originalTypeDescriptor, Type replacedTypeDescriptor) {
        return new PrimitiveSignatureType(type.equals(originalTypeDescriptor) ? replacedTypeDescriptor : type);
    }

    @Override
    public String toString() {
        return super.type.getDescriptor();
    }
}
