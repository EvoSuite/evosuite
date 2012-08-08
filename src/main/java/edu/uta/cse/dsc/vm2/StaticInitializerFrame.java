 package edu.uta.cse.dsc.vm2;

import java.lang.reflect.Member;

import edu.uta.cse.dsc.MainConfig;


/**
 * Frame for a <clinit>() invocation
 * 
 * @author csallner@uta.edu (Christoph Csallner)
 */
final class StaticInitializerFrame extends Frame {

	/**
	 * Constructor
	 */
	StaticInitializerFrame() {
		super(MainConfig.get().MAX_LOCALS_DEFAULT);
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
}
