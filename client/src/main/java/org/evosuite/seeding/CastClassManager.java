/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.seeding;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.operators.selection.RankSelection;
import org.evosuite.setup.*;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericClassFactory;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.gentyref.GenericTypeReflector;

import static java.util.Comparator.comparingInt;

/**
 * Searches primarily for classes that are used in casts in the ByteCode,
 * because typically at casts, the java compiler did type erasure.
 */
public class CastClassManager {
    /*
    TODO - Check if the priority collections orders the elements correctly.
         - Remove classMap completely.
         - Validate the functionality of this class, e.g. We probably need tests, since the correctness of this class
            can be affected by future changes in the Java Language Specification / JVM.
     */

    private static final Logger logger = LoggerFactory.getLogger(CastClassManager.class);
    private final static List<Class<?>> SPECIAL_CASES = Arrays.asList(Comparable.class, Comparator.class,
            Iterable.class, Enum.class);
    private static CastClassManager instance = new CastClassManager();
//    /**
//     * The key is the generic class that can be used.
//     * The value is the priority to use this class for casts. The priority is used as secondary sorting criteria,
//     * when the number of type parameters are equal.
//     */
//    private final Map<GenericClass<?>, Integer> classMap = new LinkedHashMap<>();

    /**
     * Store the cast classes in a sorted data structure to prevent multiple sorts on the same set of classes.
     */
    private final PriorityCollection<GenericClass<?>> priorityCollection =
            new PriorityCollection<>(comparingInt(GenericClass::getNumParameters));

    // Private constructor due to singleton pattern, use getInstance() instead
    private CastClassManager() {
        initDefaultClasses();
    }

    public static CastClassManager getInstance() {
        return instance;
    }

//    /**
//     * Sorts the classes contained in the given map by the number of generic type parameters and, if
//     * this number is equal, by their "priority" as given by the map. The rationale is that classes
//     * with less generic parameter types are easier to instantiate. Such classes will be at the
//     * front of the returned list. If the number of type parameters is equal for two classes they
//     * will be sorted based on the priority retrieved form the given map, such that classes with
//     * lower priority values proceed those with higher ones.
//     *
//     * @param classPriorityMap assigns classes to priorities
//     * @return list of classes ordered by number of generic type parameters, and by priority if tied
//     */
//    public static List<GenericClass<?>> sortByValue(Map<GenericClass<?>, Integer> classPriorityMap) {
//        final List<GenericClass<?>> classes = new LinkedList<>(classPriorityMap.keySet());
//        final Comparator<GenericClass<?>> byNumParameters = comparingInt(GenericClass::getNumParameters);
//        final Comparator<GenericClass<?>> byPriority = comparingInt(classPriorityMap::get);
//        classes.sort(byNumParameters.thenComparing(byPriority));
//        return classes;
//    }

    /**
     * Chooses and returns a class among the given ones. Classes at the front of the list will
     * have a higher chance of being chosen than those at the back.
     *
     * @param candidates list of candidates to choose from
     * @return a candidate from the list of candidates
     */
    public static GenericClass<?> selectClass(List<GenericClass<?>> candidates) {
        return candidates.get(RankSelection.getIdx(candidates));
    }

    /**
     * Requests the concrete classes from the {@link ConcreteClassAnalyzer} for a given class.
     *
     * @param _class the class.
     * @return A collection containing {@param _class} and the concrete classes.
     */
    private static Collection<Class<?>> withConcreteClasses(Class<?> _class) {
        final InheritanceTree inheritanceTree = DependencyAnalysis.getInheritanceTree();
        Set<Class<?>> candidates = new HashSet<>(ConcreteClassAnalyzer.getInstance().getConcreteClasses(_class,
                inheritanceTree));
        candidates.add(_class);
        return candidates;
    }

