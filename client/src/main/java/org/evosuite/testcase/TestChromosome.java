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
package org.evosuite.testcase;

import org.apache.commons.lang3.ArrayUtils;
import org.evosuite.Properties;
import org.evosuite.assertion.CheapPurityAnalyzer;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationExecutionResult;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.SecondaryObjective;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.ga.operators.mutation.MutationHistory;
import org.evosuite.graphs.ddg.MethodEntry;
import org.evosuite.runtime.javaee.injection.Injector;
import org.evosuite.runtime.util.AtMostOnceLogger;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.TestCluster;
import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.ConcolicExecution;
import org.evosuite.symbolic.ConcolicMutation;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.localsearch.TestCaseLocalSearch;
import org.evosuite.testcase.mutation.insertion.GuidedInsertion;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.evosuite.testcase.mutation.MutationUtils.*;

import static java.util.stream.Collectors.*;

/**
 * Chromosome representation of test cases
 *
 * @author Gordon Fraser
 */
public class TestChromosome extends ExecutableChromosome {

	private static final long serialVersionUID = 7532366007973252782L;

	private static final Logger logger = LoggerFactory.getLogger(TestChromosome.class);

	/** The test case encoded in this chromosome */
	protected TestCase test = new DefaultTestCase();

	/** To keep track of what has changed since last fitness evaluation */
	protected MutationHistory<TestMutationHistoryEntry> mutationHistory = new MutationHistory<>();

	/** Secondary objectives used during ranking */
	private static final List<SecondaryObjective<TestChromosome>> secondaryObjectives = new ArrayList<>();

	/**
	 * <p>
	 * setTestCase
	 * </p>
	 *
	 * @param testCase
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 */
	public void setTestCase(TestCase testCase) {
		test = testCase;
		clearCachedResults();
		clearCachedMutationResults();
		setChanged(true);
	}

	/**
	 * <p>
	 * getTestCase
	 * </p>
	 *
	 * @return a {@link org.evosuite.testcase.TestCase} object.
	 */
	public TestCase getTestCase() {
		return test;
	}

	/** {@inheritDoc} */
	@Override
	public void setLastExecutionResult(ExecutionResult lastExecutionResult) {
	    if (lastExecutionResult == null)
	        return ;
		assert lastExecutionResult.test.equals(this.test);
		this.lastExecutionResult = lastExecutionResult;
	}

