/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.classcreation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import de.unisb.cs.st.evosuite.utils.Utils;

/**
 * Generates classes from given abstract methods and constructors.
 * 
 * Most of the code is written with AST API, but reflection API is used in order
 * to get abstract class parameters: methods, constructors etc.
 * 
 * @author Andrey Tarasevich
 * 
 */
public class SourceCodeGenerator {

	/** Current AST object. */
	private final AST ast;

	/** Container for the current AST. */
	private final CompilationUnit unit;

	/** List of the methods for original class */
	private final List<Method> methods = new LinkedList<Method>();

	/** Original class */
	private final Class<?> clazz;

	/** Class name */
	private String className;

	/** Name of the package */
	private final String packageName;

	/** List of the constructors for original class. */
	private final Constructor<?>[] constructors;

	/** Information about stub fields and setters */
	private final Set<StubField> stubFields = new HashSet<StubField>();

	/** Current name of the field to generate */
	private String currentFieldName;

	/** If current class if abstract */
	private boolean abstractClass = false;

	/**
	 * @param clazz
	 *            class for which stub should be generated.
	 */
	public SourceCodeGenerator(Class<?> clazz) {
		ast = AST.newAST(AST.JLS3);
		unit = ast.newCompilationUnit();
		this.clazz = clazz;
		packageName = Utils.getPackageName(clazz);

		if (clazz.getCanonicalName() == null)
			className = clazz.getName();
		else
			className = clazz.getCanonicalName();

		// Get constructors and methods of a class. 
		Method[] initialMethods = clazz.getDeclaredMethods();
		constructors = clazz.getConstructors();

		// If class is abstract filter methods and select only 
		// abstract.
		if (java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
			abstractClass = true;
			for (Method m : initialMethods)
				if (java.lang.reflect.Modifier.isAbstract(m.getModifiers()))
					methods.add(m);
		} else {
			methods.addAll(Arrays.asList(initialMethods));
		}
	}

	/**
	 * Generates stub source code.
	 * 
	 * @return compilation unit of the stub
	 */
	@SuppressWarnings("unchecked")
	public CompilationUnit generateSourceCode() {
		PackageDeclaration pd = ast.newPackageDeclaration();
		pd.setName(ast.newName(packageName));
		unit.setPackage(pd);

		// Create class stub.
		TypeDeclaration type = ast.newTypeDeclaration();
		type.setName(ast.newSimpleName(clazz.getSimpleName() + "Stub"));

		if (abstractClass) {
			// Set inheritance.
			type.setSuperclassType(ast.newSimpleType(ast.newName(className)));

			// Check for abstract constructors and if any - generate them.
			if (constructors.length != 0)
				type.bodyDeclarations().addAll(generateConstructors());
		} else {
			// Add jmockit annotation 
			NormalAnnotation na = ast.newNormalAnnotation();
			na.setTypeName(ast.newName("mockit.MockClass"));
			MemberValuePair mvp = ast.newMemberValuePair();
			mvp.setName(ast.newSimpleName("realClass"));
			TypeLiteral tl = ast.newTypeLiteral();
			tl.setType(ast.newSimpleType(ast.newName(className)));
			mvp.setValue(tl);
			na.values().add(mvp);

			type.modifiers().add(na);
		}
		type.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

		type.setInterface(false);

		// Generate stubs, fields and setters for the methods.
		List<MethodDeclaration> generatedMethods = generateMethods();
		List<FieldDeclaration> generatedFields = generateFields();
		List<MethodDeclaration> generatedSetters = generateSetters();
		type.bodyDeclarations().addAll(generatedFields);
		type.bodyDeclarations().addAll(generatedMethods);
		type.bodyDeclarations().addAll(generatedSetters);

		// Add class to the compilation unit.
		unit.types().add(type);

		return unit;
	}

	/**
	 * Generates stubs for abstract methods.
	 * 
	 * @return list of the abstract method stubs
	 */
	@SuppressWarnings("unchecked")
	private List<MethodDeclaration> generateMethods() {
		List<MethodDeclaration> generatedMethods = new LinkedList<MethodDeclaration>();
		for (Method method : methods) {
			generateFieldName(method);

			MethodDeclaration md = ast.newMethodDeclaration();
			md.setName(ast.newSimpleName(method.getName()));

			// Set "Override" annotation marker if abstract class.
			// Otherwise set jmockit annotations
			NormalAnnotation na = ast.newNormalAnnotation();
			if (abstractClass)
				na.setTypeName(ast.newName("java.lang.Override"));
			else
				na.setTypeName(ast.newName("mockit.Mock"));
			md.modifiers().add(na);

			// Get original modifiers of the method, they are either 
			// public or protected.
			int modifiers = method.getModifiers();
			if (Modifier.isPublic(modifiers))
				md.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
			else
				md.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD));

