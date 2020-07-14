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
package org.evosuite.symbolic.dse.algorithm.strategies.implementations.PathSelectionStrategies;

import org.evosuite.symbolic.PathConditionNode;
import org.evosuite.symbolic.dse.algorithm.DSEPathCondition;
import org.evosuite.symbolic.dse.algorithm.strategies.PathSelectionStrategy;
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
        List<PathConditionNode> accumulatedPathConditionNodes = new ArrayList<>();
        List<PathConditionNode> currentPathConditionPathConditionNodes = currentPathConditionChild.getPathCondition().getPathConditionNodes();
        int currentPathConditionIndexGeneratedFrom = currentPathConditionChild.getGeneratedFromIndex();

        // adds the untouched prefix
        for (int indexBound = 0; indexBound < currentPathConditionIndexGeneratedFrom; ++indexBound) {
            accumulatedPathConditionNodes.add(currentPathConditionPathConditionNodes.get(indexBound));
        }

        // Important!! We start from the index the test was generated from to avoid re-create already checked paths
        for (int indexBound = currentPathConditionIndexGeneratedFrom; indexBound < currentPathConditionPathConditionNodes.size(); indexBound++) {
            PathConditionNode currentPathConditionNode = currentPathConditionPathConditionNodes.get(indexBound);

            // Adds the negated BranchCondition version to the current created pathCondition
            accumulatedPathConditionNodes.add(currentPathConditionNode.getNegatedVersion());

            DSEPathCondition newChild = new DSEPathCondition(
                new PathCondition(
                        new ArrayList(accumulatedPathConditionNodes)
                ),
                indexBound + 1
            );

            // adds the negated last condition
            generatedChildren.add(newChild);

            // replaces the negated branch condition with the original one for continuing generating
            accumulatedPathConditionNodes.set(
                accumulatedPathConditionNodes.size() - 1,
              currentPathConditionNode
            );
        }

        return generatedChildren;
    }
}
