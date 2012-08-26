package org.evosuite.symbolic.vm;

import java.util.Map;

import org.evosuite.symbolic.vm.math.ABS;
import org.evosuite.symbolic.vm.math.ACOS;
import org.evosuite.symbolic.vm.math.ASIN;
import org.evosuite.symbolic.vm.math.ATAN;
import org.evosuite.symbolic.vm.math.ATAN2;
import org.evosuite.symbolic.vm.math.CBRT;
import org.evosuite.symbolic.vm.math.CEIL;
import org.evosuite.symbolic.vm.math.COS;
import org.evosuite.symbolic.vm.math.COSH;
import org.evosuite.symbolic.vm.math.CopySign;
import org.evosuite.symbolic.vm.math.EXP;
import org.evosuite.symbolic.vm.math.EXPM1;
import org.evosuite.symbolic.vm.math.FLOOR;
import org.evosuite.symbolic.vm.math.GetExponent;
import org.evosuite.symbolic.vm.math.HYPOT;
import org.evosuite.symbolic.vm.math.IEEEremainder;
import org.evosuite.symbolic.vm.math.LOG;
import org.evosuite.symbolic.vm.math.LOG10;
import org.evosuite.symbolic.vm.math.LOG1P;
import org.evosuite.symbolic.vm.math.MAX;
import org.evosuite.symbolic.vm.math.MIN;
import org.evosuite.symbolic.vm.math.NextAfter;
import org.evosuite.symbolic.vm.math.NextUp;
import org.evosuite.symbolic.vm.math.POW;
import org.evosuite.symbolic.vm.math.RINT;
import org.evosuite.symbolic.vm.math.Round;
import org.evosuite.symbolic.vm.math.SCALB;
import org.evosuite.symbolic.vm.math.SIGNUM;
import org.evosuite.symbolic.vm.math.SIN;
import org.evosuite.symbolic.vm.math.SINH;
import org.evosuite.symbolic.vm.math.SQRT;
import org.evosuite.symbolic.vm.math.TAN;
import org.evosuite.symbolic.vm.math.TANH;
import org.evosuite.symbolic.vm.math.ToDegrees;
import org.evosuite.symbolic.vm.math.ToRadians;
import org.evosuite.symbolic.vm.math.ULP;
import org.evosuite.symbolic.vm.regex.Matcher_Matches;
import org.evosuite.symbolic.vm.regex.Pattern_Matcher;
import org.evosuite.symbolic.vm.regex.Pattern_Matches;
import org.evosuite.symbolic.vm.string.CharAt;
import org.evosuite.symbolic.vm.string.CompareTo;
import org.evosuite.symbolic.vm.string.CompareToIgnoreCase;
import org.evosuite.symbolic.vm.string.Concat;
import org.evosuite.symbolic.vm.string.Contains;
import org.evosuite.symbolic.vm.string.EndsWith;
import org.evosuite.symbolic.vm.string.Equals;
import org.evosuite.symbolic.vm.string.EqualsIgnoreCase;
import org.evosuite.symbolic.vm.string.IndexOf;
import org.evosuite.symbolic.vm.string.LastIndexOf;
import org.evosuite.symbolic.vm.string.Length;
import org.evosuite.symbolic.vm.string.Matches;
import org.evosuite.symbolic.vm.string.RegionMatches;
import org.evosuite.symbolic.vm.string.Replace;
import org.evosuite.symbolic.vm.string.ReplaceAll;
import org.evosuite.symbolic.vm.string.ReplaceFirst;
import org.evosuite.symbolic.vm.string.StartsWith;
import org.evosuite.symbolic.vm.string.Substring;
import org.evosuite.symbolic.vm.string.ToLowerCase;
import org.evosuite.symbolic.vm.string.ToString;
import org.evosuite.symbolic.vm.string.ToUpperCase;
import org.evosuite.symbolic.vm.string.Trim;
import org.evosuite.symbolic.vm.string.ValueOf;
import org.evosuite.symbolic.vm.string.builder.SB_Append;
import org.evosuite.symbolic.vm.string.builder.SB_Init;
import org.evosuite.symbolic.vm.string.builder.SB_ToString;
import org.evosuite.symbolic.vm.wrappers.B_ByteValue;
import org.evosuite.symbolic.vm.wrappers.B_Init;
import org.evosuite.symbolic.vm.wrappers.B_ValueOf;
import org.evosuite.symbolic.vm.wrappers.C_CharValue;
import org.evosuite.symbolic.vm.wrappers.C_Init;
import org.evosuite.symbolic.vm.wrappers.C_ValueOf;
import org.evosuite.symbolic.vm.wrappers.D_DoubleValue;
import org.evosuite.symbolic.vm.wrappers.D_Init;
import org.evosuite.symbolic.vm.wrappers.D_ValueOf;
import org.evosuite.symbolic.vm.wrappers.F_FloatValue;
import org.evosuite.symbolic.vm.wrappers.F_Init;
import org.evosuite.symbolic.vm.wrappers.F_ValueOf;
import org.evosuite.symbolic.vm.wrappers.I_Init;
import org.evosuite.symbolic.vm.wrappers.I_IntValue;
import org.evosuite.symbolic.vm.wrappers.I_ValueOf;
import org.evosuite.symbolic.vm.wrappers.J_Init;
import org.evosuite.symbolic.vm.wrappers.J_LongValue;
import org.evosuite.symbolic.vm.wrappers.J_ValueOf;
import org.evosuite.symbolic.vm.wrappers.S_Init;
import org.evosuite.symbolic.vm.wrappers.S_ShortValue;
import org.evosuite.symbolic.vm.wrappers.S_ValueOf;
import org.evosuite.symbolic.vm.wrappers.Z_BooleanValue;
import org.evosuite.symbolic.vm.wrappers.Z_Init;
import org.evosuite.symbolic.vm.wrappers.Z_ValueOf;

