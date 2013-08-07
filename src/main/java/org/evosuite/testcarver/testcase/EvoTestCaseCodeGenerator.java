package org.evosuite.testcarver.testcase;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

import org.evosuite.TestGenerationContext;
import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.capture.CaptureUtil;
import org.evosuite.testcarver.codegen.ICodeGenerator;
import org.evosuite.testcase.ArrayIndex;
import org.evosuite.testcase.ArrayReference;
import org.evosuite.testcase.ArrayStatement;
import org.evosuite.testcase.AssignmentStatement;
import org.evosuite.testcase.ClassPrimitiveStatement;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.FieldReference;
import org.evosuite.testcase.FieldStatement;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.NullStatement;
import org.evosuite.testcase.PrimitiveStatement;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.VariableReference;
import org.evosuite.testcase.VariableReferenceImpl;
import org.evosuite.utils.GenericConstructor;
import org.evosuite.utils.GenericField;
import org.evosuite.utils.GenericMethod;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Utils;

public final class EvoTestCaseCodeGenerator implements ICodeGenerator<TestCase> {
	//--- source generation
	private TestCase testCase;
	private VariableReference xStreamRef;

	private final Map<Integer, VariableReference> oidToVarRefMap;

	public EvoTestCaseCodeGenerator() {
		this.oidToVarRefMap = new HashMap<Integer, VariableReference>();
	}

