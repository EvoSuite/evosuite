package org.evosuite.seeding;

import java.io.Serializable;

import org.evosuite.testcase.TestCase;
import org.evosuite.utils.GenericClass;

class ObjectSequence implements Serializable {

	private static final long serialVersionUID = 346185306757522598L;

	private GenericClass generatedType;
	
	private TestCase test;
	
	public ObjectSequence(GenericClass generatedType, TestCase test) {
		this.generatedType = generatedType;
		this.test = test;
	}
	
	public GenericClass getGeneratedClass() {
		return generatedType;
	}
	
	public TestCase getSequence() {
		return test;
	}
	
}
