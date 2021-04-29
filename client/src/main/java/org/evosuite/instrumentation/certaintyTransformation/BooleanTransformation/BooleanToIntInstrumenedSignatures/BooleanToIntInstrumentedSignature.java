package org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation.BooleanToIntInstrumenedSignatures;

import org.objectweb.asm.Type;

public abstract class BooleanToIntInstrumentedSignature {

    public static BooleanToIntInstrumentedSignature generate(String originalSignature){
        try {
            if (originalSignature == null)
                return new NullBooleanToIntInstrumentedSignature();
            else
                return new RealBooleanToIntInstrumentedSignature(originalSignature);
        } catch (Throwable t){
            throw new IllegalArgumentException("Could not instrument signature " +originalSignature, t);
        }
    }

    public abstract BooleanToIntInstrumentedSignature replace(Type originalType,
                                                              Type replacedType);

    @Override
    public abstract String toString();
}
