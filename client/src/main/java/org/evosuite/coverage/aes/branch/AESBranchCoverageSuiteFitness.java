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
package org.evosuite.coverage.aes.branch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.evosuite.coverage.aes.AbstractAESCoverageSuiteFitness;
import org.evosuite.coverage.aes.Spectrum;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

public class AESBranchCoverageSuiteFitness extends AbstractAESCoverageSuiteFitness {

    private static final long serialVersionUID = 7409239464436681146L;

    /* A branch can be of 3 types:
     * 1) true branch (stored in the "trueMap"),
     * 2) false branch (stored in the "falseMap") and
     * 3) a method not containing a branch (stored in the "branchlessMethodMap") */

    private Map<Integer, Integer> trueMap;

    private Map<Integer, Integer> falseMap;

    private Map<String, Integer> branchlessMethodsMap;

    private int numberOfGoals = 0;

    public AESBranchCoverageSuiteFitness(Metric metric) {
        super(metric);
    }

    public AESBranchCoverageSuiteFitness() {
        this(Metric.AES);
    }

    private void determineCoverageGoals() {

        if (this.branchlessMethodsMap == null || this.trueMap == null || this.falseMap == null) {
            this.branchlessMethodsMap = new HashMap<String, Integer>();
            this.trueMap = new HashMap<Integer, Integer>();
            this.falseMap = new HashMap<Integer, Integer>();

            /* list "goals" contains all the branches that has been covered by the current generation of test suites */
            List<TestFitnessFunction> goals = new AESBranchCoverageFactory().getCoverageGoals();
            this.numberOfGoals = goals.size() - 1;

            /* This variable "g" is the component number */
            for (int g = 0; g < this.numberOfGoals; g++) {
                TestFitnessFunction ff = goals.get(g);

                if (ff instanceof BranchCoverageTestFitness) {
                    BranchCoverageTestFitness goal = (BranchCoverageTestFitness) ff;

                    /* For each of the goal object, which is essentially a branch covered, we must map it to the prior.
                    "branchToSuspiciousnessMap" takes care of that.*/
                    //branchToSuspiciousnessMap(goal, g);
                    if (goal.getBranch() == null) { // branchless method{
                        branchlessMethodsMap.put(goal.getClassName() + "." + goal.getMethod(), g);
                    } else if (goal.getBranchExpressionValue()) { // true branch
                        trueMap.put(goal.getBranch().getActualBranchId(), g);
                    } else { // false branch
                        falseMap.put(goal.getBranch().getActualBranchId(), g);
                    }
                }
            }
        }
    }

    @Override
    protected Spectrum getSpectrum(List<ExecutionResult> results) {
        determineCoverageGoals();
        Spectrum spectrum = new Spectrum(results.size(), this.numberOfGoals);

        for (int t = 0; t < results.size(); t++) {
            ExecutionResult result = results.get(t);

            for (String method : result.getTrace().getCoveredMethods()) {
                if (branchlessMethodsMap.containsKey(method)) {
                    spectrum.setInvolved(t, branchlessMethodsMap.get(method));
                }
            }

            for (int trueBranchId : result.getTrace().getCoveredTrueBranches()) {
                if (trueMap.containsKey(trueBranchId)) {
                    spectrum.setInvolved(t, trueMap.get(trueBranchId));
                }
            }

            for (int falseBranchId : result.getTrace().getCoveredFalseBranches()) {
                if (falseMap.containsKey(falseBranchId)) {
                    spectrum.setInvolved(t, falseMap.get(falseBranchId));
                }
            }

        }

        return spectrum;
    }

}

