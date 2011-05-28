package de.unisb.cs.st.evosuite.cfg;

public class ExitBlock extends BasicBlock {

	public ExitBlock(String className, String methodName) {
		super(className, methodName);
	}

	@Override
	public boolean isExitBlock() {
		return true;
	}
	
	@Override
	public String getName() {
		return "ExitBlock for method "+methodName;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