    /**
     * Add a cast class to this manager.
     * The class is loaded with the class loader from the {@link TestGenerationContext}.
     * <p>
     * {@param className} is the binary name of the class as specified by the Java Language Specification.
     * <p>
     * From the oracle documentation of binary names:
     * Examples of valid class names include:
     * <p>
     * "java.lang.String"
     * "javax.swing.JSpinner$DefaultEditor"
     * "java.security.KeyStore$Builder$FileBuilder$1"
     * "java.net.URLClassLoader$3$1"
     *
     * @param className The binary name of the class
     * @param depth     The secondary sorting criteria for the cast classes.
     */
    public void addCastClass(String className, int depth) {
        final ClassLoader cl = TestGenerationContext.getInstance().getClassLoaderForSUT();
        final Class<?> clazz;
        try {
            clazz = cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            // Ignore
            logger.debug("Error including cast class " + className + " because: " + e);
            return;
        }
        final GenericClass<?> castClazz = GenericClassFactory.get(clazz);
        addCastClass(castClazz.getWithWildcardTypes(), depth);
    }

    /**
     * Converts the given {@param type} to a {@link GenericClass} and passes it
     * to {@link CastClassManager#addCastClass(GenericClass, int)}
     *
     * @param type  The type to be converted and added
     * @param depth The secondary sorting criteria for the cast classes.
     */
    public void addCastClass(Type type, int depth) {
        GenericClass<?> castClazz = GenericClassFactory.get(type);
        addCastClass(castClazz.getWithWildcardTypes(), depth);
    }

    /**
     * Adds a given {@link GenericClass} to this manager.
     * <p>
     * If the class is abstract, this method searches for concrete classes in the {@link InheritanceTree}.
     * {@link TestUsageChecker#canUse(Type)} is used to check whether EvoSuite can use the class in tests.
     *
     * @param clazz The class to be added to this cast class manager.
     * @param depth The secondary sorting criteria for the cast classes.
     */
    public void addCastClass(final GenericClass<?> clazz, final int depth) {
        final Class<?> rawClass = clazz.getRawClass();

        if (rawClass == null) {
            logger.warn("ADDING NULL!");
            assert (false);
        }

        // If we have an abstract class, try to find concrete subclasses we can use instead.
        if (clazz.isAbstract()) {
            final ConcreteClassAnalyzer analyzer = ConcreteClassAnalyzer.getInstance();
            final InheritanceTree tree = TestCluster.getInheritanceTree();
            final Set<Class<?>> concreteClasses = analyzer.getConcreteClasses(rawClass, tree);

            for (final Class<?> concreteClass : concreteClasses) {
                final GenericClass<?> c = GenericClassFactory.get(concreteClass);
                if (TestUsageChecker.canUse(c.getRawClass())) {
                    putCastClass(c, depth);
                }
            }

            // If mocking is enabled, we can simply mock the abstract class in the generated tests.
            if (Properties.P_FUNCTIONAL_MOCKING > 0.0) {
                if (TestUsageChecker.canUse(rawClass)) {
                    putCastClass(clazz, depth);
                }
            }
        } else if (TestUsageChecker.canUse(rawClass)) {
            putCastClass(clazz, depth);
        }
    }

//    public GenericClass<?> selectCastClass() {
//        final List<GenericClass<?>> assignableClasses = sortByValue(classMap);
//        return selectClass(assignableClasses);
//    }

    public GenericClass<?> selectCastClass(final TypeVariable<?> typeVariable, final boolean allowRecursion,
                                           final Map<TypeVariable<?>, Type> ownerVariableMap) {
        // TODO Make this function use the priority collection.
        List<GenericClass<?>> assignableClasses = getAssignableClasses(typeVariable, allowRecursion, ownerVariableMap);

        logger.debug("Assignable classes to " + typeVariable + ": " + assignableClasses);

        // FIXME: If we disallow recursion immediately, then we will never actually
        // do recursion since we always have Object, Integer, and String as candidates.
        // Including recursion may influence performance negatively.
        //
        // If we were not able to find an assignable class without recursive types
        // we try again but allowing recursion
//		if(assignableClasses.isEmpty()) {
//			assignableClasses = getAssignableClasses(typeVariable,
//                    allowRecursion,
//                    ownerVariableMap);
//		}

        // special case
        if (assignableClasses.isEmpty()) {
            logger.debug("Trying to add new cast class");
            if (addAssignableClass(typeVariable, ownerVariableMap)) {
                logger.debug("Now trying again to get a class");
                assignableClasses = getAssignableClasses(typeVariable, allowRecursion, ownerVariableMap);
                if (assignableClasses.isEmpty()) {
                    logger.debug("Nothing is assignable");
                    return null;
                }
            } else {
                logger.debug("Nothing is assignable");
                return null;
            }
        }
        logger.debug("Now we've got assignable classes: " + assignableClasses.size());

        return selectClass(assignableClasses);
    }

