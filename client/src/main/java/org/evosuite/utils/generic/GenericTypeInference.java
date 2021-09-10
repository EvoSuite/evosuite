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

package org.evosuite.utils.generic;

import com.googlecode.gentyref.GenericTypeReflector;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestVisitor;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;

/**
 * @author Gordon Fraser
 */
public class GenericTypeInference extends TestVisitor {

    private static final Logger logger = LoggerFactory.getLogger(GenericTypeInference.class);

    private final Map<VariableReference, Set<Type>> variableMap = new LinkedHashMap<>();

    private final Map<Type, Set<VariableReference>> typeMap = new LinkedHashMap<>();

    private TestCase test;

    public void inferTypes(TestCase test) {
        this.test = test;
        logger.debug("Inferring generic types");
        // calculateExactTypes();
        for (int i = test.size() - 1; i >= 0; i--) {
            Statement statement = test.getStatement(i);
            if (statement instanceof ConstructorStatement) {
                determineExactType((ConstructorStatement) statement);
            }
        }
        logger.debug("Resulting test: " + test.toCode());
    }

    private void addVariable(Statement statement) {
        VariableReference retVal = statement.getReturnValue();
        variableMap.put(retVal, new LinkedHashSet<>());
    }

    private void addTypeAssignment(Type type, VariableReference value) {
        if (!typeMap.containsKey(type)) {
            typeMap.put(type, new LinkedHashSet<>());
        }
        typeMap.get(type).add(value);
        variableMap.get(value).add(type);
    }

    protected void calculateExactTypes() {
        // For each type, if it is a parameterized type determine most specific instantiation
        logger.info("Types to consider: " + typeMap.size());
        for (Type type : typeMap.keySet()) {
            logger.info("Current type: " + type);
            if (type instanceof ParameterizedType)
                calculateExactType((ParameterizedType) type);
            else if (type instanceof WildcardType)
                calculateExactType((WildcardType) type);
            else if (type instanceof TypeVariable<?>)
                calculateExactType((TypeVariable<?>) type);
            else if (type instanceof GenericArrayType)
                calculateExactType((GenericArrayType) type);

        }
    }

    private void calculateExactType(ParameterizedType type) {
        logger.info("Calculating exact tyep for parameterized type " + type);
        Class<?> rawClass = GenericTypeReflector.erase(type);
        Type exactType = type;
        for (VariableReference var : typeMap.get(type)) {
            ParameterizedType currentType = (ParameterizedType) var.getType();
            logger.info("Assigned variable of type: " + currentType);
            Type candidateType = GenericTypeReflector.getExactSuperType(currentType,
                    rawClass);
            logger.info("Resulting type: " + candidateType);
            if (TypeUtils.isAssignable(candidateType, exactType)) {
                exactType = candidateType;
            }
        }
        logger.info("Result: " + exactType);
    }

    private void calculateExactType(WildcardType type) {
        logger.info("Calculating exact tyep for wildcard type " + type);

    }

    private void calculateExactType(GenericArrayType type) {
        logger.info("Calculating exact tyep for generic array type " + type);

    }

    private void calculateExactType(TypeVariable<?> type) {
        logger.info("Calculating exact type for typevariable " + type);

        Type[] bounds = TypeUtils.getImplicitBounds(type);
        Type exactType = bounds[0];
        for (VariableReference var : typeMap.get(type)) {
            Type candidateType = var.getType();

            logger.info("Candidate type: " + candidateType);
            if (TypeUtils.isAssignable(candidateType, exactType)) {
                exactType = candidateType;
            }
        }
        logger.info("Result: " + exactType);
    }

    private void addToMap(TypeVariable<?> type, Type actualType,
                          Map<TypeVariable<?>, Type> typeMap) {
        typeMap.put(type, actualType);
    }

    private void addToMap(ParameterizedType type, Type actualType,
                          Map<TypeVariable<?>, Type> typeMap) {
        Type[] parameterTypes = type.getActualTypeArguments();
        TypeVariable<?>[] variables = ((Class<?>) type.getRawType()).getTypeParameters();
        for (int i = 0; i < parameterTypes.length; i++) {
            typeMap.put(variables[i], parameterTypes[i]);
        }
    }

