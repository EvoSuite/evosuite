package org.evosuite.symbolic.vm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;

/**
 * Frame for a constructor invocation (<init>)
 * 
 * @author csallner@uta.edu (Christoph Csallner)
 */
public final class ConstructorFrame extends Frame 
{	
	private final Constructor<?> constructor;

	/**
	 * Constructor
	 */
	public ConstructorFrame(Constructor<?> constructor, int maxLocals) {
		super(maxLocals);
		this.constructor = constructor;
	}
	
	@Override
	public int getNrFormalParameters() {
	  return constructor.getParameterTypes().length;
	}
	
	@Override
	public int getNrFormalParametersTotal() {
	  return getNrFormalParameters() + 1;
	}
	
	@Override
	public Member getMember() {
		return constructor;
	}
}