    public GenericClass<?> selectCastClass(final WildcardType wildcardType, final boolean allowRecursion,
                                           Map<TypeVariable<?>, Type> ownerVariableMap) throws ConstructionFailedException {
        logger.debug("Getting assignable classes for wildcard");
        List<GenericClass<?>> assignableClasses = getAssignableClasses(wildcardType, false, ownerVariableMap);
        logger.debug("Assignable classes to " + wildcardType + ": " + assignableClasses);

        // If we were not able to find an assignable class without recursive types
        // we try again but allowing recursion
        if (assignableClasses.isEmpty() && allowRecursion) {
            assignableClasses.addAll(getAssignableClasses(wildcardType, true, ownerVariableMap));
        }

        if (assignableClasses.isEmpty()) {
            logger.debug("Trying to add new cast class");

            if (addAssignableClass(wildcardType, ownerVariableMap)) {
                assignableClasses = getAssignableClasses(wildcardType, allowRecursion, ownerVariableMap);

                if (assignableClasses.isEmpty()) {
                    logger.debug("Nothing is assignable");
                    throw new ConstructionFailedException("Nothing is assignable to " + wildcardType);
                }
            } else {
                logger.debug("Making random choice because nothing is assignable");
                throw new ConstructionFailedException("Nothing is assignable to " + wildcardType);
            }
        }

        return selectClass(assignableClasses);
    }

    /**
     * Check if the manager has a cast class with a given name.
     *
     * @param className the name of the class.
     * @return Whether the class with the name is contained by this manager.
     */
    public boolean hasClass(final String className) {
        return priorityCollection.anyMatch(clazz -> clazz.getClassName().equals(className));
    }

    /**
     * Get a view on the contained classes.
     *
     * @return the view.
     */
    public Set<GenericClass<?>> getCastClasses() {
        return Collections.unmodifiableSet(priorityCollection.getElements());
    }

    /**
     * Clears all mappings
     */
    public void clear() {
//        classMap.clear();
        priorityCollection.clear();
        initDefaultClasses();
    }

    /**
     * Fills the class map with some default classes.
     */
    private void initDefaultClasses() {
        putCastClass(GenericClassFactory.get(Object.class), 0);
        putCastClass(GenericClassFactory.get(String.class), 1);
        putCastClass(GenericClassFactory.get(Integer.class), 1);
    }

//    private void handleComparable() {
//        // TODO
//        throw new UnsupportedOperationException("not yet implemented");
//    }
//
//    private void handleComparator() {
//        // TODO
//        throw new UnsupportedOperationException("not yet implemented");
//    }
//
//    private void handleEnum() {
//        // TODO
//        throw new UnsupportedOperationException("not yet implemented");
//    }
//
//    private void handleIterable() {
//        // TODO
//        throw new UnsupportedOperationException("not yet implemented");
//    }
//
//    /**
//     * True if this type variable is one of the java.* special cases
//     *
//     * @return Whether this is a special case
//     */
//    private boolean isSpecialCase(TypeVariable<?> typeVariable) {
//        return Arrays.stream(typeVariable.getBounds()).map(GenericTypeReflector::erase).anyMatch(SPECIAL_CASES::contains);
//    }
//
//    /**
//     * True if this wildcard type is one of the java.* special cases
//     *
//     * @return Whether this is a special case.
//     */
//    private boolean isSpecialCase(WildcardType wildcardType) {
//        throw new UnsupportedOperationException("not yet implemented");
//    }

