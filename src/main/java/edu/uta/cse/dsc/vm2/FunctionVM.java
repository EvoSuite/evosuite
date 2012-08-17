package edu.uta.cse.dsc.vm2;

import java.util.Map;

import edu.uta.cse.dsc.AbstractVM;
import edu.uta.cse.dsc.vm2.math.ABS;
import edu.uta.cse.dsc.vm2.math.ACOS;
import edu.uta.cse.dsc.vm2.math.ASIN;
import edu.uta.cse.dsc.vm2.math.ATAN;
import edu.uta.cse.dsc.vm2.math.ATAN2;
import edu.uta.cse.dsc.vm2.math.CBRT;
import edu.uta.cse.dsc.vm2.math.CEIL;
import edu.uta.cse.dsc.vm2.math.COS;
import edu.uta.cse.dsc.vm2.math.COSH;
import edu.uta.cse.dsc.vm2.math.CopySign;
import edu.uta.cse.dsc.vm2.math.EXP;
import edu.uta.cse.dsc.vm2.math.EXPM1;
import edu.uta.cse.dsc.vm2.math.FLOOR;
import edu.uta.cse.dsc.vm2.math.GetExponent;
import edu.uta.cse.dsc.vm2.math.HYPOT;
import edu.uta.cse.dsc.vm2.math.IEEEremainder;
import edu.uta.cse.dsc.vm2.math.LOG;
import edu.uta.cse.dsc.vm2.math.LOG10;
import edu.uta.cse.dsc.vm2.math.LOG1P;
import edu.uta.cse.dsc.vm2.math.MAX;
import edu.uta.cse.dsc.vm2.math.MIN;
import edu.uta.cse.dsc.vm2.math.NextAfter;
import edu.uta.cse.dsc.vm2.math.NextUp;
import edu.uta.cse.dsc.vm2.math.POW;
import edu.uta.cse.dsc.vm2.math.RINT;
import edu.uta.cse.dsc.vm2.math.Round;
import edu.uta.cse.dsc.vm2.math.SCALB;
import edu.uta.cse.dsc.vm2.math.SIGNUM;
import edu.uta.cse.dsc.vm2.math.SIN;
import edu.uta.cse.dsc.vm2.math.SINH;
import edu.uta.cse.dsc.vm2.math.SQRT;
import edu.uta.cse.dsc.vm2.math.TAN;
import edu.uta.cse.dsc.vm2.math.TANH;
import edu.uta.cse.dsc.vm2.math.ToDegrees;
import edu.uta.cse.dsc.vm2.math.ToRadians;
import edu.uta.cse.dsc.vm2.math.ULP;
import edu.uta.cse.dsc.vm2.string.CharAt;
import edu.uta.cse.dsc.vm2.string.CompareTo;
import edu.uta.cse.dsc.vm2.string.CompareToIgnoreCase;
import edu.uta.cse.dsc.vm2.string.Concat;
import edu.uta.cse.dsc.vm2.string.Contains;
import edu.uta.cse.dsc.vm2.string.EndsWith;
import edu.uta.cse.dsc.vm2.string.Equals;
import edu.uta.cse.dsc.vm2.string.EqualsIgnoreCase;
import edu.uta.cse.dsc.vm2.string.IndexOf;
import edu.uta.cse.dsc.vm2.string.LastIndexOf;
import edu.uta.cse.dsc.vm2.string.Length;
import edu.uta.cse.dsc.vm2.string.RegionMatches;
import edu.uta.cse.dsc.vm2.string.Replace;
import edu.uta.cse.dsc.vm2.string.ReplaceAll;
import edu.uta.cse.dsc.vm2.string.ReplaceFirst;
import edu.uta.cse.dsc.vm2.string.StartsWith;
import edu.uta.cse.dsc.vm2.string.Substring;
import edu.uta.cse.dsc.vm2.string.ToLowerCase;
import edu.uta.cse.dsc.vm2.string.ToString;
import edu.uta.cse.dsc.vm2.string.ToUpperCase;
import edu.uta.cse.dsc.vm2.string.Trim;
import edu.uta.cse.dsc.vm2.string.ValueOf;
import edu.uta.cse.dsc.vm2.string.builder.SB_Append;
import edu.uta.cse.dsc.vm2.string.builder.SB_Init;
import edu.uta.cse.dsc.vm2.string.builder.SB_ToString;
import edu.uta.cse.dsc.vm2.wrappers.D_DoubleValue;
import edu.uta.cse.dsc.vm2.wrappers.D_ValueOf;
import edu.uta.cse.dsc.vm2.wrappers.F_FloatValue;
import edu.uta.cse.dsc.vm2.wrappers.F_ValueOf;
import edu.uta.cse.dsc.vm2.wrappers.I_IntValue;
import edu.uta.cse.dsc.vm2.wrappers.I_ValueOf;
import edu.uta.cse.dsc.vm2.wrappers.L_LongValue;
import edu.uta.cse.dsc.vm2.wrappers.L_ValueOf;
import gnu.trove.map.hash.THashMap;

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
		addFunctionToTable(new I_ValueOf(env));
		addFunctionToTable(new I_IntValue(env));
		
		// java.lang.Long
		addFunctionToTable(new L_ValueOf(env));
		addFunctionToTable(new L_LongValue(env));
		
		// java.lang.Float
		addFunctionToTable(new F_ValueOf(env));
		addFunctionToTable(new F_FloatValue(env));

		// java.lang.Double
		addFunctionToTable(new D_ValueOf(env));
		addFunctionToTable(new D_DoubleValue(env));
		
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
	}

	private void addFunctionToTable(Function f) {
		FunctionKey k = new FunctionKey(f.getOwner(), f.getName(), f.getDesc());
		functionsTable.put(k, f);
	}

	@Override
	public void INVOKESTATIC(String owner, String name, String desc) {
		Function f = getStringFunction(owner, name, desc);
		if (f != null) {
			f.INVOKESTATIC();
		}
	}

	@Override
	public void INVOKEVIRTUAL(Object conc_receiver, String owner, String name,
			String desc) {
		if (conc_receiver == null) {
			return;
		}

		Function f = getStringFunction(owner, name, desc);
		if (f != null) {
			f.INVOKEVIRTUAL(conc_receiver);
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
		Function f = getStringFunction(owner, name, desc);
		if (f != null) {
			f.CALL_RESULT(res);
		}
	}

	@Override
	public void CALL_RESULT(Object res, String owner, String name, String desc) {
		Function f = getStringFunction(owner, name, desc);
		if (f != null) {
			f.CALL_RESULT(res);
		}
	}

	@Override
	public void CALL_RESULT(String owner, String name, String desc) {
		Function f = getStringFunction(owner, name, desc);
		if (f != null) {
			f.CALL_RESULT();
		}
	}

	@Override
	public void CALL_RESULT(boolean res, String owner, String name, String desc) {
		Function f = getStringFunction(owner, name, desc);
		if (f != null) {
			f.CALL_RESULT(res);
		}
	}

	@Override
	public void CALL_RESULT(long res, String owner, String name, String desc) {
		Function f = getStringFunction(owner, name, desc);
		if (f != null) {
			f.CALL_RESULT(res);
		}
	}

	@Override
	public void CALL_RESULT(double res, String owner, String name, String desc) {
		Function f = getStringFunction(owner, name, desc);
		if (f != null) {
			f.CALL_RESULT(res);
		}
	}

	@Override
	public void CALL_RESULT(float res, String owner, String name, String desc) {
		Function f = getStringFunction(owner, name, desc);
		if (f != null) {
			f.CALL_RESULT(res);
		}
	}

	@Override
	public void INVOKEVIRTUAL(String owner, String name, String desc) {
		Function f = getStringFunction(owner, name, desc);
		if (f != null) {
			f.INVOKEVIRTUAL();
		}
	}

	@Override
	public void INVOKESPECIAL(String owner, String name, String desc) {
		Function f = getStringFunction(owner, name, desc);
		if (f != null) {
			f.INVOKESPECIAL();
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
		Function f = getStringFunction(owner, name, desc);
		if (f != null) {
			f.INVOKEINTERFACE();
		}
	}

	@Override
	public void INVOKEINTERFACE(Object receiver, String owner, String name,
			String desc) {
		Function f = getStringFunction(owner, name, desc);
		if (f != null) {
			f.INVOKEINTERFACE(receiver);
		}
	}

}
