package edu.uta.cse.dsc.vm2;

public class NonNullReference implements Reference {

	private int instanceId;
	private String className;

	public NonNullReference(String className, int instanceId) {
		this.className = className;
		this.instanceId = instanceId;
	}

}
