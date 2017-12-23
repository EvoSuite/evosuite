package org.evosuite.assertion;

import org.evosuite.testcase.variable.VariableReference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ContainsTraceEntry implements OutputTraceEntry {

    protected VariableReference containerVar;

    protected Map<VariableReference, Boolean> containsMap = new HashMap<VariableReference, Boolean>();

    private final Map<Integer, VariableReference> containsMapIntVar = new HashMap<Integer, VariableReference>();

    public ContainsTraceEntry(VariableReference containerVar) {
        this.containerVar = containerVar;
    }

    public void addEntry(VariableReference other, boolean value) {
        containsMap.put(other, value);
        containsMapIntVar.put(other.getStPosition(), other);
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#differs(org.evosuite.assertion.OutputTraceEntry)
     */
    @Override
    public boolean differs(OutputTraceEntry other) {
        if (other instanceof ContainsTraceEntry) {
            ContainsTraceEntry otherEntry = (ContainsTraceEntry) other;
            if (!containerVar.equals(otherEntry.containerVar))
                return false;

            for (VariableReference otherVar : containsMap.keySet()) {
                if (!otherEntry.containsMap.containsKey(otherVar)) {
                    continue;
                }

                if (!otherEntry.containsMap.get(otherVar).equals(containsMap.get(otherVar))) {
                    return true;
                }
            }

        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#getAssertions(org.evosuite.assertion.OutputTraceEntry)
     */
    @Override
    public Set<Assertion> getAssertions(OutputTraceEntry other) {
        Set<Assertion> assertions = new HashSet<Assertion>();
        if (other instanceof ContainsTraceEntry) {
            ContainsTraceEntry otherEntry = (ContainsTraceEntry) other;
            for (Integer otherVar : containsMapIntVar.keySet()) {
                if (!otherEntry.containsMapIntVar.containsKey(otherVar)) {
                    continue;
                }

                if (otherVar == null) {
                    continue;
                }
                if (!otherEntry.containsMap.get(otherEntry.containsMapIntVar.get(otherVar)).equals(
                        containsMap.get(containsMapIntVar.get(otherVar)))) {

                    ContainsAssertion assertion = new ContainsAssertion();
                    assertion.source = containerVar;
                    assertion.containedVariable = containsMapIntVar.get(otherVar);
                    assertion.value = containsMap.get(containsMapIntVar.get(otherVar));
                    assertions.add(assertion);
                    assert (assertion.isValid());
                }
            }
        }
        return assertions;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#getAssertions()
     */
    @Override
    public Set<Assertion> getAssertions() {
        Set<Assertion> assertions = new HashSet<Assertion>();

        for (VariableReference otherVar : containsMap.keySet()) {
            if (otherVar == null) {
                continue;
            }

            ContainsAssertion assertion = new ContainsAssertion();
            assertion.source = containerVar;
            assertion.containedVariable = otherVar;
            assertion.value = containsMap.get(otherVar);
            assertions.add(assertion);
            assert (assertion.isValid());
        }
        return assertions;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#isDetectedBy(org.evosuite.assertion.Assertion)
     */
    @Override
    public boolean isDetectedBy(Assertion assertion) {
        if (assertion instanceof ContainsAssertion) {
            ContainsAssertion ass = (ContainsAssertion) assertion;
            if (ass.source.equals(containerVar) && containsMap.containsKey(ass.containedVariable)) {
                return !containsMap.get(ass.containedVariable).equals(ass.value);
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#cloneEntry()
     */
    @Override
    public OutputTraceEntry cloneEntry() {
        ContainsTraceEntry copy = new ContainsTraceEntry(containerVar);
        copy.containsMap.putAll(containsMap);
        copy.containsMapIntVar.putAll(containsMapIntVar);
        return copy;
    }

}
