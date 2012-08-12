package edu.uta.cse.dsc.vm2.string;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public abstract class ValueOf extends StringStaticFunction {
	private static final String FUNCTION_NAME = "valueOf";

	public ValueOf(SymbolicEnvironment env, String desc) {
		super(env, StringFunctionCallVM.JAVA_LANG_STRING, FUNCTION_NAME,
				OBJECT_TO_STR_DESCRIPTOR);
	}

	public static final class ValueOf_O extends ValueOf {
		public ValueOf_O(SymbolicEnvironment env, String owner, String name,
				String desc) {
			super(env, OBJECT_TO_STR_DESCRIPTOR);
		}
		
		
	}
}
