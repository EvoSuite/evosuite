/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.idNaming;

import org.apache.commons.lang3.CharUtils;
import org.evosuite.Properties;
import org.evosuite.assertion.ArrayEqualsAssertion;
import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.CompareAssertion;
import org.evosuite.assertion.EqualsAssertion;
import org.evosuite.assertion.Inspector;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.assertion.NullAssertion;
import org.evosuite.assertion.PrimitiveAssertion;
import org.evosuite.assertion.PrimitiveFieldAssertion;
import org.evosuite.assertion.SameAssertion;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.testcase.ImportsTestCodeVisitor;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestVisitor;
import org.evosuite.testcase.fm.MethodDescriptor;
import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.statements.ClassPrimitiveStatement;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.EnumPrimitiveStatement;
import org.evosuite.testcase.statements.FieldStatement;
import org.evosuite.testcase.statements.FunctionalMockStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.NullStatement;
import org.evosuite.testcase.statements.PrimitiveExpression;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.statements.environment.EnvironmentDataStatement;
import org.evosuite.testcase.statements.environment.FileNamePrimitiveStatement;
import org.evosuite.testcase.statements.environment.LocalAddressPrimitiveStatement;
import org.evosuite.testcase.statements.environment.RemoteAddressPrimitiveStatement;
import org.evosuite.testcase.statements.environment.UrlPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.NumericalPrimitiveStatement;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericField;
import org.evosuite.utils.generic.GenericMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The ExplanatoryNamingTestVisitor is a visitor that produces a mapping of variable
 * references to String names.
 *
 * @author Jose Miguel Rojas
 */
public class ExplanatoryNamingTestVisitor extends TestVisitor {

    // mapping from variable reference to *list* of candidate variable names
    protected final Map<VariableReference,SortedSet<CandidateName>> varNamesCandidates = new HashMap<>();

    protected TestCase test = null;

    protected final ImportsTestCodeVisitor itv;

    protected final Map<String, Integer> indices = new HashMap<String, Integer>();

    public ExplanatoryNamingTestVisitor(ImportsTestCodeVisitor itv) {
        this.itv = itv;
    }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.evosuite.testcase.TestVisitor#visitTestCase(org.evosuite.testcase.TestCase)
	 */
    /** {@inheritDoc} */
    @Override
    public void visitTestCase(TestCase test) {
        this.test = test;
        this.varNamesCandidates.clear();
        this.indices.clear();
    }

    /**
     * <p>
     * visitPrimitiveAssertion
     * </p>
     *
     * @param assertion
     *            a {@link org.evosuite.assertion.PrimitiveAssertion} object.
     */
    protected void visitPrimitiveAssertion(PrimitiveAssertion assertion) {
        VariableReference source = assertion.getSource();
        Object value = assertion.getValue();

        if (value == null) {
            addCandidateName(source,CandidateSource.ASSERTION, "null" + getSimpleClassName(source));
        } else if(source.getVariableClass().equals(boolean.class) || source.getVariableClass().equals(Boolean.class)){
            String descriptor = getStringForPrimitiveValue(value);
            addCandidateName(source,CandidateSource.ASSERTION, "is" + descriptor);
        }
    }

    protected void visitArrayEqualsAssertion(ArrayEqualsAssertion assertion) {
        VariableReference source = assertion.getSource();
        addCandidateName(source,CandidateSource.ASSERTION, "checkedArray");
    }

    /**
     * <p>
     * visitPrimitiveFieldAssertion
     * </p>
     *
     * @param assertion
     *            a {@link org.evosuite.assertion.PrimitiveFieldAssertion}
     *            object.
     */
    protected void visitPrimitiveFieldAssertion(PrimitiveFieldAssertion assertion) {
        VariableReference source = assertion.getSource();
        Object value = assertion.getValue();
        Field field = assertion.getField();

        if (Modifier.isStatic(field.getModifiers()))
            return;

        String fn = capitalize(field.getName());
        if (value == null) {
            addCandidateName( source, CandidateSource.ASSERTION, "hasNullField" + fn);
        }  else {
            String descriptor = getStringForPrimitiveValue(value);
            addCandidateName( source, CandidateSource.ASSERTION, "hasField" + fn + "Equals" + descriptor);
        }
    }

