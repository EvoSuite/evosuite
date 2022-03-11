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
package org.evosuite.testcase.execution;

import org.evosuite.assertion.OutputTrace;
import org.evosuite.coverage.io.input.InputCoverageGoal;
import org.evosuite.coverage.io.output.OutputCoverageGoal;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.ga.metaheuristics.mapelites.FeatureVector;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.IntStream;

public class ExecutionResult implements Cloneable {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionResult.class);

    /**
     * Test case that produced this execution result
     */
    public TestCase test;

    /**
     * Mutation that was active during execution
     */
    public Mutation mutation;

    /**
     * Map statement number to raised exception
     */
    protected Map<Integer, Throwable> exceptions = new HashMap<>();

    /**
     * Record for each exception if it was explicitly thrown
     */
    // FIXME: internal data structures should never be null...
    public Map<Integer, Boolean> explicitExceptions = new HashMap<>();

    /**
     * Trace recorded during execution
     */
    protected ExecutionTrace trace;

    /**
     * Duration of execution
     */
    protected long executionTime = 0L;

    /**
     * Number of statements executed
     */
    protected int executedStatements = 0;

    /**
     * Was there a permission denied during execution?
     */
    protected boolean hasSecurityException = false;

    /**
     * Set of System properties that were read during test execution
     */
    protected Set<String> readProperties;

    /**
     * Keep track of whether any System property was written
     */
    protected boolean wasAnyPropertyWritten;

    private List<FeatureVector> featureVectors = new ArrayList<>(1);

    /**
     * @return the executedStatements
     */
    public int getExecutedStatements() {
        return executedStatements;
    }

    /**
     * @param executedStatements the executedStatements to set
     */
    public void setExecutedStatements(int executedStatements) {
        this.executedStatements = executedStatements;
    }

    /**
     * Output traces produced by observers
     */
    protected final Map<Class<?>, OutputTrace<?>> traces = new HashMap<>();

    private Map<Integer, Set<InputCoverageGoal>> inputGoals = new LinkedHashMap<>();

    private Map<Integer, Set<OutputCoverageGoal>> outputGoals = new LinkedHashMap<>();

    // experiment .. tried to remember intermediately calculated ControlFlowDistances .. no real speed up
    //	public Map<Branch, ControlFlowDistance> intermediateDistances;

    /**
     * Default constructor when executing without mutation
     *
     * @param t a {@link org.evosuite.testcase.TestCase} object.
     */
    public ExecutionResult(TestCase t) {
        trace = null;
        mutation = null;
        test = t;
    }

    /**
     * <p>
     * Copy the input map data into internal structures
     * </p>
     *
     * @param data a {@link java.util.Map} object. It has a mapping from test
     *             sequence position toward thrown exception
     */
    public void setThrownExceptions(Map<Integer, Throwable> data) {
        exceptions.clear();
        data.forEach(this::reportNewThrownException);
    }


    /**
     * <p>
     * getFirstPositionOfThrownException
     * </p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getFirstPositionOfThrownException() {
        return exceptions.keySet().stream()
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    /**
     * <p>
     * reportNewThrownException
     * </p>
     *
     * @param position a {@link java.lang.Integer} object.
     * @param t        a {@link java.lang.Throwable} object.
     */
    public void reportNewThrownException(Integer position, Throwable t) {
        exceptions.put(position, t);
    }

    /**
     * <p>
     * getPositionsWhereExceptionsWereThrown
     * </p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Integer> getPositionsWhereExceptionsWereThrown() {
        return exceptions.keySet();
    }

    /**
     * <p>
     * getAllThrownExceptions
     * </p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<Throwable> getAllThrownExceptions() {
        return exceptions.values();
    }

    /**
     * <p>
     * isThereAnExceptionAtPosition
     * </p>
     *
     * @param position a {@link java.lang.Integer} object.
     * @return a boolean.
     */
    public boolean isThereAnExceptionAtPosition(Integer position) {
        return exceptions.containsKey(position);
    }

    /**
     * <p>
     * noThrownExceptions
     * </p>
     *
     * @return a boolean.
     */
    public boolean noThrownExceptions() {
        return exceptions.isEmpty();
    }

    /**
     * <p>
     * getExceptionThrownAtPosition
     * </p>
     *
     * @param position a {@link java.lang.Integer} object.
     * @return a {@link java.lang.Throwable} object.
     */
    public Throwable getExceptionThrownAtPosition(Integer position) {
        return exceptions.get(position);
    }

    /**
     * <p>
     * getNumberOfThrownExceptions
     * </p>
     *
     * @return a int.
     */
    public int getNumberOfThrownExceptions() {
        return exceptions.size();
    }

    /**
     * shouldn't be used
     *
     * @return a {@link java.util.Map} object.
     */
    @Deprecated
    public Map<Integer, Throwable> exposeExceptionMapping() {
        return exceptions;
    }

    /**
     * @return Mapping of statement indexes and thrown exceptions.
     */
    public Map<Integer, Throwable> getCopyOfExceptionMapping() {
        return new HashMap<>(exceptions);
    }

    /**
     * Constructor when executing with mutation
     *
     * @param t a {@link org.evosuite.testcase.TestCase} object.
     * @param m a {@link org.evosuite.coverage.mutation.Mutation} object.
     */
    public ExecutionResult(TestCase t, Mutation m) {
        trace = null;
        mutation = m;
        test = t;
    }

    /**
     * Accessor to the execution trace
     *
     * @return a {@link org.evosuite.testcase.execution.ExecutionTrace} object.
     */
    public ExecutionTrace getTrace() {
        return trace;
    }

    /**
     * Set execution trace to different value
     *
     * @param trace a {@link org.evosuite.testcase.execution.ExecutionTrace} object.
     */
    public void setTrace(ExecutionTrace trace) throws IllegalArgumentException {
        if (trace == null) {
            throw new IllegalArgumentException("Trace cannot be null");
        }
        this.trace = trace;
    }

    /**
     * Store a new output trace
     *
     * @param trace a {@link org.evosuite.assertion.OutputTrace} object.
     * @param clazz a {@link java.lang.Class} object.
     */
    public void setTrace(OutputTrace<?> trace, Class<?> clazz) {
        traces.put(clazz, trace);
    }

    /**
     * Accessor for output trace produced by an observer of a particular class
     *
     * @param clazz a {@link java.lang.Class} object.
     * @return a {@link org.evosuite.assertion.OutputTrace} object.
     */
    public OutputTrace<?> getTrace(Class<?> clazz) {
        return traces.get(clazz);
    }

    /**
     * Accessor for the output traces produced by observers
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<OutputTrace<?>> getTraces() {
        return traces.values();
    }

    /**
     * Was the reason for termination a timeout?
     *
     * @return a boolean.
     */
    public boolean hasTimeout() {
        if (test == null)
            return false;

        final int size = test.size();
        return exceptions.containsKey(size)
                && exceptions.get(size) instanceof TestCaseExecutor.TimeoutExceeded;
    }

    /**
     * Does the test contain an exception caused in the test itself?
     *
     * @return a boolean.
     */
    public boolean hasTestException() {
        if (test == null)
            return false;

        return exceptions.values().stream()
                .anyMatch(t -> t instanceof CodeUnderTestException);
    }

    /**
     * Is there an undeclared exception in the trace?
     *
     * @return a boolean.
     */
    public boolean hasUndeclaredException() {
        if (test == null)
            return false;

        for (int i : exceptions.keySet()) {
            Throwable t = exceptions.get(i);
            // Exceptions can be placed at test.size(), e.g. for timeouts
            assert i >= 0 && i <= test.size() : "Exception " + t + " at position " + i + " in test of length " + test.size() + ": " + test.toCode(exceptions);
            if (i >= test.size())
                continue;

            if (!test.getStatement(i).getDeclaredExceptions().contains(t.getClass()))
                return true;
        }

        return false;
    }

    /**
     * Returns true if any of the executed statements was a reflection statement
     *
     * @return
     */
    public boolean calledReflection() {
        return IntStream.range(0, getExecutedStatements())
                .mapToObj(numStatement -> test.getStatement(numStatement))
                .anyMatch(Statement::isReflectionStatement);
    }


    /**
     * check if the test case threw any security exception
     *
     * @return
     */
    public boolean hasSecurityException() {
        return hasSecurityException;
    }

    public void setSecurityException(boolean value) {
        logger.debug("Changing hasSecurityException from " + hasSecurityException + " to " + value);
        hasSecurityException = value;
    }

    /**
     * @return the executionTime
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * @param executionTime the executionTime to set
     */
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionResult clone() {
        ExecutionResult copy = new ExecutionResult(test, mutation);
        copy.exceptions.putAll(exceptions);
        copy.trace = trace.lazyClone();
        copy.explicitExceptions.putAll(explicitExceptions);
        copy.executionTime = executionTime;
        copy.inputGoals = new LinkedHashMap<>(inputGoals);
        copy.outputGoals = new LinkedHashMap<>(outputGoals);
        for (Class<?> clazz : traces.keySet()) {
            copy.traces.put(clazz, traces.get(clazz).clone());
        }
        if (readProperties != null) {
            copy.readProperties = new LinkedHashSet<>();
            copy.readProperties.addAll(readProperties);
        }
        copy.wasAnyPropertyWritten = wasAnyPropertyWritten;
        copy.featureVectors = new ArrayList<>(this.featureVectors);

        return copy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Trace:" + trace;
    }

    public Set<String> getReadProperties() {
        return readProperties;
    }

    public void setReadProperties(Set<String> readProperties) {
        this.readProperties = readProperties;
    }

    public boolean wasAnyPropertyWritten() {
        return wasAnyPropertyWritten;
    }

    public void setWasAnyPropertyWritten(boolean wasAnyPropertyWritten) {
        this.wasAnyPropertyWritten = wasAnyPropertyWritten;
    }

    public void setTest(TestCase tc) {
        this.test = tc;
    }

    public void setInputGoals(Map<Integer, Set<InputCoverageGoal>> coveredGoals) {
        inputGoals.putAll(coveredGoals);
    }

    public void setOutputGoals(Map<Integer, Set<OutputCoverageGoal>> coveredGoals) {
        outputGoals.putAll(coveredGoals);
    }

    public Map<Integer, Set<InputCoverageGoal>> getInputGoals() {
        return inputGoals;
    }

    public Map<Integer, Set<OutputCoverageGoal>> getOutputGoals() {
        return outputGoals;
    }

    /**
     * Add a feature vector for MAPElites
     *
     * @param vector The feature vector.
     */
    public void addFeatureVector(FeatureVector vector) {
        this.featureVectors.add(vector);
    }

    /**
     * Get the feature vectors for MAPElites
     *
     * @return The feature vector if set or {@code null}
     */
    public List<FeatureVector> getFeatureVectors() {
        return Collections.unmodifiableList(this.featureVectors);
    }
}
