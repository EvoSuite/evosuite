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

import com.googlecode.gentyref.CaptureType;
import com.googlecode.gentyref.GenericTypeReflector;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
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
import org.evosuite.classpath.ResourceList;
import org.evosuite.runtime.mock.EvoSuiteMock;
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
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.NumberFormatter;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericField;
import org.evosuite.utils.generic.GenericMethod;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The VariableNamesTestVisitor is a visitor that produces a mapping of variable
 * references to String names.
 *
 * @author Jose Miguel Rojas
 */
public class VariableNamesTestVisitor extends TestVisitor {

    // mapping from variable reference to *list* of candidate variable names
    protected final Map<VariableReference,List<CandidateName>> varNamesCandidates = new HashMap<>();

    // final mapping from variable references to variable names
    protected final Map<VariableReference,String> varNamesFinal = new HashMap<>();

    protected TestCase test = null;

    protected final ImportsTestCodeVisitor itv;

    // From TestCodeVisitor
    protected final Map<VariableReference, String> variableNames = new HashMap<VariableReference, String>();
    protected final Map<String, Integer> nextIndices = new HashMap<String, Integer>();

    public VariableNamesTestVisitor(ImportsTestCodeVisitor itv) {
        this.itv = itv;
    }


    private void addCandidateName(TestCase tc, VariableReference v, String explanation, String name) {
        if (!varNamesCandidates.containsKey(v))
            varNamesCandidates.put(v,new LinkedList<>());
        varNamesCandidates.get(v).add(new CandidateName(explanation, name));
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
        this.variableNames.clear();
        this.nextIndices.clear();
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
            addCandidateName(test,source,"PrimitiveAssertion","null" + source.getClassName());
        } if(source.getVariableClass().equals(boolean.class) || source.getVariableClass().equals(Boolean.class)){
            Boolean flag = (Boolean) value;

            addCandidateName(test,source,"PrimitiveAssertion", "is" + Boolean.toString(flag));
        } else {
            addCandidateName(test,source,"PrimitiveAssertion", "equals" + NumberFormatter.getNumberString(value));
        }
    }

    protected void visitArrayEqualsAssertion(ArrayEqualsAssertion assertion) {
        VariableReference source = assertion.getSource();
        Object[] value = (Object[]) assertion.getValue();

        addCandidateName(test,source,"ArrayEqualsAssertion", "arrayChecked");
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

        if(Modifier.isStatic(field.getModifiers()))
            return;

        String f = field.getName();

        if (value == null) {
            addCandidateName(test, source, "PrimitiveFieldAssertion", "hasNullField" + WordUtils.capitalize(f));
        }  else if(value.getClass().equals(Boolean.class)) {
            Boolean flag = (Boolean) value;
            addCandidateName(test, source, "PrimitiveFieldAssertion", "hasField" + WordUtils.capitalize(f) + "Equals" + WordUtils.capitalize(String.valueOf(flag)));
        } else
            addCandidateName(test, source, "PrimitiveFieldAssertion", "hasField" + WordUtils.capitalize(f) + "Equals" + WordUtils.capitalize(NumberFormatter.getNumberString(value)));
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
	    String call = inspector.getMethodCall();
        if (value == null) {
	        addCandidateName(test, source, "InspectorAssertion", "with" + WordUtils.capitalize(call) + "ReturningNull");
        } else if (value.getClass().equals(Long.class)) {
	        addCandidateName(test, source, "InspectorAssertion", "with" + WordUtils.capitalize(call) + "ReturningLong" + WordUtils.capitalize(NumberFormatter.getNumberString(value)));
        } else if (value.getClass().equals(Float.class)) {
	        addCandidateName(test, source, "InspectorAssertion", "with" + WordUtils.capitalize(call) + "ReturningFloat" + WordUtils.capitalize(NumberFormatter.getNumberString(value)));
        } else if (value.getClass().equals(Double.class)) {
	        addCandidateName(test, source, "InspectorAssertion", "with" + WordUtils.capitalize(call) + "ReturningDouble" + WordUtils.capitalize(NumberFormatter.getNumberString(value)));
        } else if (value.getClass().equals(Character.class)) {
	        addCandidateName(test, source, "InspectorAssertion", "with" + WordUtils.capitalize(call) + "ReturningDouble" + WordUtils.capitalize(NumberFormatter.getNumberString(value)));
        } else if (value.getClass().equals(String.class)) {
	        addCandidateName(test, source, "InspectorAssertion", "with" + WordUtils.capitalize(call) + "ReturningString" + WordUtils.capitalize(NumberFormatter.getNumberString(value)));
        } else if (value.getClass().isEnum() || value instanceof Enum) {
	        addCandidateName(test, source, "InspectorAssertion", "with" + WordUtils.capitalize(call) + "ReturningValue" + WordUtils.capitalize(NumberFormatter.getNumberString(value)));
        } else if (value.getClass().equals(boolean.class) || value.getClass().equals(Boolean.class)) {
            if (((Boolean) value).booleanValue())
	            addCandidateName(test, source, "InspectorAssertion", "with" + WordUtils.capitalize(call) + "ReturningTrue");
            else
	            addCandidateName(test, source, "InspectorAssertion", "with" + WordUtils.capitalize(call) + "ReturningFalse");
        } else
	        addCandidateName(test, source, "InspectorAssertion", "with" + WordUtils.capitalize(call) + "Returning" + WordUtils.capitalize(value.toString()));
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
        Boolean value = (Boolean) assertion.getValue();

	    String prefix = value.booleanValue() ? "null" : "nonNull";

        addCandidateName(test, source, "NullAssertion",prefix + source.getClassName());
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
        Object value = assertion.getValue();
        if (source.getType().equals(Integer.class)) {
            if ((Integer) value == 0) {
	            addCandidateName(test, source, "CompareAssertion", "comparedEquals");
	            addCandidateName(test, dest, "CompareAssertion", "comparedEquals");
            } else if ((Integer) value < 0) {
	            addCandidateName(test, source, "CompareAssertion", "comparedLessThan");
	            addCandidateName(test, dest, "CompareAssertion", "comparedGreaterOrEquals");
            } else {
	            addCandidateName(test, source, "CompareAssertion", "comparedGreaterThan");
	            addCandidateName(test, dest, "CompareAssertion", "comparedLessOrEquals");
            }
        } else {
	        addCandidateName(test, source, "CompareAssertion", "compared");
	        addCandidateName(test, dest, "CompareAssertion", "compared");
        }
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
        Object value = assertion.getValue();
        if (source.isPrimitive() && dest.isPrimitive()) {
            if (((Boolean) value).booleanValue()) {
	            addCandidateName(test, source, "EqualsAssertion", "comparedEquals");
	            addCandidateName(test, dest, "EqualsAssertion", "comparedEquals");
            } else {
	            addCandidateName(test, source, "EqualsAssertion", "comparedDiff");
	            addCandidateName(test, dest, "EqualsAssertion", "comparedDiff");
            }
        } else {
            if (((Boolean) value).booleanValue()) {
	            addCandidateName(test, source, "EqualsAssertion", "comparedEquals");
	            addCandidateName(test, dest, "EqualsAssertion", "comparedEqualsCast");
            } else {
	            addCandidateName(test, source, "EqualsAssertion", "comparedDiff");
	            addCandidateName(test, dest, "EqualsAssertion", "comparedDiffCast");
            }
        }
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
        // ignore
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
        if (statement instanceof StringPrimitiveStatement) {
            if(value == null) {
                addCandidateName(test, retval, "PrimitiveStatement", "nullString");
            } else {
                String strVal = (String) value;
	            if (strVal.isEmpty())
                    addCandidateName(test, retval, "PrimitiveStatement", "emptyString");
	            else
		            addCandidateName(test, retval, "PrimitiveStatement", "nonEmptyString");
            }
        } else if (statement instanceof EnvironmentDataStatement) {
            if (statement instanceof FileNamePrimitiveStatement)
                addCandidateName(test, retval, "PrimitiveStatement", "fileName");
            else if (statement instanceof LocalAddressPrimitiveStatement)
                addCandidateName(test, retval, "PrimitiveStatement", "localAddress");
            else if (statement instanceof RemoteAddressPrimitiveStatement)
                addCandidateName(test, retval, "PrimitiveStatement", "remoteAddress");
            else if (statement instanceof UrlPrimitiveStatement)
                addCandidateName(test, retval, "PrimitiveStatement", "url");
            else
                addCandidateName(test, retval, "PrimitiveStatement", "envData");
        } else if (statement instanceof ClassPrimitiveStatement) {
            String className = getClassName(retval);
            className = className.replaceAll("Class<(.*)(<.*>)>", "Class<$1>");
            addCandidateName(test, retval, "PrimitiveStatement", "clazz" + className);
        } else if (statement instanceof NumericalPrimitiveStatement) {
	        // IntPrimitiveStatement
            addCandidateName(test, retval, "PrimitiveStatement", "const_" + String.valueOf(value));
        } else if (statement instanceof NullStatement) {
	        String className = getClassName(retval);
	        addCandidateName(test, retval, "PrimitiveStatement", "null" + className);
        } else if (statement instanceof EnumPrimitiveStatement) {
	        addCandidateName(test, retval, "PrimitiveStatement", "const_" + String.valueOf(value));
        }
        addCandidateName(test, retval, "Baseline", getVariableName(retval));
        visitAssertions(statement);
    }

    /** {@inheritDoc} */
    @Override
    public void visitPrimitiveExpression(PrimitiveExpression statement) {
        VariableReference retval = statement.getReturnValue();

	    addCandidateName(test, retval, "PrimitiveExpression", "gets" + WordUtils.capitalize(statement.getOperator().name()));

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

        String cast_str = "";
        StringBuilder builder = new StringBuilder();

        VariableReference retval = statement.getReturnValue();
        GenericField field = statement.getField();
        String testCode = "";
        if (!retval.isAssignableFrom(field.getFieldType())) {
            cast_str += "(" + getClassName(retval) + ")";
        }

        if (exception != null) {
            builder.append(getClassName(retval));
            builder.append(" ");
            builder.append(getVariableName(retval));
            builder.append(" = null;");
            builder.append("try {  ");
        } else {
            builder.append(getClassName(retval));
            builder.append(" ");
        }
        if (!field.isStatic()) {
            VariableReference source = statement.getSource();
            builder.append(getVariableName(retval));
            builder.append(" = ");
            builder.append(cast_str);
            builder.append(getVariableName(source));
            builder.append(".");
            builder.append(field.getName());
            builder.append(";");
        } else {
            builder.append(getVariableName(retval));
            builder.append(" = ");
            builder.append(cast_str);
            builder.append(getClassName(field.getField().getDeclaringClass()));
            builder.append(".");
            builder.append(field.getName());
            builder.append(";");
        }
        if (exception != null) {
            Class<?> ex = exception.getClass();
            while (!Modifier.isPublic(ex.getModifiers()))
                ex = ex.getSuperclass();
            builder.append("} catch(");
            builder.append(getClassName(ex));
            builder.append(" e) {}");
        }

        visitAssertions(statement);
    }

    private String getPrimitiveNullCast(Class<?> declaredParamType) {
        String castString = "";
        castString += "(" + getTypeName(declaredParamType) + ") ";
        castString += "(" + getTypeName(ClassUtils.primitiveToWrapper(declaredParamType))
                + ") ";

        return castString;
    }

    private void handleParameters(Type[] parameterTypes,
                                      List<VariableReference> parameters, boolean isGenericMethod,
                                      boolean isOverloaded, int startPos, String methodName) {
        for (int i = startPos; i < parameters.size(); i++) {
            Type declaredParamType = parameterTypes[i];
            Type actualParamType = parameters.get(i).getType();
            String name = getVariableName(parameters.get(i));
	        addCandidateName(test, parameters.get(i), "argumentTo", "usedToCall" + methodName);
            Class<?> rawParamClass = declaredParamType instanceof WildcardType ? Object.class : GenericTypeReflector.erase(declaredParamType);
            if (rawParamClass.isPrimitive() && name.equals("null")) {
	            addCandidateName(test, parameters.get(i), "argumentTo", "castTo" + getTypeName(rawParamClass));
            } else if (isGenericMethod && !(declaredParamType instanceof WildcardType )) {
                if (!declaredParamType.equals(actualParamType) || name.equals("null")) {
	                addCandidateName(test, parameters.get(i), "argumentTo", "castTo" + getTypeName(declaredParamType));
                }
            } else if (name.equals("null")) {
	            addCandidateName(test, parameters.get(i), "argumentTo", "castTo" + getTypeName(declaredParamType));
            } else if (!GenericClass.isAssignable(declaredParamType, actualParamType)) {

                if (TypeUtils.isArrayType(declaredParamType)
                        && TypeUtils.isArrayType(actualParamType)) {
                    Class<?> componentClass = GenericTypeReflector.erase(declaredParamType).getComponentType();
                    if (componentClass.equals(Object.class)) {
                        GenericClass genericComponentClass = new GenericClass(componentClass);
                        if (genericComponentClass.hasWildcardOrTypeVariables()) {
                            // If we are assigning a generic array, then we don't need to cast

                        } else {
                            // If we are assigning a non-generic array, then we do need to cast
	                        addCandidateName(test, parameters.get(i), "argumentTo", "castTo" + getTypeName(declaredParamType));
                        }
                    } else { //if (!GenericClass.isAssignable(GenericTypeReflector.getArrayComponentType(declaredParamType), GenericTypeReflector.getArrayComponentType(actualParamType))) {
	                    addCandidateName(test, parameters.get(i), "argumentTo", "castTo" + getTypeName(declaredParamType));
                    }
                } else if (!(actualParamType instanceof ParameterizedType)) {
	                addCandidateName(test, parameters.get(i), "argumentTo", "castTo" + getTypeName(declaredParamType));
                }
            } else {
                // We have to cast between wrappers and primitives in case there
                // are overloaded signatures. This could be optimized by checking
                // if there actually is a problem of overloaded signatures
                GenericClass parameterClass = new GenericClass(declaredParamType);
                if (parameterClass.isWrapperType() && parameters.get(i).isPrimitive()) {
	                addCandidateName(test, parameters.get(i), "argumentTo", "castTo" + getTypeName(declaredParamType));
                } else if (parameterClass.isPrimitive()
                        && parameters.get(i).isWrapperType()) {
	                addCandidateName(test, parameters.get(i), "argumentTo", "castTo" + getTypeName(declaredParamType));
                } else if (isOverloaded) {
                    // If there is an overloaded method, we need to cast to make sure we use the right version
                    if (!declaredParamType.equals(actualParamType)) {
	                    addCandidateName(test, parameters.get(i), "argumentTo", "castTo" + getTypeName(declaredParamType));
                    }
                }
            }
        }
    }


    @Override
    public void visitFunctionalMockStatement(FunctionalMockStatement st) {

        VariableReference retval = st.getReturnValue();

        // If it is not used, then minimizer will delete the statement anyway
//		boolean unused = test!=null && !test.hasReferences(retval);
//		if(unused){
//			//no point whatsoever in creating a mock that is never used
//			return;
//		}

        String result = "";

        //by construction, we should avoid cases like:
        //  Object obj = mock(Foo.class);
        //as it leads to problems when setting up "when(...)", and anyway it would make no sense
        Class<?> rawClass = new GenericClass(retval.getType()).getRawClass();
        Class<?> targetClass = st.getTargetClass();
        assert  rawClass.getName().equals(targetClass.getName()) :
                "Mismatch between variable raw type "+rawClass+" and mocked "+targetClass;
        String rawClassName = getClassName(rawClass);

        //Foo foo = mock(Foo.class);
        String variableType = getClassName(retval);
        result += variableType + " " + getVariableName(retval);

        result += " = ";
        if(! variableType.equals(rawClassName)){
            //this can happen in case of generics, eg
            //Foo<String> foo = (Foo<String>) mock(Foo.class);
            result += "(" + variableType+") ";
        }

        result += "mock(" + rawClassName+".class);";

        //when(...).thenReturn(...)
        for(MethodDescriptor md : st.getMockedMethods()){
            if(!md.shouldBeMocked()){
                continue;
            }

            result += "when("+getVariableName(retval)+"."+md.getMethodName()+"("+md.getInputParameterMatchers()+"))";
            result += ".thenReturn( ";

            List<VariableReference> params = st.getParameters(md.getID());

            Class<?> returnType = md.getMethod().getReturnType();

            String parameter_string;

            if(! returnType.isPrimitive()) {
                Type[] types = new Type[params.size()];
                for (int i = 0; i < types.length; i++) {
                    types[i] = md.getMethod().getReturnType();
                }

                parameter_string = getParameterString(types, params, false, false, 0);//TODO unsure of these parameters
            } else {

                //if return type is a primitive, then things can get complicated due to autoboxing :(

                parameter_string = getParameterStringForFMthatReturnPrimitive(returnType, params);
            }

            result += parameter_string + " );";
        }


    }

    private String getParameterString(Type[] types, List<VariableReference> params, boolean b, boolean b1, int i) {
        return null;
    }

    private String getParameterStringForFMthatReturnPrimitive(Class<?> returnType, List<VariableReference> parameters) {

        assert returnType.isPrimitive();
        String parameterString = "";

        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                parameterString += ", ";
            }
            String name = getVariableName(parameters.get(i));
            Class<?> parameterType = parameters.get(i).getVariableClass();

            if(returnType.equals(parameterType)){
                parameterString += name;
                continue;
            }

            GenericClass parameterClass = new GenericClass(parameterType);
            if (parameterClass.isWrapperType()){

                boolean isRightWrapper = false;

                if(Integer.class.equals(parameterClass)) {
                    isRightWrapper = returnType.equals(Integer.TYPE);
                } else if(Character.class.equals(parameterClass)) {
                    isRightWrapper = returnType.equals(Character.TYPE);
                } else if(Boolean.class.equals(parameterClass)) {
                    isRightWrapper = returnType.equals(Boolean.TYPE);
                } else if(Float.class.equals(parameterClass)) {
                    isRightWrapper = returnType.equals(Float.TYPE);
                } else if(Double.class.equals(parameterClass)) {
                    isRightWrapper = returnType.equals(Double.TYPE);
                } else if(Long.class.equals(parameterClass)) {
                    isRightWrapper = returnType.equals(Long.TYPE);
                } else if(Short.class.equals(parameterClass)) {
                    isRightWrapper = returnType.equals(Short.TYPE);
                } else if(Byte.class.equals(parameterClass)) {
                    isRightWrapper = returnType.equals(Byte.TYPE);
                }

                if(isRightWrapper){
                    parameterString += name;
                    continue;
                }
            }

            //if we arrive here, it means types are different and not a right wrapper (eg Integer for int)
            parameterString += "(" + returnType.getName() +")" + name;

            if (parameterClass.isWrapperType()){
                if(Integer.class.equals(parameterClass)) {
                    parameterString += ".intValue()";
                } else if(Character.class.equals(parameterClass)) {
                    parameterString += ".charValue()";
                } else if(Boolean.class.equals(parameterClass)) {
                    parameterString += ".booleanValue()";
                } else if(Float.class.equals(parameterClass)) {
                    parameterString += ".floatValue()";
                } else if(Double.class.equals(parameterClass)) {
                    parameterString += ".doubleValue()";
                } else if(Long.class.equals(parameterClass)) {
                    parameterString += ".longValue()";
                } else if(Short.class.equals(parameterClass)) {
                    parameterString += ".shortValue()";
                } else if(Byte.class.equals(parameterClass)) {
                    parameterString += ".byteValue()";
                }
            }
        }


        return parameterString;
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
        boolean isGenericMethod = method.hasTypeParameters();

        boolean lastStatement = statement.getPosition() == statement.getTestCase().size() - 1;
        boolean unused = !Properties.ASSERTIONS ? exception != null : test != null
                && !test.hasReferences(retval);

        if (!retval.isVoid() && retval.getAdditionalVariableReference() == null
                && !unused) {
            if (exception != null) {
                if (!lastStatement || statement.hasAssertions())
	                addCandidateName(test, retval, "MethodStatement", "gets" + WordUtils.capitalize(method.getName()));
            }
        }

        if (! method.isStatic()) {
            VariableReference callee = statement.getCallee();
            if (callee instanceof ConstantValue) {
	            addCandidateName(test, callee, "MethodStatement", "castTo" + getClassName(method.getMethod().getDeclaringClass()));
            } else {
                if(!callee.isAssignableTo(method.getMethod().getDeclaringClass())) {
	                try {
                        // If the concrete callee class has that method then it's ok
                        callee.getVariableClass().getMethod(method.getName(), method.getRawParameterTypes());
		                addCandidateName(test, callee, "MethodStatement", "invokes" + WordUtils.capitalize(method.getName()));
                    } catch(NoSuchMethodException e) {
	                    addCandidateName(test, callee, "MethodStatement", "castTo" + getTypeName(method.getMethod().getDeclaringClass()));
                    }
                } else {
	                addCandidateName(test, callee, "MethodStatement", "invokes" + WordUtils.capitalize(method.getName()));
                }
            }

            if (! retval.isVoid() && !unused) {
                addCandidateName(test, retval, "MethodStatement", "resultFrom" + WordUtils.capitalize(method.getName()));
                addCandidateName(test, retval, "Baseline", getVariableName(retval));
            }
        }

        handleParameters(method.getParameterTypes(),
                parameters, isGenericMethod,
                method.isOverloaded(parameters), 0, WordUtils.capitalize(method.getName()));


        visitAssertions(statement);
    }

    private String getSimpleTypeName(Type type) {
        String typeName = getTypeName(type);
        int dotIndex = typeName.lastIndexOf(".");
        if (dotIndex >= 0 && (dotIndex + 1) < typeName.length()) {
            typeName = typeName.substring(dotIndex + 1);
        }

        return typeName;
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
        boolean isGenericConstructor = constructor.hasTypeParameters();
        boolean isNonStaticMemberClass = constructor.getConstructor().getDeclaringClass().isMemberClass()
                && !constructor.isStatic()
                && !Modifier.isStatic(constructor.getConstructor().getDeclaringClass().getModifiers());

        List<VariableReference> parameters = statement.getParameterReferences();

        addCandidateName(test,retval, "Baseline", getVariableName(retval));

        int startPos = 0;
        if (isNonStaticMemberClass) {
            startPos = 1;
        }
        if (exception != null) {
            String className = getClassName(retval);
            if (retval.isPrimitive()) {
                className = retval.getGenericClass().getUnboxedType().getSimpleName();
            }
            addCandidateName(test,retval, "ConstructorStatement", "new" + className + "_WithException");
        } else {
            addCandidateName(test,retval, "ConstructorStatement", "new" + getClassName(retval));
        }
        if (isNonStaticMemberClass) {
            addCandidateName(test, retval, "ConstructorStatement", "newInner" + getSimpleTypeName(constructor.getOwnerType()));

            addCandidateName(test, retval, "ConstructorStatement" + getSimpleTypeName(constructor.getOwnerType()) + "_OK", "hasInnerObject");
            addCandidateName(test, parameters.get(0), "baseline", getVariableName(parameters.get(0)));
        } else {
	        // TODO: When does this case happen?
            //addCandidateName(test, retval, "ConstructorStatement", "newStatic" + getTypeName(constructor.getOwnerType()));
        }



        Type[] parameterTypes = constructor.getParameterTypes();

        handleParameters(parameterTypes, parameters, isGenericConstructor,
                constructor.isOverloaded(parameters), startPos, getSimpleTypeName(constructor.getOwnerType()));


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
        List<Integer> lengths = statement.getLengths();

        String multiDimensions = "";
        if (lengths.size() == 1) {
            multiDimensions = "MultiDimension";
        }

        if (retval.getGenericClass().isGenericArray()) {
            addCandidateName(test,retval,"newGenericArray"+multiDimensions,getVariableName(retval));
        } else {
            addCandidateName(test,retval,"newRegularArray"+multiDimensions,getVariableName(retval));
        }
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

        if (!retval.getVariableClass().equals(parameter.getVariableClass())) {
            addCandidateName(test,retval,"AssignmentStatement","castFrom" + parameter.getVariableClass());
	        addCandidateName(test,parameter,"AssignmentStatement","castTo" + getClassName(retval));
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
        addCandidateName(test,retval,"NullStatement", "null" + retval.getClassName());
    }

    @Override
    public void visitStatement(Statement statement) {
        super.visitStatement(statement);
    }


    /**
     * <p>
     * getVariableName
     * </p>
     *
     * @param var
     *            a {@link org.evosuite.testcase.variable.VariableReference} object.
     * @return a {@link java.lang.String} object.
     */
    public String getVariableName(VariableReference var) {

        if (var instanceof ArrayReference) {
            String className = var.getSimpleClassName();
            // int num = 0;
            // for (VariableReference otherVar : variableNames.keySet()) {
            // if (!otherVar.equals(var)
            // && otherVar.getVariableClass().equals(var.getVariableClass()))
            // num++;
            // }
            String variableName = className.substring(0, 1).toLowerCase()
                    + className.substring(1) + "Array";
            variableName = variableName.replace(".", "_").replace("[]", "");
            if (!variableNames.containsKey(var)) {
                if (!nextIndices.containsKey(variableName)) {
                    nextIndices.put(variableName, 0);
                }

                int index = nextIndices.get(variableName);
                nextIndices.put(variableName, index + 1);

                variableName += index;

                variableNames.put(var, variableName);
            }

        } else if (!variableNames.containsKey(var)) {
            String className = var.getSimpleClassName();
            // int num = 0;
            // for (VariableReference otherVar : variableNames.keySet()) {
            // if (otherVar.getVariableClass().equals(var.getVariableClass()))
            // num++;
            // }
            String variableName = className.substring(0, 1).toLowerCase()
                    + className.substring(1);
            if (variableName.contains("[]")) {
                variableName = variableName.replace("[]", "Array");
            }
            variableName = variableName.replace(".", "_");

            // Need a way to check for exact types, not assignable
            // int numObjectsOfType = test != null ? test.getObjects(var.getType(),
            //                                                      test.size()).size() : 2;
            // if (numObjectsOfType > 1 || className.equals(variableName)) {
            if (CharUtils.isAsciiNumeric(variableName.charAt(variableName.length() - 1)))
                variableName += "_";

            if (!nextIndices.containsKey(variableName)) {
                nextIndices.put(variableName, 0);
            }

            int index = nextIndices.get(variableName);
            nextIndices.put(variableName, index + 1);

            variableName += index;
            // }

            variableNames.put(var, variableName);
        }
        return variableNames.get(var);
    }

    public void printAll() {
        System.out.println("CANDIDATE NAMES MAPPING");
        String format = "%-5s| %-10s| %s\n";
        System.out.printf(format, "test", "varRef", "candidateName");

        for (Map.Entry<VariableReference,List<CandidateName>> varEntry : varNamesCandidates.entrySet()) {
            VariableReference var = varEntry.getKey();
            varEntry.getValue().forEach((candidate) -> {
                System.out.printf(format, this.test.getID(), var, candidate.toString());
            });
        }
    }

	public Map<VariableReference,String> getAllVariableNames() {
		varNamesFinal.clear();
        for (Map.Entry<VariableReference,List<CandidateName>> varEntry : varNamesCandidates.entrySet()) {
            VariableReference key = varEntry.getKey();
            List<CandidateName> candidates = varEntry.getValue();
            // TODO: Choose the most appropriate name
            varNamesFinal.put(key, candidates.get(0).getName());
		}
		return varNamesFinal;
	}

    /**
     * <p>
     * getClassName
     * </p>
     *
     * @param clazz
     *            a {@link java.lang.Class} object.
     * @return a {@link java.lang.String} object.
     */
    private String getClassName(Class<?> clazz) {
        return ((ImportsTestCodeVisitor)this.itv).getClassName(clazz);
    }

    /**
     * <p>
     * getClassName
     * </p>
     *
     * @param var
     *            a {@link org.evosuite.testcase.variable.VariableReference} object.
     * @return a {@link java.lang.String} object.
     */
    private String getClassName(VariableReference var) {
        return ((ImportsTestCodeVisitor)this.itv).getClassName(var);
    }

    /**
     * <p>
     * getTypeName
     * </p>
     *
     * @param clazz
     *            a {@link java.lang.Class} object.
     * @return a {@link java.lang.String} object.
     */
    private String getTypeName(Class<?> clazz) {
        return ((ImportsTestCodeVisitor)this.itv).getTypeName(clazz);
    }

    /**
     * <p>
     * getTypeName
     * </p>
     *
     * @param type
     *            a {@link java.lang.reflect.Type} object.
     * @return a {@link java.lang.String} object.
     */
    private String getTypeName(Type type) {
        return ((ImportsTestCodeVisitor)this.itv).getTypeName(type);
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

    class CandidateName {
        private String explanation;
        private String name;

        public CandidateName(String explanation, String name) {
            this.explanation = explanation;
            this.name = name;
        }

        public String getExplanation() {
            return explanation;
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
                    ", explanation='" + getExplanation() + '\'' +
                    '}';
        }
    }
}