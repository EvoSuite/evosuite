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
package org.evosuite.testcarver.testcase;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.evosuite.TestGenerationContext;
import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.capture.CaptureUtil;
import org.evosuite.testcase.AssignmentStatement;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.FieldReference;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.NullStatement;
import org.evosuite.testcase.PrimitiveStatement;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.VariableReference;
import org.evosuite.testcase.VariableReferenceImpl;
import org.evosuite.utils.GenericConstructor;
import org.evosuite.utils.GenericField;
import org.evosuite.utils.GenericMethod;

@Deprecated
public class TestCaseCodeGenerator {
	private final CaptureLog log;

	//--- source generation

	public TestCaseCodeGenerator(final CaptureLog log) {
		if (log == null) {
			throw new NullPointerException();
		}

		this.log = log.clone();
	}

	// FIXME specifying classes here might not be needed anymore, if we only instrument observed classes...
	public TestCase generateCode(final Class<?>... observedClasses) {
		if (observedClasses == null || observedClasses.length == 0) {
			throw new IllegalArgumentException("No observed classes specified");
		}

		//--- 1. step: extract class names
		final HashSet<String> observedClassNames = new HashSet<String>();
		for (int i = 0; i < observedClasses.length; i++) {
			observedClassNames.add(observedClasses[i].getName());
		}

		//--- 2. step: get all oids of the instances of the observed classes
		//    NOTE: They are implicitly sorted by INIT_REC_NO because of the natural object creation order captured by the 
		//    instrumentation
		final TIntArrayList targetOIDs = new TIntArrayList();
		final int numInfoRecs = this.log.oidClassNames.size();
		for (int i = 0; i < numInfoRecs; i++) {
			if (observedClassNames.contains(this.log.oidClassNames.get(i))) {
				targetOIDs.add(this.log.oids.getQuick(i));
			}
		}

		//--- 3. step: init compilation unit

		final TestCase testCase = new DefaultTestCase();

		// no invocations on objects of observed classes -> return empty but compilable CompilationUnit
		if (targetOIDs.isEmpty()) {
			return testCase;
		}

		//--- 4. step: generating code starting with OID with lowest log rec no.

		final int numLogRecords = this.log.objectIds.size();
		int currentOID = targetOIDs.getQuick(0);

		int captureId = -1;

		// TODO knowing last logRecNo for termination criterion belonging to an observed instance would prevent processing unnecessary statements
		for (int currentRecord = this.log.oidRecMapping.get(currentOID); currentRecord < numLogRecords; currentRecord++) {
			currentOID = this.log.objectIds.getQuick(currentRecord);

			if (targetOIDs.contains(currentOID)) {
				this.restorceCodeFromLastPosTo(currentOID, currentRecord, testCase);

				// forward to end of method call sequence
				captureId = this.log.captureIds.getQuick(currentRecord);

				while (!(this.log.objectIds.getQuick(currentRecord) == currentOID
				        && this.log.captureIds.getQuick(currentRecord) == captureId && this.log.methodNames.get(currentRecord).equals(CaptureLog.END_CAPTURE_PSEUDO_METHOD))) {
					currentRecord++;
				}

				// each method call is considered as object state modification -> so save last object modification
				
				log.updateWhereObjectWasInitializedFirst(currentOID, currentRecord);		
				
			}
		}

		return testCase;
	}

