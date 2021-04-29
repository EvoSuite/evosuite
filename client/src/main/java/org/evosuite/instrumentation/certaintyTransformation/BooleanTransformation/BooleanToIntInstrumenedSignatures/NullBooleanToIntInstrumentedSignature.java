package org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation.BooleanToIntInstrumenedSignatures;

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
