/*
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
package org.evosuite.coverage.branch;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.MethodNameMatcher;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.ControlDependency;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * BranchCoverageFactory class.
 * </p>
 *
 * @author Gordon Fraser, Andre Mis
 */
public class BranchCoverageFactory extends
        AbstractFitnessFactory<BranchCoverageTestFitness> {

    private static final Logger logger = LoggerFactory.getLogger(BranchCoverageFactory.class);


    /**
     * return coverage goals of the target class or of all the contextual branches, depending on the limitToCUT parameter
     *
     * @param limitToCUT whether to consider the class under test only ({@code true}) or all known
     *                   classes ({@code false})
     * @return
     */
    private List<BranchCoverageTestFitness> computeCoverageGoals(boolean limitToCUT) {
        long start = System.currentTimeMillis();
        List<BranchCoverageTestFitness> goals = new ArrayList<>();

        // logger.info("Getting branches");
        for (String className : BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownClasses()) {
            //when limitToCUT== true, if not the class under test of a inner/anonymous class, continue
            if (limitToCUT && !isCUT(className)) continue;
            //when limitToCUT==false, consider all classes, but excludes libraries ones according the INSTRUMENT_LIBRARIES property
            if (!limitToCUT && (!Properties.INSTRUMENT_LIBRARIES && !DependencyAnalysis.isTargetProject(className)))
                continue;
            final MethodNameMatcher matcher = new MethodNameMatcher();
            // Branchless methods
            for (String method : BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchlessMethods(className)) {
                if (matcher.fullyQualifiedMethodMatches(method)) {
                    goals.add(createRootBranchTestFitness(className, method));
                }
            }

            // Branches
            for (String methodName : BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownMethods(className)) {
                if (!matcher.methodMatches(methodName)) {
                    logger.info("Method " + methodName + " does not match criteria. ");
                    continue;
                }

                for (Branch b : BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).retrieveBranchesInMethod(className,
                        methodName)) {
                    if (!b.isInstrumented()) {
                        goals.add(createBranchCoverageTestFitness(b, true));
                        goals.add(createBranchCoverageTestFitness(b, false));
                    }
                }
            }
        }
        goalComputationTime = System.currentTimeMillis() - start;
        return goals;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.evosuite.coverage.TestCoverageFactory#getCoverageGoals()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BranchCoverageTestFitness> getCoverageGoals() {
        return computeCoverageGoals(true);
    }

    public List<BranchCoverageTestFitness> getCoverageGoalsForAllKnownClasses() {
        return computeCoverageGoals(false);
    }


    /**
     * Create a fitness function for branch coverage aimed at executing the
     * given ControlDependency.
     *
     * @param cd a {@link org.evosuite.graphs.cfg.ControlDependency} object.
     * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
     * object.
     */
    public static BranchCoverageTestFitness createBranchCoverageTestFitness(
            ControlDependency cd) {
        return createBranchCoverageTestFitness(cd.getBranch(),
                cd.getBranchExpressionValue());
    }

    /**
     * Create a fitness function for branch coverage aimed at executing the
     * Branch identified by b as defined by branchExpressionValue.
     *
     * @param b                     a {@link org.evosuite.coverage.branch.Branch} object.
     * @param branchExpressionValue a boolean.
     * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
     * object.
     */
    public static BranchCoverageTestFitness createBranchCoverageTestFitness(
            Branch b, boolean branchExpressionValue) {

        return new BranchCoverageTestFitness(new BranchCoverageGoal(b,
                branchExpressionValue, b.getClassName(), b.getMethodName()));
    }

    /**
     * Create a fitness function for branch coverage aimed at covering the root
     * branch of the given method in the given class. Covering a root branch
     * means entering the method.
     *
     * @param className a {@link java.lang.String} object.
     * @param method    a {@link java.lang.String} object.
     * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
     * object.
     */
    public static BranchCoverageTestFitness createRootBranchTestFitness(
            String className, String method) {

        return new BranchCoverageTestFitness(new BranchCoverageGoal(className,
                method.substring(method.lastIndexOf(".") + 1)));
    }

    /**
     * Convenience method calling createRootBranchTestFitness(class,method) with
     * the respective class and method of the given BytecodeInstruction.
     *
     * @param instruction a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
     * object.
     */
    public static BranchCoverageTestFitness createRootBranchTestFitness(
            BytecodeInstruction instruction) {
        if (instruction == null)
            throw new IllegalArgumentException("null given");

        return createRootBranchTestFitness(instruction.getClassName(),
                instruction.getMethodName());
    }
}

////----------
//List<String> l = new ArrayList<>();
//for (BranchCoverageTestFitness callGraphEntry : goals) {
//	l.add(callGraphEntry.toString());
//}
//File f = new File("/Users/mattia/workspaces/evosuiteSheffield/evosuite/master/evosuite-report/branchgoals.txt");
//f.delete();
//try {
//	Files.write(f.toPath(), l, Charset.defaultCharset(), StandardOpenOption.CREATE);
//} catch (IOException e) { 
//	e.printStackTrace();
//}
////---------- 