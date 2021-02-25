package org.evosuite.symbolic.expr;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.str.StringValue;

public class ExpressionUtils {
  public static boolean isIntegerValue(Expression e) {
    return e instanceof IntegerValue;
  }

  public static boolean isStringValue(Expression e) {
    return e instanceof StringValue;
  }

  public static boolean isRealValue(Expression e) {
    return e instanceof RealValue;
  }
}
