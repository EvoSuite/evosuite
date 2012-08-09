package edu.uta.cse.dsc.vm2.math;

import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.RealExpression;

public abstract class MathFunction {

	public MathFunction(String name, String desc) {
		super();
		this.owner = MathFunctionCallVM.JAVA_LANG_MATH;
		this.name = name;
		this.desc = desc;
	}

	private final String owner;
	private final String name;
	private final String desc;

	public String getOwner() {
		return owner;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public RealExpression execute(Stack<Expression<?>> params, double res) {
		return null;
	}

	public RealExpression execute(Stack<Expression<?>> params, float res) {
		return null;
	}

	public IntegerExpression execute(Stack<Expression<?>> params, int res) {
		return null;
	}

	public IntegerExpression execute(Stack<Expression<?>> params, long res) {
		return null;
	}

}