    private void addToMap(Type type, Type actualType, Map<TypeVariable<?>, Type> typeMap) {
        if (type instanceof ParameterizedType) {
            addToMap((ParameterizedType) type, actualType, typeMap);
        } else if (type instanceof TypeVariable<?>) {
            addToMap((TypeVariable<?>) type, actualType, typeMap);
        } else if (type instanceof GenericArrayType) {
            logger.info("Is generic array with component type " + ((GenericArrayType) type).getGenericComponentType());
            logger.info("Actual type " + actualType + ", " + actualType.getClass());
            if (actualType instanceof GenericArrayType) {
                addToMap(((GenericArrayType) type).getGenericComponentType(), ((GenericArrayType) actualType).getGenericComponentType(), typeMap);
            } else if (actualType instanceof Class<?> && ((Class<?>) actualType).isArray()) {
                addToMap(((GenericArrayType) type).getGenericComponentType(), ((Class<?>) actualType).getComponentType(), typeMap);
            }
        } else {
            logger.info("Is unexpected type: " + type + ", " + type.getClass());
        }
    }

    private Map<TypeVariable<?>, Type> getParameterType(Type parameterType, Type valueType) {
        Map<TypeVariable<?>, Type> typeMap = new LinkedHashMap<>();
        addToMap(parameterType, valueType, typeMap);
        return typeMap;
    }

    private void determineVariableFromParameter(VariableReference parameter,
                                                Type parameterType, Map<TypeVariable<?>, Type> typeMap) {
        Map<TypeVariable<?>, Type> parameterTypeMap = getParameterType(parameterType,
                parameter.getType());
        logger.info("Resulting map: " + parameterTypeMap);
        for (TypeVariable<?> typeVar : parameterTypeMap.keySet()) {
            Type actualType = parameterTypeMap.get(typeVar);
            if (typeMap.containsKey(typeVar)) {
                logger.info("Variable is in map: " + typeVar);
                Type currentType = typeMap.get(typeVar);
                if (currentType == null
                        || TypeUtils.isAssignable(actualType, currentType)) {
                    typeMap.put(typeVar, actualType);
                } else {
                    logger.info("Not assignable: " + typeVar + " with bounds "
                            + Arrays.asList(typeVar.getBounds()) + " and current type "
                            + currentType + " from " + actualType);
                    logger.info(""
                            + GenericTypeReflector.isSuperType(currentType, actualType));
                    logger.info("" + TypeUtils.isAssignable(actualType, typeVar));
                }
            } else {
                logger.debug("Variable is not in map: " + typeVar);
                typeMap.put(typeVar, actualType);
            }
        }
    }

    private void determineVariablesFromParameters(List<VariableReference> parameters,
                                                  Type[] parameterTypes, Map<TypeVariable<?>, Type> parameterTypeMap) {
        for (int i = 0; i < parameterTypes.length; i++) {
            logger.debug("Current parameter: " + parameterTypes[i]);
            Type parameterType = parameterTypes[i];
            VariableReference parameter = parameters.get(i);
            determineVariableFromParameter(parameter, parameterType, parameterTypeMap);
        }
    }

    private void determineExactType(ConstructorStatement constructorStatement) {
        GenericConstructor constructor = constructorStatement.getConstructor();
        logger.debug("Inferring types for: " + constructorStatement.getCode()
                + " at position " + constructorStatement.getPosition());

        Map<TypeVariable<?>, Type> typeMap = constructor.getOwnerClass().getTypeVariableMap();
        if (constructor.getOwnerClass().hasTypeVariables()) {
            // if (!typeMap.isEmpty()) {
            logger.info("Has types: " + constructor.getOwnerClass());
            logger.info("Initial type map: " + typeMap);
            for (TypeVariable<?> var : typeMap.keySet()) {
                typeMap.put(var, null);
            }
            Type[] parameterTypes = constructor.getGenericParameterTypes(); //.getParameterTypes();
            List<VariableReference> parameterValues = constructorStatement.getParameterReferences();
            determineVariablesFromParameters(parameterValues, parameterTypes, typeMap);

            for (int pos = constructorStatement.getPosition() + 1; pos < test.size(); pos++) {
                if (test.getStatement(pos) instanceof MethodStatement) {
                    MethodStatement ms = (MethodStatement) test.getStatement(pos);
                    if (ms.isStatic())
                        continue;
                    if (!ms.getCallee().equals(constructorStatement.getReturnValue()))
                        continue;

                    logger.info("Found relevant statement: " + ms.getCode());
                    parameterTypes = ms.getMethod().getGenericParameterTypes();
                    parameterValues = ms.getParameterReferences();
                    determineVariablesFromParameters(parameterValues, parameterTypes,
                            typeMap);
                }
            }
            logger.info("Setting types based on map: " + typeMap);
            GenericClass<?> owner = constructor.getOwnerClass();
            List<TypeVariable<?>> variables = owner.getTypeVariables();
            List<Type> types = new ArrayList<>();
            for (TypeVariable<?> var : variables) {
                Type type = typeMap.get(var);
                if (type == null) {
                    types.add(new WildcardTypeImpl(TypeUtils.getImplicitBounds(var),
                            new Type[]{}));
                } else {
                    Class<?> paramClass = GenericTypeReflector.erase(type);
                    if (paramClass.isPrimitive()) {
                        types.add(ClassUtils.primitiveToWrapper(paramClass));
                    } else {
                        types.add(typeMap.get(var));
                    }
                }
            }

            constructorStatement.setConstructor(constructor.copyWithNewOwner(owner.getWithParameterTypes(types)));
            logger.info("New type: " + constructorStatement);
            updateMethodCallsOfGenericOwner(constructorStatement.getReturnValue());
        } else {
            logger.info("Type map empty");

        }
    }

