package org.evosuite.testcase.mutation.deletion;

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.TestCase;

public interface DeletionStrategy {
    /**
     * Delete the statement at position from the test case and remove all
     * references to it
     *
     * @param test
     * @param position
     * @return false if it was not possible to delete the statement
     * @throws ConstructionFailedException
     */
    public boolean deleteStatement(TestCase test, int position) throws ConstructionFailedException;

    /**
     *
     * @param test
     * @param position
     * @return true if statements was deleted or any dependency was modified
     * @throws ConstructionFailedException
     */
    public boolean deleteStatementGracefully(TestCase test, int position)
            throws ConstructionFailedException;
}
