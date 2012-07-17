package org.evosuite.testcarver.testcase;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.capture.CaptureUtil;
import org.evosuite.testcarver.codegen.ICodeGenerator;
import org.evosuite.testcase.AssignmentStatement;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.FieldReference;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.NullStatement;
import org.evosuite.testcase.PrimitiveStatement;
import org.evosuite.testcase.StaticTestCluster;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.VariableReference;
import org.evosuite.testcase.VariableReferenceImpl;




public final class EvoTestCaseCodeGenerator implements ICodeGenerator<TestCase>
{
	//--- source generation
	private TestCase testCase;
	private VariableReference xStreamRef;
	
	private TIntObjectHashMap<VariableReference> oidToVarRefMap;

	public EvoTestCaseCodeGenerator()
	{
		this.oidToVarRefMap = new TIntObjectHashMap<VariableReference>();
	}
	
	@Override
	public void createMethodCallStmt(CaptureLog log, int logRecNo) 
	{
		// assumption: all necessary statements are created and there is one variable for reach referenced object
		final int      oid        = log.objectIds.get(logRecNo);
		final Object[] methodArgs = log.params.get(logRecNo);
		final String   methodName = log.methodNames.get(logRecNo);
		
		
		final  String                   methodDesc       = log.descList.get(logRecNo);
		final  org.objectweb.asm.Type[] methodParamTypes = org.objectweb.asm.Type.getArgumentTypes(methodDesc);
		
		final Class<?>[] methodParamTypeClasses = new Class[methodParamTypes.length];
		for(int i = 0; i < methodParamTypes.length; i++)
		{
			methodParamTypeClasses[i] = getClassFromType(methodParamTypes[i]);
		}
		
		final String  typeName  = log.oidClassNames.get(log.oidRecMapping.get(oid));
		Class<?> type;
		try 
		{
			type = getClassForName(typeName);
			final ArrayList<VariableReference> args = new ArrayList<VariableReference>();
			
			Integer  argOID; // is either an oid or null
			for(int i = 0; i < methodArgs.length; i++)
			{
				argOID = (Integer) methodArgs[i];
				if(argOID == null)
				{
					args.add(testCase.addStatement(new NullStatement(testCase, methodParamTypeClasses[i])));
				}
				else
				{
					args.add(this.oidToVarRefMap.get(argOID));
				}
			}
			
			if(CaptureLog.OBSERVED_INIT.equals(methodName))
			{
				 // Person var0 = new Person();
	
				final ConstructorStatement constStmt = new ConstructorStatement(testCase, 
																			    type.getDeclaredConstructor(methodParamTypeClasses), 
																			    type, 
																			    args);
				
				this.oidToVarRefMap.put(oid, testCase.addStatement(constStmt));
			}
			else //------------------ handling for ordinary method calls e.g. var1 = var0.doSth();
			{
				final Object returnValue = log.returnValues.get(logRecNo);
				if(CaptureLog.RETURN_TYPE_VOID.equals(returnValue))
				{
					final MethodStatement m = new MethodStatement(testCase, 
											  this.getDeclaredMethod(type, methodName, methodParamTypeClasses), 
											  this.oidToVarRefMap.get(oid), 
											  type.getMethod(methodName, methodParamTypeClasses).getReturnType(), 
											  args);
					testCase.addStatement(m);
				}
				else
				{
					final  org.objectweb.asm.Type returnType = org.objectweb.asm.Type.getReturnType(methodDesc);
					
					// Person var0 = var.getPerson();
					final MethodStatement m = new MethodStatement(testCase, 
																  this.getDeclaredMethod(type, methodName, methodParamTypeClasses), 
																  this.oidToVarRefMap.get(oid), 
																  getClassFromType(returnType),
																  args);
	
					final Integer            returnValueOID = (Integer) returnValue;
					this.oidToVarRefMap.put(returnValueOID, testCase.addStatement(m));
				}
			}
		} 
		catch(final Exception e) 
		{
			throw new RuntimeException(e);
		}
	}
	
	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void createPlainInitStmt(CaptureLog log, int logRecNo) {
		// NOTE: PLAIN INIT: has always one non-null param
		// TODO: use primitives
		final int oid = log.objectIds.get(logRecNo);
		
		if(this.oidToVarRefMap.containsKey(oid))
		{
			// TODO this might happen because of Integer.valueOf(), for example. . Is this approach ok?
			return;
		}
		
		final String             type           = log.oidClassNames.get(log.oidRecMapping.get(oid));
		final Object             value          = log.params.get(logRecNo)[0];
		
		if(! (value instanceof Class)) // Class is a plain type according to log
		{
			final PrimitiveStatement primitiveValue = PrimitiveStatement.getPrimitiveStatement(testCase,  getClassForName(type));
			primitiveValue.setValue(value);
			final VariableReference varRef = testCase.addStatement(primitiveValue);
			this.oidToVarRefMap.put(oid, varRef);
		}
	}
	
	

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void createUnobservedInitStmt(CaptureLog log, int logRecNo) {

		// NOTE: PLAIN INIT: has always one non-null param
		// TODO: use primitives
		final int    oid     = log.objectIds.get(logRecNo);
		final String type    = log.oidClassNames.get(log.oidRecMapping.get(oid));
		
		try
		{
			
			 final Class<?> xStreamType = getClassForName("com.thoughtworks.xstream.XStream");//Class.forName("com.thoughtworks.xstream.XStream", true, StaticTestCluster.classLoader);
			 
			
			final Class<?> typeClass = getClassForName(type);//Class.forName(type, true, StaticTestCluster.classLoader);
		
			final Object value   = log.params.get(logRecNo)[0];
			
			
			if(xStreamRef == null)
			{
				final ConstructorStatement constr = new ConstructorStatement(testCase, 
																		     xStreamType.getConstructor(new Class<?>[0]), 
																		     xStreamType, 
																		     Collections.EMPTY_LIST);
				xStreamRef = testCase.addStatement(constr);
			}
			
			

			final Class<?> stringType = getClassForName("java.lang.String");//Class.forName("java.lang.String", true, StaticTestCluster.classLoader);
			
			final PrimitiveStatement stringRep = PrimitiveStatement.getPrimitiveStatement(testCase, stringType);
			stringRep.setValue(value);
			final VariableReference stringRepRef = testCase.addStatement(stringRep);
			
			final MethodStatement m = new MethodStatement(testCase, 
														  xStreamType.getMethod("fromXML", stringType), 
														  xStreamRef, 
														 typeClass,
														  Arrays.asList(stringRepRef));
			
			this.oidToVarRefMap.put(oid,  testCase.addStatement(m));
		}
		catch(final Exception e)
		{
			throw new RuntimeException(e);
		}
	}	
	
	
	
