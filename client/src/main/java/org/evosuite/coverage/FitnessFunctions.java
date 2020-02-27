/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.ambiguity.AmbiguityCoverageFactory;
import org.evosuite.coverage.ambiguity.AmbiguityCoverageSuiteFitness;
import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.branch.OnlyBranchCoverageFactory;
import org.evosuite.coverage.branch.OnlyBranchCoverageSuiteFitness;
import org.evosuite.coverage.branch.OnlyBranchCoverageTestFitness;
import org.evosuite.coverage.cbranch.CBranchFitnessFactory;
import org.evosuite.coverage.cbranch.CBranchSuiteFitness;
import org.evosuite.coverage.cbranch.CBranchTestFitness;
import org.evosuite.coverage.dataflow.AllDefsCoverageFactory;
import org.evosuite.coverage.dataflow.AllDefsCoverageSuiteFitness;
import org.evosuite.coverage.dataflow.AllDefsCoverageTestFitness;
import org.evosuite.coverage.dataflow.DefUseCoverageFactory;
import org.evosuite.coverage.dataflow.DefUseCoverageSuiteFitness;
import org.evosuite.coverage.dataflow.DefUseCoverageTestFitness;
import org.evosuite.coverage.exception.ExceptionCoverageFactory;
import org.evosuite.coverage.exception.ExceptionCoverageSuiteFitness;
import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.exception.TryCatchCoverageFactory;
import org.evosuite.coverage.exception.TryCatchCoverageSuiteFitness;
import org.evosuite.coverage.exception.TryCatchCoverageTestFitness;
import org.evosuite.coverage.ibranch.IBranchFitnessFactory;
import org.evosuite.coverage.ibranch.IBranchSuiteFitness;
import org.evosuite.coverage.ibranch.IBranchTestFitness;
import org.evosuite.coverage.io.input.InputCoverageFactory;
import org.evosuite.coverage.io.input.InputCoverageSuiteFitness;
import org.evosuite.coverage.io.input.InputCoverageTestFitness;
import org.evosuite.coverage.io.output.OutputCoverageFactory;
import org.evosuite.coverage.io.output.OutputCoverageSuiteFitness;
import org.evosuite.coverage.io.output.OutputCoverageTestFitness;
import org.evosuite.coverage.length.LengthCoverageFactory;
import org.evosuite.coverage.length.LengthSuiteFitness;
import org.evosuite.coverage.line.LineCoverageFactory;
import org.evosuite.coverage.line.LineCoverageSuiteFitness;
import org.evosuite.coverage.line.LineCoverageTestFitness;
import org.evosuite.coverage.line.OnlyLineCoverageSuiteFitness;
import org.evosuite.coverage.method.MethodCoverageFactory;
import org.evosuite.coverage.method.MethodCoverageSuiteFitness;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.coverage.method.MethodNoExceptionCoverageFactory;
import org.evosuite.coverage.method.MethodNoExceptionCoverageSuiteFitness;
import org.evosuite.coverage.method.MethodNoExceptionCoverageTestFitness;
import org.evosuite.coverage.method.MethodTraceCoverageFactory;
import org.evosuite.coverage.method.MethodTraceCoverageSuiteFitness;
import org.evosuite.coverage.method.MethodTraceCoverageTestFitness;
import org.evosuite.coverage.mutation.MutationFactory;
import org.evosuite.coverage.mutation.MutationTestFitness;
import org.evosuite.coverage.mutation.OnlyMutationFactory;
import org.evosuite.coverage.mutation.OnlyMutationSuiteFitness;
import org.evosuite.coverage.mutation.OnlyMutationTestFitness;
import org.evosuite.coverage.mutation.StrongMutationSuiteFitness;
import org.evosuite.coverage.mutation.StrongMutationTestFitness;
import org.evosuite.coverage.mutation.WeakMutationSuiteFitness;
import org.evosuite.coverage.mutation.WeakMutationTestFitness;
import org.evosuite.coverage.readability.ReadabilitySuiteFitness;
import org.evosuite.coverage.rho.RhoCoverageFactory;
import org.evosuite.coverage.rho.RhoCoverageSuiteFitness;
import org.evosuite.coverage.statement.StatementCoverageFactory;
import org.evosuite.coverage.statement.StatementCoverageSuiteFitness;
import org.evosuite.coverage.statement.StatementCoverageTestFitness;
import org.evosuite.coverage.time.ExecutionTimeCoverageFactory;
import org.evosuite.coverage.time.ExecutionTimeSuiteFitness;
import org.evosuite.regression.RegressionSuiteFitness;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * factory class for fitness functions
 * @author mattia
 *
 */