    /**
     * <p>
     * visitInspectorAssertion
     * </p>
     *
     * @param assertion
     *            a {@link org.evosuite.assertion.InspectorAssertion} object.
     */
    protected void visitInspectorAssertion(InspectorAssertion assertion) {
        VariableReference source = assertion.getSource();
        Object value = assertion.getValue();
        Inspector inspector = assertion.getInspector();
	    String call = capitalize(inspector.getMethodCall());
        if (value == null) {
	        addCandidateName( source, CandidateSource.ASSERTION, "with" + call + "ReturningNull");
        } else if ((value.getClass().equals(Long.class))
                || (value.getClass().equals(Float.class))
                || (value.getClass().equals(Double.class))
                || (value.getClass().equals(Character.class))
                || (value.getClass().equals(String.class))
                || (value.getClass().isEnum() || value instanceof Enum)
                || (value.getClass().equals(boolean.class) || value.getClass().equals(Boolean.class))){
            String descriptor = getStringForPrimitiveValue(value);
	        addCandidateName( source, CandidateSource.ASSERTION, "with" + call + "Returning" + descriptor);
        } else
	        addCandidateName( source, CandidateSource.ASSERTION, "with" + call + "ReturningNonNull");
    }

    /**
     * <p>
     * visitNullAssertion
     * </p>
     *
     * @param assertion
     *            a {@link org.evosuite.assertion.NullAssertion} object.
     */
    protected void visitNullAssertion(NullAssertion assertion) {
        VariableReference source = assertion.getSource();
	    String prefix = ((Boolean) assertion.getValue()) ? "null" : "nonNull";
        addCandidateName( source, CandidateSource.ASSERTION, prefix + getSimpleClassName(source));
    }

    /**
     * <p>
     * visitCompareAssertion
     * </p>
     *
     * @param assertion
     *            a {@link org.evosuite.assertion.CompareAssertion} object.
     */
    protected void visitCompareAssertion(CompareAssertion assertion) {
        VariableReference source = assertion.getSource();
        VariableReference dest = assertion.getDest();
        addCandidateName( source, CandidateSource.ASSERTION, "usedInComparison");
        addCandidateName( dest, CandidateSource.ASSERTION, "usedInComparison");
    }

    /**
     * <p>
     * visitEqualsAssertion
     * </p>
     *
     * @param assertion
     *            a {@link org.evosuite.assertion.EqualsAssertion} object.
     */
    protected void visitEqualsAssertion(EqualsAssertion assertion) {
        VariableReference source = assertion.getSource();
        VariableReference dest = assertion.getDest();
        addCandidateName( source, CandidateSource.ASSERTION, "usedInEquals");
        addCandidateName( dest, CandidateSource.ASSERTION, "usedInEquals");
    }

    /**
     * <p>
     * visitSameAssertion
     * </p>
     *
     * @param assertion
     *            a {@link org.evosuite.assertion.SameAssertion} object.
     */
    protected void visitSameAssertion(SameAssertion assertion) {
        VariableReference source = assertion.getSource();
        VariableReference dest = assertion.getDest();
        addCandidateName( source, CandidateSource.ASSERTION, "usedInComparison");
        addCandidateName( dest, CandidateSource.ASSERTION, "usedInComparison");
    }

    protected void visitAssertion(Assertion assertion) {
        if (assertion instanceof PrimitiveAssertion) {
            visitPrimitiveAssertion((PrimitiveAssertion) assertion);
        } else if (assertion instanceof PrimitiveFieldAssertion) {
            visitPrimitiveFieldAssertion((PrimitiveFieldAssertion) assertion);
        } else if (assertion instanceof InspectorAssertion) {
            visitInspectorAssertion((InspectorAssertion) assertion);
        } else if (assertion instanceof NullAssertion) {
            visitNullAssertion((NullAssertion) assertion);
        } else if (assertion instanceof CompareAssertion) {
            visitCompareAssertion((CompareAssertion) assertion);
        } else if (assertion instanceof EqualsAssertion) {
            visitEqualsAssertion((EqualsAssertion) assertion);
        } else if (assertion instanceof SameAssertion) {
            visitSameAssertion((SameAssertion) assertion);
        } else if (assertion instanceof ArrayEqualsAssertion) {
            visitArrayEqualsAssertion((ArrayEqualsAssertion) assertion);
        } else {
            // do nothing
        }
    }

