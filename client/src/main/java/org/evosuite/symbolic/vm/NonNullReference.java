package org.evosuite.symbolic.vm;

import java.lang.ref.WeakReference;

import org.objectweb.asm.Type;

/**
 * 
 * @author galeotti
 * 
 */
public class NonNullReference implements Reference {

	private final int instanceId;
	private final Type objectType;

	private WeakReference<Object> weakReference;
	private int concIdentityHashCode;

	public NonNullReference(Type objectType, int instanceId) {
		this.objectType = objectType;
		this.instanceId = instanceId;

		weakReference = null;
		concIdentityHashCode = -1;
	}

	@Override
	public String toString() {
		return this.getClassName() + "$" + this.instanceId;
	}

	public void initializeReference(Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException(
					"Cannot initialize a NonNullReference with the null value");
		}
		if (weakReference != null) {
			throw new IllegalStateException("Reference already initialized!");
		}

		this.weakReference = new WeakReference<Object>(obj);
		this.concIdentityHashCode = System.identityHashCode(obj);
	}

	public boolean isInitialized() {
		return this.weakReference != null;
	}

	public Object getWeakConcreteObject() {
		if (!isInitialized())
			throw new IllegalStateException(
					"Object has to be initialized==true for this method to be invoked");
		return this.weakReference.get();
	}

	public int getConcIdentityHashCode() {
		if (!isInitialized())
			throw new IllegalStateException(
					"Object has to be initialized==true for this method to be invoked");
		return this.concIdentityHashCode;
	}

	public boolean isCollectable() {
		return this.isInitialized() && this.getWeakConcreteObject() == null;
	}

	public String getClassName() {
		return this.objectType.getClassName();
	}

	public boolean isString() {
		Type stringType = Type.getType(String.class);
		return this.objectType.equals(stringType);
	}
}
