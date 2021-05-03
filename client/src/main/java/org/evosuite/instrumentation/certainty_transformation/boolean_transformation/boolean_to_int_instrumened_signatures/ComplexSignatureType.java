package org.evosuite.instrumentation.certainty_transformation.boolean_transformation.boolean_to_int_instrumened_signatures;

import org.objectweb.asm.Type;

import java.util.List;
import java.util.stream.Collectors;

public class ComplexSignatureType extends SignatureType{

    private String innerType;

    public ComplexSignatureType(Type type, List<SignatureType> genericTypes) {
        super(type, genericTypes);
    }

    public ComplexSignatureType(Type type, List<SignatureType> genericTypes, String innerType){
        super(type,genericTypes);
        this.innerType = innerType;
    }

    public SignatureType replace(Type originalTypeDescriptor, Type replacedTypeDescriptor) {
        return new ComplexSignatureType(type.equals(originalTypeDescriptor) ? replacedTypeDescriptor : type,
                genericTypes.stream().map(t -> t.replace(originalTypeDescriptor,
                        replacedTypeDescriptor)).collect(Collectors.toList()));
    }

    @Override
    public String toString() {
        String typeDescriptor = type.getDescriptor();
        return typeDescriptor.substring(0,typeDescriptor.length()-1) + "<" + genericTypes.stream().map(SignatureType::toString).collect(Collectors.joining("")) +
                ">" + (innerType != null ? innerType : ";");
    }
}