    private void visitAssertions(Statement statement) {
        if (getException(statement) != null) {
            // Assumption: The statement that throws an exception is the last statement of a test.
            VariableReference returnValue = statement.getReturnValue();
            for (Assertion assertion : statement.getAssertions()) {
                if (assertion != null
                        && !assertion.getReferencedVariables().contains(returnValue)) {
                    visitAssertion(assertion);
                }
            }
        } else {
            for (Assertion assertion : statement.getAssertions()) {
                if (assertion != null) {
                    visitAssertion(assertion);
                }
            }
        }
    }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.evosuite.testcase.TestVisitor#visitPrimitiveStatement(org.evosuite.testcase.PrimitiveStatement)
	 */
    /** {@inheritDoc} */
    @Override
    public void visitPrimitiveStatement(PrimitiveStatement<?> statement) {
        VariableReference retval = statement.getReturnValue();
        Object value = statement.getValue();
        String strVal = "";
        if (statement instanceof StringPrimitiveStatement) {
            // will be inlined
            if(value == null)
                strVal = "nullString";
            else
                strVal = ((String) value).isEmpty() ? "emptyString" : "nonEmptyString";
        } else if (statement instanceof EnvironmentDataStatement) {
            if (statement instanceof FileNamePrimitiveStatement)
                strVal = "fileName";
            else if (statement instanceof LocalAddressPrimitiveStatement)
                strVal = "localAddress";
            else if (statement instanceof RemoteAddressPrimitiveStatement)
                strVal = "remoteAddress";
            else if (statement instanceof UrlPrimitiveStatement)
                strVal = "url";
            else
                strVal = "envData";
        } else if (statement instanceof ClassPrimitiveStatement) {
            strVal = "clazz";
        } else if (statement instanceof NumericalPrimitiveStatement) {
            // will be inlined
            strVal = "const_" + String.valueOf(value);
        } else if (statement instanceof NullStatement) {
	        String className = getSimpleClassName(retval);
            strVal = "null" + className;
        } else if (statement instanceof EnumPrimitiveStatement) {
	        strVal = "const_" + String.valueOf(value);
        }

        addCandidateName( retval, CandidateSource.PRIMITIVE_STATEMENT, strVal, countKilledMutants(statement));

        visitAssertions(statement);
    }

    /** {@inheritDoc} */
    @Override
    public void visitPrimitiveExpression(PrimitiveExpression statement) {
        VariableReference retval = statement.getReturnValue();
	    addCandidateName( retval, CandidateSource.PRIMITIVE_EXPRESSION, "gets" + capitalize(statement.getOperator().name()), countKilledMutants(statement));

        visitAssertions(statement);
    }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.evosuite.testcase.TestVisitor#visitFieldStatement(org.evosuite.testcase.FieldStatement)
	 */
    /** {@inheritDoc} */
    @Override
    public void visitFieldStatement(FieldStatement statement) {
        Throwable exception = getException(statement);

        VariableReference retval = statement.getReturnValue();
        GenericField field = statement.getField();
        String fieldName = capitalize(field.getName());
        String castStr = (retval.isAssignableFrom(field.getFieldType())) ? "" : "Cast";
        String excStr = (exception == null) ? "" : "WithExc";
        String statStr = (field.isStatic()) ? "Static" : "";
        addCandidateName( retval, CandidateSource.FIELD_STATEMENT, "gets" + castStr + statStr + fieldName + excStr, countKilledMutants(statement));

        visitAssertions(statement);
    }

    private void handleParameters(Type[] parameterTypes, List<VariableReference> parameters,
                                  int startPos, String suffix, int nKilledMutants) {
        for (int i = startPos; i < parameters.size(); i++) {
            Type declaredParamType = parameterTypes[i];
            Type actualParamType = parameters.get(i).getType();
            if (parameters.get(i) instanceof ConstantValue)
                continue;

            String prefix = "used";
            if (! GenericClass.isAssignable(declaredParamType,actualParamType)) {
                prefix = "cast";
            }
            addCandidateName( parameters.get(i), CandidateSource.PARAMETER, prefix + suffix, nKilledMutants);
        }
    }


