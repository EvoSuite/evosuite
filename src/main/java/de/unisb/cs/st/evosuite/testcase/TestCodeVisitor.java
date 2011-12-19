/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import de.unisb.cs.st.evosuite.assertion.Assertion;
import de.unisb.cs.st.evosuite.assertion.CompareAssertion;
import de.unisb.cs.st.evosuite.assertion.EqualsAssertion;
import de.unisb.cs.st.evosuite.assertion.Inspector;
import de.unisb.cs.st.evosuite.assertion.InspectorAssertion;
import de.unisb.cs.st.evosuite.assertion.NullAssertion;
import de.unisb.cs.st.evosuite.assertion.PrimitiveAssertion;
import de.unisb.cs.st.evosuite.assertion.PrimitiveFieldAssertion;
import de.unisb.cs.st.evosuite.utils.NumberFormatter;

/**
 * @author fraser
 * 
 */
public class TestCodeVisitor implements TestVisitor {

	private String testCode = "";

	private final Map<Integer, Throwable> exceptions = new HashMap<Integer, Throwable>();

	private TestCase test = null;

	private final Map<VariableReference, String> variableNames = new HashMap<VariableReference, String>();

	public String getCode() {
		return testCode;
	}

	public void setExceptions(Map<Integer, Throwable> exceptions) {
		this.exceptions.putAll(exceptions);
	}

	public void setException(StatementInterface statement, Throwable exception) {
		exceptions.put(statement.getPosition(), exception);
	}

	private Throwable getException(StatementInterface statement) {
		if (exceptions != null && exceptions.containsKey(statement.getPosition()))
			return exceptions.get(statement.getPosition());

		return null;
	}

