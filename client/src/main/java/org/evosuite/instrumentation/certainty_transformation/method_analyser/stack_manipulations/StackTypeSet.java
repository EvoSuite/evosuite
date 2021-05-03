package org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations;

import org.objectweb.asm.Type;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.objectweb.asm.Type.*;


public class StackTypeSet implements Serializable{

    private Set<Integer> types;

    public static final StackTypeSet VOID = new StackTypeSet(Type.VOID);
    public static final StackTypeSet INT = new StackTypeSet(Type.INT);
    public static final StackTypeSet BOOLEAN = new StackTypeSet(Type.BOOLEAN);
    public static final StackTypeSet BYTE = new StackTypeSet(Type.BYTE);
    public static final StackTypeSet DOUBLE = new StackTypeSet(Type.DOUBLE);
    public static final StackTypeSet LONG = new StackTypeSet(Type.LONG);
    public static final StackTypeSet FLOAT = new StackTypeSet(Type.FLOAT);
    public static final StackTypeSet ARRAY = new StackTypeSet(Type.ARRAY);
    public static final StackTypeSet OBJECT = new StackTypeSet(Type.OBJECT);
    public static final StackTypeSet CHAR = new StackTypeSet(Type.CHAR);
    public static final StackTypeSet SHORT = new StackTypeSet(Type.SHORT);
    public static final StackTypeSet NON_BOOLEAN = new StackTypeSet(Type.INT,Type.BYTE,Type.CHAR,Type.SHORT);
    public static final StackTypeSet TWO_SLOT_TYPES = new StackTypeSet(Type.DOUBLE, Type.FLOAT, Type.LONG);

    /**
     * Some common combinations
     */
    // Array or object for AStore etc.
    public static final StackTypeSet AO = StackTypeSet.of(Type.ARRAY, Type.OBJECT);
    // Two complement encoded number for IStore etc.
    public static final StackTypeSet TWO_COMPLEMENT = StackTypeSet.of(Type.INT, Type.BYTE, Type.CHAR, Type.BOOLEAN, Type.SHORT);
    // All possible
    public static final StackTypeSet ANY = StackTypeSet.of(Type.INT, Type.BOOLEAN, Type.BYTE, Type.DOUBLE, Type.LONG,
            Type.FLOAT, Type.ARRAY, Type.OBJECT, Type.CHAR, Type.SHORT);

    private StackTypeSet(Collection<? extends Integer> c) {
        types = Set.copyOf(c);
    }

    private StackTypeSet(Integer... args) {
        this(Arrays.asList(args));
    }

    public StackTypeSet(StackTypeSet stackTypeSet) {
        this(stackTypeSet.types);
    }

    public static StackTypeSet of(Collection<? extends Integer> c) {
        if(c.size() == 0)
            throw new IllegalArgumentException("A stack type must be of at least one type");
        return new StackTypeSet(c);
    }

    public static StackTypeSet ofMergeTypes(Type type){
        if(type.getDescriptor().equals("Ljava/lang/Object;"))
            return AO;
        if(Arrays.asList(INT_TYPE, BYTE_TYPE, CHAR_TYPE, SHORT_TYPE).contains(type))
            return TWO_COMPLEMENT;
        else
            return of(type.getSort());
    }

    public static StackTypeSet of(Integer singleType) {
        switch (singleType) {
            case Type.VOID:
                return VOID;
            case Type.INT:
                return INT;
            case Type.BOOLEAN:
                return BOOLEAN;
            case Type.BYTE:
                return BYTE;
            case Type.LONG:
                return LONG;
            case Type.FLOAT:
                return FLOAT;
            case Type.DOUBLE:
                return DOUBLE;
            case Type.ARRAY:
                return ARRAY;
            case Type.OBJECT:
                return OBJECT;
            case Type.CHAR:
                return CHAR;
            case Type.SHORT:
                return SHORT;
            default:
                throw new IllegalArgumentException("Unknown type: " + singleType);
        }
    }

    public static StackTypeSet of(Integer... types) {
        return new StackTypeSet(types);
    }

    @Deprecated
    public static StackTypeSet copy(StackTypeSet stackTypeSet) {
        return new StackTypeSet(stackTypeSet);
    }

    public boolean contains(Integer type) {
        return types.contains(type);
    }

    /**
     * Whether this intersects with {@param other} in at least one type
     *
     * @param other the other type set.
     * @return whether this intersects with {@param other} in at least one type
     */
    public boolean matches(StackTypeSet other) {
        return types.stream().anyMatch(other::contains);
    }

    /**
     * Computes the types contained by this and other
     *
     * @param other
     * @return The intersection between the sets of this and {@param other}
     */
    public Set<Integer> intersection(StackTypeSet other){
        return types.stream().filter(other::contains).collect(Collectors.toSet());
        /*Set<Integer> res = new HashSet<>(types);
        res.retainAll(other.types);
        return res;*/
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StackTypeSet union = (StackTypeSet) o;

        return types.equals(union.types);
    }

    @Override
    public int hashCode() {
        return types.hashCode();
    }

    static String type2String(Integer type){
        switch (type){
            case Type.VOID:
                return VOID_TYPE.getDescriptor();
            case Type.BOOLEAN:
                return BOOLEAN_TYPE.getDescriptor();
            case Type.CHAR:
                return CHAR_TYPE.getDescriptor();
            case Type.BYTE:
                return BYTE_TYPE.getDescriptor();
            case Type.SHORT:
                return SHORT_TYPE.getDescriptor();
            case Type.INT:
                return INT_TYPE.getDescriptor();
            case Type.FLOAT:
                return FLOAT_TYPE.getDescriptor();
            case Type.LONG:
                return LONG_TYPE.getDescriptor();
            case Type.DOUBLE:
                return DOUBLE_TYPE.getDescriptor();
            case Type.ARRAY:
            case Type.OBJECT:
            case METHOD:
                return "A";
        }
        throw new IllegalArgumentException("unknown Type " + type);
    }

    @Override
    public String toString() {
        return "TypeUnion{" +
                types.stream().map(StackTypeSet::type2String).sorted().collect(Collectors.joining("|")) +
                "}";
    }
}