import edu.uta.cse.dsc.AbstractVM;
import gnu.trove.map.hash.THashMap;

/**
 * 
 * @author galeotti
 * 
 */
public final class FunctionVM extends AbstractVM {

	private static class FunctionKey {
		public FunctionKey(String owner, String name, String desc) {
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
			if (o == null || !o.getClass().equals(FunctionKey.class)) {
				return false;
			} else {
				FunctionKey that = (FunctionKey) o;
				return this.owner.equals(that.owner)
						&& this.name.equals(that.name)
						&& this.desc.equals(that.desc);
			}
		}
	}

	private final SymbolicEnvironment env;

	public FunctionVM(SymbolicEnvironment env) {
		this.env = env;
		fillFunctionsTable();
	}

	private void fillFunctionsTable() {
		// java.lang.Integer
		addFunctionToTable(new I_Init(env));
		addFunctionToTable(new I_ValueOf(env));
		addFunctionToTable(new I_IntValue(env));

		// java.lang.Long
		addFunctionToTable(new J_Init(env));
		addFunctionToTable(new J_ValueOf(env));
		addFunctionToTable(new J_LongValue(env));

		// java.lang.Float
		addFunctionToTable(new F_Init(env));
		addFunctionToTable(new F_ValueOf(env));
		addFunctionToTable(new F_FloatValue(env));

		// java.lang.Double
		addFunctionToTable(new D_Init(env));
		addFunctionToTable(new D_ValueOf(env));
		addFunctionToTable(new D_DoubleValue(env));

		// java.lang.Short
		addFunctionToTable(new S_Init(env));
		addFunctionToTable(new S_ValueOf(env));
		addFunctionToTable(new S_ShortValue(env));

		// java.lang.Byte
		addFunctionToTable(new B_Init(env));
		addFunctionToTable(new B_ValueOf(env));
		addFunctionToTable(new B_ByteValue(env));

		// java.lang.Character
		addFunctionToTable(new C_Init(env));
		addFunctionToTable(new C_ValueOf(env));
		addFunctionToTable(new C_CharValue(env));

		// java.lang.Boolean
		addFunctionToTable(new Z_Init(env));
		addFunctionToTable(new Z_ValueOf(env));
		addFunctionToTable(new Z_BooleanValue(env));

		// java.lang.Math
		addFunctionToTable(new ABS.ABS_I(env));
		addFunctionToTable(new ABS.ABS_L(env));
		addFunctionToTable(new ABS.ABS_F(env));
		addFunctionToTable(new ABS.ABS_D(env));
		addFunctionToTable(new ACOS(env));
		addFunctionToTable(new ASIN(env));
		addFunctionToTable(new ATAN(env));
		addFunctionToTable(new ATAN2(env));
		addFunctionToTable(new CBRT(env));
		addFunctionToTable(new CEIL(env));
		addFunctionToTable(new CopySign.CopySign_F(env));
		addFunctionToTable(new CopySign.CopySign_D(env));
		addFunctionToTable(new COS(env));
		addFunctionToTable(new COSH(env));
		addFunctionToTable(new EXP(env));
		addFunctionToTable(new EXPM1(env));
		addFunctionToTable(new FLOOR(env));
		addFunctionToTable(new GetExponent.GetExponent_F(env));
		addFunctionToTable(new GetExponent.GetExponent_D(env));
		addFunctionToTable(new HYPOT(env));
		addFunctionToTable(new IEEEremainder(env));
		addFunctionToTable(new LOG(env));
		addFunctionToTable(new LOG10(env));
		addFunctionToTable(new LOG1P(env));
		addFunctionToTable(new MIN.MIN_I(env));
		addFunctionToTable(new MIN.MIN_L(env));
		addFunctionToTable(new MIN.MIN_F(env));
		addFunctionToTable(new MIN.MIN_D(env));
		addFunctionToTable(new MAX.MAX_I(env));
		addFunctionToTable(new MAX.MAX_L(env));
		addFunctionToTable(new MAX.MAX_F(env));
		addFunctionToTable(new MAX.MAX_D(env));
		addFunctionToTable(new NextAfter.NextAfter_F(env));
		addFunctionToTable(new NextAfter.NextAfter_D(env));
		addFunctionToTable(new NextUp.NextUp_F(env));
		addFunctionToTable(new NextUp.NextUp_D(env));
		addFunctionToTable(new POW(env));
		addFunctionToTable(new RINT(env));
		addFunctionToTable(new Round.Round_F(env));
		addFunctionToTable(new Round.Round_D(env));
		addFunctionToTable(new SCALB.SCALB_F(env));
		addFunctionToTable(new SCALB.SCALB_D(env));
		addFunctionToTable(new SIGNUM.SIGNUM_F(env));
		addFunctionToTable(new SIGNUM.SIGNUM_D(env));
		addFunctionToTable(new SIN(env));
		addFunctionToTable(new SINH(env));
		addFunctionToTable(new SQRT(env));
		addFunctionToTable(new TAN(env));
		addFunctionToTable(new TANH(env));
		addFunctionToTable(new ToDegrees(env));
		addFunctionToTable(new ToRadians(env));
		addFunctionToTable(new ULP.ULP_F(env));
		addFunctionToTable(new ULP.ULP_D(env));

		// java.lang.String
		addFunctionToTable(new CharAt(env));
		addFunctionToTable(new CompareTo(env));
		addFunctionToTable(new CompareToIgnoreCase(env));
		addFunctionToTable(new Concat(env));
		addFunctionToTable(new Contains(env));
		addFunctionToTable(new EndsWith(env));
		addFunctionToTable(new Matches(env));
		addFunctionToTable(new Equals(env));
		addFunctionToTable(new EqualsIgnoreCase(env));
		addFunctionToTable(new IndexOf.IndexOf_C(env));
		addFunctionToTable(new IndexOf.IndexOf_S(env));
		addFunctionToTable(new IndexOf.IndexOf_CI(env));
		addFunctionToTable(new IndexOf.IndexOf_SI(env));
		addFunctionToTable(new LastIndexOf.LastIndexOf_C(env));
		addFunctionToTable(new LastIndexOf.LastIndexOf_S(env));
		addFunctionToTable(new LastIndexOf.LastIndexOf_CI(env));
		addFunctionToTable(new LastIndexOf.LastIndexOf_SI(env));
		addFunctionToTable(new Length(env));
		addFunctionToTable(new RegionMatches(env));
		addFunctionToTable(new Replace.Replace_C(env));
		addFunctionToTable(new Replace.Replace_CS(env));
		addFunctionToTable(new ReplaceAll(env));
		addFunctionToTable(new ReplaceFirst(env));
		addFunctionToTable(new StartsWith(env));
		addFunctionToTable(new Substring(env));
		addFunctionToTable(new ToLowerCase(env));
		addFunctionToTable(new ToString(env));
		addFunctionToTable(new ToUpperCase(env));
		addFunctionToTable(new Trim(env));
		addFunctionToTable(new ValueOf.ValueOf_O(env));

		// java.lang.StringBuilder
		addFunctionToTable(new SB_Init.StringBuilderInit_CS(env));
		addFunctionToTable(new SB_Init.StringBuilderInit_S(env));
		addFunctionToTable(new SB_Append.Append_B(env));
		addFunctionToTable(new SB_Append.Append_C(env));
		addFunctionToTable(new SB_Append.Append_D(env));
		addFunctionToTable(new SB_Append.Append_F(env));
		addFunctionToTable(new SB_Append.Append_I(env));
		addFunctionToTable(new SB_Append.Append_L(env));
		addFunctionToTable(new SB_Append.Append_O(env));
		addFunctionToTable(new SB_Append.Append_S(env));
		addFunctionToTable(new SB_ToString(env));

		// java.util.regex.Pattern
		addFunctionToTable(new Pattern_Matches(env));
		addFunctionToTable(new Pattern_Matcher(env));

		// java.util.regex.Matcher
		addFunctionToTable(new Matcher_Matches(env));
	}

