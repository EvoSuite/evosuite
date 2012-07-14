package de.unisb.cs.st.testcarver.capture;

import org.objectweb.asm.Type;


public final class CaptureUtil 
{
	private CaptureUtil(){}
	
	public static Class<?> loadClass(final String internalClassName)
	{
		final String className = internalClassName.replace('/', '.');
		
		try 
		{
			return Class.forName(className);
		} 
		catch (final ClassNotFoundException e) 
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static final Class<?> getClassFromDesc(final String desc)
	{
		final Type type = Type.getType(desc);
		
		if(type.equals(Type.BOOLEAN_TYPE))
		{
			return Boolean.class;
		}
		else if(type.equals(Type.BYTE_TYPE))
		{
			return Byte.class;
		}
		else if(type.equals(Type.CHAR_TYPE))
		{
			return Character.class;
		}
		else if(type.equals(Type.DOUBLE_TYPE))
		{
			return Double.class;
		}
		else if(type.equals(Type.FLOAT_TYPE))
		{
			return Float.class;
		}
		else if(type.equals(Type.INT_TYPE))
		{
			return Integer.class;
		}
		else if(type.equals(Type.LONG_TYPE))
		{
			return Long.class;
		}
		else if(type.equals(Type.SHORT_TYPE))
		{
			return Short.class;
		}
		
		try 
		{
			return Class.forName(type.getInternalName().replace('/', '.'));
		} 
		catch (final ClassNotFoundException e) 
		{
			throw new RuntimeException(e);
		}
	}
	
	
}
