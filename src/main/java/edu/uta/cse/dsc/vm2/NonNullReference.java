package edu.uta.cse.dsc.vm2;

import java.lang.ref.WeakReference;

public class NonNullReference implements Reference {

	private final int instanceId;
	private final String className;

	private WeakReference<Object> weakReference;
	private int concIdentityHashCode;

	public NonNullReference(String className, int instanceId) {
		this.className = className;
		this.instanceId = instanceId;

		weakReference = null;
		concIdentityHashCode = -1;
	}

	public String toString() {
		return this.className + "$" + this.instanceId;
	}

	public void initializeReference(Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException(
					"Cannot initialize a NonNullReference with the null value");
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

}
