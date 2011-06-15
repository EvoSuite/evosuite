package de.unisb.cs.st.evosuite.cfg;

public class EntryBlock extends BasicBlock {

	public EntryBlock(String className, String methodName) {
		super(className, methodName);
	}

	@Override
	public boolean isEntryBlock() {
		return true;
	}
	
	@Override
	public String getName() {
		return "EntryBlock for method "+methodName;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
