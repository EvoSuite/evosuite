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
package org.evosuite.symbolic.dse.algorithm.strategies.implementations.PathExtensionStrategies;

import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.PathCondition;
import org.evosuite.symbolic.dse.algorithm.GenerationalSearchPathCondition;
import org.evosuite.symbolic.dse.algorithm.strategies.PathExtensionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classic DFS exploration. New path conditions are created incrementally from the smallest to the largest.
 * <p>
 * TODO: This is very similar to {@link org.evosuite.symbolic.dse.algorithm.strategies.implementations.PathExtensionStrategies.ExpandExecutionStrategy}
 * It can be an extensions later on.
 *
 * @author Ignacio Lebrero
 */
public class DFSStrategy implements PathExtensionStrategy {

    public static final String DEBUG_MSG_NEGATING_INDEX_OF_PATH_CONDITION = "negating index {} of path condition";

    Logger logger = LoggerFactory.getLogger(DFSStrategy.class);

    @Override
    public List<GenerationalSearchPathCondition> generateChildren(GenerationalSearchPathCondition currentPathConditionChild) {
        List<GenerationalSearchPathCondition> result = new ArrayList();
        List<BranchCondition> accumulatedBranchConditions = new ArrayList();
        List<BranchCondition> currentPathConditionBranchConditions = currentPathConditionChild.getPathCondition().getBranchConditions();

        // Create the PCs from the longest to the shortest
        for (int i = 0; i < currentPathConditionBranchConditions.size(); i++) {
            logger.debug(DEBUG_MSG_NEGATING_INDEX_OF_PATH_CONDITION, i);

            BranchCondition currentBranchCondition = currentPathConditionBranchConditions.get(i);

            // Add negated version of current branch
            accumulatedBranchConditions.add(currentBranchCondition.getNegatedVersion());

            GenerationalSearchPathCondition newChild = new GenerationalSearchPathCondition(
                    new PathCondition(
                            new ArrayList(accumulatedBranchConditions)
                    ),
                    0 // not relevant for DFS
            );

            // Append the new PC
            result.add(newChild);

            // Replace the negated branch condition with the original one for continuing generating
            accumulatedBranchConditions.set(
                    accumulatedBranchConditions.size() - 1,
                    currentBranchCondition
            );
        }

        // Revert it from largest to smallest
        Collections.reverse(result);

        return result;
    }
}