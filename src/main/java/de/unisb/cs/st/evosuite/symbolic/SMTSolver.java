/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.smtlib.CharSequenceReader;
import org.smtlib.ICommand;
import org.smtlib.IExpr;
import org.smtlib.IExpr.IKeyword;
import org.smtlib.IParser;
import org.smtlib.IParser.ParserException;
import org.smtlib.IPrinter;
import org.smtlib.IResponse;
import org.smtlib.ISolver;
import org.smtlib.ISort;
import org.smtlib.ISource;
import org.smtlib.SMT;
import org.smtlib.Utils;

import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;

/**
 * @author Gordon Fraser
 * 
 */
public class SMTSolver {

	private final ISolver solver;

	private final SMT smt = new SMT();

	private ICommand logicCommand;

	private ICommand modelCommand;

	private final ISort boolSort;

	private final ISort intSort;

	private final ISort bv32Sort;

	private static Logger logger = Logger.getLogger(SMTSolver.class);

	public SMTSolver() {
		solver = new org.smtlib.solvers.Solver_cvc(smt.smtConfig,
		        "/Users/fraser/Documents/Source/cvc3-2.2/bin/i386-darwin10.2.0/cvc3");
		//solver = new org.smtlib.solvers.Solver_yices(smt.smtConfig,
		//        "/Users/fraser/Downloads/yices-1.0.29/bin/yices");
		IKeyword opt = smt.smtConfig.exprFactory.keyword(Utils.PRODUCE_MODELS);
		//IAttributeValue val = smt.smtConfig.commandFactory.;
		solver.set_option(opt, Utils.TRUE);
		solver.set_option(smt.smtConfig.exprFactory.keyword(":produce-models"),
		                  smt.smtConfig.exprFactory.symbol("true"));

		ISource source = smt.smtConfig.smtFactory.createSource(new CharSequenceReader(
		        new java.io.StringReader(
		                "(set-option :produce-models true)(set-logic QF_LIA)")), null);
		//		ISource source = smt.smtConfig.smtFactory.createSource(new CharSequenceReader(
		//		                                                              		        new java.io.StringReader("(set-logic QF_ABV)")), null);
		IParser parser = smt.smtConfig.smtFactory.createParser(smt.smtConfig, source);
		try {
			modelCommand = parser.parseCommand();
			logicCommand = parser.parseCommand();
		} catch (IOException e) {
		} catch (ParserException e) {
		}
		ISort.IFactory sortfactory = smt.smtConfig.sortFactory;
		boolSort = sortfactory.createSortExpression(smt.smtConfig.exprFactory.symbol("Bool"));
		intSort = sortfactory.createSortExpression(smt.smtConfig.exprFactory.symbol("Int"));
		List<IExpr.INumeral> nums = new LinkedList<IExpr.INumeral>();
		nums.add(smt.smtConfig.exprFactory.numeral(32)); // TODO - room for improvement in ease of use here...
		bv32Sort = sortfactory.createSortExpression(smt.smtConfig.exprFactory.id(smt.smtConfig.exprFactory.symbol("BitVec"),
		                                                                         nums));
	}

	public void setup() {
		IResponse response = solver.start();
		if (response.isError()) {
			logger.warn("Error starting SMT solver: " + response.toString());
		} else {
			logger.info("Solver started: " + response);
		}

	}

	public void pullDown() {
		solver.exit();
	}

	public void solve(Set<Constraint> constraints) {
		ICommand.IScript script = new org.smtlib.impl.Script();
		SMTConverter converter = new SMTConverter();
		solver.set_option(smt.smtConfig.exprFactory.keyword(":produce-models"),
		                  smt.smtConfig.exprFactory.symbol("true"));

		solver.push(1);
		Set<IExpr> expressions = new HashSet<IExpr>();
		for (Constraint<?> constraint : constraints) {
			expressions.add(converter.visit(constraint));
		}
		//		script.commands().add(new org.smtlib.command.C_set_option(smt.smtConfig.exprFactory.keyword("produce-models"),
		//		                                                          smt.smtConfig.exprFactory."true"));
		script.commands().add(modelCommand);
		script.commands().add(logicCommand);
		for (IExpr.ISymbol symbol : converter.getSymbols()) {
			script.commands().add(new org.smtlib.command.C_declare_fun(symbol,
			                              new ArrayList<ISort>(), intSort));
		}

		for (IExpr expr : expressions) {
			ICommand command = new org.smtlib.command.C_assert(expr);
			script.commands().add(command);
		}
		List<IExpr> vals = new ArrayList<IExpr>();
		for (IExpr.ISymbol symbol : converter.getSymbols()) {
			vals.add(symbol);
		}
		script.commands().add(new org.smtlib.command.C_get_value(vals));
		script.commands().add(new org.smtlib.command.C_exit());
		IPrinter printer = smt.smtConfig.defaultPrinter;
		logger.info(printer.toString(script));
		IResponse response = script.execute(solver);

		logger.info(printer.toString(response));
		solver.pop(1);

	}
}