	@Override
	public void createFieldWriteAccessStmt(CaptureLog log, int logRecNo) {
		// assumption: all necessary statements are created and there is one variable for reach referenced object
		
		final Object[] methodArgs = log.params.get(logRecNo);
		final int      oid        = log.objectIds.get(logRecNo);
		final int      captureId  = log.captureIds.get(logRecNo);
		
		final String  fieldName = log.namesOfAccessedFields.get(captureId);
		final String  typeName  = log.oidClassNames.get(log.oidRecMapping.get(oid));
		
		try 
		{
			final Class<?> type = getClassForName(typeName);
			
			final String   fieldDesc = log.descList.get(logRecNo);
			final Class<?> fieldType = CaptureUtil.getClassFromDesc(fieldDesc);
			
			final FieldReference targetFieldRef  = new FieldReference(testCase, this.getDeclaredField(type, fieldName));
			
			final AssignmentStatement assignment;
			
			final Integer arg = (Integer) methodArgs[0];
			if(arg == null)
			{
				 final NullStatement     nullStmt      = new NullStatement(testCase, fieldType);
				 final VariableReference nullReference = testCase.addStatement(nullStmt);
				 
				 assignment = new AssignmentStatement(testCase, targetFieldRef, nullReference);
			}
			else
			{
				assignment = new AssignmentStatement(testCase, targetFieldRef, this.oidToVarRefMap.get(arg));
			}
			
			final VariableReference varRef = testCase.addStatement(assignment);
			if(arg != null)
			{
				this.oidToVarRefMap.put(arg, varRef);
			}
		} 
		catch (final Exception e) 
		{
			throw new RuntimeException(e);
		}
	}	
	
	
	
