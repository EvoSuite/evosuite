package org.evosuite.instrumentation.certainty_transformation.boolean_transformation.boolean_to_int_instrumened_signatures;

import org.objectweb.asm.Type;

public class NullBooleanToIntInstrumentedSignature extends BooleanToIntInstrumentedSignature{

    public NullBooleanToIntInstrumentedSignature(){}

    @Override
    public BooleanToIntInstrumentedSignature replace(Type originalTypeDescriptor, Type replacedTypeDescriptor) {
        return this;
    }

    @Override
    public String toString() {
        return null;
    }
}