	@Override
	public void createMethodCallStmt(CaptureLog log, int logRecNo) {
		// assumption: all necessary statements are created and there is one variable for each referenced object
		final int oid = log.objectIds.get(logRecNo);
		final Object[] methodArgs = log.params.get(logRecNo);
		final String methodName = log.methodNames.get(logRecNo);

		final Class<?>[] methodParamTypeClasses = getMethodParamTypeClasses(log, logRecNo);
		final ArrayList<VariableReference> args = getArguments(methodArgs,
		                                                       methodParamTypeClasses);

		final String typeName = log.getTypeName(oid);
		Class<?> type;
		try {
			type = getClassForName(typeName);

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

					GenericMethod geneticMethod = new GenericMethod(
					        this.getDeclaredMethod(type, methodName,
					                               methodParamTypeClasses), type);

					MethodStatement m = new MethodStatement(testCase, geneticMethod,
					        this.oidToVarRefMap.get(oid), args);

					testCase.addStatement(m);
				} else {
					// final org.objectweb.asm.Type returnType = org.objectweb.asm.Type.getReturnType(methodDesc);

					// Person var0 = var.getPerson();
					final MethodStatement m = new MethodStatement(
					        testCase,
					        new GenericMethod(
					                this.getDeclaredMethod(type, methodName,
					                                       methodParamTypeClasses), type),
					        this.oidToVarRefMap.get(oid), args);

					final Integer returnValueOID = (Integer) returnValue;
					this.oidToVarRefMap.put(returnValueOID, testCase.addStatement(m));
				}
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
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
					        + argOID);
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void createUnobservedInitStmt(CaptureLog log, int logRecNo) {

		// NOTE: PLAIN INIT: has always one non-null param
		// TODO: use primitives
		final int oid = log.objectIds.get(logRecNo);

		try {

			final Class<?> xStreamType = getClassForName("com.thoughtworks.xstream.XStream");//Class.forName("com.thoughtworks.xstream.XStream", true, StaticTestCluster.classLoader);

			final Object value = log.params.get(logRecNo)[0];

			if (xStreamRef == null) {
				final ConstructorStatement constr = new ConstructorStatement(
				        testCase,
				        new GenericConstructor(
				                xStreamType.getConstructor(new Class<?>[0]), xStreamType),
				        Collections.EMPTY_LIST);
				xStreamRef = testCase.addStatement(constr);
			}

			final Class<?> stringType = getClassForName("java.lang.String");//Class.forName("java.lang.String", true, StaticTestCluster.classLoader);

			final PrimitiveStatement stringRep = PrimitiveStatement.getPrimitiveStatement(testCase,
			                                                                              stringType);
			stringRep.setValue(value);
			final VariableReference stringRepRef = testCase.addStatement(stringRep);

			final MethodStatement m = new MethodStatement(testCase, new GenericMethod(
			        xStreamType.getMethod("fromXML", stringType), xStreamType),
			        xStreamRef,
			        //			        xStreamType.getMethod("fromXML", stringType), typeClass), xStreamRef,
			        Arrays.asList(stringRepRef));

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
			if (arg != null) {
				this.oidToVarRefMap.put(arg, varRef);
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
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
			final String descriptor = log.descList.get(logRecNo);
			final org.objectweb.asm.Type fieldTypeType = org.objectweb.asm.Type.getType(descriptor);
			final String typeName = log.getTypeName(oid);
			final String fieldName = log.getNameOfAccessedFields(captureId);

			try {
				final Class<?> fieldType = getClassFromType(fieldTypeType); //Class.forName(fieldTypeName, true, StaticTestCluster.classLoader);
				final Class<?> type = getClassForName(typeName);// Class.forName(typeName, true, StaticTestCluster.classLoader);

				final FieldReference valueRef = new FieldReference(testCase,
				        new GenericField(getDeclaredField(type, fieldName), type));
				final VariableReference targetVar = new VariableReferenceImpl(testCase,
				        fieldType);

				
				final FieldStatement fieldStatement = new FieldStatement(testCase, new GenericField(type.getField(fieldName), type), this.oidToVarRefMap.get(oid));
				//final AssignmentStatement assignment = new AssignmentStatement(testCase,
				//        targetVar, valueRef);
				// VariableReference varRef = testCase.addStatement(assignment);
				VariableReference varRef = testCase.addStatement(fieldStatement);

				this.oidToVarRefMap.put(returnValueOID, varRef);

			} catch (final Exception e) {
				LoggingUtils.getEvoLogger().debug("Error while trying to get field "
				                                          + fieldName + " of class "
				                                          + getClassForName(typeName));
				throw new RuntimeException(e);
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

			if (elementType.equals(org.objectweb.asm.Type.BOOLEAN_TYPE)) {
				return boolean[].class;
			} else if (elementType.equals(org.objectweb.asm.Type.BYTE_TYPE)) {
				return byte[].class;
			} else if (elementType.equals(org.objectweb.asm.Type.CHAR_TYPE)) {
				return char[].class;
			} else if (elementType.equals(org.objectweb.asm.Type.DOUBLE_TYPE)) {
				return double[].class;
			} else if (elementType.equals(org.objectweb.asm.Type.FLOAT_TYPE)) {
				return float[].class;
			} else if (elementType.equals(org.objectweb.asm.Type.INT_TYPE)) {
				return int[].class;
			} else if (elementType.equals(org.objectweb.asm.Type.LONG_TYPE)) {
				return long[].class;
			} else if (elementType.equals(org.objectweb.asm.Type.SHORT_TYPE)) {
				return short[].class;
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
			if (type.equals("boolean") || type.equals("Boolean")) {
				return Boolean.TYPE;
			} else if (type.equals("byte") || type.equals("Byte")) {
				return Byte.TYPE;
			} else if (type.equals("char") || type.equals("Character")) {
				return Character.TYPE;
			} else if (type.equals("double") || type.equals("Double")) {
				return Double.TYPE;
			} else if (type.equals("float") || type.equals("Float")) {
				return Float.TYPE;
			} else if (type.equals("int") || type.equals("Integer")) {
				return Integer.TYPE;
			} else if (type.equals("long") || type.equals("Long")) {
				return Long.TYPE;
			} else if (type.equals("short") || type.equals("Short")) {
				return Short.TYPE;
			} else if (type.equals("String")) {
				return Class.forName("java.lang." + type, true,
				                     TestGenerationContext.getClassLoader());
			}

			if (type.endsWith("[]")) {
				type = type.replace("[]", "");
				return Class.forName("[L" + type + ";", true,
				                     TestGenerationContext.getClassLoader());
			} else {
				return Class.forName(Utils.getClassNameFromResourcePath(type), true,
				                     TestGenerationContext.getClassLoader());
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
	        Class<?>[] paramTypes) throws NoSuchFieldException {
		if (clazz == null || Object.class.equals(clazz)) {
			throw new NoSuchFieldException(methodName + "(" + Arrays.toString(paramTypes)
			        + ")");
		}

		try {
			final Method m = clazz.getDeclaredMethod(methodName, paramTypes);
			m.setAccessible(true);
			return m;
		} catch (final NoSuchMethodException e) {
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

		// create array access statements var[0] = var1;
		//		final  String                   methodDesc       = log.descList.get(logRecNo);
		//		final  org.objectweb.asm.Type[] methodParamTypes = org.objectweb.asm.Type.getArgumentTypes(methodDesc);

		final Class<?> arrCompClass = arrType.getComponentType();
		//		
		//		final Class<?>[] methodParamTypeClasses = new Class[methodParamTypes.length];
		//		for(int i = 0; i < methodParamTypes.length; i++)
		//		{
		//			
		//			methodParamTypeClasses[i] = getClassFromType(methodParamTypes[i]);
		//		}

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
			}

			arrIndex = new ArrayIndex(testCase, arrRef, i);
			assignStmt = new AssignmentStatement(testCase, arrIndex, valueRef);
			testCase.addStatement(assignStmt);
		}
	}

	private boolean hasDefaultConstructor(final Class<?> clazz) {
		for (final Constructor<?> c : clazz.getConstructors()) {
			if (c.getParameterTypes().length == 0) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void createCollectionInitStmt(final CaptureLog log, final int logRecNo) {
		try {
			final int oid = log.objectIds.get(logRecNo);
			final Object[] params = log.params.get(logRecNo);
			String collTypeName = log.getTypeName(oid);
			final Class<?> collType = getClassForName(collTypeName);

			// -- determine if an alternative collection must be used for code generation
			final boolean isPrivate = java.lang.reflect.Modifier.isPrivate(collType.getModifiers());
			if (isPrivate || !hasDefaultConstructor(collType)) {
				if (Set.class.isAssignableFrom(collType)) {
					collTypeName = HashSet.class.getName();
				} else if (List.class.isAssignableFrom(collType)) {
					collTypeName = ArrayList.class.getName();
				} else if (Queue.class.isAssignableFrom(collType)) {
					collTypeName = ArrayDeque.class.getName();
				} else {
					throw new RuntimeException("Collection " + collType
					        + " is not supported");
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

			final Class<?>[] methodParamTypeClasses = getMethodParamTypeClasses(log,
			                                                                    logRecNo);

			MethodStatement methodStmt;
			Integer argOID; // is either an oid or null
			ArrayList<VariableReference> paramList;
			Method method;
			for (int i = 0; i < params.length; i++) {
				paramList = new ArrayList<VariableReference>(1);
				argOID = (Integer) params[i];
				if (argOID == null) {
					paramList.add(testCase.addStatement(new NullStatement(testCase,
					        methodParamTypeClasses[i])));
				} else {
					paramList.add(this.oidToVarRefMap.get(argOID));
				}

				method = collType.getMethod("add", Object.class);
				methodStmt = new MethodStatement(testCase, new GenericMethod(method,
				        collType), collRef, paramList);
				testCase.addStatement(methodStmt);
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
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
			final boolean isPrivate = java.lang.reflect.Modifier.isPrivate(collType.getModifiers());
			if (isPrivate || !hasDefaultConstructor(collType)) {
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

			final Class<?>[] methodParamTypeClasses = getMethodParamTypeClasses(log,
			                                                                    logRecNo);

			MethodStatement methodStmt;
			Integer argOID; // is either an oid or null
			ArrayList<VariableReference> paramList = new ArrayList<VariableReference>(2);

			for (int i = 0; i < params.length; i++) {
				argOID = (Integer) params[i];
				if (argOID == null) {
					paramList.add(testCase.addStatement(new NullStatement(testCase,
					        methodParamTypeClasses[i])));
				} else {
					paramList.add(this.oidToVarRefMap.get(argOID));
				}

				if (i % 2 == 1) {

					//					final MethodStatement m = new MethodStatement(testCase, 
					//							  this.getDeclaredMethod(type, methodName, methodParamTypeClasses), 
					//							  this.oidToVarRefMap.get(oid), 
					//							  type.getMethod(methodName, methodParamTypeClasses).getReturnType(), 
					//							  args);
					//					testCase.addStatement(m);

					final Method method = collType.getMethod("put", Object.class,
					                                         Object.class);
					methodStmt = new MethodStatement(testCase, new GenericMethod(method,
					        collType), collRef, paramList);

					//					methodStmt = new MethodStatement(testCase, collType.getMethod("put", Object.class, Object.class), collRef, collType, paramList);
					testCase.addStatement(methodStmt);
					paramList = new ArrayList<VariableReference>(2);
				}

			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void before(CaptureLog log) {
		this.testCase = new DefaultTestCase();
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
		this.xStreamRef = null;
		this.oidToVarRefMap.clear();
	}

}