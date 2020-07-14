package org.evosuite.symbolic.solver.smt;

public class SmtArrayVariable {

  public static class SmtIntegerArrayVariable extends SmtVariable {
    public SmtIntegerArrayVariable(String varName) {
      super(varName);
    }

    @Override
    public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
      return v.visit(this, arg);
    }
  }

  public static class SmtRealArrayVariable extends SmtVariable {
    public SmtRealArrayVariable(String varName) {
      super(varName);
    }

    @Override
    public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
      return v.visit(this, arg);
    }
  }

  public static class SmtStringArrayVariable extends SmtVariable {
    public SmtStringArrayVariable(String varName) {
      super(varName);
    }

    @Override
    public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
      return v.visit(this, arg);
    }
  }

  public static class SmtReferenceArrayVariable extends SmtVariable {
    public SmtReferenceArrayVariable(String varName) {
      super(varName);
    }

    @Override
    public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
      return v.visit(this, arg);
    }
  }
}
