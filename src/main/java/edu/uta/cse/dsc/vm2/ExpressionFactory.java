package edu.uta.cse.dsc.vm2;

import org.evosuite.symbolic.expr.IntegerConstant;
import org.evosuite.symbolic.expr.RealConstant;
import org.evosuite.symbolic.expr.StringConstant;

public abstract class ExpressionFactory {

	public static final RealConstant RCONST_2 = new RealConstant(2);
	public static final RealConstant RCONST_1 = new RealConstant(1);
	public static final RealConstant RCONST_0 = new RealConstant(0);
	public static final IntegerConstant ICONST_5 = new IntegerConstant(5);
	public static final IntegerConstant ICONST_4 = new IntegerConstant(4);
	public static final IntegerConstant ICONST_3 = new IntegerConstant(3);
	public static final IntegerConstant ICONST_2 = new IntegerConstant(2);
	public static final IntegerConstant ICONST_1 = new IntegerConstant(1);
	public static final IntegerConstant ICONST_0 = new IntegerConstant(0);
	public static final IntegerConstant ICONST_M1 = new IntegerConstant(-1);

	public static IntegerConstant buildNewIntegerConstant(int value) {
		return buildNewIntegerConstant((long) value);
	}

	public static IntegerConstant buildNewIntegerConstant(long value) {
		if (value == -1)
			return ICONST_M1;
		else if (value == 0)
			return ICONST_0;
		else if (value == 1)
			return ICONST_1;
		else if (value == 2)
			return ICONST_2;
		else if (value == 3)
			return ICONST_3;
		else if (value == 4)
			return ICONST_4;
		else if (value == 5)
			return ICONST_5;

		return new IntegerConstant(value);
	}

	public static RealConstant buildNewRealConstant(float x) {
		return buildNewRealConstant((double) x);
	}

	public static RealConstant buildNewRealConstant(double x) {
		if (x == 0)
			return RCONST_0;
		else if (x == 1)
			return RCONST_1;
		else if (x == 2)
			return RCONST_2;

		return new RealConstant(x);
	}

	public static StringConstant buildNewStringConstant(String string) {
		return new StringConstant(string.intern());
	}

}