    /**
     * Filters the analyzed classes of the test cluster for assignable classes.
     * <p>
     * Additionally to {@param filter}, only classes that can be used in test cases are returned
     * ({@link TestUsageChecker#canUse(Type)}).
     *
     * @param filter A predicate whether a class is assignable.
     * @return The set of classes that match the aforementioned criteria.
     */
    private Set<Class<?>> getAssignableClassesFromTestCluster(Predicate<Class<?>> filter) {
        // TODO why is this function deprecated? Because it is an accessor? Shall we replace it with a view and make it
        // 		not deprecated anymore
        final Set<Class<?>> classes = TestCluster.getInstance().getAnalyzedClasses();
        return classes.stream() //
                .filter(TestUsageChecker::canUse) //
                .filter(filter) //
                .collect(Collectors.toSet());
    }

    /**
     * Filters the values of {@param typeMap} for assignable classes.
     * <p>
     * Additionally to {@param filter}, only classes that can be used in test cases are returned
     * ({@link TestUsageChecker#canUse(Type)}.
     *
     * @param filter  A predicate whether a class is assignable.
     * @param typeMap the type map to be filtered.
     * @return The set of classes that match the aforementioned criteria.
     */
    private Set<Class<?>> getAssignableClassesFromTypeVariableMap(Predicate<Class<?>> filter, Map<TypeVariable<?>,
            Type> typeMap) {
        return typeMap.values().stream() //
                .filter(t -> !(t instanceof WildcardType)) //
                .map(GenericTypeReflector::erase) //
                .filter(TestUsageChecker::canUse) //
                .filter(filter) //
                .collect(Collectors.toSet());
    }

    /**
     * Convert given boundaries to a Pair<{@link GenericClass}, {@link Class}>, where the class is the erasure of the
     * generic class.
     * <p>
     * Before computing the resulting pair, the types are mapped with the function {@param replaceTypeVariable}
     * that shall replace type variables with their instantiation.
     * <p>
     * Additionally, a pair is only returned, if the erasure is usable in test cases according to
     * {@link TestUsageChecker#canUse(Type)}.
     *
     * @param bounds              The bounds to be converted.
     * @param replaceTypeVariable The function to replace type variables.
     * @return The set of pairs meeting the aforementioned criteria.
     */
    private Set<Pair<GenericClass<?>, Class<?>>> getBoundariesWithGenericClass(Type[] bounds,
                                                                               Function<Type, Type> replaceTypeVariable) {
        final Function<Type, Pair<Type, Class<?>>> getWithErasure = t -> Pair.of(t, GenericTypeReflector.erase(t));
        final Function<Pair<Type, Class<?>>, Pair<GenericClass<?>, Class<?>>> convertTypeToGenericClass =
                p -> Pair.of(GenericClassFactory.get(p.getLeft()), p.getRight());
        return Arrays.stream(bounds) //
                .map(replaceTypeVariable) //
                .map(getWithErasure) //
                .filter(p -> TestUsageChecker.canUse(p.getRight())) //
                .map(convertTypeToGenericClass) //
                .collect(Collectors.toSet());
    }

    /**
     * Select from a Collection of assignable classes one element and add it to the class map, if at least
     * one assignable class is in the collection.
     *
     * @param assignableClasses the collection of assignable classes.
     * @param priority          the priority stored in the class map.
     * @return Whether a class was added to the class map.
     */
    private boolean addToClassMapIfNotEmpty(Collection<Class<?>> assignableClasses, int priority) {
        if (!assignableClasses.isEmpty()) {
            final Class<?> choice = Randomness.choice(assignableClasses);
            final GenericClass<?> castClass = GenericClassFactory.get(choice);
            logger.debug("Adding cast class " + castClass);
            putCastClass(castClass, priority);
            return true;
        }
        return false;
    }

    /**
     * Compute the candidate instantiations from the boundaries of a {@link WildcardType}.
     * Type variables are replaced with their value stored in {@param typeMap} (if present).
     *
     * @param wildcardType The boundaries of this type are looked at.
     * @param typeMap      The type map for the instantiation.
     * @return A set of the candidate boundaries.
     */
    private Set<Pair<GenericClass<?>, Class<?>>> candidateBoundariesForWildcard(WildcardType wildcardType,
                                                                                Map<TypeVariable<?>, Type> typeMap) {
        final Function<Type, Type> replaceTypeVariable = t -> t instanceof TypeVariable && typeMap.containsKey(t) ?
                typeMap.get(t) : t;

        return getBoundariesWithGenericClass(wildcardType.getUpperBounds(), replaceTypeVariable);
    }