	private void addFunctionToTable(Function f) {
		FunctionKey k = new FunctionKey(f.getOwner(), f.getName(), f.getDesc());
		functionsTable.put(k, f);
	}

	@Override
	public void INVOKESTATIC(String owner, String name, String desc) {
		functionUnderExecution = getStringFunction(owner, name, desc);
		if (functionUnderExecution != null) {
			functionUnderExecution.INVOKESTATIC();
		}
	}

	@Override
	public void INVOKEVIRTUAL(Object conc_receiver, String owner, String name,
			String desc) {
		if (conc_receiver == null) {
			return;
		}

		functionUnderExecution = getStringFunction(owner, name, desc);
		if (functionUnderExecution != null) {
			functionUnderExecution.INVOKEVIRTUAL(conc_receiver);
		}
	}

	private final Map<FunctionKey, Function> functionsTable = new THashMap<FunctionKey, Function>();

	private Function getStringFunction(String owner, String name, String desc) {
		FunctionKey k = new FunctionKey(owner, name, desc);
		Function f = functionsTable.get(k);
		return f;
	}

	@Override
	public void CALL_RESULT(int res, String owner, String name, String desc) {
		if (functionUnderExecution != null) {
			functionUnderExecution.CALL_RESULT(res);
		}
		functionUnderExecution = null;
	}

