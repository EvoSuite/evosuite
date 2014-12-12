package org.evosuite.symbolic.solver.cvc4;

import org.evosuite.symbolic.solver.smt.SmtExpr;
import org.evosuite.symbolic.solver.smt.SmtStringConstant;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RegExpVisitor;

public class RegExpToCVC4Visitor extends RegExpVisitor<SmtExpr> {

	@Override
	public SmtExpr visitUnion(RegExp e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SmtExpr visitString(RegExp e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SmtExpr visitRepeatMinMax(RegExp e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SmtExpr visitRepeatMin(RegExp e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SmtExpr visitRepeat(RegExp arg) {
		SmtExpr expr = this.visitRegExp(arg);
		if (expr == null) {
			return null;
		}
		SmtExpr repeatExpr = CVC4ExprBuilder.mkReKleeneStar(expr);
		return repeatExpr;
	}

	@Override
	public SmtExpr visitOptional(RegExp e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SmtExpr visitInterval(RegExp e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SmtExpr visitIntersection(RegExp e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SmtExpr visitEmpty(RegExp e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SmtExpr visitConcatenation(RegExp left, RegExp right) {
		SmtExpr leftExpr = this.visitRegExp(left);
		SmtExpr rightExpr = this.visitRegExp(right);
		if (leftExpr == null || rightExpr == null) {
			return null;
		}
		SmtExpr concat = CVC4ExprBuilder.mkRegExpConcat(leftExpr, rightExpr);
		return concat;
	}

	@Override
	public SmtExpr visitComplement(RegExp e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SmtExpr visitCharRange(RegExp e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SmtExpr visitChar(char c) {
		String str = String.valueOf(c);
		SmtStringConstant strConstant = CVC4ExprBuilder.mkStringConstant(str);
		SmtExpr expr = CVC4ExprBuilder.mkStrToRegExp(strConstant);
		return expr;
	}

	@Override
	public SmtExpr visitAutomaton(RegExp e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SmtExpr visitAnyString(RegExp e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SmtExpr visitAnyChar(RegExp e) {
		// TODO Auto-generated method stub
		return null;
	}

}
