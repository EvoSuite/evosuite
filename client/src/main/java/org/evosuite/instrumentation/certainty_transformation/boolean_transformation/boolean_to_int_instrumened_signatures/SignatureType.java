package org.evosuite.instrumentation.certainty_transformation.boolean_transformation.boolean_to_int_instrumened_signatures;

import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class SignatureType {
    protected Type type;
    protected List<SignatureType> genericTypes;

    public SignatureType(Type type, List<SignatureType> genericTypes) {
        this.type = type;
        this.genericTypes = new ArrayList<>(genericTypes);
    }

    public abstract SignatureType replace(Type originalType, Type replacedType);

    public static List<SignatureType> generate(String signature) {
        List<SignatureType> signatureTypes = new ArrayList<>();
        String remaining = signature;
        while (!StringUtils.isBlank(remaining)) {
            SignatureType type = getFirstType(remaining);
            signatureTypes.add(type);
            if(remaining.indexOf(type.toString().length() -1) == '.'){
                throw new IllegalArgumentException();
            }
            remaining = remaining.substring(type.toString().length());
        }
        return signatureTypes;
    }

    public static SignatureType getFirstType(String signature) {
        return getFirstType(signature, Collections.emptyList());
    }

    public static SignatureType getFirstType(String signature, List<GenericType> defined) {
        if(signature == null){
            return null;
        }
        if (signature.contains("<") && signature.startsWith("L")) {
            if (signature.indexOf(";") < signature.indexOf("<")) {
                String firstType = signature.substring(0, signature.indexOf(";") + 1);
                return new PrimitiveSignatureType(Type.getType(firstType));
            }
            String firstType = signature.substring(0, signature.indexOf('<')) + ";";
            String generic = getGenericStartingAt(signature, signature.indexOf('<'));
            generic = generic.substring(1, generic.length() - 1);
            List<SignatureType> genericTypes = generate(generic);
            if(signature.charAt(signature.indexOf('<') + generic.length() + 2) == '.'){
                String substring = signature.substring(signature.indexOf('<') + generic.length() + 2);
                String innerType = substring.substring(0, substring.indexOf(';')+1);
                return new ComplexSignatureType(Type.getType(firstType),genericTypes, innerType);
            }
            return new ComplexSignatureType(Type.getType(firstType), genericTypes);
        } else if (signature.startsWith("L")) {
            String firstType = signature.substring(0, signature.indexOf(";") + 1);
            return new PrimitiveSignatureType(Type.getType(firstType));
        } else if (signature.startsWith("*")) {
            return new WildcardSignature();
        } else if (signature.startsWith("T")) {
            return new TypeVarSignatue(signature.substring(0,signature.indexOf(";")+1));
        } else if (signature.startsWith("+") || signature.startsWith("-")) {
            return new WildCardIndicator(signature.substring(0,1),signature);
        } else {
            try {
                return new PrimitiveSignatureType(Type.getType(signature));
            } catch (Throwable t){
                throw new IllegalArgumentException(signature,t);
            }
        }
    }

    private static String getGenericStartingAt(String signature, int index){
        if(signature.charAt(index) != '<')
            throw new IllegalArgumentException("index not a <");
        int opened = 0;
        StringBuilder generic = new StringBuilder();
        for(char c: signature.substring(index).toCharArray()){
            if(c == '<')
                opened++;
            else if(c == '>')
                opened--;
            generic.append(c);
            if(opened == 0)
                return generic.toString();
        }
        throw new IllegalStateException("could not found end to generic");
    }

    private static List<Integer> indicesOf(String s, char c) {
        return IntStream.range(0, s.length()).filter(i -> s.charAt(i) == c).boxed().collect(Collectors.toList());
    }

    @Override
    public abstract String toString();
}