    /**
     * Filter a set of candidate boundaries for the ones with at least one type variable.
     * Additionally, only assignable candidates are returned.
     *
     * @param candidateBounds     the set of candidate bounds.
     * @param satisfiesBoundaries predicate to decide whether a {@link GenericClass} is assignable.
     * @return The assignable candidate bounds with at least one type variable.
     */
    private Set<Class<?>> onlyAssignableAllowTypeVariables(Set<Pair<GenericClass<?>, Class<?>>> candidateBounds,
                                                           Predicate<Pair<GenericClass<?>, Class<?>>> satisfiesBoundaries) {
        return candidateBounds.stream() //
                .filter(p -> p.getLeft().hasTypeVariables()) //
                .filter(satisfiesBoundaries) //
                .map(Pair::getRight) //
                .collect(Collectors.toSet());
    }

    /**
     * Filter a set of candidate boundaries for the ones without type variables.
     * A candidate bound is a Pair of {@link GenericClass} and the corresponding {@link Class}
     *
     * @param candidateBounds the set of candidate bounds.
     * @return The candidate bounds without type variables.
     */
    private Set<Pair<GenericClass<?>, Class<?>>> onlyAssignableForbidTypeVariables(Set<Pair<GenericClass<?>,
            Class<?>>> candidateBounds) {
        return candidateBounds.stream() //
                .filter(p -> !p.getLeft().hasTypeVariables()) //
                .collect(Collectors.toSet());
    }

    /**
     * Add an assignable class for a wildcard type for a given type variable map.
     * <p>
     * A type is only added to the classMap, if it is usable according to {@link TestUsageChecker#canUse(Type)}.
     *
     * @param wildcardType the wildcard type to be instantiated.
     * @param typeMap      the type map.
     * @return Whether an assignable class was added.
     */
    private boolean addAssignableClass(final WildcardType wildcardType, final Map<TypeVariable<?>, Type> typeMap) {
        // Predicate to decide if a class is assignable to the wildcard type
        final Predicate<Class<?>> isAssignableToWildcard =
                c -> GenericClassFactory.get(c).getWithWildcardTypes().satisfiesBoundaries(wildcardType, typeMap);


        // Filter what classes from the TestCluster can be assigned to the wildcard type
        Set<Class<?>> assignableClassesFromTestCluster = getAssignableClassesFromTestCluster(isAssignableToWildcard);
        final Set<Class<?>> assignableClasses = new LinkedHashSet<>(assignableClassesFromTestCluster);
        logger.debug("From the classes in the TestCluster {} are {} assignable.",
                TestCluster.getInstance().getAnalyzedClasses(), assignableClassesFromTestCluster);

        // Filter from the value set of the type variable map.
        Set<? extends Class<?>> assignableTypeVariables =
                getAssignableClassesFromTypeVariableMap(isAssignableToWildcard, typeMap);
        assignableClasses.addAll(assignableTypeVariables);
        logger.debug("From the type variables in the type variable map {} are {} assignable.", typeMap.values(),
                assignableTypeVariables);

        Set<Pair<GenericClass<?>, Class<?>>> candidateBounds = candidateBoundariesForWildcard(wildcardType, typeMap);

        // Compute boundaries with type variables that are assignable to the wildcard
        Set<Class<?>> assignableBoundariesWithTypeVariables = onlyAssignableAllowTypeVariables(candidateBounds,
                p -> p.getLeft().getWithWildcardTypes().satisfiesBoundaries(wildcardType, typeMap));

        // Compute boundaries without type variables that are assignable to the wildcard.
        Set<Pair<GenericClass<?>, Class<?>>> assignableBoundariesWithoutTypeVariables =
                onlyAssignableForbidTypeVariables(candidateBounds);

        logger.debug("From the upper bounds of the wildcard type {} are {} assignable and have type variables",
                Arrays.toString(wildcardType.getUpperBounds()), assignableBoundariesWithTypeVariables);
        logger.debug("From the upper bounds of the wildcard type {} are {} assignable and have no type variables. " + "Those are added directly", Arrays.toString(wildcardType.getUpperBounds()), assignableBoundariesWithoutTypeVariables);

        assignableClasses.addAll(assignableBoundariesWithoutTypeVariables.stream().map(Pair::getRight).collect(Collectors.toSet()));
        assignableClasses.addAll(assignableBoundariesWithTypeVariables);
        assignableBoundariesWithoutTypeVariables.stream().map(Pair::getLeft).forEach(gc -> putCastClass(gc, 10));

        logger.debug("Found assignable classes for wildcard type " + wildcardType + ": " + assignableClasses.size());

        // random selection of the assignable classes is added to class map with priority 10
        return addToClassMapIfNotEmpty(assignableClasses, 10);
    }

