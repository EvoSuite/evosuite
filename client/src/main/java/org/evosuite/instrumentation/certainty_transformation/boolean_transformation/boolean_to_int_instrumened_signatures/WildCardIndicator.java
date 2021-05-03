package org.evosuite.instrumentation.certainty_transformation.boolean_transformation.boolean_to_int_instrumened_signatures;

import org.objectweb.asm.Type;

import java.util.Collections;

public class WildCardIndicator extends SignatureType {
    private String indicator;
    private final SignatureType signature;
    public WildCardIndicator(String indicator, String signature) {
        super(null, Collections.emptyList());
        this.indicator = indicator;
        this.signature = getFirstType(signature.substring(1));
    }

    public WildCardIndicator(String indicator, SignatureType signature){
        super(null, Collections.emptyList());
        this.indicator = indicator;
        this.signature = signature;
    }

    @Override
    public SignatureType replace(Type originalType, Type replacedType){
        return new WildCardIndicator(indicator, signature.replace(originalType, replacedType));
    }

    @Override
    public String toString(){return indicator + signature.toString();}
}