    @Override
    public void visitFunctionalMockStatement(FunctionalMockStatement st) {
        VariableReference retval = st.getReturnValue();
        addCandidateName( retval, CandidateSource.MOCK, "mock" + getSimpleClassName(retval));

        for(MethodDescriptor md : st.getMockedMethods()){
            if(md.shouldBeMocked()){
                for (VariableReference paramVar : st.getParameters(md.getID())) {
                    addCandidateName( paramVar, CandidateSource.MOCK, "usedToCallMock" + capitalize(md.getMethodName()));
                }
            }
        }
    }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.evosuite.testcase.TestVisitor#visitMethodStatement(org.evosuite.testcase.MethodStatement)
	 */
    /** {@inheritDoc} */
    @Override
    public void visitMethodStatement(MethodStatement statement) {
        VariableReference retval = statement.getReturnValue();
        GenericMethod method = statement.getMethod();
        Throwable exception = getException(statement);
        List<VariableReference> parameters = statement.getParameterReferences();

        String methodName = capitalize(method.getName());
        int nKilledMutants = countKilledMutants(statement);
        boolean unused = !Properties.ASSERTIONS ? exception != null : test != null
                && !test.hasReferences(retval);

        if (!retval.isVoid() && retval.getAdditionalVariableReference() == null && !unused) {
            String excStr = (exception == null) ? "" : "WithException";
            addCandidateName( retval, CandidateSource.METHOD_STATEMENT_RETURN, "resultFrom" + methodName + excStr, nKilledMutants);
        }

        VariableReference callee = statement.getCallee();
        if (!(callee instanceof ConstantValue)) {
            String prefix = "invokes";
            if(!callee.isAssignableTo(method.getMethod().getDeclaringClass())) {
                try {
                    // If the concrete callee class has that method then it's ok
                    callee.getVariableClass().getMethod(method.getName(), method.getRawParameterTypes());
                } catch(NoSuchMethodException e) {
                    prefix = "castToInvoke";
                }
            }
            addCandidateName( callee, CandidateSource.METHOD_STATEMENT_CALL, prefix + methodName, nKilledMutants);
        }

        int startPos = method.isStatic() ? 0 : 1;
        handleParameters(method.getParameterTypes(), parameters, startPos, "ToCall" + methodName, nKilledMutants);

        visitAssertions(statement);
    }

    private int countKilledMutants(Statement statement) {
        Set<Mutation> allKilledMutants = new LinkedHashSet<>();
        for (Assertion assertion : statement.getAssertions()) {
            allKilledMutants.addAll(assertion.getKilledMutations());
        }
        return allKilledMutants.size();
    }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.evosuite.testcase.TestVisitor#visitConstructorStatement(org.evosuite.testcase.ConstructorStatement)
	 */
    /** {@inheritDoc} */
    @Override
    public void visitConstructorStatement(ConstructorStatement statement) {
        GenericConstructor constructor = statement.getConstructor();
        VariableReference retval = statement.getReturnValue();
        Throwable exception = getException(statement);
        int nKilledMutants = countKilledMutants(statement);
        boolean isNonStaticMemberClass = constructor.getConstructor().getDeclaringClass().isMemberClass()
                && !constructor.isStatic()
                && !Modifier.isStatic(constructor.getConstructor().getDeclaringClass().getModifiers());

        List<VariableReference> parameters = statement.getParameterReferences();

        String className = capitalize(getSimpleClassName(retval));
        String excStr = (exception == null) ? "" : "WithException";
        addCandidateName(retval, CandidateSource.CONSTRUCTOR, "new" + className + excStr, nKilledMutants);

        int startPos = isNonStaticMemberClass ? 1 : 0;
        Type[] parameterTypes = constructor.getParameterTypes();

        handleParameters(parameterTypes, parameters, startPos, "ToInitialize" + capitalize(className), nKilledMutants);

        visitAssertions(statement);
    }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.evosuite.testcase.TestVisitor#visitArrayStatement(org.evosuite.testcase.ArrayStatement)
	 */
    /** {@inheritDoc} */
    @Override
    public void visitArrayStatement(ArrayStatement statement) {
        VariableReference retval = statement.getReturnValue();

        String typeArrStr = (retval.getGenericClass().isGenericArray()) ? "generic" : getSimpleClassName(retval);
        addCandidateName( retval, CandidateSource.ARRAY_STATEMENT, typeArrStr + "Array", countKilledMutants(statement));

        visitAssertions(statement);
    }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.evosuite.testcase.TestVisitor#visitAssignmentStatement(org.evosuite.testcase.AssignmentStatement)
	 */
    /** {@inheritDoc} */
    @Override
    public void visitAssignmentStatement(AssignmentStatement statement) {
        VariableReference retval = statement.getReturnValue();
        VariableReference parameter = statement.getValue();
        int nKilledMutants = countKilledMutants(statement);
        addCandidateName(retval,CandidateSource.ASSIGNMENT_STATEMENT,"gets" + getSimpleClassName(parameter), nKilledMutants);
        if (!retval.getVariableClass().equals(parameter.getVariableClass())) {
	        addCandidateName(parameter,CandidateSource.ASSIGNMENT_STATEMENT,"castTo" + getSimpleClassName(retval), nKilledMutants);
        }

        visitAssertions(statement);
    }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.evosuite.testcase.TestVisitor#visitNullStatement(org.evosuite.testcase.NullStatement)
	 */
    /** {@inheritDoc} */
    @Override
    public void visitNullStatement(NullStatement statement) {
        VariableReference retval = statement.getReturnValue();
        addCandidateName(retval,CandidateSource.NULL_STATEMENT, "null" + capitalize(getSimpleClassName(retval)));
    }