	@Override
	public void createFieldReadAccessStmt(CaptureLog log, int logRecNo) {
		// assumption: all necessary statements are created and there is one variable for reach referenced object
		final int      oid        = log.objectIds.get(logRecNo);
		final int      captureId  = log.captureIds.get(logRecNo);
		
		final Object returnValue = log.returnValues.get(logRecNo);
		if(! CaptureLog.RETURN_TYPE_VOID.equals(returnValue))  // TODO necessary?
		{
			Integer 	  returnValueOID  = (Integer) returnValue;
			final String  descriptor 	  = log.descList.get(logRecNo);
			final org.objectweb.asm.Type  fieldTypeType   = org.objectweb.asm.Type.getType(descriptor);
			final String  typeName        = log.oidClassNames.get(log.oidRecMapping.get(oid));
			final String  fieldName       = log.namesOfAccessedFields.get(captureId);
			
			try {
				final Class<?> fieldType = getClassFromType(fieldTypeType); //Class.forName(fieldTypeName, true, StaticTestCluster.classLoader);
				final Class<?> type      = getClassForName(typeName);// Class.forName(typeName, true, StaticTestCluster.classLoader);
				
				final FieldReference    valueRef  = new FieldReference(testCase, type.getField(fieldName));
				final VariableReference targetVar = new VariableReferenceImpl(testCase, fieldType);
				
				final AssignmentStatement assignment = new AssignmentStatement(testCase, targetVar, valueRef);
				VariableReference varRef = testCase.addStatement(assignment);
				
				this.oidToVarRefMap.put(returnValueOID, varRef);
				
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
	}	
	
	
	
	
	
	
	
	
	private final Class<?> getClassFromType(final org.objectweb.asm.Type type)
	{
		
		if(type.equals(org.objectweb.asm.Type.BOOLEAN_TYPE))
		{
			return Boolean.TYPE;
		}
		else if(type.equals(org.objectweb.asm.Type.BYTE_TYPE))
		{
			return Byte.TYPE;
		}
		else if(type.equals(org.objectweb.asm.Type.CHAR_TYPE))
		{
			return Character.TYPE;
		}
		else if(type.equals(org.objectweb.asm.Type.DOUBLE_TYPE))
		{
			return Double.TYPE;
		}
		else if(type.equals(org.objectweb.asm.Type.FLOAT_TYPE))
		{
			return Float.TYPE;
		}
		else if(type.equals(org.objectweb.asm.Type.INT_TYPE))
		{
			return Integer.TYPE;
		}
		else if(type.equals(org.objectweb.asm.Type.LONG_TYPE))
		{
			return Long.TYPE;
		}
		else if(type.equals(org.objectweb.asm.Type.SHORT_TYPE))
		{
			return Short.TYPE;
		}
		else if(type.getSort() == org.objectweb.asm.Type.ARRAY)
		{
			final org.objectweb.asm.Type elementType = type.getElementType();
			
			if(elementType.equals(org.objectweb.asm.Type.BOOLEAN_TYPE))
			{
				return boolean[].class;
			}
			else if(elementType.equals(org.objectweb.asm.Type.BYTE_TYPE))
			{
				return byte[].class;
			}
			else if(elementType.equals(org.objectweb.asm.Type.CHAR_TYPE))
			{
				return char[].class;
			}
			else if(elementType.equals(org.objectweb.asm.Type.DOUBLE_TYPE))
			{
				return double[].class;
			}
			else if(elementType.equals(org.objectweb.asm.Type.FLOAT_TYPE))
			{
				return float[].class;
			}
			else if(elementType.equals(org.objectweb.asm.Type.INT_TYPE))
			{
				return int[].class;
			}
			else if(elementType.equals(org.objectweb.asm.Type.LONG_TYPE))
			{
				return long[].class;
			}
			else if(elementType.equals(org.objectweb.asm.Type.SHORT_TYPE))
			{
				return short[].class;
			}
		}
		
		try 
		{
			return Class.forName(type.getClassName(), true, StaticTestCluster.classLoader);
		} 
		catch (final ClassNotFoundException e) 
		{
			throw new RuntimeException(e);
		}
	}
	
	
	
	
	
	private final Class<?> getClassForName(String type)
	{
		try 
		{
			if( type.equals("boolean"))
			{
				return Boolean.TYPE;
			}
			else if(type.equals("byte"))
			{
				return Byte.TYPE;
			}
			else if( type.equals("char"))
			{
				return Character.TYPE;
			}
			else if( type.equals("double"))
			{
				return Double.TYPE;
			}
			else if(type.equals("float"))
			{
				return Float.TYPE;
			}
			else if(type.equals("int"))
			{
				return Integer.TYPE;
			}
			else if( type.equals("long"))
			{
				return Long.TYPE;
			}
			else if(type.equals("short"))
			{
				return Short.TYPE;
			}
			else if(type.equals("String") ||type.equals("Boolean") ||type.equals("Boolean") || type.equals("Short") ||type.equals("Long") ||
					type.equals("Integer") || type.equals("Float") || type.equals("Double") ||type.equals("Byte") || 
					type.equals("Character") )
			{
				return Class.forName("java.lang." + type, true, StaticTestCluster.classLoader);
			}
		
			if(type.endsWith("[]"))
			{
				type = type.replace("[]", "");
				return Class.forName("[L" + type + ";", true, StaticTestCluster.classLoader);
			}
			else
			{
				return Class.forName(type.replace('/', '.'), true, StaticTestCluster.classLoader);
			}
		} 
		catch (final ClassNotFoundException e) 
		{
			throw new RuntimeException(e);
		}
	}
	
	
	private Field getDeclaredField(final Class<?> clazz, final String fieldName) throws NoSuchFieldException
	{
		if(clazz == null || Object.class.equals(clazz))
		{
			throw new NoSuchFieldException(fieldName);
		}
		
		try
		{
			final Field field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			return field;
		}
		catch(final NoSuchFieldException e)
		{
			return getDeclaredField(clazz.getSuperclass(), fieldName);
		}
	}
	
	
	private Method getDeclaredMethod(final Class<?> clazz, final String methodName, Class<?>[] paramTypes) throws NoSuchFieldException
	{
		if(clazz == null || Object.class.equals(clazz))
		{
			throw new NoSuchFieldException(methodName + "(" + Arrays.toString(paramTypes) + ")");
		}
		
		try
		{
			final Method m = clazz.getDeclaredMethod(methodName, paramTypes);
			m.setAccessible(true);
			return m;
		}
		catch(final NoSuchMethodException e)
		{
			return getDeclaredMethod(clazz.getSuperclass(), methodName, paramTypes);
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
		this.testCase   = null;
		this.xStreamRef = null;
		this.oidToVarRefMap.clear();
	}
}