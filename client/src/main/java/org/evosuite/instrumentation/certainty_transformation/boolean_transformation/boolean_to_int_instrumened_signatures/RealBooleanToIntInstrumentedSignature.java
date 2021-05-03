package org.evosuite.instrumentation.certainty_transformation.boolean_transformation.boolean_to_int_instrumened_signatures;

import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RealBooleanToIntInstrumentedSignature extends BooleanToIntInstrumentedSignature {

    private List<GenericType> genericTypes = Collections.emptyList();
    private List<SignatureType> argTypes;
    private SignatureType retType;

    public RealBooleanToIntInstrumentedSignature(String originalSignature) {
        Objects.requireNonNull(originalSignature);
        if(hasGenericParameter(originalSignature)) {
            originalSignature = genericTypes(originalSignature);
        }
        String[] argumentsAndReturn = originalSignature.substring(1).split("\\)");
        String arguments = argumentsAndReturn[0];
        String _return = argumentsAndReturn[1];
        argTypes = SignatureType.generate(arguments);
        retType = SignatureType.generate(_return).iterator().next();
    }

    private String genericTypes(String originalSignature){
        this.genericTypes = GenericType.generateTypes(originalSignature);
        return originalSignature.substring(stringifyGenericTypes().length());
    }

    private static boolean hasGenericParameter(String originalSignature) {
        return originalSignature.startsWith("<");
    }

    private RealBooleanToIntInstrumentedSignature(List<SignatureType> argTypes, SignatureType retType) {
        this.argTypes = argTypes;
        this.retType = retType;
    }

    private RealBooleanToIntInstrumentedSignature(List<SignatureType> argTypes, SignatureType retType,
                                                  List<GenericType> genericTypes){
        this.genericTypes = genericTypes;
        this.argTypes = argTypes;
        this.retType = retType;
    }

    private String stringifyGenericTypes(){
        if(genericTypes.isEmpty())
            return "";
        return String.format("<%s>", genericTypes.stream().map(GenericType::toString).collect(Collectors.joining("")));
    }

    @Override
    public BooleanToIntInstrumentedSignature replace(Type originalType, Type replacedType) {
        return new RealBooleanToIntInstrumentedSignature(argTypes.stream().map(t -> t.replace(originalType,
                replacedType)).collect(Collectors.toList()), retType.replace(originalType,
                replacedType),genericTypes);
    }

    @Override
    public String toString() {
        return stringifyGenericTypes() + "(" + argTypes.stream().map(SignatureType::toString).collect(Collectors.joining("")) + ")" + retType.toString();
    }
}
