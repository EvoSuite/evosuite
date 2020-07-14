package org.evosuite.symbolic.solver.smt;

public class SmtArrayConstant {

  public static class SmtIntegerArrayConstant extends SmtConstant {
    private final Object array;

    public SmtIntegerArrayConstant(Object arrayValue) {
      this.array = arrayValue;
    }

    @Override
    public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
          return v.visit(this,arg);
    }

    public Object getConstantValue() {
      return array;
  	}
  }

  public static class SmtRealArrayConstant extends SmtConstant {
    private final Object array;

    public SmtRealArrayConstant(Object arrayValue) {
      this.array = arrayValue;
    }

    @Override
    public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
          return v.visit(this,arg);
    }

    public Object getConstantValue() {
      return array;
  	}
  }

  public static class SmtStringArrayConstant extends SmtConstant {
    private final Object array;

    public SmtStringArrayConstant(Object arrayValue) {
      this.array = arrayValue;
    }

    @Override
    public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
          return v.visit(this,arg);
    }

    public Object getConstantValue() {
      return array;
  	}
  }

  public static class SmtReferenceArrayConstant extends SmtConstant {
    private final Object array;

    public SmtReferenceArrayConstant(Object arrayValue) {
      this.array = arrayValue;
    }

    @Override
    public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
          return v.visit(this,arg);
    }

    public Object getConstantValue() {
      return array;
  	}
  }
}
