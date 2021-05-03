package org.evosuite.instrumentation.certainty_transformation.boolean_transformation.boolean_to_int_instrumened_signatures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GenericType {
    private String identifier;
    private SignatureType typeExtends;
    private List<SignatureType> interfaces;

    public GenericType(String identifier, SignatureType typeExtends, List<SignatureType> interfaces) {
        this.identifier = identifier;
        this.typeExtends = typeExtends;
        this.interfaces = interfaces;
    }

    public static GenericType generate(String definition){
        if(!definition.contains(":"))
            throw new IllegalArgumentException("No definition of a generic type parameter provided");
        String[] split = definition.split(":");
        SignatureType classBound = split[1].equals("") ? null : SignatureType.getFirstType(split[1]);
        List<SignatureType> interfaces = new ArrayList<>(split.length-2);
        for(int i= 2; i<split.length; ++i){
            interfaces.add(SignatureType.getFirstType(split[i]));
        }
        return new GenericType(split[0], classBound, interfaces);
    }

    public static List<GenericType> generateTypes(String signature){
        if(!signature.startsWith("<"))
            return Collections.emptyList();
        ArrayList<GenericType> genericTypes = new ArrayList<>();
        signature = signature.substring(1);
        while(!signature.startsWith(">")) {
            GenericType generate = generate(signature);
            genericTypes.add(generate);
            signature = signature.substring(generate.toString().length());
        }
        return genericTypes;
    }

    @Override
    public String toString(){
        return String.format("%s:%s", identifier, typeExtends == null?"":typeExtends);
    }
}
