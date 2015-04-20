package org.evosuite.symbolic.vm;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Fake Stack Frame for simulating the invokation from the command line to 
 * the main(String[] a) method with no arguments.
 * 
 * @author csallner@uta.edu (Christoph Csallner)
 */
public final class FakeMainCallerFrame extends Frame {

	private final Method method;

	/**
	 * Constructor
	 */
	FakeMainCallerFrame(Method method, int maxLocals) {
		super(maxLocals);
		this.method = method;
	}

	@Override
	public int getNrFormalParameters() {
		return method.getParameterTypes().length;
	}

	@Override
	public int getNrFormalParametersTotal() {
		return getNrFormalParameters()
				+ (Modifier.isStatic(method.getModifiers()) ? 0 : 1);
	}

	@Override
	public Member getMember() {
		return method;
	}
}
