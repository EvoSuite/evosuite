/**
 * Copyright (C) 2010-2020 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.DSE.algorithm.strategies.implementations.PathSelectionStrategies;

import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.DSE.algorithm.DSEPathCondition;
import org.evosuite.symbolic.DSE.algorithm.strategies.PathSelectionStrategy;
import org.evosuite.symbolic.PathCondition;

import java.util.ArrayList;
import java.util.List;

/**
 * SAGE based strategy, creates the children by iteratively negating all the branches of the current path condition.
 *
 * @author ignacio lebrero
 */
public class generationalGenerationStrategy implements PathSelectionStrategy {

    @Override
    public List<DSEPathCondition> generateChildren(DSEPathCondition currentPathConditionChild) {
        List<DSEPathCondition> generatedChildren = new ArrayList<>();
        List<BranchCondition> accumulatedBranchConditions = new ArrayList<>();
        List<BranchCondition> currentPathConditionBranchConditions = currentPathConditionChild.getPathCondition().getBranchConditions();
        int currentPathConditionIndexGeneratedFrom = currentPathConditionChild.getGeneratedFromIndex();

        // adds the untouched prefix
        for (int indexBound = 0; indexBound < currentPathConditionIndexGeneratedFrom; ++indexBound) {
            accumulatedBranchConditions.add(currentPathConditionBranchConditions.get(indexBound));
        }

        // Important!! We start from the index the test was generated from to avoid re-create already checked paths
        for (int indexBound = currentPathConditionIndexGeneratedFrom; indexBound < currentPathConditionBranchConditions.size(); indexBound++) {
            BranchCondition currentBranchCondition = currentPathConditionBranchConditions.get(indexBound);

            // Adds the negated BranchCondition version to the current created pathCondition
            accumulatedBranchConditions.add(currentBranchCondition.getNegatedVersion());

            DSEPathCondition newChild = new DSEPathCondition(
                new PathCondition(
                        new ArrayList(accumulatedBranchConditions)
                ),
                indexBound + 1
            );

            // adds the negated last condition
            generatedChildren.add(newChild);

            // replaces the negated branch condition with the original one for continuing generating
            accumulatedBranchConditions.set(
                accumulatedBranchConditions.size() - 1,
                currentBranchCondition
            );
        }

        return generatedChildren;
    }
}