    /**
     * Check if a class is assignable to the type variable.
     * If the class is Parameterized type containing the type variable it is considered not assignable.
     *
     * @param typeVariable the type variable to be resolved.
     * @param clazz        the class to be checked.
     * @return Whether the class can be used as the type variable.
     */
    private boolean classIsAssignable(TypeVariable<?> typeVariable, Class<?> clazz) {
        for (final Type bound : typeVariable.getBounds()) {
            if (GenericTypeReflector.erase(bound).equals(Enum.class) && clazz.isEnum()) {
                continue;
            }

            if (!GenericClassUtils.isAssignable(bound, clazz)) {
                return false;
            }

            if (bound instanceof ParameterizedType) {
                final Type[] typeArgs = ((ParameterizedType) bound).getActualTypeArguments();
                if (Arrays.asList(typeArgs).contains(typeVariable)) return false;
            }
        }
        return true;
    }


    /**
     * Add an assignable class for a type variable for a given type variable map.
     * <p>
     * A type is only added to the classMap, if it is usable according to {@link TestUsageChecker#canUse(Type)}.
     *
     * @param typeVariable the type variable to be instantiated.
     * @param typeMap      the type map.
     * @return Whether an assignable class was added.
     */
    private boolean addAssignableClass(final TypeVariable<?> typeVariable, final Map<TypeVariable<?>, Type> typeMap) {
        // Predicate to decide if a class is assignable to the wildcard type
        final Predicate<Class<?>> satisfiesBoundaries =
                c -> GenericClassFactory.get(c).getWithWildcardTypes().satisfiesBoundaries(typeVariable, typeMap);

        // Filter what classes from the TestCluster can be assigned to the wildcard type
        Set<Class<?>> assignableClassesFromTestCluster = getAssignableClassesFromTestCluster(satisfiesBoundaries);
        logger.debug("From the classes in the TestCluster {} are {} assignable.",
                TestCluster.getInstance().getAnalyzedClasses(), assignableClassesFromTestCluster);

        final Set<Class<?>> assignableClasses = new LinkedHashSet<>(assignableClassesFromTestCluster);

        // Filter from the value set of the type variable map.
        Set<? extends Class<?>> assignableTypeVariables =
                getAssignableClassesFromTypeVariableMap(satisfiesBoundaries, typeMap);
        assignableClasses.addAll(assignableTypeVariables);
        logger.debug("From the type variables in the type variable map {} are {} assignable.", typeMap.values(),
                assignableTypeVariables);

        if (addToClassMapIfNotEmpty(assignableClasses, 10)) return true;

        // Compute the bound candidates of the type variable.
        final Set<Class<?>> boundCandidates = Arrays.stream(typeVariable.getBounds()) //
                .map(GenericTypeReflector::erase) //
                .map(CastClassManager::withConcreteClasses) //
                .flatMap(Collection::stream) //
                .collect(Collectors.toSet());

        logger.debug("Bound candidate for the type variable are: {}", boundCandidates);

        // Filter the bound candidates such that only the assignable remain.
        Set<Class<?>> assignableBoundCandidates = boundCandidates.stream() //
                .filter(TestUsageChecker::canUse) //
                .filter(c -> classIsAssignable(typeVariable, c)) //
                .collect(Collectors.toSet());
        assignableClasses.addAll(assignableBoundCandidates);

        logger.debug("After adding bounds, found " + assignableClasses.size() + " assignable classes for type " +
                "variable " + typeVariable + ": " + assignableClasses);

        // random selection of the assignable classes is added to class map with priority 10
        return addToClassMapIfNotEmpty(assignableClasses, 10);
    }