    @Override
    public void visitStatement(Statement statement) {
        super.visitStatement(statement);
    }


    public String getSimpleClassName(VariableReference var) {

        if (var.isPrimitive()) {
            return var.getGenericClass().getUnboxedType().getSimpleName();
        }

        String className = var.getSimpleClassName();
        return className.substring(0, 1).toLowerCase()
                + className.substring(1).replace(".", "_").replace("[]", "");
    }
    /**
     * <p>
     * getVariableName
     * </p>
     *
     * @param variableName
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getFinalVariableName(String variableName) {

        if (!indices.containsKey(variableName)) {
            indices.put(variableName, 0);
        } else {
            int index = indices.get(variableName);
            indices.put(variableName, index + 1);
            variableName += index;
        }
        return variableName;
    }

    public void printAll() {
        System.out.println("CANDIDATE NAMES MAPPING");
        String format = "%-5s| %-10s| %s\n";
        System.out.printf(format, "test", "varRef", "candidateName");

        for (Map.Entry<VariableReference,SortedSet<CandidateName>> varEntry : varNamesCandidates.entrySet()) {
            VariableReference var = varEntry.getKey();
            varEntry.getValue().forEach((candidate) -> {
                System.out.printf(format, this.test.getID(), var, candidate.toString());
            });
        }
    }

	public Map<VariableReference,String> getAllVariableNames() {
        Map<VariableReference,String> variableNames = new HashMap<>();
        for (Map.Entry<VariableReference,SortedSet<CandidateName>> varEntry : varNamesCandidates.entrySet()) {
            VariableReference key = varEntry.getKey();
            SortedSet<CandidateName> candidates = varEntry.getValue();
            variableNames.put(key, getFinalVariableName(candidates.first().getName())); // ordered list of candidates
		}
        printAll();
		return variableNames;
	}

    /**
     * <p>
     * getException
     * </p>
     *
     * @param statement
     *            a {@link Statement} object.
     * @return a {@link Throwable} object.
     */
    public Throwable getException(Statement statement) {
        if (this.itv.getExceptions() != null && this.itv.getExceptions().containsKey(statement.getPosition()))
            return this.itv.getExceptions().get(statement.getPosition());

        return null;
    }

    /**
     * Make first letter upper case
     *
     * @param input
     * @return
     */
    private static String capitalize(String input) {
        final char[] buffer = input.toCharArray();
        buffer[0] = Character.toTitleCase(buffer[0]);
        return new String(buffer);
    }