    private void updateMethodCallsOfGenericOwner(VariableReference callee) {
        for (int pos = callee.getStPosition() + 1; pos < test.size(); pos++) {
            Statement statement = test.getStatement(pos);
            if (!(statement instanceof MethodStatement))
                continue;
            MethodStatement ms = (MethodStatement) statement;
            if (ms.isStatic())
                continue;
            if (!ms.getCallee().equals(callee))
                continue;
            GenericMethod method = ms.getMethod();
            logger.info("Updating callee of statement " + statement.getCode());
            ms.setMethod(method.copyWithNewOwner(callee.getGenericClass()));
            ms.getReturnValue().setType(ms.getMethod().getReturnType());
            logger.info("Result: " + statement.getCode());

        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestVisitor#visitTestCase(org.evosuite.testcase.TestCase)
     */
    @Override
    public void visitTestCase(TestCase test) {
        this.test = test;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestVisitor#visitPrimitiveStatement(org.evosuite.testcase.PrimitiveStatement)
     */
    @Override
    public void visitPrimitiveStatement(PrimitiveStatement<?> statement) {
        // Primitives have no generic type
        addVariable(statement);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestVisitor#visitFieldStatement(org.evosuite.testcase.FieldStatement)
     */
    @Override
    public void visitFieldStatement(FieldStatement statement) {
        addVariable(statement);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestVisitor#visitMethodStatement(org.evosuite.testcase.MethodStatement)
     */
    @Override
    public void visitMethodStatement(MethodStatement statement) {
        addVariable(statement);
        GenericMethod method = statement.getMethod();
        List<VariableReference> parameterVariables = statement.getParameterReferences();

        Type[] genericParameterTypes = method.getGenericParameterTypes();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            Type genericType = genericParameterTypes[i];
            VariableReference value = parameterVariables.get(i);
            addTypeAssignment(genericType, value);
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestVisitor#visitConstructorStatement(org.evosuite.testcase.ConstructorStatement)
     */
    @Override
    public void visitConstructorStatement(ConstructorStatement statement) {
        addVariable(statement);

        GenericConstructor constructor = statement.getConstructor();
        List<VariableReference> parameterVariables = statement.getParameterReferences();

        Type[] genericParameterTypes = constructor.getGenericParameterTypes();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            Type genericType = genericParameterTypes[i];
            VariableReference value = parameterVariables.get(i);
            addTypeAssignment(genericType, value);
        }

        determineExactType(statement);

    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestVisitor#visitArrayStatement(org.evosuite.testcase.ArrayStatement)
     */
    @Override
    public void visitArrayStatement(ArrayStatement statement) {
        addVariable(statement);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestVisitor#visitAssignmentStatement(org.evosuite.testcase.AssignmentStatement)
     */
    @Override
    public void visitAssignmentStatement(AssignmentStatement statement) {
        addVariable(statement);

    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestVisitor#visitNullStatement(org.evosuite.testcase.NullStatement)
     */
    @Override
    public void visitNullStatement(NullStatement statement) {
        addVariable(statement);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestVisitor#visitPrimitiveExpression(org.evosuite.testcase.PrimitiveExpression)
     */
    @Override
    public void visitPrimitiveExpression(PrimitiveExpression primitiveExpression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitFunctionalMockStatement(FunctionalMockStatement functionalMockStatement) {

    }
}