public class FitnessFunctions {

	private static Logger logger = LoggerFactory.getLogger(FitnessFunctions.class);

	/**
	 * <p>
	 * getFitnessFunction
	 * </p>
	 *
	 * @param criterion
	 *            a {@link org.evosuite.Properties.Criterion} object.
	 * @return a {@link org.evosuite.testsuite.TestSuiteFitnessFunction} object.
	 */
	public static TestSuiteFitnessFunction getFitnessFunction(Criterion criterion) {
		switch (criterion) {
		case STRONGMUTATION:
			return new StrongMutationSuiteFitness();
		case WEAKMUTATION:
			return new WeakMutationSuiteFitness();
		case MUTATION:
			return new StrongMutationSuiteFitness();
		case ONLYMUTATION:
			return new OnlyMutationSuiteFitness();
		case DEFUSE:
			return new DefUseCoverageSuiteFitness();
		case BRANCH:
			return new BranchCoverageSuiteFitness();
		case CBRANCH:
			return new CBranchSuiteFitness();
		case IBRANCH:
			return new IBranchSuiteFitness();
		case STATEMENT:
			return new StatementCoverageSuiteFitness();
		case RHO:
			return new RhoCoverageSuiteFitness();
		case AMBIGUITY:
			return new AmbiguityCoverageSuiteFitness();
		case ALLDEFS:
			return new AllDefsCoverageSuiteFitness();
		case EXCEPTION:
			return new ExceptionCoverageSuiteFitness();
		case REGRESSION:
			return new RegressionSuiteFitness();
		case READABILITY:
			return new ReadabilitySuiteFitness();
		case ONLYBRANCH:
			return new OnlyBranchCoverageSuiteFitness();
		case METHODTRACE:
			return new MethodTraceCoverageSuiteFitness();
		case METHOD:
			return new MethodCoverageSuiteFitness();
		case METHODNOEXCEPTION:
			return new MethodNoExceptionCoverageSuiteFitness();
		case ONLYLINE:
			return new OnlyLineCoverageSuiteFitness();
		case LINE:
			return new LineCoverageSuiteFitness();
		case OUTPUT:
			return new OutputCoverageSuiteFitness();
		case INPUT:
			return new InputCoverageSuiteFitness();
		case TRYCATCH:
			return new TryCatchCoverageSuiteFitness();
		case BZU_TIME://Hadi
			return new ExecutionTimeSuiteFitness();
		case BZU_LENGTH:
			return new LengthSuiteFitness();
		default:
			logger.warn("No TestSuiteFitnessFunction defined for {}; using default one (BranchCoverageSuiteFitness)", Arrays.toString(Properties.CRITERION));
			return new BranchCoverageSuiteFitness();
		}
	}

