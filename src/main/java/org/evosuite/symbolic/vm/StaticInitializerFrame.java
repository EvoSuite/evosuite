package org.evosuite.symbolic.vm;

import java.lang.reflect.Member;

import edu.uta.cse.dsc.MainConfig;

/**
 * Frame for a <clinit>() invocation
 * 
 * @author csallner@uta.edu (Christoph Csallner)
 */
final class StaticInitializerFrame extends Frame {

	private String className;

	/**
	 * Constructor
	 */
	StaticInitializerFrame(String className) {
		super(MainConfig.get().MAX_LOCALS_DEFAULT);
		this.className = className;
	}

	@Override
	public int getNrFormalParameters() {
		return 0;
	}

	@Override
	public int getNrFormalParametersTotal() {
		return 0;
	}

	@Override
	public Member getMember() {
		return null;
	}

	public Object getClassName() {
		return className;
	}
}
