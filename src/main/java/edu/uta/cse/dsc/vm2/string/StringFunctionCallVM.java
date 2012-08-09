package edu.uta.cse.dsc.vm2.string;

import java.util.HashMap;

import edu.uta.cse.dsc.AbstractVM;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

/**
 * This listeners deals with trapping function calls to symbolic functions from
 * java.lang.String
 * 
 * This listener is expected to be executed after the CallVM listener.
 * 
 * @author galeotti
 * 
 */
public final class StringFunctionCallVM extends AbstractVM {

	private static class StringFunctionKey {
		public StringFunctionKey(String owner, String name, String desc) {
			super();
			this.owner = owner;
			this.name = name;
			this.desc = desc;
		}

		public String owner;
		public String name;
		public String desc;

		@Override
		public int hashCode() {
			return owner.hashCode() + name.hashCode() + desc.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !o.getClass().equals(StringFunctionKey.class)) {
				return false;
			} else {
				StringFunctionKey that = (StringFunctionKey) o;
				return this.owner.equals(that.owner)
						&& this.name.equals(that.name)
						&& this.desc.equals(that.desc);
			}
		}
	}

	public static final String JAVA_LANG_STRING = String.class.getName()
			.replace(".", "/");

	private HashMap<StringFunctionKey, StringFunction> stringFunctionTable = new HashMap<StringFunctionKey, StringFunction>();

	private final SymbolicEnvironment env;

	public StringFunctionCallVM(SymbolicEnvironment env) {
		this.env = env;
		fillStringFunctionTable();
	}

	private void fillStringFunctionTable() {
		stringFunctionTable.clear();
		addStringFunctionToTable(new CharAt(env));
		addStringFunctionToTable(new CompareTo(env));
		addStringFunctionToTable(new CompareToIgnoreCase(env));
		addStringFunctionToTable(new Concat(env));
		addStringFunctionToTable(new Contains(env));
		addStringFunctionToTable(new EndsWith(env));
		addStringFunctionToTable(new Equals(env));
		addStringFunctionToTable(new EqualsIgnoreCase(env));
		addStringFunctionToTable(new IndexOf.IndexOf_C(env));
		addStringFunctionToTable(new IndexOf.IndexOf_S(env));
		addStringFunctionToTable(new IndexOf.IndexOf_CI(env));
		addStringFunctionToTable(new IndexOf.IndexOf_SI(env));
		addStringFunctionToTable(new LastIndexOf.LastIndexOf_C(env));
		addStringFunctionToTable(new LastIndexOf.LastIndexOf_S(env));
		addStringFunctionToTable(new LastIndexOf.LastIndexOf_CI(env));
		addStringFunctionToTable(new LastIndexOf.LastIndexOf_SI(env));
		addStringFunctionToTable(new Length(env));
		addStringFunctionToTable(new RegionMatches(env));
		addStringFunctionToTable(new Replace.Replace_C(env));
		addStringFunctionToTable(new Replace.Replace_CS(env));
		addStringFunctionToTable(new ReplaceAll(env));
		addStringFunctionToTable(new ReplaceFirst(env));
		addStringFunctionToTable(new StartsWith(env));
		addStringFunctionToTable(new Substring(env));
		addStringFunctionToTable(new ToLowerCase(env));
		addStringFunctionToTable(new ToUpperCase(env));
		addStringFunctionToTable(new Trim(env));
	}

	private void addStringFunctionToTable(StringFunction f) {
		StringFunctionKey k = new StringFunctionKey(f.getOwner(), f.getName(),
				f.getDesc());
		StringFunction prev = this.stringFunctionTable.put(k, f);
		if (prev != null) {
			throw new IllegalArgumentException(
					"Adding two functions with the same key!");
		}
	}

	@Override
	public void CALL_RESULT(int res, String owner, String name, String desc) {
		StringFunction f = getStringFunction(owner, name, desc);
		if (f == null) {
			return; // do nothing
		}
		f.CALL_RESULT(res);
	}

	@Override
	public void INVOKEVIRTUAL(Object receiver, String owner, String name,
			String desc) {
		if (receiver == null) {
			// CallVM takes care of all NullPointerException details
			return; // do nothing;
		}

		StringFunction f = this.getStringFunction(owner, name, desc);
		if (f == null) {
			// Unsupported string function
			return; // do nothing
		}
		f.INVOKEVIRTUAL(receiver);
	}
	
	@Override
	public void INVOKEVIRTUAL(String owner, String name,
			String desc) {
		StringFunction f = this.getStringFunction(owner, name, desc);
		if (f == null) {
			// Unsupported string function
			return; // do nothing
		}
		f.INVOKEVIRTUAL();
	}

	@Override
	public void CALL_RESULT(Object res, String owner, String name, String desc) {
		StringFunction f = getStringFunction(owner, name, desc);
		if (f == null) {
			return; // do nothing
		}
		f.CALL_RESULT(res);
	}

	private StringFunction getStringFunction(String owner, String name,
			String desc) {
		StringFunction f;
		StringFunctionKey k = new StringFunctionKey(owner, name, desc);
		f = stringFunctionTable.get(k);
		return f;
	}

	@Override
	public void CALL_RESULT(boolean res, String owner, String name, String desc) {
		StringFunction f = getStringFunction(owner, name, desc);
		if (f == null) {
			return; // do nothing
		}
		f.CALL_RESULT(res);
	}
}