	/**
	 * <p>
	 * getFitnessFactory
	 * </p>
	 *
	 * @param crit
	 *            a {@link org.evosuite.Properties.Criterion} object.
	 * @return a {@link org.evosuite.coverage.TestFitnessFactory} object.
	 */
	public static TestFitnessFactory<? extends TestFitnessFunction> getFitnessFactory(
	        Criterion crit) {
		switch (crit) {
		case STRONGMUTATION:
		case MUTATION:
			return new MutationFactory();
		case WEAKMUTATION:
			return new MutationFactory(false);
		case ONLYMUTATION:
			return new OnlyMutationFactory();
		case DEFUSE:
			return new DefUseCoverageFactory();
		case BRANCH:
			return new BranchCoverageFactory();
		case CBRANCH:
			return new CBranchFitnessFactory();
		case IBRANCH:
			return new IBranchFitnessFactory();
		case STATEMENT:
			return new StatementCoverageFactory();
		case RHO:
			return new RhoCoverageFactory();
		case AMBIGUITY:
			return new AmbiguityCoverageFactory();
		case ALLDEFS:
			return new AllDefsCoverageFactory();
		case EXCEPTION:
			return new ExceptionCoverageFactory();
		case ONLYBRANCH:
			return new OnlyBranchCoverageFactory();
		case METHODTRACE:
			return new MethodTraceCoverageFactory();
		case METHOD:
			return new MethodCoverageFactory();
		case METHODNOEXCEPTION:
			return new MethodNoExceptionCoverageFactory();
		case LINE:
			return new LineCoverageFactory();
		case ONLYLINE:
			return new LineCoverageFactory();
		case OUTPUT:
			return new OutputCoverageFactory();
		case INPUT:
			return new InputCoverageFactory();
		case TRYCATCH:
			return new TryCatchCoverageFactory();
		case BZU_TIME:
			return new ExecutionTimeCoverageFactory();
		case  BZU_LENGTH:
			return  new LengthCoverageFactory();
		default:
			logger.warn("No TestFitnessFactory defined for " + crit
			        + " using default one (BranchCoverageFactory)");
			return new BranchCoverageFactory();
		}
	}

	/**
	 * Converts a {@link org.evosuite.Properties.Criterion} object to a
	 * {@link org.evosuite.testcase.TestFitnessFunction} class.
	 *
	 * @param criterion a {@link org.evosuite.Properties.Criterion} object.
	 * @return a {@link java.lang.Class} object.
	 */
	public static Class<?> getTestFitnessFunctionClass(Criterion criterion) {
		switch (criterion) {
		case STRONGMUTATION:
				return StrongMutationTestFitness.class;
		case WEAKMUTATION:
				return WeakMutationTestFitness.class;
		case MUTATION:
				return MutationTestFitness.class;
		case ONLYMUTATION:
				return OnlyMutationTestFitness.class;
		case DEFUSE:
				return DefUseCoverageTestFitness.class;
		case BRANCH:
				return BranchCoverageTestFitness.class;
		case CBRANCH:
				return CBranchTestFitness.class;
		case IBRANCH:
				return IBranchTestFitness.class;
		case STATEMENT:
				return StatementCoverageTestFitness.class;
		case RHO:
				return LineCoverageTestFitness.class;
		case AMBIGUITY:
				return LineCoverageTestFitness.class;
		case ALLDEFS:
				return AllDefsCoverageTestFitness.class;
		case EXCEPTION:
				return ExceptionCoverageTestFitness.class;
		case REGRESSION:
				throw new RuntimeException("No test fitness function defined for " + criterion.name());
		case READABILITY:
				throw new RuntimeException("No test fitness function defined for " + criterion.name());
		case ONLYBRANCH:
				return OnlyBranchCoverageTestFitness.class;
		case METHODTRACE:
				return MethodTraceCoverageTestFitness.class;
		case METHOD:
				return MethodCoverageTestFitness.class;
		case METHODNOEXCEPTION:
				return MethodNoExceptionCoverageTestFitness.class;
		case ONLYLINE:
				return LineCoverageTestFitness.class;
		case LINE:
				return LineCoverageTestFitness.class;
		case OUTPUT:
				return OutputCoverageTestFitness.class;
		case INPUT:
				return InputCoverageTestFitness.class;
		case TRYCATCH:
				return TryCatchCoverageTestFitness.class;
		case BZU_TIME: // Hadi
			return ExecutionTimeSuiteFitness.class;
		case BZU_LENGTH:
			return LengthSuiteFitness.class;
		default:
				throw new RuntimeException("No criterion defined for " + criterion.name());
		}
	}

}