			// Set return type of the method.
			md.setReturnType2(generateType(method.getReturnType()));

			// Generate method parameters if any.
			List<?> parameters = generateMethodParameters(method.getParameterTypes());
			if (!parameters.isEmpty())
				md.parameters().addAll(parameters);

			// Set default return value.
			md.setBody(generateMethodBody(method.getReturnType()));

			// Add exceptions thrown by method if any.
			List<?> exceptions = (generateThrownExceptions(method.getExceptionTypes()));
			if (!exceptions.isEmpty())
				md.thrownExceptions().addAll(exceptions);

			generatedMethods.add(md);
		}
		return generatedMethods;
	}

	/**
	 * Creates setters for the stub fields.
	 * 
	 * @return generated setters
	 */
	@SuppressWarnings("unchecked")
	private List<MethodDeclaration> generateSetters() {
		List<MethodDeclaration> setters = new LinkedList<MethodDeclaration>();
		for (StubField sf : stubFields) {

			Assignment assignment = ast.newAssignment();
			assignment.setLeftHandSide(ast.newSimpleName(sf.getFieldName()));
			assignment.setRightHandSide(ast.newSimpleName("value"));

			Block block = ast.newBlock();
			ExpressionStatement es = ast.newExpressionStatement(assignment);
			block.statements().add(es);

			MethodDeclaration md = ast.newMethodDeclaration();
			md.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
			md.setName(ast.newSimpleName("set_" + sf.getFieldName()));
			md.setBody(block);

			SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
			svd.setType(sf.getFieldSetterType());
			svd.setName(ast.newSimpleName("value"));

			md.parameters().add(svd);

			setters.add(md);
		}
		return setters;
	}

	/**
	 * Creates fields for the stub.
	 * 
	 * @return generated fields.
	 */
	@SuppressWarnings("unchecked")
	private List<FieldDeclaration> generateFields() {
		List<FieldDeclaration> fields = new LinkedList<FieldDeclaration>();
		for (StubField sf : stubFields) {
			VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
			vdf.setInitializer(sf.getFieldValue());
			vdf.setName(ast.newSimpleName(sf.getFieldName()));

			FieldDeclaration fd = ast.newFieldDeclaration(vdf);
			fd.setType(sf.getFieldType());
			fd.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));

			fields.add(fd);
		}
		return fields;
	}

	/**
	 * Generates abstract constructor stubs.
	 * 
	 * @return list of the abstract constructor stubs.
	 */
	@SuppressWarnings("unchecked")
	private List<MethodDeclaration> generateConstructors() {
		List<MethodDeclaration> generatedConstructors = new LinkedList<MethodDeclaration>();
		for (Constructor<?> abstractConstructor : constructors) {

			MethodDeclaration cd = ast.newMethodDeclaration();
			cd.setName(ast.newSimpleName(clazz.getSimpleName() + "Stub"));
			cd.setConstructor(true);

			// Abstract constructors could only be public. 
			cd.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

			// Generate constructor parameters if any.
			List<?> parameters = generateMethodParameters(abstractConstructor.getParameterTypes());
			if (!parameters.isEmpty())
				cd.parameters().addAll(parameters);

			// Set default return type.
			cd.setBody(generateConstructorBody(abstractConstructor.getParameterTypes().length));

			// Add exceptions thrown by constructor if any.
			List<?> exceptions = (generateThrownExceptions(abstractConstructor.getExceptionTypes()));
			if (!exceptions.isEmpty())
				cd.thrownExceptions().addAll(exceptions);

			generatedConstructors.add(cd);
		}
		return generatedConstructors;
	}

	/**
	 * Generates constructor body, i.e. creates only return statement with super
	 * constructor invocation.
	 * 
	 * @param numberOfParameters
	 *            number of parameters in original constructor.
	 * @return generated statements within a block.
	 */
	@SuppressWarnings("unchecked")
	private Block generateConstructorBody(int numberOfParameters) {

		// Constructor body should be in a block statement. 
		Block block = ast.newBlock();

		SuperConstructorInvocation sci = ast.newSuperConstructorInvocation();
		for (int i = 0; i < numberOfParameters; i++)
			sci.arguments().add(ast.newName("var" + i));

		block.statements().add(sci);
		return block;
	}

	/**
	 * Generates thrown exceptions.
	 * 
	 * @param thrownExceptions
	 *            array of exception types in original method.generateMethodBody
	 * @return list of exceptions.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List generateThrownExceptions(Class<?>[] thrownExceptions) {
		List exceptions = new LinkedList();
		for (Class<?> exception : thrownExceptions) {
			exceptions.add(ast.newName(exception.getCanonicalName()));
		}
		return exceptions;
	}

	/**
	 * Generates method parameters.
	 * 
	 * @param parameterTypes
	 *            types of the parameters in original method.
	 * @return list of the parameters.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List generateMethodParameters(Class<?>[] parameterTypes) {
		List parameters = new LinkedList();
		int counter = 0;

		for (Class<?> parameter : parameterTypes) {
			SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
			svd.setType(generateType(parameter));
			svd.setName(ast.newSimpleName("var" + counter++));
			parameters.add(svd);
		}
		return parameters;
	}

	/**
	 * Transforms type from reflection representation to AST representation.
	 * 
	 * @param type
	 *            reflection representation of the type.
	 * @return AST representation of the type.
	 */
	private Type generateType(Class<?> type) {
		if (type.isArray())
			return generateArrayType(type);
		if (type.isPrimitive())
			return generatePrimitiveType(type);
		return generateSimpleType(type);
	}

	/**
	 * Generates array type
	 * 
	 * @param type
	 *            reflection represantation of the array type
	 * @return AST representation of the type
	 */
	private Type generateArrayType(Class<?> type) {
		return ast.newArrayType(generateType(type.getComponentType()));
	}

	/**
	 * Generates primitive type. (int, double, char etc).
	 * 
	 * @param type
	 *            reflection representation of the type.
	 * @return AST representation of the type/
	 */
	private Type generatePrimitiveType(Class<?> type) {
		return ast.newPrimitiveType(PrimitiveType.toCode(type.getCanonicalName()));
	}

	/**
	 * Generates simple type. (Class or String types for instance).
	 * 
	 * @param type
	 *            reflection representation of the type.
	 * @return AST representation of the type/
	 */
	private Type generateSimpleType(Class<?> type) {
		return ast.newSimpleType(ast.newName(type.getCanonicalName()));
	}

	/**
	 * Generates stub method body, i.e. creates only return statement with
	 * default value.
	 * 
	 * @param returnType
	 *            return of the method.
	 * @return block with return statement inside.
	 */
	@SuppressWarnings("unchecked")
	private Block generateMethodBody(Class<?> returnType) {
		ReturnStatement rs = ast.newReturnStatement();
		rs.setExpression(generateReturnExpression(returnType));
		Block block = ast.newBlock();
		block.statements().add(rs);
		return block;
	}

	/**
	 * Generates return expression.
	 * 
	 * @param returnType
	 *            return type.
	 * @return AST return expression.
	 */
	private Expression generateReturnExpression(Class<?> returnType) {

		// If return type is String, then generate string literal.
		if (returnType.getName().equals("java.lang.String")) {
			StringLiteral sl = ast.newStringLiteral();
			sl.setLiteralValue("null_value");

			rememberFieldParams(sl, ast.newSimpleType(ast.newName("java.lang.String")),
			                    ast.newSimpleType(ast.newName("java.lang.String")));

			return ast.newSimpleName(currentFieldName);
		}

		if (returnType.isArray()) {
			NullLiteral nl = ast.newNullLiteral();
			rememberFieldParams(nl, generateArrayType(returnType),
			                    generateArrayType(returnType));
			return ast.newSimpleName(currentFieldName);
		}

		// if primitive, then generate primitive expression.
		if (returnType.isPrimitive())
			return generatePrimitiveExpression(generatePrimitiveType(returnType));

		// Generate only simplest constructors.
		if (returnType.getTypeParameters().length == 0)
			return generateConstructorCall(returnType);

		// Default "null" return statement.
		return ast.newNullLiteral();
	}

	/**
	 * Generates expression for a primitive types. (int, boolean etc.)
	 * 
	 * @param type
	 *            primitive type to generate.
	 * @return expression with default value for the primitive type.
	 */
	private Expression generatePrimitiveExpression(Type type) {
		PrimitiveType primitiveType = (PrimitiveType) type;

		// Don't set return type for void.  
		if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.VOID))
			return null;

		// Check for all possible primitive types and remember them for 
		// later use in field generation.
		if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.BOOLEAN)) {
			rememberFieldParams(ast.newBooleanLiteral(false),
			                    ast.newPrimitiveType(PrimitiveType.BOOLEAN),
			                    ast.newPrimitiveType(PrimitiveType.BOOLEAN));
		}

		if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.CHAR)) {
			CharacterLiteral cl = ast.newCharacterLiteral();
			cl.setCharValue('0');

			rememberFieldParams(cl, ast.newPrimitiveType(PrimitiveType.CHAR),
			                    ast.newPrimitiveType(PrimitiveType.CHAR));
		}

		if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.BYTE)) {
			rememberFieldParams(ast.newNumberLiteral("0"),
			                    ast.newPrimitiveType(PrimitiveType.BYTE),
			                    ast.newPrimitiveType(PrimitiveType.BYTE));
		}

		if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.DOUBLE)) {
			rememberFieldParams(ast.newNumberLiteral("0.0"),
			                    ast.newPrimitiveType(PrimitiveType.DOUBLE),
			                    ast.newPrimitiveType(PrimitiveType.DOUBLE));
		}

		if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.FLOAT)) {
			rememberFieldParams(ast.newNumberLiteral("0.0f"),
			                    ast.newPrimitiveType(PrimitiveType.FLOAT),
			                    ast.newPrimitiveType(PrimitiveType.FLOAT));
		}

		if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.INT)) {
			rememberFieldParams(ast.newNumberLiteral("0"),
			                    ast.newPrimitiveType(PrimitiveType.INT),
			                    ast.newPrimitiveType(PrimitiveType.INT));
		}

		if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.LONG)) {
			rememberFieldParams(ast.newNumberLiteral("0l"),
			                    ast.newPrimitiveType(PrimitiveType.LONG),
			                    ast.newPrimitiveType(PrimitiveType.LONG));
		}

		if (primitiveType.getPrimitiveTypeCode().equals(PrimitiveType.SHORT)) {

			rememberFieldParams(ast.newNumberLiteral("0"),
			                    ast.newPrimitiveType(PrimitiveType.SHORT),
			                    ast.newPrimitiveType(PrimitiveType.SHORT));
		}
		return ast.newSimpleName(currentFieldName);
	}

	/**
	 * Wrapper method for setting the stub field and setter parameters. Although
	 * types for setter and field are the same, they should be generated twice,
	 * since the structure of the AST API does not allow to use same AST object
	 * for different nodes.
	 * 
	 * @param fieldValue
	 *            value of the field to generate.
	 * @param fieldType
	 *            type of the field to generate.
	 * @param fieldSetterType
	 *            type of the setter to generate.
	 */
	private void rememberFieldParams(Expression fieldValue, Type fieldType,
	        Type fieldSetterType) {
		stubFields.add(new StubField(currentFieldName, fieldValue, fieldType,
		        fieldSetterType));
	}

	/**
	 * Does some tricky string manipulation to get name of the field.
	 * 
	 * @param method
	 *            method for which name should be created.
	 */
	private void generateFieldName(Method method) {
		String fieldName = method.getName();
		String methodDescriptor = org.objectweb.asm.Type.getMethodDescriptor(method);
		String[] methodParams = methodDescriptor.replace(')', ';').replace('(', ';').split(";");

		for (int i = 1; i < methodParams.length - 1; i++) {
			String[] temp = methodParams[i].split("/");
			String paramName = temp[temp.length - 1];

			if (!paramName.equals(""))
				fieldName += "_" + paramName.replace('[', 'a');
		}
		currentFieldName = fieldName;
	}

	/**
	 * Generates simplest constructor calls.
	 * 
	 * @param clazz
	 *            type for which constructor call should be created.
	 * @return constructor call or null if not able to generate simple
	 *         constructor call.
	 */
	private Expression generateConstructorCall(Class<?> clazz) {

		// Check if class can be instantiated.
		if (canInstantiate(clazz)) {
			// let's leave it that way at the moment. 
			// ClassInstanceCreation cic = ast.newClassInstanceCreation();
			NullLiteral cic = ast.newNullLiteral();
			Type classType = ast.newSimpleType(ast.newName(clazz.getCanonicalName()));
			Type classType2 = ast.newSimpleType(ast.newName(clazz.getCanonicalName()));
			//cic.setType(classType);
			rememberFieldParams(cic, classType, classType2);

			return ast.newSimpleName(currentFieldName);
		}

		// Return null literal, if constructor can not be created.
		return ast.newNullLiteral();
	}

	/**
	 * Check if there are any constructors, that can be used.
	 * 
	 * @param clazz
	 *            class to check for constructors.
	 * @return true, if it is possible to use constructors, false otherwise.
	 */
	private boolean canInstantiate(Class<?> clazz) {

		// If class is abstract, then return null, since it's not
		// possible to instantiate abstract class. 
		if (Modifier.isAbstract(clazz.getModifiers()))
			return false;

		// Check if constructor without parameters exists. 
		Constructor<?>[] constructors = clazz.getConstructors();
		for (Constructor<?> c : constructors) {
			if (c.getParameterTypes().length == 0)
				return true;
		}

		return false;
	}
}
