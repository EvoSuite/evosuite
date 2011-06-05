package de.unisb.cs.st.evosuite.symbolic.bytecode;

import jpf.mytest.nativpeer.concolic.JPF_jpf_mytest_primitive_PrimitiveGenerator;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;

public class PathConstraintDummy extends ListenerAdapter {

	@Override
	public void stateStored(Search search) {
		PathConstraint.getInstance().stateStored(search);
		JPF_jpf_mytest_primitive_PrimitiveGenerator.stateStored(search);
	}

	@Override
	public void stateRestored(Search search) {
		PathConstraint.getInstance().stateRestored(search);
		JPF_jpf_mytest_primitive_PrimitiveGenerator.stateRestored(search);
	}

}
