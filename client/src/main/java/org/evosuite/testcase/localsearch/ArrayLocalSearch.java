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

package org.evosuite.testcase.localsearch;

import org.evosuite.Properties;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.TestCaseExpander;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.ArrayReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * ArrayLocalSearch class.
 * </p>
 *
 * @author Gordon Fraser
 */
public class ArrayLocalSearch extends StatementLocalSearch {

    private int oldLength = 0;

    private static final Logger logger = LoggerFactory.getLogger(TestCaseLocalSearch.class);

    private int positionDelta = 0;

    @Override
    public int getPositionDelta() {
        return positionDelta;
    }


    /* (non-Javadoc)
     * @see org.evosuite.testcase.LocalSearch#doSearch(org.evosuite.testcase.TestChromosome, int, org.evosuite.ga.LocalSearchObjective)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doSearch(TestChromosome test, int statement,
                            LocalSearchObjective<TestChromosome> objective) {

        boolean hasImproved = false;
        ArrayStatement p = (ArrayStatement) test.getTestCase().getStatement(statement);
        logger.debug("Doing array local search on statement " + statement + ": "
                + test.getTestCase().toCode());

        int difference = stripAssignments(p, test, objective);
        logger.debug("Adjusting position from " + statement + " to "
                + (statement - difference) + ": " + test.getTestCase().toCode());
        positionDelta = difference;
        statement = statement - difference;
        p = (ArrayStatement) test.getTestCase().getStatement(statement);

        hasImproved = searchLength(test, statement, objective);
        TestCaseExpander expander = new TestCaseExpander();
        int lengthWithoutAssignments = test.size();
        p = (ArrayStatement) test.getTestCase().getStatement(statement);
        expander.visitArrayStatement(test.getTestCase(), p);
        int assignmentLength = test.size() - lengthWithoutAssignments;
        for (int position = statement + 1; position < statement + assignmentLength; position++) {
            logger.debug("Local search on statement " + position);
            StatementLocalSearch search = StatementLocalSearch.getLocalSearchFor(test.getTestCase().getStatement(position));
            if (search != null) {
                if (search.doSearch(test, position, objective)) {
                    hasImproved = true;
                }
            }
        }

        logger.debug("Finished local search with result {}", p.getCode());
        return hasImproved;
    }

    private int stripAssignments(ArrayStatement statement, TestChromosome test,
                                 LocalSearchObjective<TestChromosome> objective) {
        int difference = 0;
        ArrayReference arrRef = (ArrayReference) statement.getReturnValue();
        TestFactory factory = TestFactory.getInstance();
        for (int position = test.size() - 1; position > statement.getPosition(); position--) {
            logger.debug("Current delete position: " + position);
            if (test.getTestCase().getStatement(position) instanceof AssignmentStatement) {
                logger.debug("Is assignment statement");
                AssignmentStatement assignment = (AssignmentStatement) test.getTestCase().getStatement(position);
                Statement valueStatement = test.getTestCase().getStatement(assignment.getValue().getStPosition());
                if (assignment.getReturnValue().getAdditionalVariableReference() == arrRef) {

                    int currentDelta = 0;
                    int differenceDelta = 0;

                    logger.debug("Assigns to target array. Checking if we can remove it without worsening fitness");
                    backup(test);
                    factory.deleteStatement(test.getTestCase(), position);

                    if (valueStatement instanceof PrimitiveStatement
                            || valueStatement instanceof NullStatement) {
                        if (!test.getTestCase().hasReferences(valueStatement.getReturnValue())) {
                            if (valueStatement.getPosition() < statement.getPosition())
                                differenceDelta = 1;
                            currentDelta = 1;
                            logger.debug("Deleting primitive statement assigned to this array at "
                                    + valueStatement.getPosition());
                            factory.deleteStatement(test.getTestCase(),
                                    valueStatement.getPosition());
                        }
                    }
                    if (!objective.hasNotWorsened(test)) {
                        logger.debug("Fitness has decreased, so restoring test");
                        restore(test);
                        currentDelta = 0;
                        differenceDelta = 0;
                    }
                    position -= currentDelta;
                    difference += differenceDelta;
                }
            }
        }

        return difference;
    }

    private boolean searchLength(TestChromosome test, int statement,
                                 LocalSearchObjective<TestChromosome> objective) {

        boolean hasImproved = false;

        ArrayStatement p = (ArrayStatement) test.getTestCase().getStatement(statement);
        logger.debug("Performing local search on array length, starting with length {}",
                p.size());
        ExecutionResult oldResult = test.getLastExecutionResult();
        oldLength = p.size();
        boolean done = false;
        while (!done) {
            done = true;
            // Try +1
            p.setSize(oldLength + 1);
            logger.debug("Trying increment of {}", p.getCode());
            if (objective.hasImproved(test)) {
                done = false;
                hasImproved = true;

                boolean improved = true;
                while (improved) {
                    oldLength = p.size();
                    oldResult = test.getLastExecutionResult();
                    p.setSize(oldLength + 1);
                    logger.debug("Trying increment of {}", p.getCode());
                    improved = objective.hasImproved(test);
                }
                p.setSize(oldLength);
                test.setLastExecutionResult(oldResult);
                test.setChanged(false);

            } else {
                if (oldLength > 0) {
                    // Restore original, try -1
                    p.setSize(oldLength);
                    test.setLastExecutionResult(oldResult);
                    test.setChanged(false);

                    p.setSize(oldLength - 1);
                } else {
                    p.setSize(Properties.MAX_ARRAY);
                }
                logger.debug("Trying decrement of {}", p.getCode());
                if (objective.hasImproved(test)) {
                    done = false;
                    hasImproved = true;

                    boolean improved = true;
                    while (improved && p.size() > 0) {
                        oldLength = p.size();
                        oldResult = test.getLastExecutionResult();
                        p.setSize(oldLength - 1);
                        logger.debug("Trying decrement of {}", p.getCode());
                        improved = objective.hasImproved(test);
                    }
                    p.setSize(oldLength);
                    test.setLastExecutionResult(oldResult);
                    test.setChanged(false);
                } else {
                    p.setSize(oldLength);
                    test.setLastExecutionResult(oldResult);
                    test.setChanged(false);
                }
            }
        }

        logger.debug("Finished local array length search with result {}", p.getCode());
        return hasImproved;
    }

}