    /**
     * <p>
     * getNumberString
     * </p>
     *
     * @param value
     *            a {@link java.lang.Object} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getStringForPrimitiveValue(Object value) {
        assert(value != null);
        if (value.getClass().equals(char.class)
                || value.getClass().equals(Character.class)) {
            if (CharUtils.isAsciiNumeric((Character) value))
                return "Numeric";
            else if (CharUtils.isAsciiAlpha((Character) value))
                return "Alpha";
            else
                return "NotAlphanumeric";
        } else if (value.getClass().equals(String.class)) {
            return ((String)value).isEmpty() ? "EmptyString" : "NonEmptyString";
        } else if (value.getClass().equals(float.class)
                || value.getClass().equals(Float.class)) {
            if (value.toString().equals("" + Float.NaN))
                return "NaN";
            else if (value.toString().equals("" + Float.NEGATIVE_INFINITY))
                return "NegativeInf";
            else if (value.toString().equals("" + Float.POSITIVE_INFINITY))
                return "PositiveInf";
            else
                return (((Float) value) < 0F) ? "Negative" : (((Float) value) == 0F) ? "Zero" : "Positive";
        } else if (value.getClass().equals(double.class)
                || value.getClass().equals(Double.class)) {
            if (value.toString().equals("" + Double.NaN))
                return "NaN";
            else if (value.toString().equals("" + Double.NEGATIVE_INFINITY))
                return "NegativeInf";
            else if (value.toString().equals("" + Double.POSITIVE_INFINITY))
                return "PositiveInf";
            else
                return (((Double) value) < 0.0) ? "Negative" : (((Double) value) == 0.0) ? "Zero" : "Positive";
        } else if (value.getClass().equals(long.class)
                || value.getClass().equals(Long.class)) {
            return (((Long) value) < 0) ? "Negative" : (((Long) value) == 0) ? "Zero" : "Positive";
        } else if (value.getClass().equals(byte.class)
                || value.getClass().equals(Byte.class)) {
            return (((Byte) value) < 0) ? "Negative" : (((Byte) value) == 0) ? "Zero" : "Positive";
        } else if (value.getClass().equals(short.class)
                || value.getClass().equals(Short.class)) {
            return (((Short) value) < 0) ? "Negative" : (((Short) value) == 0) ? "Zero" : "Positive";
        } else if (value.getClass().equals(int.class)
                || value.getClass().equals(Integer.class)) {
            int val = ((Integer) value).intValue();
            if (val == Integer.MAX_VALUE)
                return "MaxInt";
            else if (val == Integer.MIN_VALUE)
                return "MinInt";
            else
                return (((Integer) value) < 0) ? "Negative" : (((Integer) value) == 0) ? "Zero" : "Positive";
        } else if (value.getClass().isEnum() || value instanceof Enum) {
            return "EnumValue";
        } else if(value.getClass().equals(Boolean.class)) {
            return capitalize(Boolean.toString((Boolean) value));
        } else {
            // This should not happen
            assert(false);
            return value.toString();
        }
    }

    private void addCandidateName(VariableReference v, CandidateSource source, String name) {
        addCandidateName(v, source, name, 0);
    }

    private void addCandidateName(VariableReference v, CandidateSource source, String name, int nKilledMutants) {
        if (!varNamesCandidates.containsKey(v))
            varNamesCandidates.put(v, new TreeSet<>());

        varNamesCandidates.get(v).add(new CandidateName(source, name, nKilledMutants));
    }

    public enum CandidateSource {
        ASSERTION               (0),
        FIELD_STATEMENT         (1),
        PRIMITIVE_STATEMENT     (1),
        PRIMITIVE_EXPRESSION    (1),
        ARRAY_STATEMENT         (1),
        ASSIGNMENT_STATEMENT    (2),
        MOCK                    (2),
        NULL_STATEMENT          (3),
        CONSTRUCTOR             (3),
        METHOD_STATEMENT_CALL   (4),
        METHOD_STATEMENT_RETURN (5),
        PARAMETER               (5);

        private final int priority;
        CandidateSource(int priority) {
            this.priority = priority;
        }
    }

    class CandidateName implements Comparable<CandidateName> {
        private CandidateSource source;
        private String name;
        private int nKilledMutants;

        public CandidateName(CandidateSource source, String name, int nKilledMutants) {
            this.source = source;
            this.name = name;
            this.nKilledMutants = nKilledMutants;
        }

        public CandidateSource getSource() {
            return source;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Candidate{" +
                    "name='" + getName() + '\'' +
                    ", source='" + getSource().name() + '\'' +
                    ", killed=" + nKilledMutants + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CandidateName that = (CandidateName) o;

            if (source != null ? !source.equals(that.source) : that.source != null) return false;
            if (nKilledMutants != that.nKilledMutants) return false;
            return name != null ? name.equals(that.name) : that.name == null;

        }

        @Override
        public int hashCode() {
            int result = source != null ? source.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + nKilledMutants;
            return result;
        }

        @Override
        public int compareTo(CandidateName o) {
            if (this == o) return 0;
            if (o == null) return 1;
            if (o.nKilledMutants == this.nKilledMutants) {
                return Integer.compare(o.source.priority, this.source.priority); // reverse comparison
            } else
                return Integer.compare(o.nKilledMutants, this.nKilledMutants); // reverse comparison
        }

    }

}