/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.testcarver.testcase;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ResourceList;
import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.capture.CaptureUtil;
import org.evosuite.testcarver.codegen.ICodeGenerator;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.FieldReference;
import org.evosuite.testcase.variable.NullReference;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.statements.ClassPrimitiveStatement;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.FieldStatement;
import org.evosuite.testcase.statements.ImmutableStringPrimitiveStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.NullStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericField;
import org.evosuite.utils.generic.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EvoTestCaseCodeGenerator implements ICodeGenerator<TestCase> {
	
	private static final Logger logger = LoggerFactory.getLogger(EvoTestCaseCodeGenerator.class);
	//--- source generation
	private TestCase testCase;

	private final Map<Integer, VariableReference> oidToVarRefMap;

	public EvoTestCaseCodeGenerator() {
		this.oidToVarRefMap = new HashMap<>();
	}
	
	@Override
	public boolean isMaximumLengthReached() {
		return testCase.size() > Properties.CHROMOSOME_LENGTH;
	}

	@Override
	public void createMethodCallStmt(final CaptureLog log, final int logRecNo) {
		if(log == null)
			throw new IllegalArgumentException("captured log must not be null");
		if(logRecNo <= -1)
			throw new IllegalArgumentException("log record number is invalid: " + logRecNo);
		if(isMaximumLengthReached())
			return;
		
		// assumption: all necessary statements are created and there is one variable for each referenced object
		final int oid = log.objectIds.get(logRecNo);
		final Object[] methodArgs = log.params.get(logRecNo);
		final String methodName = log.methodNames.get(logRecNo);
		Class<?> type;
		try {
			final String typeName = log.getTypeName(oid);
			type = getClassForName(typeName);

			logger.debug("Creating method call statement for call to method {}.{}", typeName, methodName);

			final Class<?>[] methodParamTypeClasses = getMethodParamTypeClasses(log, logRecNo);
			final ArrayList<VariableReference> args = getArguments(methodArgs,
					methodParamTypeClasses);


			if (CaptureLog.OBSERVED_INIT.equals(methodName)) {
				// Person var0 = new Person();
				final ConstructorStatement constStmt = new ConstructorStatement(
						testCase,
						new GenericConstructor(
								type.getDeclaredConstructor(methodParamTypeClasses), type),
						args);

				this.oidToVarRefMap.put(oid, testCase.addStatement(constStmt));
			} else {
				//------------------ handling for ordinary method calls e.g. var1 = var0.doSth();
				final Object returnValue = log.returnValues.get(logRecNo);

				if (CaptureLog.RETURN_TYPE_VOID.equals(returnValue)) {

					GenericMethod genericMethod = new GenericMethod(
							this.getDeclaredMethod(type, methodName,
									methodParamTypeClasses)
							, type);

					MethodStatement m = new MethodStatement(testCase, genericMethod,
							this.oidToVarRefMap.get(oid), args);

					testCase.addStatement(m);
				} else {
					// final org.objectweb.asm.Type returnType = org.objectweb.asm.Type.getReturnType(methodDesc);
					logger.debug("Callee: {} ({})", this.oidToVarRefMap.get(oid), this.oidToVarRefMap.keySet());
					// Person var0 = var.getPerson();
					final MethodStatement m = new MethodStatement(
							testCase,
							new GenericMethod(
									this.getDeclaredMethod(type, methodName,
											methodParamTypeClasses), type),

							this.oidToVarRefMap.get(oid),
							args);
					final Integer returnValueOID = (Integer) returnValue;
					this.oidToVarRefMap.put(returnValueOID, testCase.addStatement(m));
				}
			}
		} catch(NoSuchMethodException e) {
			logger.info("Method not found; this may happen e.g. if an exception is thrown in the constructor");
			return;
		} catch (final Exception e) {
			logger.info("Error at log record number {}: {}", logRecNo, e.toString());
			logger.info("Test case so far: "+testCase.toCode());
			logger.info(log.toString());

			CodeGeneratorException.propagateError(e, "[logRecNo = %s] - an unexpected error occurred while creating method call stmt for %s.", logRecNo, methodName);
		}
	}

	private Class<?>[] getMethodParamTypeClasses(CaptureLog log, int logRecNo) {
		final String methodDesc = log.descList.get(logRecNo);
		final org.objectweb.asm.Type[] methodParamTypes = org.objectweb.asm.Type.getArgumentTypes(methodDesc);

		final Class<?>[] methodParamTypeClasses = new Class[methodParamTypes.length];
		for (int i = 0; i < methodParamTypes.length; i++) {
			methodParamTypeClasses[i] = getClassFromType(methodParamTypes[i]);
		}
		return methodParamTypeClasses;
	}

	private ArrayList<VariableReference> getArguments(final Object[] methodArgs,
	        final Class<?>[] methodParamTypeClasses) throws IllegalArgumentException {

		ArrayList<VariableReference> args = new ArrayList<VariableReference>();

		Integer argOID; // is either an oid or null
		for (int i = 0; i < methodArgs.length; i++) {
			argOID = (Integer) methodArgs[i];
			if (argOID == null) {
				args.add(testCase.addStatement(new NullStatement(testCase,
				        methodParamTypeClasses[i])));
			} else {
				VariableReference ref = this.oidToVarRefMap.get(argOID);
				if (ref == null) {
					throw new RuntimeException("VariableReference is null for argOID "
					        + argOID+"; have oids: "+this.oidToVarRefMap.keySet());
				} else {
					args.add(ref);
				}
			}
		}
		return args;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void createPlainInitStmt(CaptureLog log, int logRecNo) {
		// NOTE: PLAIN INIT: has always one non-null param
		// TODO: use primitives
		final int oid = log.objectIds.get(logRecNo);

		if (this.oidToVarRefMap.containsKey(oid)) {
			// TODO this might happen because of Integer.valueOf(), for example. . Is this approach ok?
			return;
		}

		final String type = log.getTypeName(oid);
		final Object value = log.params.get(logRecNo)[0];

		final VariableReference varRef;

		if (value instanceof Class) {
			// final PrimitiveStatement cps = ClassPrimitiveStatement.getPrimitiveStatement(testCase, getClassForName(type));
			final PrimitiveStatement cps = new ClassPrimitiveStatement(testCase,
			        getClassForName(type));
			cps.setValue(value);

			varRef = testCase.addStatement(cps);
		} else {
			final PrimitiveStatement primitiveValue = PrimitiveStatement.getPrimitiveStatement(testCase,
			                                                                                   getClassForName(type));
			primitiveValue.setValue(value);

			varRef = testCase.addStatement(primitiveValue);
		}

		this.oidToVarRefMap.put(oid, varRef);
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void createUnobservedInitStmt(CaptureLog log, int logRecNo) {

		// NOTE: PLAIN INIT: has always one non-null param
		// TODO: use primitives
		final int oid = log.objectIds.get(logRecNo);

		try {

			final Object value = log.params.get(logRecNo)[0];
			final PrimitiveStatement stringRep = new ImmutableStringPrimitiveStatement(testCase, (String)value);
			final VariableReference stringRepRef = testCase.addStatement(stringRep);

			final MethodStatement m = new MethodStatement(testCase, new GenericMethod(EvoSuiteXStream.class.getMethod("fromString", new Class<?>[] {String.class}), EvoSuiteXStream.class), null, Arrays.asList(stringRepRef));
			this.oidToVarRefMap.put(oid, testCase.addStatement(m));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void createFieldWriteAccessStmt(CaptureLog log, int logRecNo) {
		// assumption: all necessary statements are created and there is one variable for each referenced object

		final Object[] methodArgs = log.params.get(logRecNo);
		final int oid = log.objectIds.get(logRecNo);
		final int captureId = log.captureIds.get(logRecNo);

		final String fieldName = log.getNameOfAccessedFields(captureId);
		final String typeName = log.getTypeName(oid);

		try {
			final Class<?> type = getClassForName(typeName);

			final String fieldDesc = log.descList.get(logRecNo);
			final Class<?> fieldType = CaptureUtil.getClassFromDesc(fieldDesc);

			final FieldReference targetFieldRef = new FieldReference(testCase,
			        new GenericField(this.getDeclaredField(type, fieldName), type), this.oidToVarRefMap.get(oid));

			final AssignmentStatement assignment;

			final Integer arg = (Integer) methodArgs[0];
			if (arg == null) {
				final NullStatement nullStmt = new NullStatement(testCase, fieldType);
				final VariableReference nullReference = testCase.addStatement(nullStmt);

				assignment = new AssignmentStatement(testCase, targetFieldRef,
				        nullReference);
			} else {
				assignment = new AssignmentStatement(testCase, targetFieldRef,
				        this.oidToVarRefMap.get(arg));
			}
			final VariableReference varRef = testCase.addStatement(assignment);
			logger.debug("Adding assignment statement: "+assignment.getCode());
			if (arg != null) {
				this.oidToVarRefMap.put(arg, varRef);
			}
		} catch (final Exception e) {
			CodeGeneratorException.propagateError(e, "[logRecNo = %s] - an unexpected error occurred while creating field write access stmt. Log: %s", logRecNo, log);
		}
	}

	@Override
	public void createFieldReadAccessStmt(CaptureLog log, int logRecNo) {
		// assumption: all necessary statements are created and there is one variable for reach referenced object
		final int oid = log.objectIds.get(logRecNo);
		final int captureId = log.captureIds.get(logRecNo);

		final Object returnValue = log.returnValues.get(logRecNo);
		if (!CaptureLog.RETURN_TYPE_VOID.equals(returnValue)) // TODO necessary?
		{
			Integer returnValueOID = (Integer) returnValue;
//			final String descriptor = log.descList.get(logRecNo);
//			final org.objectweb.asm.Type fieldTypeType = org.objectweb.asm.Type.getType(descriptor);
			final String typeName = log.getTypeName(oid);
			final String fieldName = log.getNameOfAccessedFields(captureId);

			try {
//				final Class<?> fieldType = getClassFromType(fieldTypeType);
				final Class<?> type = getClassForName(typeName);

//				final FieldReference valueRef = new FieldReference(testCase,
//				        new GenericField(getDeclaredField(type, fieldName), type));
//				final VariableReference targetVar = new VariableReferenceImpl(testCase,
//				        fieldType);

				final FieldStatement fieldStatement = new FieldStatement(testCase, new GenericField(FieldUtils.getField(type, fieldName, true), type), this.oidToVarRefMap.get(oid));
				//final AssignmentStatement assignment = new AssignmentStatement(testCase,
				//        targetVar, valueRef);
				// VariableReference varRef = testCase.addStatement(assignment);
				VariableReference varRef = testCase.addStatement(fieldStatement);

				this.oidToVarRefMap.put(returnValueOID, varRef);

			} catch (final Exception e) {
				logger.debug("Error while trying to get field "
				                                          + fieldName + " of class "
				                                          + getClassForName(typeName)+": "+e);
				CodeGeneratorException.propagateError(e, "[logRecNo = %s] - an unexpected error occurred while creating field read access stmt. Log: %s", logRecNo, log);
			}
		}
	}

	private final Class<?> getClassFromType(final org.objectweb.asm.Type type) {

		if (type.equals(org.objectweb.asm.Type.BOOLEAN_TYPE)) {
			return Boolean.TYPE;
		} else if (type.equals(org.objectweb.asm.Type.BYTE_TYPE)) {
			return Byte.TYPE;
		} else if (type.equals(org.objectweb.asm.Type.CHAR_TYPE)) {
			return Character.TYPE;
		} else if (type.equals(org.objectweb.asm.Type.DOUBLE_TYPE)) {
			return Double.TYPE;
		} else if (type.equals(org.objectweb.asm.Type.FLOAT_TYPE)) {
			return Float.TYPE;
		} else if (type.equals(org.objectweb.asm.Type.INT_TYPE)) {
			return Integer.TYPE;
		} else if (type.equals(org.objectweb.asm.Type.LONG_TYPE)) {
			return Long.TYPE;
		} else if (type.equals(org.objectweb.asm.Type.SHORT_TYPE)) {
			return Short.TYPE;
		} else if (type.getSort() == org.objectweb.asm.Type.ARRAY) {
			final org.objectweb.asm.Type elementType = type.getElementType();
			int[] dimensions = new int[type.getDimensions()];
			
			if (elementType.equals(org.objectweb.asm.Type.BOOLEAN_TYPE)) {
				return Array.newInstance(boolean.class, dimensions).getClass();
			} else if (elementType.equals(org.objectweb.asm.Type.BYTE_TYPE)) {
				return Array.newInstance(byte.class, dimensions).getClass();
			} else if (elementType.equals(org.objectweb.asm.Type.CHAR_TYPE)) {
				return Array.newInstance(char.class, dimensions).getClass();
			} else if (elementType.equals(org.objectweb.asm.Type.DOUBLE_TYPE)) {
				return Array.newInstance(double.class, dimensions).getClass();
			} else if (elementType.equals(org.objectweb.asm.Type.FLOAT_TYPE)) {
				return Array.newInstance(float.class, dimensions).getClass();
			} else if (elementType.equals(org.objectweb.asm.Type.INT_TYPE)) {
				return Array.newInstance(int.class, dimensions).getClass();
			} else if (elementType.equals(org.objectweb.asm.Type.LONG_TYPE)) {
				return Array.newInstance(long.class, dimensions).getClass();
			} else if (elementType.equals(org.objectweb.asm.Type.SHORT_TYPE)) {
				return Array.newInstance(short.class, dimensions).getClass();
			}
		}

		//		try 
		//		{
		return getClassForName(type.getClassName());
		//			return Class.forName(type.getClassName(), true, StaticTestCluster.classLoader);
		//		} 
		//		catch (final ClassNotFoundException e) 
		//		{
		//			throw new RuntimeException(e);
		//		}
	}

	private final Class<?> getClassForName(String type) {
		try {
			if (type.equals("boolean") || type.equals("java.lang.Boolean")) {
				return Boolean.TYPE;
			} else if (type.equals("byte") || type.equals("java.lang.Byte")) {
				return Byte.TYPE;
			} else if (type.equals("char") || type.equals("java.lang.Character")) {
				return Character.TYPE;
			} else if (type.equals("double") || type.equals("java.lang.Double")) {
				return Double.TYPE;
			} else if (type.equals("float") || type.equals("java.lang.Float")) {
				return Float.TYPE;
			} else if (type.equals("int") || type.equals("java.lang.Integer")) {
				return Integer.TYPE;
			} else if (type.equals("long") || type.equals("java.lang.Long")) {
				return Long.TYPE;
			} else if (type.equals("short") || type.equals("java.lang.Short")) {
				return Short.TYPE;
			} else if (type.equals("String")) {
				return Class.forName("java.lang." + type, true,
				                     TestGenerationContext.getInstance().getClassLoaderForSUT());
			}

			if (type.endsWith("[]")) {
				// see http://stackoverflow.com/questions/3442090/java-what-is-this-ljava-lang-object
				
				final StringBuilder arrayTypeNameBuilder = new StringBuilder(30);
				
				int index = 0;
				while((index = type.indexOf('[', index)) != -1)
				{
					arrayTypeNameBuilder.append('[');
					index++;
				}
				
				arrayTypeNameBuilder.append('L'); // always needed for Object arrays
				
				// remove bracket from type name get array component type
				type = type.replace("[]", "");
				arrayTypeNameBuilder.append(type);

				arrayTypeNameBuilder.append(';'); // finalize object array name
				
				return Class.forName(arrayTypeNameBuilder.toString(), true,
				                     TestGenerationContext.getInstance().getClassLoaderForSUT());
			} else {
				return Class.forName(ResourceList.getClassNameFromResourcePath(type), true,
				                     TestGenerationContext.getInstance().getClassLoaderForSUT());
			}
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private Field getDeclaredField(final Class<?> clazz, final String fieldName)
	        throws NoSuchFieldException {
		if (clazz == null || Object.class.equals(clazz)) {
			throw new NoSuchFieldException(fieldName);
		}

		try {
			final Field field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			return field;
		} catch (final NoSuchFieldException e) {
			return getDeclaredField(clazz.getSuperclass(), fieldName);
		}
	}

	private Method getDeclaredMethod(final Class<?> clazz, final String methodName,
	        Class<?>[] paramTypes) throws NoSuchMethodException {
		// logger.info("Trying to get method "+methodName +" from class "+clazz+" with parameters "+Arrays.asList(paramTypes));
		if (clazz == null || Object.class.equals(clazz)) {
			throw new NoSuchMethodException(methodName + "(" + Arrays.toString(paramTypes)
			        + ")");
		}

		try {
			final Method m = clazz.getDeclaredMethod(methodName, paramTypes);
			m.setAccessible(true);
			return m;
		} catch (final NoSuchMethodException e) {			
			//logger.info("Not found {}, available methods: {}", methodName, Arrays.asList(clazz.getDeclaredMethods()));
			return getDeclaredMethod(clazz.getSuperclass(), methodName, paramTypes);
		}
	}

	@Override
	public void createArrayInitStmt(final CaptureLog log, final int logRecNo) {
		final int oid = log.objectIds.get(logRecNo);

		final Object[] params = log.params.get(logRecNo);
		final String arrTypeName = log.getTypeName(oid);
		final Class<?> arrType = getClassForName(arrTypeName);

		// --- create array instance creation e.g. int[] var = new int[10];

		final ArrayReference arrRef;

		// create array only once
		if (this.oidToVarRefMap.containsKey(oid)) {
			arrRef = (ArrayReference) this.oidToVarRefMap.get(oid);
		} else {
			arrRef = new ArrayReference(testCase, arrType);
			final ArrayStatement arrStmt = new ArrayStatement(testCase, arrRef);
			arrStmt.setSize(params.length);
			testCase.addStatement(arrStmt);
			this.oidToVarRefMap.put(oid, arrRef);
		}

		final Class<?> arrCompClass = arrType.getComponentType();

		AssignmentStatement assignStmt;
		ArrayIndex arrIndex;
		VariableReference valueRef;
		Integer argOID; // is either an oid or null
		for (int i = 0; i < params.length; i++) {
			argOID = (Integer) params[i];
			if (argOID == null) {
				valueRef = testCase.addStatement(new NullStatement(testCase, arrCompClass));
			} else {
				valueRef = this.oidToVarRefMap.get(argOID);
				if(valueRef == null) {
					logger.info("ValueREF is NULL for "+argOID);
					continue;
				}
			}

			arrIndex = new ArrayIndex(testCase, arrRef, i);
			assignStmt = new AssignmentStatement(testCase, arrIndex, valueRef);
			testCase.addStatement(assignStmt);
			logger.debug("Adding assignment (array): "+assignStmt.getCode());
		}
	}

	private boolean hasDefaultConstructor(final Class<?> clazz) {
		for (final Constructor<?> c : clazz.getConstructors()) {
			if (c.getParameterTypes().length == 0 && Modifier.isPublic(c.getModifiers())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void createCollectionInitStmt(final CaptureLog log, final int logRecNo) {
		try 
		{
			final int oid = log.objectIds.get(logRecNo);
			final Object[] params = log.params.get(logRecNo);
			String collTypeName = log.getTypeName(oid);
			Class<?> collType = getClassForName(collTypeName);

			// -- determine if an alternative collection must be used for code generation
			final boolean isPublic = java.lang.reflect.Modifier.isPublic(collType.getModifiers());
			if (! isPublic || !hasDefaultConstructor(collType)) {
				if (Set.class.isAssignableFrom(collType)) {
					collTypeName = HashSet.class.getName();
					collType = HashSet.class;
				} else if (List.class.isAssignableFrom(collType)) {
					collTypeName = ArrayList.class.getName();
					collType = ArrayList.class;
				} else if (Queue.class.isAssignableFrom(collType)) {
					collTypeName = ArrayDeque.class.getName();
					collType = ArrayDeque.class;
				} else {
					CodeGeneratorException.propagateError("[logRecNo = %s] - collection %s is not supported", logRecNo, collType);
				}
			}

			// -- create code for instantiating collection

			final List<VariableReference> noParams = Collections.emptyList();
			final ConstructorStatement constrStmt = new ConstructorStatement(testCase,
			        new GenericConstructor(collType.getConstructor(new Class<?>[0]),
			                collType), noParams);

			final VariableReference collRef = testCase.addStatement(constrStmt);
			this.oidToVarRefMap.put(oid, collRef);

			// --- fill collection

			MethodStatement methodStmt;
			Integer argOID; // is either an oid or null
			ArrayList<VariableReference> paramList;
			Method method;

			for (int i = 0; i < params.length; i++) 
			{
				paramList = new ArrayList<VariableReference>(1);
				argOID = (Integer) params[i];
				if (argOID == null || !this.oidToVarRefMap.containsKey(argOID)) {
					VariableReference var = testCase.addStatement(new NullStatement(testCase,
					        Object.class));
					paramList.add(var);
				} else {
					VariableReference var = this.oidToVarRefMap.get(argOID); 
					paramList.add(var);
				}

				method = collType.getMethod("add", Object.class);
				methodStmt = new MethodStatement(testCase, new GenericMethod(method,
				        collType), collRef, paramList);
				testCase.addStatement(methodStmt);
			}
		} 
		catch (final Exception e) {
			CodeGeneratorException.propagateError("[logRecNo = %s] - an unexpected error occurred while creating collection init stmt", logRecNo,  e);
		}
	}

	private void replaceNullWithNullReferences(List<VariableReference> paramList, Class<?> ... paramTypes)
	{
		CodeGeneratorException.check(paramList.size() == paramTypes.length, "[paramList = %s, paramTypes] - number of params does not correspond number of paramTypes", paramList, Arrays.toString(paramTypes));
		
		Object v;
		for(int j = 0; j < paramList.size(); j++)
		{
			v = paramList.get(j);
			if(v == null)
			{
				paramList.set(j, new NullReference(testCase, paramTypes[j]));
			}
		}

	}
	
	@Override
	public void createMapInitStmt(final CaptureLog log, final int logRecNo) {
		try {
			final int oid = log.objectIds.get(logRecNo);
			final Object[] params = log.params.get(logRecNo);
			String collTypeName = log.getTypeName(oid);
			Class<?> collType = getClassForName(collTypeName);

			// -- determine if an alternative collection must be used for code generation
			final boolean isPublic= java.lang.reflect.Modifier.isPublic(collType.getModifiers());
			if (!isPublic|| !hasDefaultConstructor(collType)) {
				collType = HashMap.class;
			}

			// -- create code for instantiating collection

			final List<VariableReference> noParams = Collections.emptyList();
			final ConstructorStatement constrStmt = new ConstructorStatement(testCase,
			        new GenericConstructor(collType.getConstructor(new Class<?>[0]),
			                collType), noParams);

			final VariableReference collRef = testCase.addStatement(constrStmt);
			this.oidToVarRefMap.put(oid, collRef);

			// --- fill collection

			MethodStatement methodStmt;
			Integer argOID; // is either an oid or null
			ArrayList<VariableReference> paramList = new ArrayList<VariableReference>();

			for (int i = 0; i < params.length; i++) {
				argOID = (Integer) params[i];
				if (argOID == null) {
										
					paramList.add(testCase.addStatement(new NullStatement(testCase,
					        Object.class)));
					
				} else {
					paramList.add(this.oidToVarRefMap.get(argOID));
				}

				if (i % 2 == 1) {

					final Method method = collType.getMethod("put", Object.class,
					                                         Object.class);
					
					replaceNullWithNullReferences(paramList, Object.class, Object.class);
					
					methodStmt = new MethodStatement(testCase, new GenericMethod(method,
					        collType), collRef, paramList);
					testCase.addStatement(methodStmt);
					paramList = new ArrayList<VariableReference>(2);
				}

			}
		} catch (final Exception e) {
			CodeGeneratorException.propagateError( e, "[logRecNo = %s] - an unexpected error occurred while creating map init stmt", logRecNo);
		}
	}

	@Override
	public void before(CaptureLog log) {
		this.testCase = new CarvedTestCase();
	}

	@Override
	public void after(CaptureLog log) {
	}

	@Override
	public TestCase getCode() {
		return this.testCase;
	}

	@Override
	public void clear() {
		this.testCase = null;
		this.oidToVarRefMap.clear();
	}

}