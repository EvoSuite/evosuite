package org.evosuite.symbolic.solver.cvc4;

import org.evosuite.symbolic.solver.smt.SmtExpr;
import org.evosuite.symbolic.solver.smt.SmtIntConstant;
import org.evosuite.symbolic.solver.smt.SmtStringConstant;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RegExpVisitor;

public class RegExpToCVC4Visitor extends RegExpVisitor<SmtExpr> {

	@Override
	public SmtExpr visitUnion(RegExp left, RegExp right) {
		SmtExpr leftExpr = visitRegExp(left);
		SmtExpr rightExpr = visitRegExp(right);
		if (leftExpr == null || rightExpr == null) {
			return null;
		}
		SmtExpr unionExpr = CVC4ExprBuilder.mkRegExpUnion(leftExpr, rightExpr);
		return unionExpr;
	}

	@Override
	public SmtExpr visitString(String s) {
		SmtStringConstant strConstant = CVC4ExprBuilder.mkStringConstant(s);
		SmtExpr strToRegExpr = CVC4ExprBuilder.mkStrToRegExp(strConstant);
		return strToRegExpr;
	}

	@Override
	public SmtExpr visitRepeatMinMax(RegExp e, int min, int max) {
		SmtExpr regExpr = this.visitRegExp(e);
		if (regExpr == null) {
			return null;
		}
		SmtIntConstant minExpr = CVC4ExprBuilder.mkIntConstant(min);
		SmtIntConstant maxExpr = CVC4ExprBuilder.mkIntConstant(max);
		SmtExpr loopExpr = CVC4ExprBuilder.mkLoop(regExpr, minExpr, maxExpr);
		return loopExpr;

	}

	@Override
	public SmtExpr visitRepeatMin(RegExp e, int min) {
		SmtExpr regExpr = this.visitRegExp(e);
		if (regExpr == null) {
			return null;
		}
		if (min == 1) {
			SmtExpr kleeneCrossExpr = CVC4ExprBuilder
					.mkRegExpKleeCross(regExpr);
			return kleeneCrossExpr;
		} else {
			SmtIntConstant minExpr = CVC4ExprBuilder.mkIntConstant(min);
			SmtExpr loopExpr = CVC4ExprBuilder.mkLoop(regExpr, minExpr);
			return loopExpr;
		}
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
		SmtExpr expr = this.visitRegExp(e);
		if (expr == null) {
			return null;
		}
		SmtExpr optExpr = CVC4ExprBuilder.mkRegExpOptional(expr);
		return optExpr;
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
	public SmtExpr visitAnyChar() {
		return CVC4ExprBuilder.mkRegExpAllChar();
	}

}
