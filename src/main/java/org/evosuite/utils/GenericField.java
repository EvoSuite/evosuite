/**
 * 
 */
package org.evosuite.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.evosuite.TestGenerationContext;

import com.googlecode.gentyref.GenericTypeReflector;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericField extends GenericAccessibleObject {

	private static final long serialVersionUID = -2344346234923642901L;

	private transient Field field;

	public GenericField(Field field, GenericClass owner) {
		super(owner);
		this.field = field;
	}

	public GenericField(Field field, Class<?> owner) {
		super(new GenericClass(owner));
		this.field = field;
	}

	public GenericField(Field field, Type owner) {
		super(new GenericClass(owner));
		this.field = field;
	}

	@Override
	public GenericAccessibleObject copyWithNewOwner(GenericClass newOwner) {
		return new GenericField(field, newOwner);
	}
	
	@Override
	public GenericAccessibleObject copyWithOwnerFromReturnType(
			ParameterizedType returnType) {
		GenericClass newOwner = new GenericClass(getTypeFromExactReturnType(returnType, (ParameterizedType)getOwnerType()));
		return new GenericField(field, newOwner);
	}
	

	
	public Field getField() {
		return field;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#getDeclaringClass()
	 */
	@Override
	public Class<?> getDeclaringClass() {
		return field.getDeclaringClass();
	}

	public Type getFieldType() {
		return GenericTypeReflector.getExactFieldType(field, owner.getType());
		// 		try {
		// fieldType = field.getGenericType();
		// } catch (java.lang.reflect.GenericSignatureFormatError e) {
		// Ignore
		// fieldType = field.getType();
		// }
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#isField()
	 */
	@Override
	public boolean isField() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#isStatic()
	 */
	@Override
	public boolean isStatic() {
		return Modifier.isStatic(field.getModifiers());
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#getName()
	 */
	@Override
	public String getName() {
		return field.getName();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#toString()
	 */
	@Override
	public String toString() {
		return field.toGenericString();
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		// Write/save additional fields
		oos.writeObject(field.getDeclaringClass().getName());
		oos.writeObject(field.getName());
	}

	// assumes "static java.util.Date aDate;" declared
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		ois.defaultReadObject();

		// Read/initialize additional fields
		Class<?> methodClass = TestGenerationContext.getClassLoader().loadClass((String) ois.readObject());
		String fieldName = (String) ois.readObject();

		try {
			field = methodClass.getDeclaredField(fieldName);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void changeClassLoader(ClassLoader loader) {
		super.changeClassLoader(loader);

		try {
			Class<?> oldClass = field.getDeclaringClass();
			Class<?> newClass = loader.loadClass(oldClass.getName());
			this.field = newClass.getDeclaredField(field.getName());
			this.field.setAccessible(true);
		} catch (ClassNotFoundException e) {
			LoggingUtils.getEvoLogger().info("Class not found - keeping old class loader ",
			                                 e);
		} catch (SecurityException e) {
			LoggingUtils.getEvoLogger().info("Class not found - keeping old class loader ",
			                                 e);
		} catch (NoSuchFieldException e) {
			LoggingUtils.getEvoLogger().info("Field " + field.getName()
			                                         + " not found in class "
			                                         + field.getDeclaringClass());
		}
	}
}
