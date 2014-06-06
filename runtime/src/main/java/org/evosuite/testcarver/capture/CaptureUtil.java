package org.evosuite.testcarver.capture;

import org.evosuite.classpath.ResourceList;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class CaptureUtil {
	
	private static final transient Logger logger = LoggerFactory.getLogger(CaptureUtil.class);
	
	private CaptureUtil(){}
	
	public static Class<?> loadClass(final String internalClassName)
	{
		final String className = ResourceList.getClassNameFromResourcePath(internalClassName);
		
		try 
		{
			return Class.forName(className);
		} 
		catch (final ClassNotFoundException e) 
		{			
			throw new RuntimeException(e);
		}
	}
	
	public static final Class<?> getClassFromDesc(final String desc)
	{
		final Type type = Type.getType(desc);
		if(type.equals(Type.BOOLEAN_TYPE))
		{
			return boolean.class;
		}
		else if(type.equals(Type.BYTE_TYPE))
		{
			return byte.class;
		}
		else if(type.equals(Type.CHAR_TYPE))
		{
			return char.class;
		}
		else if(type.equals(Type.DOUBLE_TYPE))
		{
			return double.class;
		}
		else if(type.equals(Type.FLOAT_TYPE))
		{
			return float.class;
		}
		else if(type.equals(Type.INT_TYPE))
		{
			return int.class;
		}
		else if(type.equals(Type.LONG_TYPE))
		{
			return long.class;
		}
		else if(type.equals(Type.SHORT_TYPE))
		{
			return short.class;
		}
		
		try 
		{
			return Class.forName(ResourceList.getClassNameFromResourcePath(type.getInternalName()));
		} 
		catch (final ClassNotFoundException e) 
		{
			throw new RuntimeException(e);
		}
	}
	
	
}