    /**
     * Search for the assignable classes for a wildcard type stored in this manager.
     * The elements are sorted first by the number of parameters, then by the assigned priority.
     *
     * @param wildcardType     The wildcard type to be resolved.
     * @param allowRecursion   Whether classes are allowed to contain type variables or wildcards.
     * @param ownerVariableMap A mapping from the type variable to the type of the owner.
     * @return Sorted list of classes being assignable (may be empty).
     */
    private List<GenericClass<?>> getAssignableClasses(final WildcardType wildcardType, final boolean allowRecursion,
                                                       final Map<TypeVariable<?>, Type> ownerVariableMap) {
        // Filter, whether a class is assignable.
        Predicate<GenericClass<?>> keepClass =
                gc -> gc.satisfiesBoundaries(wildcardType, ownerVariableMap) && (allowRecursion || !gc.hasWildcardOrTypeVariables());
        return priorityCollection.toSortedList(keepClass);
//        final Map<GenericClass<?>, Integer> assignableClasses = new LinkedHashMap<>();
//
//        logger.debug("Getting assignable classes for wildcard type " + wildcardType);
//        for (final Entry<GenericClass<?>, Integer> entry : classMap.entrySet()) {
//            final GenericClass<?> clazz = entry.getKey();
//            logger.debug("Current class for wildcard " + wildcardType + ": " + clazz);
//
//            if (!clazz.satisfiesBoundaries(wildcardType, ownerVariableMap)) {
//                logger.debug("Does not satisfy boundaries");
//                continue;
//            }
//
//            if (!allowRecursion && clazz.hasWildcardOrTypeVariables()) {
//                logger.debug("Stopping because of type recursion");
//                continue;
//            }
//            logger.debug("Is assignable");
//
//            final int priority = entry.getValue();
//            assignableClasses.put(clazz, priority);
//        }
//
//        return sortByValue(assignableClasses);
    }

    /**
     * Search for the assignable classes for a wildcard type stored in this manager.
     * The elements are sorted first by the number of parameters, then by the assigned priority.
     *
     * @param typeVariable     The type variable to be resolved.
     * @param allowRecursion   Whether classes are allowed to contain type variables or wildcards.
     * @param ownerVariableMap A mapping from the type variable to the type of the owner.
     * @return Sorted list of classes being assignable (may be empty).
     */
    private List<GenericClass<?>> getAssignableClasses(final TypeVariable<?> typeVariable,
                                                       final boolean allowRecursion,
                                                       final Map<TypeVariable<?>, Type> ownerVariableMap) {
        Predicate<GenericClass<?>> keepClass =
                gc -> gc.satisfiesBoundaries(typeVariable, ownerVariableMap) && (allowRecursion || !gc.hasWildcardOrTypeVariables());
        return priorityCollection.toSortedList(keepClass);
//        final Map<GenericClass<?>, Integer> assignableClasses = new LinkedHashMap<>();
//
//        logger.debug("Getting assignable classes for type variable " + typeVariable);
//        for (final Entry<GenericClass<?>, Integer> entry : classMap.entrySet()) {
//            final GenericClass<?> clazz = entry.getKey();
//            logger.debug("Current class for type variable " + typeVariable + ": " + clazz);
//
//            if (!clazz.satisfiesBoundaries(typeVariable, ownerVariableMap)) {
//                logger.debug("Bounds not satisfied");
//                continue;
//            }
//
//            if (!allowRecursion && clazz.hasWildcardOrTypeVariables()) {
//                logger.debug("Recursion not allowed but type has wildcard or type variables");
//                continue;
//            }
//
//            assignableClasses.put(entry.getKey(), entry.getValue());
//        }
//
//        logger.debug("Found assignable classes: " + assignableClasses.size());
//
//        return sortByValue(assignableClasses);
    }

    private void putCastClass(GenericClass<?> _class, int priority) {
//        classMap.put(_class, priority);
        priorityCollection.add(_class, priority);
    }

}
