package com.examples.with.different.packagename.stable;

public class ExitVM {
	
	private final boolean exit;
	public ExitVM(boolean exit) {
		this.exit = exit;
	}
	
	public boolean getExit() {
		if (exit)
			return true;
		else
			return false;
	}

	public void exit() {
		if (exit) {
			System.exit(0);
		}
	}
}