	@Override
	public void CALL_RESULT(Object res, String owner, String name, String desc) {
		if (functionUnderExecution != null) {
			functionUnderExecution.CALL_RESULT(res);
		}
		functionUnderExecution = null;
	}

	@Override
	public void CALL_RESULT(String owner, String name, String desc) {
		if (functionUnderExecution != null) {
			functionUnderExecution.CALL_RESULT();
		}
		functionUnderExecution = null;
	}

	@Override
	public void CALL_RESULT(boolean res, String owner, String name, String desc) {
		if (functionUnderExecution != null) {
			functionUnderExecution.CALL_RESULT(res);
		}
		functionUnderExecution = null;
	}

	@Override
	public void CALL_RESULT(long res, String owner, String name, String desc) {
		if (functionUnderExecution != null) {
			functionUnderExecution.CALL_RESULT(res);
		}
		functionUnderExecution = null;
	}

	@Override
	public void CALL_RESULT(double res, String owner, String name, String desc) {
		if (functionUnderExecution != null) {
			functionUnderExecution.CALL_RESULT(res);
		}
	}

	@Override
	public void CALL_RESULT(float res, String owner, String name, String desc) {
		if (functionUnderExecution != null) {
			functionUnderExecution.CALL_RESULT(res);
		}
	}

