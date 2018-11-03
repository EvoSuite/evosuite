package org.evosuite.assertion;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.variable.VariableReference;

import java.util.Collection;
import java.util.Set;

public class ContainsAssertion extends Assertion {

    private static final long serialVersionUID = -86374077651820640L;

    /** Variable on which the assertion is made */
    protected VariableReference containedVariable;

    public VariableReference getContainedVariable() {
        return containedVariable;
    }


    /** {@inheritDoc} */
    @Override
    public Assertion copy(TestCase newTestCase, int offset) {
        ContainsAssertion s = new ContainsAssertion();
        s.source = source.copy(newTestCase, offset);
        s.value = value;
        s.containedVariable = containedVariable.copy(newTestCase, offset);
        s.comment = comment;
        return s;
    }

    /** {@inheritDoc} */
    @Override
    public String getCode() {
        return "assertTrue(" + source.getName() + ".contains(" + containedVariable.getName() + "));";
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.Assertion#evaluate(org.evosuite.testcase.Scope)
     */
    @Override
    public boolean evaluate(Scope scope) {
        try {
            if (source.getObject(scope) == null)
                return value == null;
            else {
                Object container = source.getObject(scope);
                Object object    = containedVariable.getObject(scope);
                if(container instanceof Collection) {
                    return ((Collection)container).contains(object);
                } else {
                    return false; // Is this possible?
                }
            }
        } catch (CodeUnderTestException e) {
            throw new UnsupportedOperationException();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.evosuite.assertion.Assertion#getReferencedVariables()
     */
    /** {@inheritDoc} */
    @Override
    public Set<VariableReference> getReferencedVariables() {
        Set<VariableReference> vars = super.getReferencedVariables();
        vars.add(source);
        vars.add(containedVariable);
        return vars;
    }
}
