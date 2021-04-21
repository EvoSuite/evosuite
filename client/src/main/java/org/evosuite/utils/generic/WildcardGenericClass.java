package org.evosuite.utils.generic;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.Properties;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.seeding.CastClassManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WildcardGenericClass extends AbstractGenericClass<WildcardType> implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(WildcardGenericClass.class);

    public WildcardGenericClass(WildcardType type, Class<?> rawClass) {
        super(type, rawClass);
    }

    @Override
    public boolean changeClassLoader(ClassLoader loader) {
        try {
            if (rawClass != null) {
                rawClass = GenericClassUtils.getClass(rawClass.getName(), loader);
            }
            if (type != null) {
                Type[] oldUpperBounds = type.getUpperBounds();
                Type[] oldLowerBounds = type.getLowerBounds();

                Function<Type, Type> changeClassLoaderOfType = (Type type1) -> {
                    GenericClass<?> genericClass = GenericClassFactory.get(type1);
                    genericClass.changeClassLoader(loader);
                    return genericClass.getType();
                };
                Type[] upperBounds = Arrays.stream(oldUpperBounds).map(changeClassLoaderOfType).toArray(Type[]::new);
                Type[] lowerBounds = Arrays.stream(oldLowerBounds).map(changeClassLoaderOfType).toArray(Type[]::new);
                this.type = new WildcardTypeImpl(upperBounds, lowerBounds);
            } else {
                // TODO what to do if type == null?
                throw new IllegalStateException("Type of generic class is null. Don't know what to do.");
            }
            return true;
        } catch (ClassNotFoundException | SecurityException e) {
            logger.warn("Class not found: " + rawClass + " - keeping old class loader ", e);
        }
        return false;
    }

    @Override
    public Collection<GenericClass<?>> getGenericBounds() {
        List<Type> types = Arrays.asList(type.getUpperBounds());
        types.addAll(Arrays.asList(type.getLowerBounds()));
        return types.stream().map(GenericClassFactory::get).collect(Collectors.toList());
    }

    @Override
    public GenericClass<?> getGenericInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {

        logger.debug("Instantiation " + toString() + " with type map " + typeMap);
        if (recursionLevel > Properties.MAX_GENERIC_DEPTH) {
            logger.debug("Nothing to replace: " + toString() + ", " + isRawClass() + ", " + hasWildcardOrTypeVariables());
            return GenericClassFactory.get(this);
        }

        logger.debug("Is wildcard type.");
        return getGenericWildcardInstantiation(typeMap, recursionLevel);
    }

    @Override
    public AbstractGenericClass<WildcardType> getOwnerType() {
        throw new UnsupportedOperationException("A wildcard type has no owner type");
    }

    @Override
    public List<Type> getParameterTypes() {
        return Collections.emptyList();
    }

    @Override
    public String getTypeName() {
        return type.toString();
    }

    @Override
    public List<TypeVariable<?>> getTypeVariables() {
        return Collections.emptyList();
    }

    @Override
    public Class<?> getUnboxedType() {
        return rawClass;
    }

    @Override
    public GenericClass<?> getWithComponentClass(GenericClass<?> componentClass) {
        return new WildcardGenericClass(type, rawClass);
    }

    @Override
    public boolean hasOwnerType() {
        return false;
    }

    @Override
    public boolean hasWildcardOrTypeVariables() {
        return true;
    }

    @Override
    public boolean hasTypeVariables() {
        return false;
    }

    @Override
    public boolean hasWildcardTypes() {
        return true;
    }

    @Override
    public boolean isGenericArray() {
        return false;
    }

    @Override
    public boolean isParameterizedType() {
        return false;
    }

    @Override
    public boolean isRawClass() {
        return false;
    }

    @Override
    public boolean isTypeVariable() {
        return false;
    }

    @Override
    public boolean isWildcardType() {
        return true;
    }

    @Override
    public GenericClass<?> getGenericWildcardInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {
        GenericClass<?> selectedClass = CastClassManager.getInstance().selectCastClass(type,
                recursionLevel < Properties.MAX_GENERIC_DEPTH, typeMap);
        return selectedClass.getGenericInstantiation(typeMap, recursionLevel + 1);
    }

    @Override
    protected Map<TypeVariable<?>, Type> computeTypeVariableMapIfTypeVariable() {
        return Collections.emptyMap();
    }

    @Override
    protected Map<TypeVariable<?>, Type> updateInheritedTypeVariables(Map<TypeVariable<?>, Type> typeMap) {
        return typeMap;
    }

    @Override
    boolean canBeInstantiatedTo(TypeVariableGenericClass otherType) {
        Class<?> otherRawClass = otherType.getRawClass();
        if (otherRawClass.isAssignableFrom(rawClass)) {
            Map<TypeVariable<?>, Type> typeMap = otherType.getTypeVariableMap();
            try {
                GenericClass<?> instantiation = getGenericInstantiation(typeMap);
                return equals(instantiation) ? !hasWildcardOrTypeVariables() :
                        instantiation.canBeInstantiatedTo(otherType);
            } catch (ConstructionFailedException e) {
                logger.debug("Failed to instantiate " + toString());
                return false;
            }
        }
        return false;
    }

    @Override
    boolean canBeInstantiatedTo(WildcardGenericClass otherType) {
        try {
            if (otherType.isGenericSuperTypeOf(this)) {
                return true;
            }
        } catch (RuntimeException e) {
            // FIXME: GentyRef sometimes throws:
            // java.lang.RuntimeException: not implemented: class sun.reflect.generics.reflectiveObjects
            // .TypeVariableImpl
            // While I have no idea why, it should be safe to proceed if we can ignore this type
//            return false;
            throw new IllegalStateException("GentryRef threw an exception", e);
        }
        Class<?> otherRawClass = otherType.getRawClass();
        if (otherRawClass.isAssignableFrom(rawClass)) {
            Map<TypeVariable<?>, Type> typeMap = otherType.getTypeVariableMap();
            try {
                GenericClass<?> instantiation = getGenericInstantiation(typeMap);
                return equals(instantiation) ? !hasWildcardOrTypeVariables() :
                        instantiation.canBeInstantiatedTo(otherType);
            } catch (ConstructionFailedException e) {
                logger.debug("Failed to instantiate " + toString());
                return false;
            }
        }
        return false;
    }

    @Override
    boolean canBeInstantiatedTo(GenericArrayGenericClass otherType) {
        try {
            if (otherType.isGenericSuperTypeOf(this)) {
                return true;
            }
        } catch (RuntimeException e) {
            // FIXME: GentyRef sometimes throws:
            // java.lang.RuntimeException: not implemented: class sun.reflect.generics.reflectiveObjects
            // .TypeVariableImpl
            // While I have no idea why, it should be safe to proceed if we can ignore this type
//            return false;
            throw new IllegalStateException("GentryRef threw an exception", e);
        }
        Class<?> otherRawClass = otherType.getRawClass();
        if (otherRawClass.isAssignableFrom(rawClass)) {
            Map<TypeVariable<?>, Type> typeMap = otherType.getTypeVariableMap();
            try {
                GenericClass<?> instantiation = getGenericInstantiation(typeMap);
                return equals(instantiation) ? !hasWildcardOrTypeVariables() :
                        instantiation.canBeInstantiatedTo(otherType);
            } catch (ConstructionFailedException e) {
                logger.debug("Failed to instantiate " + toString());
                return false;
            }
        }
        return false;
    }

    @Override
    boolean canBeInstantiatedTo(RawClassGenericClass otherType) {
        try {
            if (otherType.isGenericSuperTypeOf(this)) {
                return true;
            }
        } catch (RuntimeException e) {
            // FIXME: GentyRef sometimes throws:
            // java.lang.RuntimeException: not implemented: class sun.reflect.generics.reflectiveObjects
            // .TypeVariableImpl
            // While I have no idea why, it should be safe to proceed if we can ignore this type
//                return false;
            throw new IllegalStateException("GentryRef threw an exception", e);
        }
        Class<?> otherRawClass = otherType.getRawClass();
        if (otherRawClass.isAssignableFrom(rawClass)) {
            Map<TypeVariable<?>, Type> typeMap = otherType.getTypeVariableMap();
            try {
                GenericClass<?> instantiation = getGenericInstantiation(typeMap);
                return equals(instantiation) ? !hasWildcardOrTypeVariables() :
                        instantiation.canBeInstantiatedTo(otherType);
            } catch (ConstructionFailedException e) {
                logger.debug("Failed to instantiate " + toString());
                return false;
            }
        }

        return false;
    }

    @Override
    boolean canBeInstantiatedTo(ParameterizedGenericClass otherType) {
        try {
            if (otherType.isGenericSuperTypeOf(this)) {
                return true;
            }
        } catch (RuntimeException e) {
            // FIXME: GentyRef sometimes throws:
            // java.lang.RuntimeException: not implemented: class sun.reflect.generics.reflectiveObjects
            // .TypeVariableImpl
            // While I have no idea why, it should be safe to proceed if we can ignore this type
//            return false;
            throw new IllegalStateException("GentryRef threw an exception", e);
        }
        Class<?> otherRawClass = otherType.getRawClass();
        if (otherRawClass.isAssignableFrom(rawClass)) {
            Map<TypeVariable<?>, Type> typeMap = otherType.getTypeVariableMap();
            if (otherType.isParameterizedType())
                typeMap.putAll(TypeUtils.determineTypeArguments(rawClass, otherType.getType()));
            try {
                GenericClass<?> instantiation = getGenericInstantiation(typeMap);
                return equals(instantiation) ? !hasWildcardOrTypeVariables() :
                        instantiation.canBeInstantiatedTo(otherType);
            } catch (ConstructionFailedException e) {
                logger.debug("Failed to instantiate " + toString());
                return false;
            }
        }

        return false;
    }




    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        logger.warn("reading");
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        URL cpURL = new File(Properties.CP).toURI().toURL();
        if (!(contextClassLoader instanceof URLClassLoader) || !Arrays.asList(((URLClassLoader) contextClassLoader).getURLs()).contains(cpURL)) {
            URL[] urls;
            urls = new URL[]{cpURL};
            URLClassLoader urlClassLoader = new URLClassLoader(urls, contextClassLoader);
            Thread.currentThread().setContextClassLoader(urlClassLoader);
        }

        String name = (String) ois.readObject();
        if(name == null){
            this.rawClass = null;
            this.type = null;
            return;
        }
        this.rawClass = GenericClassUtils.getClassByFullyQualifiedName(name);
        List<String> uBounds = (List<String>) ois.readObject();
        List<String> lBounds = (List<String>) ois.readObject();
        Type[] upperBounds = new Type[uBounds.size()];
        Type[] lowerBounds = new Type[lBounds.size()];
        for (int i = 0; i < uBounds.size(); i++)
            upperBounds[i] = GenericClassUtils.getClassByFullyQualifiedName(uBounds.get(i));
        for (int i = 0; i < lBounds.size(); i++)
            lowerBounds[i] = GenericClassUtils.getClassByFullyQualifiedName(lBounds.get(i));
        type = AbstractGenericClass.wildcardType(upperBounds, lowerBounds);
        logger.warn("Reading - Finished");
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        logger.warn("writing");
        if(rawClass == null)
            oos.writeObject(null);
        else {
            oos.writeObject(rawClass.getName());
            Type[] upperBounds = type.getUpperBounds();
            List<String> uBounds = Arrays.stream(upperBounds).map(Type::getTypeName).collect(Collectors.toList());
            oos.writeObject(uBounds);
            Type[] lowerBounds = type.getLowerBounds();
            List<String> lBounds = Arrays.stream(lowerBounds).map(Type::getTypeName).collect(Collectors.toList());
            oos.writeObject(lBounds);
        }
        logger.warn("Writing - Finished");
    }
}