	private void restorceCodeFromLastPosTo(final int oid, final int end, TestCase testCase) {
		final int oidInfoRecNo = this.log.oidRecMapping.get(oid);

		// start from last OID modification point
		int currentRecord = log.getRecordIndexOfWhereObjectWasInitializedFirst(oid);
		
		if (currentRecord > 0) {
			// last modification of object happened here
			// -> we start looking for interesting records after retrieved record
			currentRecord++;
		} else {
			// object new instance statement
			// -> retrieved loc record no is included
			currentRecord = -currentRecord;
		}

		String methodName;
		int currentOID;
		int captureId;

		Object[] methodArgs;
		Integer methodArgOID;

		Integer returnValue;
		Object returnValueObj;

		for (; currentRecord <= end; currentRecord++) {
			currentOID = this.log.objectIds.getQuick(currentRecord);
			returnValueObj = this.log.returnValues.get(currentRecord);
			returnValue = returnValueObj.equals(CaptureLog.RETURN_TYPE_VOID) ? -1
			        : (Integer) returnValueObj;

			if (oid == currentOID || returnValue == oid) {
				methodName = this.log.methodNames.get(currentRecord);

				if (CaptureLog.PLAIN_INIT.equals(methodName)) // e.g. String var = "Hello World";
				{
					this.createPlainInitStmt(currentRecord, testCase);

					// TODO: NOT NICE!!! DO IT ALSO FOR PLAIN AND NOT OBSERVED INIT to be consistent
					captureId = this.log.captureIds.getQuick(currentRecord);
					while (!(this.log.objectIds.getQuick(currentRecord) == currentOID
					        && this.log.captureIds.getQuick(currentRecord) == captureId && this.log.methodNames.get(currentRecord).equals(CaptureLog.END_CAPTURE_PSEUDO_METHOD))) {
						currentRecord++;
					}
				} else if (CaptureLog.NOT_OBSERVED_INIT.equals(methodName)) // e.g. Person var = (Person) XSTREAM.fromXML("<xml/>");
				{
					this.createUnobservedInitStmt(currentRecord, testCase);

					// TODO: NOT NICE!!! DO IT ALSO FOR PLAIN AND NOT OBSERVED INIT to be consistent
					captureId = this.log.captureIds.getQuick(currentRecord);
					while (!(this.log.objectIds.getQuick(currentRecord) == currentOID
					        && this.log.captureIds.getQuick(currentRecord) == captureId && this.log.methodNames.get(currentRecord).equals(CaptureLog.END_CAPTURE_PSEUDO_METHOD))) {
						currentRecord++;
					}
				} else if (CaptureLog.PUTFIELD.equals(methodName)
				        || CaptureLog.PUTSTATIC.equals(methodName)
				        || // field write access such as p.id = id or Person.staticVar = "something"
				        CaptureLog.GETFIELD.equals(methodName)
				        || CaptureLog.GETSTATIC.equals(methodName)) // field READ access such as "int a =  p.id" or "String var = Person.staticVar"
				{

					if (CaptureLog.PUTFIELD.equals(methodName)
					        || CaptureLog.PUTSTATIC.equals(methodName)) {
						// a field assignment has always one argument
						methodArgs = this.log.params.get(currentRecord);
						methodArgOID = (Integer) methodArgs[0];
						if (methodArgOID != null && methodArgOID != oid) {
							// create history of assigned value
							this.restorceCodeFromLastPosTo(methodArgOID, currentRecord,
							                               testCase);
						}

						this.createFieldWriteAccessStmt(currentRecord, testCase);
					} else {
						this.createFieldReadAccessStmt(currentRecord, testCase);
					}

					// TODO: NOT NICE!!! DO IT ALSO FOR PLAIN AND NOT OBSERVED INIT to be consistent
					captureId = this.log.captureIds.getQuick(currentRecord);
					while (!(this.log.objectIds.getQuick(currentRecord) == currentOID
					        && this.log.captureIds.getQuick(currentRecord) == captureId && this.log.methodNames.get(currentRecord).equals(CaptureLog.END_CAPTURE_PSEUDO_METHOD))) {
						currentRecord++;
					}

					if (CaptureLog.GETFIELD.equals(methodName)
					        || CaptureLog.GETSTATIC.equals(methodName)) {
						// GETFIELD and GETSTATIC should only happen, if we obtain an instance whose creation has not been observed
						log.updateWhereObjectWasInitializedFirst(currentOID, currentRecord);		
						

						if (returnValue != -1) {
							log.updateWhereObjectWasInitializedFirst(returnValue, currentRecord);		
							
						}
					}
				} else // var0.call(someArg) or Person var0 = new Person()
				{
					methodArgs = this.log.params.get(currentRecord);

					for (int i = 0; i < methodArgs.length; i++) {
						// there can only be OIDs or null
						methodArgOID = (Integer) methodArgs[i];
						if (methodArgOID != null && methodArgOID != oid) {
							this.restorceCodeFromLastPosTo(methodArgOID, currentRecord,
							                               testCase);
						}
					}

					this.createMethodCallStmt(currentRecord, testCase);

					// forward to end of method call sequence

					captureId = this.log.captureIds.getQuick(currentRecord);

					while (!(this.log.objectIds.getQuick(currentRecord) == currentOID
					        && this.log.captureIds.getQuick(currentRecord) == captureId && this.log.methodNames.get(currentRecord).equals(CaptureLog.END_CAPTURE_PSEUDO_METHOD))) {
						currentRecord++;
					}

					// each method call is considered as object state modification -> so save last object modification
					log.updateWhereObjectWasInitializedFirst(currentOID, currentRecord);		
					

					if (returnValue != -1) {
						// if returnValue has not type VOID, mark current log record as record where the return value instance was created
						// --> if an object is created within an observed method, it would not be semantically correct
						//     (and impossible to handle properly) to create an extra instance of the return value type outside this method
						log.updateWhereObjectWasInitializedFirst(returnValue, currentRecord);		
						
					}

					// consider each passed argument as being modified at the end of the method call sequence
					for (int i = 0; i < methodArgs.length; i++) {
						// there can only be OIDs or null
						methodArgOID = (Integer) methodArgs[i];
						if (methodArgOID != null && methodArgOID != oid) {
							log.updateWhereObjectWasInitializedFirst(methodArgOID, currentRecord);									
						}
					}
				}
			}
		}
	}