	private String getVariableName(VariableReference var) {
		if (var instanceof ConstantValue) {
			return var.getName();
		} else if (var instanceof FieldReference) {
			VariableReference source = ((FieldReference) var).getSource();
			Field field = ((FieldReference) var).getField();
			if (source != null)
				return getVariableName(source) + "." + field.getName();
			else
				return field.getDeclaringClass().getSimpleName() + "." + field.getName();
		} else if (var instanceof ArrayIndex) {
			VariableReference array = ((ArrayIndex) var).getArray();
			int index = ((ArrayIndex) var).getArrayIndex();
			return getVariableName(array) + "[" + index + "]";
		} else if (var instanceof ArrayReference) {

			String className = var.getSimpleClassName();
			int num = 0;
			for (VariableReference otherVar : variableNames.keySet()) {
				if (!otherVar.equals(var)
				        && otherVar.getVariableClass().equals(var.getVariableClass()))
					num++;
			}
			String variableName = className.substring(0, 1).toLowerCase()
			        + className.substring(1) + "Array" + num;
			variableName = variableName.replace(".", "_").replace("[]", "");
			variableNames.put(var, variableName);
		} else if (!variableNames.containsKey(var)) {
			String className = var.getSimpleClassName();
			int num = 0;
			for (VariableReference otherVar : variableNames.keySet()) {
				if (!otherVar.equals(var)
				        && otherVar.getVariableClass().equals(var.getVariableClass()))
					num++;
			}
			String variableName = className.substring(0, 1).toLowerCase()
			        + className.substring(1) + num;
			if (variableName.contains("[]")) {
				variableName = variableName.replace("[]", "Array");
			}
			variableName = variableName.replace(".", "_");
			variableNames.put(var, variableName);
		}
		return variableNames.get(var);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitTestCase(de.unisb.cs.st.evosuite.testcase.TestCase)
	 */
	@Override
	public void visitTestCase(TestCase test) {
		this.test = test;
	}

	private void visitPrimitiveAssertion(PrimitiveAssertion assertion) {
		VariableReference source = assertion.getSource();
		Object value = assertion.getValue();

		if (value == null) {
			testCode += "assertNull(" + getVariableName(source) + ");";
		} else if (source.getVariableClass().equals(float.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + ", 0.01F);";
		} else if (source.getVariableClass().equals(double.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + ", 0.01D);";
		} else if (value.getClass().isEnum()) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + ");";
		} else if (source.isWrapperType()) {
			if (source.getVariableClass().equals(Float.class)) {
				testCode += "assertEquals(" + NumberFormatter.getNumberString(value)
				        + "(float)" + getVariableName(source) + ", 0.01F);";
			} else if (source.getVariableClass().equals(Double.class)) {
				testCode += "assertEquals(" + NumberFormatter.getNumberString(value)
				        + "(double)" + getVariableName(source) + ", 0.01D);";
			} else if (value.getClass().isEnum()) {
				testCode += "assertEquals(" + NumberFormatter.getNumberString(value)
				        + ", " + getVariableName(source) + ");";
			} else
				testCode += "assertEquals(" + NumberFormatter.getNumberString(value)
				        + ", (" + NumberFormatter.getBoxedClassName(value) + ")"
				        + getVariableName(source) + ");";
		} else
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + ");";
	}

	private void visitPrimitiveFieldAssertion(PrimitiveFieldAssertion assertion) {
		VariableReference source = assertion.getSource();
		Object value = assertion.getValue();
		Field field = assertion.getField();

		if (value == null) {
			testCode += "assertNull(" + getVariableName(source) + "." + field.getName()
			        + ");";
		} else if (value.getClass().equals(Long.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + field.getName() + ");";
		} else if (value.getClass().equals(Float.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + field.getName() + ", 0.01F);";
		} else if (value.getClass().equals(Double.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + field.getName() + ", 0.01D);";
		} else if (value.getClass().equals(Character.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + field.getName() + ");";
		} else if (value.getClass().equals(String.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + field.getName() + ");";
		} else
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + field.getName() + ");";
	}

	private void visitInspectorAssertion(InspectorAssertion assertion) {
		VariableReference source = assertion.getSource();
		Object value = assertion.getValue();
		Inspector inspector = assertion.getInspector();

		if (value == null) {
			testCode += "assertNull(" + getVariableName(source) + "."
			        + inspector.getMethodCall() + "());";
		} else if (value.getClass().equals(Long.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + inspector.getMethodCall() + "());";
		} else if (value.getClass().equals(Float.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + inspector.getMethodCall()
			        + "(), 0.01F);";
		} else if (value.getClass().equals(Double.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + inspector.getMethodCall()
			        + "(), 0.01D);";
		} else if (value.getClass().equals(Character.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + inspector.getMethodCall() + "());";
		} else if (value.getClass().equals(String.class)) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + inspector.getMethodCall() + "());";
		} else if (value.getClass().isEnum()) {
			testCode += "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
			        + getVariableName(source) + "." + inspector.getMethodCall() + "());";

		} else
			testCode += "assertEquals(" + value + ", " + getVariableName(source) + "."
			        + inspector.getMethodCall() + "());";
	}

	private void visitNullAssertion(NullAssertion assertion) {
		VariableReference source = assertion.getSource();
		Boolean value = (Boolean) assertion.getValue();
		if (value.booleanValue()) {
			testCode += "assertNull(" + getVariableName(source) + ");";
		} else
			testCode += "assertNotNull(" + getVariableName(source) + ");";
	}

	private void visitCompareAssertion(CompareAssertion assertion) {
		VariableReference source = assertion.getSource();
		VariableReference dest = assertion.getDest();
		Object value = assertion.getValue();

		if (source.getType().equals(Integer.class)) {
			if ((Integer) value == 0)
				testCode += "assertTrue(" + getVariableName(source) + " == "
				        + getVariableName(dest) + ");";
			else if ((Integer) value < 0)
				testCode += "assertTrue(" + getVariableName(source) + " < "
				        + getVariableName(dest) + ");";
			else
				testCode += "assertTrue(" + getVariableName(source) + " > "
				        + getVariableName(dest) + ");";

		} else {
			testCode += "assertEquals(" + getVariableName(source) + ".compareTo("
			        + getVariableName(dest) + "), " + value + ");";
		}
	}

	private void visitEqualsAssertion(EqualsAssertion assertion) {
		VariableReference source = assertion.getSource();
		VariableReference dest = assertion.getDest();
		Object value = assertion.getValue();

		if (source.isPrimitive() && dest.isPrimitive()) {
			if (((Boolean) value).booleanValue())
				testCode += "assertTrue(" + getVariableName(source) + " == "
				        + getVariableName(dest) + ");";
			else
				testCode += "assertFalse(" + getVariableName(source) + " == "
				        + getVariableName(dest) + ");";
		} else {
			if (((Boolean) value).booleanValue())
				testCode += "assertTrue(" + getVariableName(source) + ".equals("
				        + getVariableName(dest) + "));";
			else
				testCode += "assertFalse(" + getVariableName(source) + ".equals("
				        + getVariableName(dest) + "));";
		}
	}

	private void visitAssertion(Assertion assertion) {
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
		} else {
			throw new RuntimeException("Unknown assertion type: " + assertion);
		}
	}

	private void addAssertions(StatementInterface statement) {
		boolean assertionAdded = false;
		for (Assertion assertion : statement.getAssertions()) {
			if (assertion != null) {
				visitAssertion(assertion);
				testCode += "\n";
				assertionAdded = true;
			}
		}
		if (assertionAdded)
			testCode += "\n";
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitPrimitiveStatement(de.unisb.cs.st.evosuite.testcase.PrimitiveStatement)
	 */
	@Override
	public void visitPrimitiveStatement(PrimitiveStatement<?> statement) {
		VariableReference retval = statement.getReturnValue();
		Object value = statement.getValue();

		if (statement instanceof StringPrimitiveStatement) {
			testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
			        + retval.getName() + " = \""
			        + StringEscapeUtils.escapeJava((String) value) + "\";\n";
		} else if (statement instanceof LongPrimitiveStatement) {
			testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
			        + retval.getName() + " = " + value + "L;\n";
		} else if (statement instanceof FloatPrimitiveStatement) {
			testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
			        + retval.getName() + " = " + value + "F;\n";
		} else if (statement instanceof CharPrimitiveStatement) {
			testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
			        + retval.getName() + " = '"
			        + StringEscapeUtils.escapeJava(value.toString()) + "';\n";
		} else if (statement instanceof EnumPrimitiveStatement) {
			if (value != null)
				testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
				        + retval.getName() + " = "
				        + NumberFormatter.getNumberString(value) + ";\n";
			else
				testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
				        + retval.getName() + " = ("
				        + ((Class<?>) retval.getType()).getSimpleName() + ") null;\n";
		}

		testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
		        + getVariableName(retval) + " = " + value + ";\n";
		addAssertions(statement);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitFieldStatement(de.unisb.cs.st.evosuite.testcase.FieldStatement)
	 */
	@Override
	public void visitFieldStatement(FieldStatement statement) {
		Throwable exception = getException(statement);

		String cast_str = "";
		StringBuilder builder = new StringBuilder();

		VariableReference retval = statement.getReturnValue();
		Field field = statement.getField();

		if (!retval.getVariableClass().isAssignableFrom(field.getType())) {
			cast_str += "(" + retval.getSimpleClassName() + ")";
		}

		if (exception != null) {
			builder.append(retval.getSimpleClassName());
			builder.append(" ");
			builder.append(getVariableName(retval));
			builder.append(" = null;\n");
			builder.append("try {\n  ");
		} else {
			builder.append(retval.getSimpleClassName());
			builder.append(" ");
		}
		if (!Modifier.isStatic(field.getModifiers())) {
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
			builder.append(field.getDeclaringClass().getSimpleName());
			builder.append(".");
			builder.append(field.getName());
			builder.append(";");
		}
		if (exception != null) {
			Class<?> ex = exception.getClass();
			while (!Modifier.isPublic(ex.getModifiers()))
				ex = ex.getSuperclass();
			builder.append("\n} catch(");
			builder.append(ClassUtils.getShortClassName(ex));
			builder.append(" e) {}");
		}
		builder.append("\n");

		testCode += builder.toString();
		addAssertions(statement);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitMethodStatement(de.unisb.cs.st.evosuite.testcase.MethodStatement)
	 */
	@Override
	public void visitMethodStatement(MethodStatement statement) {
		String result = "";
		VariableReference retval = statement.getReturnValue();
		Method method = statement.getMethod();
		Throwable exception = getException(statement);
		List<VariableReference> parameters = statement.getParameterReferences();

		if (exception != null && !statement.isDeclaredException(exception)) {
			result += "// Undeclared exception!\n";
		}

		boolean lastStatement = statement.getPosition() == statement.tc.size() - 1;
		boolean unused = test != null ? !test.hasReferences(retval) : false;

		if (retval.getType() != Void.TYPE
		        && retval.getAdditionalVariableReference() == null && !unused) {
			if (exception != null) {
				if (!lastStatement)
					result += retval.getSimpleClassName() + " " + getVariableName(retval)
					        + " = " + retval.getDefaultValueString() + ";\n";
			} else
				result += retval.getSimpleClassName() + " ";
		}
		if (exception != null)
			result += "try {\n  ";

		String parameter_string = "";
		for (int i = 0; i < parameters.size(); i++) {
			if (i > 0) {
				parameter_string += ", ";
			}
			Class<?> declaredParamType = method.getParameterTypes()[i];
			Class<?> actualParamType = parameters.get(i).getVariableClass();
			String name = getVariableName(parameters.get(i));
			if ((!declaredParamType.isAssignableFrom(actualParamType) || name.equals("null"))
			        && !method.getParameterTypes()[i].equals(Object.class)
			        && !method.getParameterTypes()[i].equals(Comparable.class)) {
				parameter_string += "("
				        + new GenericClass(method.getParameterTypes()[i]).getSimpleName()
				        + ") ";
			}
			parameter_string += name;
		}

		String callee_str = "";
		if (exception == null
		        && !retval.getVariableClass().isAssignableFrom(method.getReturnType())
		        && !retval.getVariableClass().isAnonymousClass() && !unused) {
			String name = retval.getSimpleClassName();
			if (!name.matches(".*\\.\\d+$")) {
				callee_str = "(" + name + ")";
			}
		}

		if (Modifier.isStatic(method.getModifiers())) {
			callee_str += method.getDeclaringClass().getSimpleName();
		} else {
			VariableReference callee = statement.getCallee();
			callee_str += getVariableName(callee);
		}

		if (retval.getType() == Void.TYPE) {
			result += callee_str + "." + method.getName() + "(" + parameter_string + ");";
		} else {
			if (exception == null || !lastStatement)
				if (!unused)
					result += getVariableName(retval) + " = ";

			result += callee_str + "." + method.getName() + "(" + parameter_string + ");";
		}

		if (exception != null) {
			//boolean isExpected = getDeclaredExceptions().contains(exception.getClass());
			Class<?> ex = exception.getClass();
			while (!Modifier.isPublic(ex.getModifiers()))
				ex = ex.getSuperclass();
			//if (isExpected)
			result += "\n  fail(\"Expecting exception: "
			        + ClassUtils.getShortClassName(ex) + "\");";
			result += "\n} catch(" + ClassUtils.getShortClassName(ex) + " e) {\n";
			if (exception.getMessage() != null) {
				//if (!isExpected)
				//	result += "\n  fail(\"Undeclared exception: "
				//	        + ClassUtils.getShortClassName(ex) + "\");\n";
				result += "  /*\n";
				for (String msg : exception.getMessage().split("\n")) {
					result += "   * " + StringEscapeUtils.escapeJava(msg) + "\n";
				}
				result += "   */\n";
			}
			result += "}";
		}

		testCode += result + "\n";
		addAssertions(statement);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitConstructorStatement(de.unisb.cs.st.evosuite.testcase.ConstructorStatement)
	 */
	@Override
	public void visitConstructorStatement(ConstructorStatement statement) {
		String parameter_string = "";
		String result = "";
		Constructor<?> constructor = statement.getConstructor();
		VariableReference retval = statement.getReturnValue();
		Throwable exception = getException(statement);

		List<VariableReference> parameters = statement.getParameterReferences();
		if (!parameters.isEmpty()) {
			for (int i = 0; i < parameters.size(); i++) {
				if (i > 0) {
					parameter_string += ", ";
				}
				Class<?> declaredParamType = constructor.getParameterTypes()[i];
				Class<?> actualParamType = parameters.get(i).getVariableClass();
				String name = getVariableName(parameters.get(i));
				if ((!declaredParamType.isAssignableFrom(actualParamType) || name.equals("null"))
				        && !constructor.getParameterTypes()[i].equals(Object.class)
				        && !constructor.getParameterTypes()[i].equals(Comparable.class)) {
					parameter_string += "("
					        + new GenericClass(constructor.getParameterTypes()[i]).getSimpleName()
					        + ") ";
				}
				parameter_string += name;
			}

		}
		// String result = ((Class<?>) retval.getType()).getSimpleName()
		// +" "+getVariableName(retval)+ " = null;\n";
		if (exception != null) {
			result = retval.getSimpleClassName() + " " + getVariableName(retval)
			        + " = null;\n";
			result += "try {\n  ";
		} else {
			result += retval.getSimpleClassName() + " ";
		}
		result += getVariableName(retval) + " = new "
		        + ConstructorStatement.getReturnType(constructor.getDeclaringClass())
		        + "(" + parameter_string + ");";

		if (exception != null) {
			Class<?> ex = exception.getClass();
			//boolean isExpected = getDeclaredExceptions().contains(ex);

			while (!Modifier.isPublic(ex.getModifiers()))
				ex = ex.getSuperclass();
			//if (isExpected)
			result += "\n  fail(\"Expecting exception: "
			        + ClassUtils.getShortClassName(ex) + "\");";

			result += "\n} catch(" + ClassUtils.getShortClassName(ex) + " e) {\n";
			if (exception.getMessage() != null) {
				//if (!isExpected)
				//	result += "\n  fail(\"Undeclared exception: "
				//	        + ClassUtils.getShortClassName(ex) + "\");\n";
				result += "  /*\n";
				for (String msg : exception.getMessage().split("\n")) {
					result += "   * " + StringEscapeUtils.escapeJava(msg) + "\n";
				}
				result += "   */\n";
			}
			result += "}";
		}

		testCode += result + "\n";
		addAssertions(statement);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitArrayStatement(de.unisb.cs.st.evosuite.testcase.ArrayStatement)
	 */
	@Override
	public void visitArrayStatement(ArrayStatement statement) {
		VariableReference retval = statement.getReturnValue();
		int length = statement.size();

		String type = retval.getSimpleClassName().replaceFirst("\\[\\]", "");
		String multiDimensions = "";
		while (type.contains("[]")) {
			multiDimensions += "[]";
			type = type.replaceFirst("\\[\\]", "");
		}
		testCode += retval.getSimpleClassName() + " " + getVariableName(retval)
		        + " = new " + type + "[" + length + "]" + multiDimensions + ";\n";
		addAssertions(statement);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitAssignmentStatement(de.unisb.cs.st.evosuite.testcase.AssignmentStatement)
	 */
	@Override
	public void visitAssignmentStatement(AssignmentStatement statement) {
		String cast = "";
		VariableReference retval = statement.getReturnValue();
		VariableReference parameter = statement.parameter;

		if (!retval.getVariableClass().equals(parameter.getVariableClass()))
			cast = "(" + retval.getSimpleClassName() + ") ";

		testCode += getVariableName(retval) + " = " + cast + getVariableName(parameter)
		        + ";\n";
		addAssertions(statement);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitNullStatement(de.unisb.cs.st.evosuite.testcase.NullStatement)
	 */
	@Override
	public void visitNullStatement(NullStatement statement) {
		VariableReference retval = statement.getReturnValue();

		testCode += retval.getSimpleClassName() + " " + getVariableName(retval)
		        + " = null;\n";
	}

	public void visitStatement(StatementInterface statement) {
		if (statement instanceof PrimitiveStatement<?>)
			visitPrimitiveStatement((PrimitiveStatement<?>) statement);
		else if (statement instanceof FieldStatement)
			visitFieldStatement((FieldStatement) statement);
		else if (statement instanceof ConstructorStatement)
			visitConstructorStatement((ConstructorStatement) statement);
		else if (statement instanceof MethodStatement)
			visitMethodStatement((MethodStatement) statement);
		else if (statement instanceof AssignmentStatement)
			visitAssignmentStatement((AssignmentStatement) statement);
		else if (statement instanceof ArrayStatement)
			visitArrayStatement((ArrayStatement) statement);
		else if (statement instanceof NullStatement)
			visitNullStatement((NullStatement) statement);
		else
			throw new RuntimeException("Unknown statement type: " + statement);
	}
}
