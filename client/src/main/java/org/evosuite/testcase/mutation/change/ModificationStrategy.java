package org.evosuite.testcase.mutation.change;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.Statement;

public interface ModificationStrategy {
    public boolean changeRandomCall(TestCase test, Statement statement);
}