	/** {@inheritDoc} */
	@Override
	public void setChanged(boolean changed) {
		super.setChanged(changed);
		if (changed) {
			clearCachedResults();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Create a deep copy of the chromosome
	 */
	@Override
	public Chromosome clone() {
		TestChromosome c = new TestChromosome();
		c.test = test.clone();
		c.setFitnessValues(getFitnessValues());
		c.setPreviousFitnessValues(getPreviousFitnessValues());
		c.copyCachedResults(this);
		c.setChanged(isChanged());
		c.setLocalSearchApplied(hasLocalSearchBeenApplied());
		if (Properties.LOCAL_SEARCH_SELECTIVE) {
			for (TestMutationHistoryEntry mutation : mutationHistory) {
				if(test.contains(mutation.getStatement()))
					c.mutationHistory.addMutationEntry(mutation.clone(c.getTestCase()));
			}
		}
		// c.mutationHistory.set(mutationHistory);
		c.setNumberOfMutations(this.getNumberOfMutations());
		c.setNumberOfEvaluations(this.getNumberOfEvaluations());
		c.setKineticEnergy(getKineticEnergy());
		c.setNumCollisions(getNumCollisions());

		return c;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutableChromosome#copyCachedResults(org.evosuite.testcase.ExecutableChromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public void copyCachedResults(ExecutableChromosome other) {
		if (test == null)
			throw new RuntimeException("Test is null!");

		if (other.lastExecutionResult != null) {
			this.lastExecutionResult = other.lastExecutionResult.clone();
			this.lastExecutionResult.setTest(this.test);
		}

		if (other.lastMutationResult != null) {
			for (Mutation mutation : other.lastMutationResult.keySet()) {
				MutationExecutionResult copy = other.lastMutationResult.get(mutation); //.clone();
				//copy.test = test;
				this.lastMutationResult.put(mutation, copy);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Single point cross over
	 */
	@Override
	public void crossOver(Chromosome other, int position1, int position2)
	        throws ConstructionFailedException {
		logger.debug("Crossover starting");
		TestChromosome otherChromosome = (TestChromosome)other;
		TestChromosome offspring = new TestChromosome();
		TestFactory testFactory = TestFactory.getInstance();

		for (int i = 0; i < position1; i++) {
			final Statement clone = test.getStatement(i).clone(offspring.test);
//			clone.resetTTL();
			offspring.test.addStatement(clone);
		}

		for (int i = position2; i < other.size(); i++) {
			GenericAccessibleObject<?> accessibleObject = otherChromosome.test.getStatement(i).getAccessibleObject();
			if(accessibleObject != null) {
				if (accessibleObject.getDeclaringClass().equals(Injector.class))
					continue;
				if(!ConstraintVerifier.isValidPositionForInsertion(accessibleObject, offspring.test, offspring.test.size())) {
					continue;
				}
			}
			testFactory.appendStatement(offspring.test,
					otherChromosome.test.getStatement(i));
		}
		if (!Properties.CHECK_MAX_LENGTH
				|| offspring.test.size() <= Properties.CHROMOSOME_LENGTH) {
			test = offspring.test;
			setChanged(true);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Two chromosomes are equal if their tests are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestChromosome other = (TestChromosome) obj;
		if (test == null) {
			return other.test == null;
		} else return test.equals(other.test);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return test.hashCode();
	}

	public MutationHistory<TestMutationHistoryEntry> getMutationHistory() {
		return mutationHistory;
	}

	public void clearMutationHistory() {
		mutationHistory.clear();
	}

	public boolean hasRelevantMutations() {

		if (mutationHistory.isEmpty()) {
			logger.info("Mutation history is empty");
			return false;
		}

		// Only apply local search up to the point where an exception was thrown
		int lastPosition = test.size() - 1;
		if (lastExecutionResult != null && !isChanged()) {
			Integer lastPos = lastExecutionResult.getFirstPositionOfThrownException();
			if (lastPos != null)
				lastPosition = lastPos;
		}

		for (TestMutationHistoryEntry mutation : mutationHistory) {
			logger.info("Considering: " + mutation.getMutationType());

			if (mutation.getMutationType() != TestMutationHistoryEntry.TestMutation.DELETION
			        && mutation.getStatement().getPosition() <= lastPosition) {
				if (Properties.LOCAL_SEARCH_SELECTIVE_PRIMITIVES) {
					if (!(mutation.getStatement() instanceof PrimitiveStatement<?>))
						continue;
				}
				final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();

				if (!test.hasReferences(mutation.getStatement().getReturnValue())
				        && !mutation.getStatement().getReturnClass().equals(targetClass)) {
					continue;
				}

				int newPosition = IntStream.rangeClosed(0, lastPosition)
						.filter(pos -> test.getStatement(pos) == mutation.getStatement())
						.findFirst().orElse(-1);

				// Couldn't find statement, may have been deleted in other mutation?
				assert (newPosition >= 0);

				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.Chromosome#localSearch()
	 */
	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public boolean localSearch(LocalSearchObjective<? extends Chromosome> objective) {
		TestCaseLocalSearch localSearch = TestCaseLocalSearch.selectTestCaseLocalSearch();
		return localSearch.doSearch(this,
		                            (LocalSearchObjective<TestChromosome>) objective);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Each statement is mutated with probability 1/l
	 */
	@Override
	public void mutate() {
		boolean changed = false;
		mutationHistory.clear();

		if (Properties.ENABLE_TTL) {
			test.decreaseStatementTTL();
		}

		if(mockChange()) {
			changed = true;
		}

		if(Properties.CHOP_MAX_LENGTH && size() >= Properties.CHROMOSOME_LENGTH) {
			int lastPosition = getLastMutatableStatement();
			test.chop(lastPosition + 1);
		}

		if (Properties.MUTATION_STRATEGY == Properties.MutationStrategy.GUIDED) {
			/*
			 * The guided mutation strategy uses execution traces and coverage information
			 * of the given test case from the previous generation. Performing one of
			 * mutationInsert(), mutationChange() and mutationDelete() invalidates this
			 * information for the other two, which means they have to fall back to uninformed
			 * random operations. Since mutationInsert() can make the most of execution
			 * traces and coverage information, it makes sense to always execute it before
			 * mutationChange() and mutationDelete(). This way, mutationInsert() can always
			 * rely on valid information.
			 */

			// Insert
			if (Randomness.nextDouble() <= Properties.P_TEST_INSERT) {
				logger.debug("Mutation: insert");
				if (mutationInsert())
					changed = true;
			}

			// Change
			if (Randomness.nextDouble() <= Properties.P_TEST_CHANGE) {
				if (changed) { // coverage information etc. is invalid
                    logger.debug("Mutation: random change");
                    changed = mutationChange();
				} else {
                    logger.debug("Mutation: guided change");
					if (guidedChange()) {
						changed = true;
					}
				}
			}

			// Delete
			if (Randomness.nextDouble() <= Properties.P_TEST_DELETE) {
				if (Randomness.nextDouble() < 0.25) {
                    logger.debug("Mutation: random delete");
                    mutationDelete();
                } else {
                    logger.debug("Mutation: guided delete");
						if (guidedDeletion()) {
							changed = true;
					}
				}
			}
		} else {
			// Delete
			if (Randomness.nextDouble() <= Properties.P_TEST_DELETE) {
				logger.debug("Mutation: delete");
				if (mutationDelete())
					changed = true;
			}

			// Change
			if (Randomness.nextDouble() <= Properties.P_TEST_CHANGE) {
				logger.debug("Mutation: change");
				if (mutationChange())
					changed = true;
			}

			// Insert
			if (Randomness.nextDouble() <= Properties.P_TEST_INSERT) {
				logger.debug("Mutation: insert");
				if (mutationInsert())
					changed = true;
			}
		}

		if (changed) {
			this.increaseNumberOfMutations();
			setChanged(true);
			test.clearCoveredGoals();
		}

		test.forEach(Statement::isValid);

		// be sure that mutation did not break any constraint.
		// if it happens, it means a bug in EvoSuite
		assert ConstraintVerifier.verifyTest(test);
		assert ! ConstraintVerifier.hasAnyOnlyForAssertionMethod(test);
	}

	// TODO: Reorder already existing statements? E.g. top() after push()?
	//	Stack<Object> stack0 = Stack.factory();
	//	stack0.top();
	//	stack0.top();
	//	stack0.push(stack0);
	//	stack0.push(string0);
	private boolean guidedChange() {
	    if (test.isEmpty()) { // there are no statements that could possibly be changed...
	        logger.debug("test case is empty, nothing to mutate...");
            return false;
        }

        // FIXME Retrieving the current goals like that... I don't find this so pretty, but hey...
		final Set<TestFitnessFunction> goals = GuidedInsertion.getInstance().goals();

        // Check if the previous goal has been reached.
		final TestFitnessFunction previousGoal = test.getTarget();
		final boolean previousGoalReached = !goals.contains(previousGoal);

		if (previousGoalReached) {
		    logger.debug("previous intended goal reached");

		    // Try to find a new intended goal for the test case. Public goals are preferred
            // because they're supposedly easier to cover. (We can call public methods directly,
            // whereas non-public methods are completely hidden and can only be reached indirectly.)
		    final Map<Boolean, List<TestFitnessFunction>> newGoals = goals.stream()
                            .collect(Collectors.partitioningBy(TestFitnessFunction::isPublic));

		    // We check if there's a public goal for which the target method is already invoked.
            // If so, it means that control flow already enters the right method, but still misses
            // the target. We try to fuzz the input parameters of the method in an attempt to
            // direct control flow towards the target.
            final Set<TestFitnessFunction> publicGoals = newGoals.get(true).stream()
                    .filter(test::callsMethod)
                    .collect(Collectors.toSet());
            if (!publicGoals.isEmpty()) {
                logger.debug("choosing new goal for test case");
				final Optional<TestFitnessFunction> g = chooseGoal(publicGoals);
				final TestFitnessFunction newGoal = g.orElseThrow(IllegalStateException::new);
                test.setTarget(newGoal);
                final EntityWithParametersStatement call = getStatementFor(newGoal);
                return call != null && changeParametersOf(call);
            }

            // There are no public goals. We cannot reach them directly. Instead, we must call
            // some public method which eventually calls the non-public target method for us.
            // We check if such a public method is already invoked by the test case.
            final List<TestFitnessFunction> nonPublicGoals = newGoals.get(false);
            final Set<MethodEntry> publicCallers = nonPublicGoals.stream()
                    .flatMap(goal -> getPublicCallersOf(goal).stream())
                    .filter(call -> test.callsMethod(call.getClassName(), call.getMethodNameDesc()))
                    .collect(Collectors.toSet());

            if (publicCallers.isEmpty()) {
                logger.debug("No public callers for current targets");

                // Just change something randomly. This is our last-ditch effort if guided change
                // is not possible.
                return mutationChange();
            }

            // Select a random amount of such public callers (but always at least 1), try to fuzz
            // their input parameters and hope that control flow now reaches the target.
            final Set<EntityWithParametersStatement> calls = getStatementsFor(publicCallers);

			if (calls.isEmpty()) {
				return false;
			}

            final int numberCalls = 1 + Randomness.nextInt(calls.size());
            final Set<EntityWithParametersStatement> chosenCalls = calls.stream()
                    .limit(numberCalls)
                    .collect(Collectors.toSet());
            return !chosenCalls.isEmpty() && changeParametersOf(chosenCalls);
		} else { // The intended goal has not been reached yet but the test already contains
		         // method calls that try to reach it.
            final String className = previousGoal.getTargetClassName();
            final String methodName = previousGoal.getTargetMethodName();

            // Fuzz the input parameters of the public target or, if the goals is non-public,
            // one of its public callers.
            if (previousGoal.isPublic()) {
                logger.debug("Trying to fuzz input parameters for current goal");

                final EntityWithParametersStatement call = getStatementFor(previousGoal);
                return call != null && changeParametersOf(call);
            } else {
                logger.debug("Trying to fuzz input parameters of public callers for current goal");

                final Set<MethodEntry> publicCallers = DependencyAnalysis.getCallGraph()
                        .getPublicCallersOf(className, methodName);
                final Set<EntityWithParametersStatement> calls = getStatementsFor(publicCallers);

				if (calls.isEmpty()) {
					return false;
				}

                final int numberCalls = 1 + Randomness.nextInt(calls.size());
                final Set<EntityWithParametersStatement> chosenCalls = calls.stream()
                        .limit(numberCalls)
                        .collect(Collectors.toSet());
                return changeParametersOf(chosenCalls);
            }
		}
    }

    private Set<MethodEntry> getPublicCallersOf(TestFitnessFunction target) {
        return DependencyAnalysis.getCallGraph().getPublicCallersOf(toMethodEntry(target));
    }

    private EntityWithParametersStatement getStatementFor(TestFitnessFunction goal) {
        final String className = goal.getTargetClassName();
        final String methodName = goal.getTargetMethodName();
        int index = test.lastIndexOfCallTo(className, methodName);

        if (index < 0) {
            logger.warn("Test has no call to {}.{}", className, methodName);
            return null;
        }

        final Statement stmt = test.getStatement(index);

        if (!(stmt instanceof EntityWithParametersStatement)) {
            logger.error("Statement is neither a method nor constructor call");
            return null; // shouldn't really happen, but better safe than sorry...
        }

        return (EntityWithParametersStatement) stmt;
    }

    private Set<EntityWithParametersStatement> getStatementsFor(Set<MethodEntry> goals) {
        final IntStream indexes = goals.stream()
                .mapToInt(call -> test.lastIndexOfCallTo(call.getClassName(), call.getMethodNameDesc()))
                .filter(idx -> 0 <= idx && idx <= getLastMutatableStatement());
        final Stream<EntityWithParametersStatement> statements = indexes
                .mapToObj(test::getStatement)
                .filter(stmt -> stmt instanceof EntityWithParametersStatement)
                .map(stmt -> ((EntityWithParametersStatement) stmt));
        return statements.collect(Collectors.toSet());
    }

	/**
	 * Tries to fuzz one or more randomly chosen input parameters of the given call.
	 *
	 * @param call the call whose parameters to fuzz
	 * @return {@code true} if successful, {@code false} otherwise
	 */
	private boolean changeParametersOf(EntityWithParametersStatement call) {
        final List<VariableReference> parameters = call.getParameterReferences();

        if (parameters.isEmpty()) {
            logger.debug("Given call {} has no parameters", call);
            return false;
        }

        // How many parameters to fuzz.
		final int num = 1 + Randomness.nextInt(parameters.size());

        // Which parameters to fuzz.
		final int[] indexes = IntStream.range(0, parameters.size()).toArray();
		ArrayUtils.shuffle(indexes);

		boolean success = true;
        for (int i = 0; i < num && success; i++) {
            final VariableReference var = parameters.get(indexes[i]);
            success = changeVariable(var);
        }

        logger.debug("Fuzzing parameters for goal: {}", success ? "Success!" : "Failure");

        return success;
    }

    private boolean changeParametersOf(Set<EntityWithParametersStatement> calls) {
        if (!calls.isEmpty()) {
            boolean success = false;

            for (EntityWithParametersStatement call : calls) {
                success |= changeParametersOf(call);
            }

            return success;
        }

        return false;
	}

    private boolean changeVariable(VariableReference var) {
	    logger.debug("Trying to mutate variable {}", var);

        final Statement stmt = test.getStatement(var.getStPosition());
        final int oldDistance = stmt.getReturnValue().getDistance();

        final boolean changed = stmt.mutate(test);
        if (changed) {
            mutationHistory.addMutationEntry(new TestMutationHistoryEntry(
                    TestMutationHistoryEntry.TestMutation.CHANGE, stmt));
            assert test.isValid();
            assert ConstraintVerifier.verifyTest(test);
        }
        stmt.getReturnValue().setDistance(oldDistance);

        logger.debug("Mutation: {}", changed ? "Success!" : "Failure");

        return changed;
    }

    /**
     * Changes a random variable of the given type, but only considers statements up to the given
     * index.
     *
     * @param type the type of the variable to change
     * @param index the index up to which statements can be changed
     * @return {@code true} if successful, {@code false} otherwise
     */
	private boolean changeVariableOfType(Class<?> type, int index) {
	    // The set of statements that declare a variable of the given type.
        final Set<Statement> statements = test.getObjects(type, index).stream()
                // every variable must be declared before it is used, so the resulting
                // statements still have indexes <= the given index
                .map(v -> test.getStatement(v.getStPosition()))
                .filter(s -> !s.isReflectionStatement())
                .collect(Collectors.toSet());

        assert statements.stream().map(Statement::getPosition).allMatch(p -> p <= index);

        if (statements.isEmpty()) {
            return mutationChange();
        }

        final Statement stmt = Randomness.choice(statements);
        final int oldDistance = stmt.getReturnValue().getDistance();

        final boolean changed = stmt.mutate(test);

        if (changed) {
            mutationHistory.addMutationEntry(new TestMutationHistoryEntry(
                    TestMutationHistoryEntry.TestMutation.CHANGE, stmt));
            assert test.isValid();
            assert ConstraintVerifier.verifyTest(test);
        }

        stmt.getReturnValue().setDistance(oldDistance);

        return changed;
    }

	private boolean guidedDeletion() {
		if (test.isEmpty()) {
			return false;
		}

		boolean changed;
		final Class<?> cut = Properties.getTargetClassAndDontInitialise();

		// List of unique variable references that define a new instance of the CUT, ordered by the
		// positions they occur in the test case.
		final List<VariableReference> objects = test.getObjects(cut, getLastMutatableStatement());

		// Partition the CUT references depending on whether they are used after their definition.
		final Map<Boolean, List<VariableReference>> usedMap =
				objects.stream().collect(Collectors.partitioningBy(test::hasReferences));

		// References to CUT instances that are never used are useless and must be deleted.
		final List<VariableReference> unusedObjects = usedMap.get(false);
		changed = hardDelete(unusedObjects);

		if (changed) {
			assert ConstraintVerifier.verifyTest(test);
		}

		final List<VariableReference> usedObjects = usedMap.get(true);
		changed |= mergeRedundantObjects(usedObjects);

		if (changed) {
			assert ConstraintVerifier.verifyTest(test);
		}

		return changed;
	}

	private boolean mergeRedundantObjects(List<VariableReference> objectsToMerge) {
		boolean changed = false;

		// Groups all the given objects by their type. This must be done because the CUT could be
		// generic. For example, a Box<Integer> is a different type than Box<String>. And besides
		// that, a Box<String> cannot be assigned to a Box<Object> due to the invariance of generic
		// types in Java,
		final Map<Type, List<VariableReference>> typeMap = objectsToMerge.stream()
				.collect(Collectors.groupingBy(VariableReference::getType));

		// For every object type, try to merge redundant instances of the CUT.
		for (Map.Entry<Type, List<VariableReference>> entry : typeMap.entrySet()) {
			final List<VariableReference> list = entry.getValue();
			final Type type = entry.getKey();

			if (list.size() < 2) { // merge is only possible if there are at least two objects
				logger.debug("Merging: not of enough objects of type {}", type);
				continue;
			}

			/*
			 * Merging is done by skipping the first occurrence of the object and "gracefully"
			 * deleting all following occurrences. When an object is deleted gracefully, a
			 * replacement for the deleted object will be searched (instead of simply deleting
			 * all other statements that reference the deleted object). By construction, there
			 * is only one such possible replacement object, namely the first occurrence of the
			 * object that we skipped.
			 */

			final Iterator<VariableReference> iter = list.iterator();
			iter.next();

			// Sometimes, it might be desirable to have at least two objects of the same type
			// in the test, e.g., when invoking methods such as equals() or a copy constructor.
			final boolean allowDuplicates = test.getCalledMethods().stream().anyMatch(
					m -> m.getParameterReferences().stream().anyMatch(
							p -> p.isAssignableFrom(type)
					)
			);

			if (allowDuplicates && Randomness.nextDouble() < 0.25) {
				iter.next();
			}

			while (iter.hasNext()) {
				final VariableReference var = iter.next();
				final Statement statement = test.getStatement(var.getStPosition());

				if (!statement.isTTLExpired()) {
					logger.debug("Not merging object {} because TTL is not expired", var);
					continue;
				}

				// Collect the parameters for the current statement. If the current statement is
				// deleted, its parameters might have to be deleted as well.
				Set<VariableReference> parameters = new HashSet<>();
				if (statement instanceof MethodStatement) {
					MethodStatement method = (MethodStatement) statement;
					final VariableReference callee = method.getCallee(); // null for static methods
					if (callee != null) {
						parameters.add(callee);
					}
					parameters.addAll(method.getParameterReferences());
				} else if (statement instanceof ConstructorStatement) {
					ConstructorStatement ctor = (ConstructorStatement) statement;
					parameters.addAll(ctor.getParameterReferences());
				}

				// We will perform the deletion on a defensive copy, so that changes are reversible.
				final TestCase copy = test.clone();

				boolean success;
				mutationHistory.addMutationEntry(new TestMutationHistoryEntry(
						TestMutationHistoryEntry.TestMutation.DELETION));

				// Try to delete the current statement gracefully.
				try {
					success = TestFactory.getInstance()
							.deleteStatementGracefully(copy, var.getStPosition());
				} catch (ConstructionFailedException e) {
					logger.warn("Merging failed: {}", var);
					return false;
				}

				test = copy;

				if (!success) {
					logger.debug("Could not merge object {}", var);
					continue;
				}

				assert ConstraintVerifier.verifyTest(test);

				// Try to delete all references to objects which have now become obsolete.
				success = deleteIfUnused(parameters);
				if (!success) {
					logger.debug("Some unused variables could not be deleted");
				}

				changed = true;

				assert ConstraintVerifier.verifyTest(test);
			}
		}

		return changed;
	}

	/**
	 * For every given variable reference, tries to delete the statement that defines that
	 * reference from the test case. The deletion is recursive, that is, if the deleted statement
	 * was a method or constructor, its parameters and callee will also be deleted. Deletion of
	 * a statement is only performed if the variable reference defined by the statement is never
	 * referenced anywhere else in the test case.
	 *
	 * @param vars the variables to delete
	 */
	private boolean deleteIfUnused(Set<VariableReference> vars) {
		final Set<VariableReference> params = new HashSet<>();
		boolean success = false;

		for (VariableReference var : vars) {

			// If the variable is still referenced somewhere else in the test case, we cannot
			// delete it, as this would yield illegal Java code.
			if (test.hasReferences(var)) {
				continue;
			}

			final int position = var.getStPosition();
			final Statement stmt = test.getStatement(position);

			if (!stmt.isTTLExpired()) {
				logger.debug("Not deleting statement {} because TTL is not expired", position);
				continue;
			}

			/*
			 * If the statement is a method or constructor, its input parameters and/or the callee
			 * object might become unused as well when we delete the current statement. Hence, we
			 * have to recursively check them, too.
			 */
			if (stmt instanceof MethodStatement) {
				MethodStatement method = (MethodStatement) stmt;
				final VariableReference callee = method.getCallee();
				if (callee != null) {
					params.add(callee);
				}
				params.addAll(method.getParameterReferences());
			} else if (stmt instanceof ConstructorStatement) {
				ConstructorStatement ctor = (ConstructorStatement) stmt;
				params.addAll(ctor.getParameterReferences());
			}

			// We will perform the deletion on a defensive copy, so that changes are reversible.
			final TestCase copy = test.clone();

			boolean localSuccess;
			try {
				localSuccess = TestFactory.getInstance().deleteStatement(copy, position);
			} catch (ConstructionFailedException e) {
				return false;
			}

			test = copy;

			if (localSuccess) {
				success |= deleteIfUnused(params);
			} else {
				logger.warn("Could not delete statement {}", stmt);
			}
		}

		return success;
	}

	/**
	 * Deletes the objects represented by the given variable references from the test case, without
	 * performing any further checks prior to deletion. It is the responsibility of the caller to
	 * ensure that the test case is in an appropriate state before calling this method. This means
	 * that all given objects must never be referenced anywhere else in the test case.
	 *
	 * @param unusedObjects the unreferenced objects to delete
	 * @return {@code true} if at least one object was deleted, {@code false} otherwise
	 */
	private boolean hardDelete(List<VariableReference> unusedObjects) {
		boolean changed = false;

		for (VariableReference var : unusedObjects) {
			final int position = var.getStPosition();
			final Statement stmt = test.getStatement(position);

			// We cannot delete the statement if its TTL is not expired.
			if (!stmt.isTTLExpired()) {
				logger.debug("Not deleting statement, TTL is not expired");
				continue;
			}

			// If the statement invokes non-static method that is not cheap-pure, the method is
			// probably executed because it performs some sort of side-effect. Even if its return
			// value is never used in the test case, we still want to keep such methods.
			if (stmt instanceof MethodStatement) {
				Method method = ((MethodStatement) stmt).getMethod().getMethod();
				if (!Modifier.isStatic(method.getModifiers())
						&& !CheapPurityAnalyzer.getInstance().isPure(method)) {
					continue;
				}
			}

			final boolean success = test.remove(position);
			if (success) {
				changed = true;
				assert ConstraintVerifier.verifyTest(test);
			}
		}

		return changed;
	}


	private boolean mockChange()  {

		/*
			Be sure to update the mocked values if there has been any change in
			behavior in the last execution.

			Note: mock "expansion" cannot be done after a test has been mutated and executed,
			as the expansion itself might have side effects. Therefore, it has to be done
			before a test is evaluated.
		 */

		boolean changed = false;

		for(int i=0; i<test.size(); i++){
			Statement st = test.getStatement(i);
			if(! (st instanceof FunctionalMockStatement)){
				continue;
			}

			FunctionalMockStatement fms = (FunctionalMockStatement) st;
			if(! fms.doesNeedToUpdateInputs()){
				continue;
			}

			int preLength = test.size();

			try {
				List<Type> missing = fms.updateMockedMethods();
				int pos = st.getPosition();
				logger.debug("Generating parameters for mock call");
				// Added 'null' as additional parameter - fix for @NotNull annotations issue on evo mailing list
				List<VariableReference> refs = TestFactory.getInstance().satisfyParameters(test, null, missing,null, pos, 0, true, false,true);
				fms.addMissingInputs(refs);
			} catch (Exception e){
				//shouldn't really happen because, in the worst case, we could create mocks for missing parameters
				String msg = "Functional mock problem: "+e.toString();
				AtMostOnceLogger.warn(logger, msg);
				fms.fillWithNullRefs();
				return changed;
			}
			changed = true;

			int increase = test.size() - preLength;
			i += increase;
		}

		return changed;
	}

	/**
	 * In the test case encoded by this chromosome, returns the position of the last statement that
	 * can be mutated. If an exception occurred during the last execution of the test case, the
	 * method returns the position of the last valid statement, i.e., the position of the statement
	 * that directly precedes the exception-causing statement.
	 *
	 * @return the position of the last valid statement that can be mutated
	 */
	private int getLastMutatableStatement() {
		final ExecutionResult result = getLastExecutionResult();
		final int size = test.size();

		if (result != null && !result.noThrownExceptions()) {
			// If an exception was thrown during execution, the test case is only valid up to the
			// point right before where the exception occurred.
			final int pos = result.getFirstPositionOfThrownException();

			// It may happen that pos > size() after statements have been deleted.
			return pos >= size ? size - 1 : pos;
		} else {
			return test.size() - 1;
		}
	}

	/**
	 * Each statement is deleted with probability 1/length
	 *
	 * @return
	 */
	private boolean mutationDelete() {
		if(test.isEmpty()){
			return false; //nothing to delete
		}

		boolean changed = false;
		int lastMutableStatement = getLastMutatableStatement();

		final int[] indexes;
		if (Properties.ENABLE_TTL) {
			// Only consider statements whose TTL is already expired.
			indexes = IntStream.rangeClosed(0, lastMutableStatement)
					.filter(i -> test.getStatement(i).isTTLExpired())
					.toArray();
		} else {
			indexes = IntStream.rangeClosed(0, lastMutableStatement).toArray();
		}

		// We will iterate over the statements in reverse order.
		ArrayUtils.reverse(indexes);

		double pl = 1d / indexes.length;
		TestFactory testFactory = TestFactory.getInstance();

		for (int num : indexes) {

			if(num >= test.size()){
				continue; //in case the delete remove more than one statement
			}

			// Each statement is deleted with probability 1/l
			if (Randomness.nextDouble() <= pl) {
				changed |= deleteStatement(testFactory, num);

				assert !changed || ConstraintVerifier.verifyTest(test);
			}
		}

		assert !changed || ConstraintVerifier.verifyTest(test);

		return changed;
	}

	protected boolean deleteStatement(TestFactory testFactory, int num) {

		try {

            TestCase copy = test.clone();

            mutationHistory.addMutationEntry(new TestMutationHistoryEntry(
					TestMutationHistoryEntry.TestMutation.DELETION));
            boolean modified = testFactory.deleteStatementGracefully(copy, num);

            test = copy;
           	return modified;

        } catch (ConstructionFailedException e) {
            logger.warn("Deletion of statement failed: " + test.getStatement(num).getCode());
            logger.warn(test.toCode());
			return false; //modifications were on copy
        }
	}

	/**
	 * Each statement is replaced with probability 1/length
	 *
	 * @return
	 */
	private boolean mutationChange() {
		boolean changed = false;
		final int lastMutatableStatement = getLastMutatableStatement();

		// Indexes of the statements that are eligible for modification.
		final int[] indexes;
		if (Properties.ENABLE_TTL) {
			// Only consider statements whose TTL is already expired.
			indexes = IntStream.rangeClosed(0, lastMutatableStatement)
					.filter(i -> test.getStatement(i).isTTLExpired())
					.toArray();
		} else {
			indexes = IntStream.rangeClosed(0, lastMutatableStatement).toArray();
		}

		// Every expired statement will be modified with the same probability of
		// pl = 1 / #(expired statements).
		final double pl = 1d / indexes.length;
		TestFactory testFactory = TestFactory.getInstance();

		if (Randomness.nextDouble() < Properties.CONCOLIC_MUTATION) {
			try {
				changed = mutationConcolic();
			} catch (Exception exc) {
				logger.warn("Encountered exception when trying to use concolic mutation: {}", exc.getMessage());
				logger.debug("Detailed exception trace: ", exc);
			}
		}

		if (!changed) {
			for (int position : indexes) {
				final Statement statement = test.getStatement(position);

				if (Randomness.nextDouble() <= pl) {
					assert (test.isValid());

					if(statement.isReflectionStatement())
						continue;

					int oldDistance = statement.getReturnValue().getDistance();

					//constraints are handled directly in the statement mutations
					if (statement.mutate(test)) {
						changed = true;
						mutationHistory.addMutationEntry(new TestMutationHistoryEntry(
						        TestMutationHistoryEntry.TestMutation.CHANGE, statement));
						assert (test.isValid());
						assert ConstraintVerifier.verifyTest(test);

					} else if (!statement.isAssignmentStatement() &&
							ConstraintVerifier.canDelete(test,position)) {
						//if a statement should not be deleted, then it cannot be either replaced by another one

						int pos = statement.getPosition();
						if (testFactory.changeRandomCall(test, statement)) {
							changed = true;
							mutationHistory.addMutationEntry(new TestMutationHistoryEntry(
							        TestMutationHistoryEntry.TestMutation.CHANGE,
							        test.getStatement(pos)));
							assert ConstraintVerifier.verifyTest(test);
						}
						assert (test.isValid());
					}

					statement.getReturnValue().setDistance(oldDistance);

					// TODO: can this really happen? And if so, wouldn't you have to recompute
					//  lastMutatableStatement as well?
					// position = statement.getPosition(); // Might have changed due to mutation
				}
			}
		}

		assert !changed || ConstraintVerifier.verifyTest(test);

		return changed;
	}

	/**
	 * With exponentially decreasing probability, insert statements at random
	 * position
	 *
	 * @return
	 */
	public boolean mutationInsert() {
		boolean changed = false;
		final double ALPHA = Properties.P_STATEMENT_INSERTION; //0.5;
		int count = 0;
		TestFactory testFactory = TestFactory.getInstance();

		while (Randomness.nextDouble() <= Math.pow(ALPHA, count)
		        && (!Properties.CHECK_MAX_LENGTH || size() < Properties.CHROMOSOME_LENGTH)) {

			count++;
			// Insert at position as during initialization (i.e., using helper sequences)
			int position = testFactory.insertRandomStatement(test, getLastMutatableStatement());

			if (position >= 0 && position < test.size()) {
				changed = true;
				mutationHistory.addMutationEntry(new TestMutationHistoryEntry(
				        TestMutationHistoryEntry.TestMutation.INSERTION,
				        test.getStatement(position)));
			}
		}
		return changed;
	}

	/**
	 * Collect path constraints and negate one of them to derive new integer
	 * inputs
	 *
	 * @return
	 */
	private boolean mutationConcolic() {
		logger.info("Applying DSE mutation");
		// concolicExecution = new ConcolicExecution();

		// Apply DSE to gather constraints
		List<BranchCondition> branches = ConcolicExecution.getSymbolicPath(this);
		logger.debug("Conditions: " + branches);
		if (branches.isEmpty())
			return false;

		boolean mutated = false;

		List<BranchCondition> targetBranches = branches.stream()
				.filter(b -> TestCluster.isTargetClassName(b.getClassName()))
				.collect(toCollection(ArrayList::new));

		// Select random branch
		List<BranchCondition> bs = targetBranches.isEmpty() ? branches : targetBranches;
		BranchCondition branch =  Randomness.choice(bs);

		logger.debug("Trying to negate branch " + branch.getInstructionIndex()
		        + " - have " + targetBranches.size() + "/" + branches.size()
		        + " target branches");

		// Try to solve negated constraint
		TestCase newTest = ConcolicMutation.negateCondition(branches, branch, test);

		// If successful, add resulting test to test suite
		if (newTest != null) {
			logger.debug("CONCOLIC: Created new test");
			// logger.info(newTest.toCode());
			// logger.info("Old test");
			// logger.info(test.toCode());
			this.test = newTest;
			this.setChanged(true);
			this.lastExecutionResult = null;
		} else {
			logger.debug("CONCOLIC: Did not create new test");
		}

		return mutated;
	}

	/**
	 * {@inheritDoc}
	 *
	 * The size of a chromosome is the length of its test case
	 */
	@Override
	public int size() {
		return test.size();
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(Chromosome o) {
		int result = super.compareTo(o);
		if (result != 0) {
			return result;
		}
		// make this deliberately not 0
		// because then ordering of results will be random
		// among tests of equal fitness
		if (o instanceof TestChromosome) {
			return test.toCode().compareTo(((TestChromosome) o).test.toCode());
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return test.toCode();
	}

	/**
	 * <p>
	 * hasException
	 * </p>
	 *
	 * @return a boolean.
	 */
	public boolean hasException() {
		return lastExecutionResult != null && !lastExecutionResult.noThrownExceptions();
	}


	/* (non-Javadoc)
	 * @see org.evosuite.ga.Chromosome#applyDSE()
	 */
	/** {@inheritDoc} */
	/*
	@Override
	public boolean applyDSE(GeneticAlgorithm<?> ga) {
		// TODO Auto-generated method stub
		return false;
	}
	*/

	/** {@inheritDoc} */
	@Override
	public ExecutionResult executeForFitnessFunction(
	        TestSuiteFitnessFunction testSuiteFitnessFunction) {
		return testSuiteFitnessFunction.runTest(this.test);
	}

	@Override
	@SuppressWarnings("unchecked")
	public  <T extends Chromosome> int compareSecondaryObjective(T o) {
		int objective = 0;
		int c = 0;

		while (c == 0 && objective < secondaryObjectives.size()) {

			SecondaryObjective<T> so = (SecondaryObjective<T>) secondaryObjectives.get(objective++);
			if (so == null)
				break;
			c = so.compareChromosomes((T) this, o);
		}
		return c;
	}
	/**
	 * Add an additional secondary objective to the end of the list of
	 * objectives
	 *
	 * @param objective
	 *            a {@link org.evosuite.ga.SecondaryObjective} object.
	 */
	public static void addSecondaryObjective(SecondaryObjective<TestChromosome> objective) {
		secondaryObjectives.add(objective);
	}

	public static void ShuffleSecondaryObjective() {
		Collections.shuffle(secondaryObjectives);
	}

	public static void reverseSecondaryObjective() {
		Collections.reverse(secondaryObjectives);
	}

	/**
	 * Remove secondary objective from list, if it is there
	 *
	 * @param objective
	 *            a {@link org.evosuite.ga.SecondaryObjective} object.
	 */
	public static void removeSecondaryObjective(SecondaryObjective<?> objective) {
		secondaryObjectives.remove(objective);
	}

	/**
	 * <p>
	 * Getter for the field <code>secondaryObjectives</code>.
	 * </p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public static List<SecondaryObjective<TestChromosome>> getSecondaryObjectives() {
		return secondaryObjectives;
	}

}
