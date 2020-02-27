package org.evosuite.coverage.length;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

public class LengthTestFitness extends TestFitnessFunction {

    private static int minimum_test_case_size = 3;

    protected final String className;


    public LengthTestFitness(String className) throws IllegalArgumentException{
        if ((className == null) ) {
            throw new IllegalArgumentException("className cannot be null");
        }
        this.className = className;
    }

    @Override
    public double getFitness(TestChromosome individual, ExecutionResult result) {
        System.out.println("*****Length*****");
        System.out.println(individual);
        System.out.println(individual.size());
        System.out.println(result.getExecutedStatements());
        System.out.println("*****Length*****");
        int length  = individual.size();
        double fitness = 0.0;
        if(length <minimum_test_case_size){
            fitness = 1.0;
        }
        updateIndividual(this, individual, length);

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
        LengthTestFitness other = (LengthTestFitness) obj;
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
        System.out.println("*****compareTocompareTocompareTocompareTo*****");
        if (other instanceof LengthTestFitness) {
            LengthTestFitness otherMethodFitness = (LengthTestFitness) other;
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
