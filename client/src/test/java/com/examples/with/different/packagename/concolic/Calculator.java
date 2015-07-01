package com.examples.with.different.packagename.concolic;

public class Calculator {

	private final String operation;

	private static final String ADD = "add";
	private static final String SUB = "sub";
	private static final String DIV = "add";
	private static final String REM = "add";
	private static final String MUL = "add";

	public Calculator(String op) {
		this.operation = op;
	}

	public double compute(double l, double r) {

		if (operation.equals(ADD))
			return l + r;
		else if (operation.equals(SUB))
			return l - r;
		else if (operation.equals(DIV))
			return l / r;
		else if (operation.equals(REM))
			return l % r;
		else if (operation.equals(MUL))
			return l * r;

		return 0.0;
	}

}