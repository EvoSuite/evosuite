package org.evosuite.coverage.time;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

public class ExecutionTimeTestFitness extends TestFitnessFunction {

    protected final String className;

    public ExecutionTimeTestFitness(String className) throws IllegalArgumentException{
        if ((className == null) ) {
            throw new IllegalArgumentException("className cannot be null");
        }
        this.className = className;
    }

    @Override
    public double getFitness(TestChromosome individual, ExecutionResult result) {

        System.out.println("**********");
        System.out.println(individual);
        System.out.println(result.getExecutionTime());
        System.out.println(result.getExecutedStatements());
        System.out.println("**********");
        double fitness = 0.0;
        // Update the fitness of the test case with the new score.
        updateIndividual(this,individual,fitness);

        return fitness;
    }

    public int hashCode() {
        int iConst = 13;
        return 51 * iConst + className.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExecutionTimeTestFitness other = (ExecutionTimeTestFitness) obj;
        return className.equals(other.className);
    }


    public String getClassName() {
        return className;
    }


    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
     */
    @Override
    public int compareTo(TestFitnessFunction other) {
        if (other instanceof ExecutionTimeTestFitness) {
            ExecutionTimeTestFitness otherMethodFitness = (ExecutionTimeTestFitness) other;
            if (className.equals(otherMethodFitness.getClassName()))
                return 0;
            else
                return className.compareTo(otherMethodFitness.getClassName());
        }
        return compareClassName(other);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#getTargetClass()
     */
    @Override
    public String getTargetClass() {
        return getClassName();
    }

    @Override
    public String getTargetMethod() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "[CLASS] " + className;
    }

}
