package com.examples.with.different.packagename;

public abstract class AbstractClassWithStaticFactory {

	public abstract boolean coverMe(int x);
	
	public static AbstractClassWithStaticFactory create() {
		return new AbstractClassWithStaticFactory() {
			@Override
			public boolean coverMe(int x) {
				if(x == 0)
					return true;
				else
					return false;
			}
		};
	}
}