	@Override
	public void INVOKEVIRTUAL(String owner, String name, String desc) {
		functionUnderExecution = getStringFunction(owner, name, desc);
		if (functionUnderExecution != null) {
			functionUnderExecution.INVOKEVIRTUAL();
		}
	}

	@Override
	public void INVOKESPECIAL(String owner, String name, String desc) {
		functionUnderExecution = getStringFunction(owner, name, desc);
		if (functionUnderExecution != null) {
			functionUnderExecution.INVOKESPECIAL();
		}
	}

	@Override
	public void INVOKESPECIAL(Object receiver, String owner, String name,
			String desc) {
		Function f = getStringFunction(owner, name, desc);
		if (f != null) {
			f.INVOKESPECIAL(receiver);
		}
	}

	@Override
	public void INVOKEINTERFACE(String owner, String name, String desc) {
		functionUnderExecution = getStringFunction(owner, name, desc);
		if (functionUnderExecution != null) {
			functionUnderExecution.INVOKEINTERFACE();
		}
	}

	private Function functionUnderExecution;

	@Override
	public void INVOKEINTERFACE(Object receiver, String owner, String name,
			String desc) {
		functionUnderExecution = getStringFunction(owner, name, desc);
		if (functionUnderExecution != null) {
			functionUnderExecution.INVOKEINTERFACE(receiver);
		}
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, int value) {
		if (functionUnderExecution != null) {
			functionUnderExecution.CALLER_STACK_PARAM(nr, calleeLocalsIndex,
					value);
		}
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, boolean value) {
		if (functionUnderExecution != null) {
			functionUnderExecution.CALLER_STACK_PARAM(nr, calleeLocalsIndex,
					value);
		}
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, short value) {
		if (functionUnderExecution != null) {
			functionUnderExecution.CALLER_STACK_PARAM(nr, calleeLocalsIndex,
					value);
		}
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, byte value) {
		if (functionUnderExecution != null) {
			functionUnderExecution.CALLER_STACK_PARAM(nr, calleeLocalsIndex,
					value);
		}
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, char value) {
		if (functionUnderExecution != null) {
			functionUnderExecution.CALLER_STACK_PARAM(nr, calleeLocalsIndex,
					value);
		}
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, long value) {
		if (functionUnderExecution != null) {
			functionUnderExecution.CALLER_STACK_PARAM(nr, calleeLocalsIndex,
					value);
		}
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, float value) {
		if (functionUnderExecution != null) {
			functionUnderExecution.CALLER_STACK_PARAM(nr, calleeLocalsIndex,
					value);
		}
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, double value) {
		if (functionUnderExecution != null) {
			functionUnderExecution.CALLER_STACK_PARAM(nr, calleeLocalsIndex,
					value);
		}
	}

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, Object value) {
		if (functionUnderExecution != null) {
			functionUnderExecution.CALLER_STACK_PARAM(nr, calleeLocalsIndex,
					value);
		}
	}

}
