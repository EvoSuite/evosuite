//
//Copyright (C) 2005 United States Government as represented by the
//Administrator of the National Aeronautics and Space Administration
//(NASA).  All Rights Reserved.
//
//This software is distributed under the NASA Open Source Agreement
//(NOSA), version 1.3.  The NOSA has been approved by the Open Source
//Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
//directory tree for the complete NOSA document.
//
//THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
//KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
//LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
//SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
//A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
//THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
//DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package de.unisb.cs.st.evosuite.symbolic.expr;

public enum Operator {

	// Simple operators
	DIV("/", 2), //
	MUL("*", 2), //
	MINUS("-", 2), //
	PLUS("+", 2), //
	NEG("-",1),//negate
	REM("%",2),//Remainder

	// Logical operators
	AND("&", 2), //
	OR("|", 2), //
	XOR("^", 2), //

	// special operators
	D2L("d2l", 1), //
	L2D("l2d", 1), //
	CMP("cmp", 2), //

	// Integer operators
	IAND("iand", 2), //
	IOR("ior", 2), //
	SHR("ishr", 2), //
	SHL("ishl", 2), //
	IXOR("ixor", 2), //

	// More complex Math operators
	SIN("sin", 1), //
	COS("cos", 1), //
	POW("pow", 2), //
	SQRT("sqrt", 1), //
	ROUND("round", 1), //
	FLOOR("floor",1),
	CEIL("ceil",1),
	EXP("exp", 1), //
	ASIN("asin", 1), //
	ACOS("acos", 1), //
	ATAN("atan", 1), //
	ATAN2("atan2", 2), //
	LOG("log", 1), //
	LOG10("log10", 1), //
	TAN("tan", 1),
	
	// String comparisons
	EQUALSIGNORECASE("equalsIgnoreCase", 2),
	EQUALS("equals", 2),
	STARTSWITH("startsWith", 3),
	ENDSWITH("endsWith", 2),
	CONTAINS("contains", 2),
	REGIONMATCHES("regionMatches", 6), 
	COMPARETO("compareTo", 2), 
	COMPARETOIGNORECASE("compareToIgnoreCase", 2),
	// String operators
	
	TRIM("trim", 1), 
	LENGTH("length", 1), 
	TOLOWERCASE("toLowerCase", 1), 
	
	CONCAT("concat", 2), 
	APPEND("append", 2), 
	INDEXOFC("indexOfC", 2),
	INDEXOFS("indexOfS", 2),
	CHARAT("charAt", 2),
	
	REPLACEC("replacec", 3),
	REPLACECS("replacecs", 3), 
	REPLACEALL("replaceAll", 3),
	REPLACEFIRST("replaceFirst", 3),
	INDEXOFCI("indexOfCI", 3),
	INDEXOFSI("indexOfSI", 3),
	SUBSTRING("substring", 3) ;

	private final String str;
	private final int arity;

	Operator(String str, int arity) {
		this.str = str;
		this.arity = arity;
	}

	public int getArity() {
		return arity;
	}

	@Override
	public String toString() {
		return " " + str + " ";
	}
}