	private void createMethodCallStmt(final int logRecNo, final TestCase testCase) {
		// assumption: all necessary statements are created and there is one variable for reach referenced object
		final int oid = this.log.objectIds.get(logRecNo);
		final Object[] methodArgs = this.log.params.get(logRecNo);
		final String methodName = this.log.methodNames.get(logRecNo);

		final String methodDesc = this.log.descList.get(logRecNo);
		final org.objectweb.asm.Type[] methodParamTypes = org.objectweb.asm.Type.getArgumentTypes(methodDesc);

		final Class<?>[] methodParamTypeClasses = new Class[methodParamTypes.length];
		for (int i = 0; i < methodParamTypes.length; i++) {
			methodParamTypeClasses[i] = getClassFromType(methodParamTypes[i]);
		}

		final String typeName = this.log.oidClassNames.get(this.log.oidRecMapping.get(oid));
		Class<?> type;
		try {
			type = getClassForName(typeName);// Class.forName(typeName, true, StaticTestCluster.classLoader);

			final ArrayList<VariableReference> args = new ArrayList<VariableReference>();

			Integer argOID; // is either an oid or null
			for (int i = 0; i < methodArgs.length; i++) {
				argOID = (Integer) methodArgs[i];
				if (argOID == null) {
					args.add(testCase.addStatement(new NullStatement(testCase,
					        methodParamTypeClasses[i])));
				} else {
					args.add(this.oidToVarRefMap.get(argOID));
				}
			}

			if (CaptureLog.OBSERVED_INIT.equals(methodName)) {
				// Person var0 = new Person();

				final ConstructorStatement constStmt = new ConstructorStatement(
				        testCase,
				        new GenericConstructor(
				                type.getDeclaredConstructor(methodParamTypeClasses), type),
				        args);

				this.oidToVarRefMap.put(oid, testCase.addStatement(constStmt));
			} else //------------------ handling for ordinary method calls e.g. var1 = var0.doSth();
			{
				final Object returnValue = this.log.returnValues.get(logRecNo);
				if (CaptureLog.RETURN_TYPE_VOID.equals(returnValue)) {
					final MethodStatement m = new MethodStatement(
					        testCase,
					        new GenericMethod(
					                this.getDeclaredMethod(type, methodName,
					                                       methodParamTypeClasses),
					                type.getMethod(methodName, methodParamTypeClasses).getReturnType()),
					        this.oidToVarRefMap.get(oid),

					        args);
					testCase.addStatement(m);
				} else {
					final org.objectweb.asm.Type returnType = org.objectweb.asm.Type.getReturnType(methodDesc);

					// Person var0 = var.getPerson();
					final MethodStatement m = new MethodStatement(testCase,
					        new GenericMethod(
					                this.getDeclaredMethod(type, methodName,
					                                       methodParamTypeClasses),
					                getClassFromType(returnType)),
					        this.oidToVarRefMap.get(oid), args);

					final Integer returnValueOID = (Integer) returnValue;
					this.oidToVarRefMap.put(returnValueOID, testCase.addStatement(m));
				}
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private final TIntObjectHashMap<VariableReference> oidToVarRefMap = new TIntObjectHashMap<VariableReference>();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createPlainInitStmt(final int logRecNo, final TestCase testCase) {
		// NOTE: PLAIN INIT: has always one non-null param
		// TODO: use primitives
		final int oid = this.log.objectIds.get(logRecNo);

		if (this.oidToVarRefMap.containsKey(oid)) {
			// TODO this might happen because of Integer.valueOf(), for example. . Is this approach ok?
			return;
		}

		final String type = this.log.oidClassNames.get(this.log.oidRecMapping.get(oid));
		final Object value = this.log.params.get(logRecNo)[0];

		if (value instanceof Class) // Class is a plain type according to log
		{
			// FIXME this code needs to get working
			//			try
			//			{
			//				final VariableReference varRef   = new VariableReferenceImpl(testCase, Class.class);
			//				final VariableReference    valueRef = new VariableReferenceImpl(testCase, getClassForName(type));
			//				
			//				final AssignmentStatement assign = new AssignmentStatement(testCase, varRef, valueRef);
			//				this.oidToVarRefMap.put(oid, testCase.addStatement(assign));
			//			}
			//			catch(final Exception e)
			//			{
			//				throw new RuntimeException(e);
			//			}
		} else {
			final PrimitiveStatement primitiveValue = PrimitiveStatement.getPrimitiveStatement(testCase,
			                                                                                   getClassForName(type));
			primitiveValue.setValue(value);
			final VariableReference varRef = testCase.addStatement(primitiveValue);
			this.oidToVarRefMap.put(oid, varRef);
		}
	}

	private VariableReference xStreamRef;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createUnobservedInitStmt(final int logRecNo, final TestCase testCase) {
		// NOTE: PLAIN INIT: has always one non-null param
		// TODO: use primitives
		final int oid = this.log.objectIds.get(logRecNo);
		final String type = this.log.oidClassNames.get(this.log.oidRecMapping.get(oid));

		try {

			final Class<?> xStreamType = getClassForName("com.thoughtworks.xstream.XStream");//Class.forName("com.thoughtworks.xstream.XStream", true, StaticTestCluster.classLoader);

			final Class<?> typeClass = getClassForName(type);//Class.forName(type, true, StaticTestCluster.classLoader);

			final Object value = this.log.params.get(logRecNo)[0];

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
			        xStreamType.getMethod("fromXML", stringType), typeClass), xStreamRef,
			        Arrays.asList(stringRepRef));

			this.oidToVarRefMap.put(oid, testCase.addStatement(m));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void createFieldWriteAccessStmt(final int logRecNo, final TestCase testCase) {
		// assumption: all necessary statements are created and there is one variable for reach referenced object

		final Object[] methodArgs = this.log.params.get(logRecNo);
		final int oid = this.log.objectIds.get(logRecNo);
		final int captureId = this.log.captureIds.get(logRecNo);

		final String fieldName = this.log.namesOfAccessedFields.get(captureId);
		final String typeName = this.log.oidClassNames.get(this.log.oidRecMapping.get(oid));

		try {
			final Class<?> type = getClassForName(typeName);

			final String fieldDesc = this.log.descList.get(logRecNo);
			final Class<?> fieldType = CaptureUtil.getClassFromDesc(fieldDesc);

			final FieldReference targetFieldRef = new FieldReference(testCase,
			        new GenericField(this.getDeclaredField(type, fieldName), type));

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

	private void createFieldReadAccessStmt(final int logRecNo, final TestCase testCase) {
		// assumption: all necessary statements are created and there is one variable for reach referenced object
		final int oid = this.log.objectIds.get(logRecNo);
		final int captureId = this.log.captureIds.get(logRecNo);

		final Object returnValue = this.log.returnValues.get(logRecNo);
		if (!CaptureLog.RETURN_TYPE_VOID.equals(returnValue)) // TODO necessary?
		{
			Integer returnValueOID = (Integer) returnValue;
			final String descriptor = this.log.descList.get(logRecNo);
			final org.objectweb.asm.Type fieldTypeType = org.objectweb.asm.Type.getType(descriptor);
			final String typeName = this.log.oidClassNames.get(this.log.oidRecMapping.get(oid));
			final String fieldName = this.log.namesOfAccessedFields.get(captureId);

			try {
				final Class<?> fieldType = getClassFromType(fieldTypeType); //Class.forName(fieldTypeName, true, StaticTestCluster.classLoader);
				final Class<?> type = getClassForName(typeName);// Class.forName(typeName, true, StaticTestCluster.classLoader);

				final FieldReference valueRef = new FieldReference(testCase,
				        new GenericField(type.getField(fieldName), type));
				final VariableReference targetVar = new VariableReferenceImpl(testCase,
				        fieldType);

				final AssignmentStatement assignment = new AssignmentStatement(testCase,
				        targetVar, valueRef);
				VariableReference varRef = testCase.addStatement(assignment);

				this.oidToVarRefMap.put(returnValueOID, varRef);

			} catch (final Exception e) {
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

		try {
			return Class.forName(type.getClassName(), true,
			                     TestGenerationContext.getClassLoader());
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private final Class<?> getClassForName(final String type) {
		try {
			if (type.equals("Boolean")) {
				return Boolean.TYPE;
			} else if (type.equals("Byte")) {
				return Byte.TYPE;
			} else if (type.equals("Character")) {
				return Character.TYPE;
			} else if (type.equals("Double")) {
				return Double.TYPE;
			} else if (type.equals("Float")) {
				return Float.TYPE;
			} else if (type.equals("Integer")) {
				return Integer.TYPE;
			} else if (type.equals("Long")) {
				return Long.TYPE;
			} else if (type.equals("Short")) {
				return Short.TYPE;
			} else if (type.equals("String")) {
				return Class.forName("java.lang.String", true,
				                     TestGenerationContext.getClassLoader());
			}

			return Class.forName(type, true, TestGenerationContext.getClassLoader());
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
}