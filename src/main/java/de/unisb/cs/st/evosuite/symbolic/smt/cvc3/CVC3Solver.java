/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.smt.cvc3;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cvc3.Expr;
import cvc3.FlagsMut;
import cvc3.SatResult;
import cvc3.ValidityChecker;
import de.unisb.cs.st.evosuite.symbolic.Solver;
import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;

/**
 * @author Gordon Fraser
 * 
 */
public class CVC3Solver extends Thread implements Solver {

	private static Logger logger = LoggerFactory.getLogger(Solver.class);

	private ValidityChecker vc = null;

	private FlagsMut flags = null;

	private CVC3Converter cvc3 = null;

	private final Pattern paramPattern = Pattern.compile("\\(([0-9a-zA-Z_]+) = (-?[0-9\\.,]+)\\)");

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.symbolic.Solver#getModel(java.util.Collection)
	 */
	@Override
	public Map<String, Object> getModel(Collection<Constraint<?>> constraints) {
		Map<String, Object> result = null;
		initializeSolver();
		vc.push();
		Expr cvc3Expr = cvc3.convert(constraints);
		SatResult r = vc.checkUnsat(cvc3Expr);

		if (r == SatResult.SATISFIABLE) {
			@SuppressWarnings("unchecked")
			Map<Expr, Expr> rawModel = vc.getConcreteModel();
			result = convertRawModel(rawModel);
		}

		vc.pop();

		deinitialize();
		return result;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.symbolic.Solver#solve(java.util.Collection)
	 */
	@Override
	public boolean solve(Collection<Constraint<?>> constraints) {
		initializeSolver();
		vc.push();
		Expr cvc3Expr = cvc3.convert(constraints);
		logger.debug("Checking new expression: " + cvc3Expr);
		SatResult result = vc.checkUnsat(cvc3Expr);
		deinitialize();
		if (result == SatResult.SATISFIABLE)
			return true;

		vc.pop();
		return false;
	}

	private Map<String, Object> convertRawModel(Map<Expr, Expr> model) {
		logger.debug("Converting raw Model.");
		Map<String, Object> result = new HashMap<String, Object>();
		for (Map.Entry<Expr, Expr> entry : model.entrySet()) {
			String paramString = getParamValueString(entry.getKey(), entry.getValue());
			logger.debug("Analyzing expression to get the parameter value: "
			        + paramString);
			Matcher matcher = paramPattern.matcher(paramString);
			matcher.find();
			String name = matcher.group(1);
			String valueString = matcher.group(2);
			// TODO Treat other types as they occur...
			Long value = Long.parseLong(valueString);
			result.put(name, value);
		}

		return result;
	}

	private String getParamValueString(Expr paramExpr, Expr valueExpr) {
		Expr eq;
		if (paramExpr.getType().isBoolean()) {
			if (valueExpr.isTrue()) {
				eq = paramExpr;
			} else {
				eq = vc.notExpr(paramExpr);
			}
		} else {
			eq = vc.eqExpr(paramExpr, valueExpr);
		}
		String paramString = eq.toString();
		return paramString;
	}

	private void initializeSolver() {
		logger.debug("Initializing solver.");
		flags = ValidityChecker.createFlags(null);
		// Print expressions with sharing as DAGs
		flags.setFlag("dagify-exprs", false);
		// Set time resource limit in tenths of seconds for a query(0==no limit)
		flags.setFlag("stimeout", 30 * 60 * 10);
		// Kill cvc3 process after given number of seconds (0==no limit)
		flags.setFlag("timeout", 45 * 60 * 10);

		vc = ValidityChecker.create(flags);
		cvc3 = new CVC3Converter(vc);
		logger.debug("CVC3 initialized.");
	}

	private void deinitialize() {
		vc.delete();
		flags.delete();
		vc = null;
		flags = null;
		cvc3 = null;
		logger.debug("Deinitialized cvc3.");
	}
}
