/**
 * 
 */
package org.evosuite.utils;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * @author Gordon Fraser
 * 
 */
public abstract class GenericAccessibleObject implements Serializable {

	
	private static final long serialVersionUID = 7069749492563662621L;

	protected GenericClass owner;

	public GenericAccessibleObject(GenericClass owner) {
		this.owner = owner;
	}

	public Type getOwnerType() {
		return owner.getType();
	}

	public GenericClass getOwnerClass() {
		return owner;
	}

	public abstract Class<?> getDeclaringClass();

	public boolean isMethod() {
		return false;
	}

	public boolean isConstructor() {
		return false;
	}

	public boolean isField() {
		return false;
	}

	public boolean isStatic() {
		return false;
	}

	public abstract String getName();

	@Override
	public abstract String toString();

	public void changeClassLoader(ClassLoader loader) {
		owner.changeClassLoader(loader);
	}

}
