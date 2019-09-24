package org.evosuite.testcase.mutation.deletion;

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.ConstraintVerifier;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.FieldReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DefaultDeletion implements DeletionStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DefaultDeletion.class);

    private DefaultDeletion() { }

    public static DefaultDeletion getInstance() {
        return SingletonContainer.instance;
    }

    private void filterVariablesByClass(Collection<VariableReference> variables, Class<?> clazz) {
        // Remove invalid classes if this is an Object.class reference
        variables.removeIf(r -> !r.getVariableClass().equals(clazz));
    }

    @Override
    public boolean deleteStatement(TestCase test, int position) throws ConstructionFailedException {
        if (!ConstraintVerifier.canDelete(test, position)
                || !test.getStatement(position).isTTLExpired()) {
            return false;
        }

        logger.debug("Deleting target statement - {}", position);

        Set<Integer> toDelete = new LinkedHashSet<>();
        recursiveDeleteInclusion(test, toDelete, position);

        List<Integer> pos = new ArrayList<>(toDelete);
        pos.sort(Collections.reverseOrder());

        for (int i : pos) {
            logger.debug("Deleting statement: {}", i);
            test.remove(i);
        }

        return true;
    }

    @Override
    public boolean deleteStatementGracefully(TestCase test, int position) throws ConstructionFailedException {
        if (!test.getStatement(position).isTTLExpired()) {
            return false;
        }

        VariableReference var = test.getReturnValue(position);

        if (var instanceof ArrayIndex) {
            return deleteStatement(test, position);
        }

        boolean changed = false;

        boolean replacingPrimitive = test.getStatement(position) instanceof PrimitiveStatement;

        // Get possible replacements
        List<VariableReference> alternatives = test.getObjects(var.getType(), position);

        int maxIndex = 0;
        if (var instanceof ArrayReference) {
            maxIndex = ((ArrayReference) var).getMaximumIndex();
        }

        // Remove invalid classes if this is an Object.class reference
        if (test.getStatement(position) instanceof MethodStatement) {
            MethodStatement ms = (MethodStatement) test.getStatement(position);
            if (ms.getReturnType().equals(Object.class)) {
                //				filterVariablesByClass(alternatives, var.getVariableClass());
                filterVariablesByClass(alternatives, Object.class);
            }
        } else if (test.getStatement(position) instanceof ConstructorStatement) {
            ConstructorStatement cs = (ConstructorStatement) test.getStatement(position);
            if (cs.getReturnType().equals(Object.class)) {
                filterVariablesByClass(alternatives, Object.class);
            }
        }

        // Remove self, and all field or array references to self
        alternatives.remove(var);
        Iterator<VariableReference> replacement = alternatives.iterator();
        while (replacement.hasNext()) {
            VariableReference r = replacement.next();
            if (test.getStatement(r.getStPosition()) instanceof FunctionalMockStatement) {
                // we should ensure that a FM should never be a callee
                replacement.remove();
            } else if (var.equals(r.getAdditionalVariableReference())) {
                replacement.remove();
            } else if (var.isFieldReference()) {
                FieldReference fref = (FieldReference) var;
                if (fref.getField().isFinal()) {
                    replacement.remove();
                }
            } else if (r instanceof ArrayReference) {
                if (maxIndex >= ((ArrayReference) r).getArrayLength())
                    replacement.remove();
            } else if (!replacingPrimitive) {
                if (test.getStatement(r.getStPosition()) instanceof PrimitiveStatement) {
                    replacement.remove();
                }
            }
        }

        if (!alternatives.isEmpty()) {
            // Change all references to return value at position to something else
            for (int i = position + 1; i < test.size(); i++) {
                Statement s = test.getStatement(i);
                if (s.references(var)) {
                    if (s.isAssignmentStatement()) {
                        AssignmentStatement assignment = (AssignmentStatement) s;
                        if (assignment.getValue() == var) {
                            VariableReference replacementVar = Randomness.choice(alternatives);
                            if (assignment.getReturnValue().isAssignableFrom(replacementVar)) {
                                s.replace(var, replacementVar);
                                changed = true;
                            }
                        } else if (assignment.getReturnValue() == var) {
                            VariableReference replacementVar = Randomness.choice(alternatives);
                            if (replacementVar.isAssignableFrom(assignment.getValue())) {
                                s.replace(var, replacementVar);
                                changed = true;
                            }
                        }
                    } else {
						/*
							if 'var' is a bounded variable used in 's', then it should not be
							replaced with another one. should be left as it is, as to make it
							deletable
						 */
                        boolean bounded = false;
                        if (s instanceof EntityWithParametersStatement) {
                            EntityWithParametersStatement es = (EntityWithParametersStatement) s;
                            bounded = es.isBounded(var);
                        }

                        if (!bounded) {
                            s.replace(var, Randomness.choice(alternatives));
                            changed = true;
                        }
                    }
                }
            }
        }

        if (var instanceof ArrayReference) {
            alternatives = test.getObjects(var.getComponentType(), position);
            // Remove self, and all field or array references to self
            alternatives.remove(var);
            replacement = alternatives.iterator();
            while (replacement.hasNext()) {
                VariableReference r = replacement.next();
                if (var.equals(r.getAdditionalVariableReference()))
                    replacement.remove();
                else if (r instanceof ArrayReference) {
                    if (maxIndex >= ((ArrayReference) r).getArrayLength())
                        replacement.remove();
                }
            }
            if (!alternatives.isEmpty()) {
                // Change all references to return value at position to something else
                for (int i = position; i < test.size(); i++) {
                    Statement s = test.getStatement(i);
                    for (VariableReference var2 : s.getVariableReferences()) {
                        if (var2 instanceof ArrayIndex) {
                            ArrayIndex ai = (ArrayIndex) var2;
                            if (ai.getArray().equals(var)) {
                                s.replace(var2, Randomness.choice(alternatives));
                                changed = true;
                            }
                        }
                    }
                }
            }
        }

        // Remove everything else
        boolean deleted = deleteStatement(test, position);
        return deleted || changed;
    }

    private void recursiveDeleteInclusion(TestCase test, Set<Integer> toDelete, int position) {

        if (toDelete.contains(position)) {
            return; //end of recursion
        }

        toDelete.add(position);

        Set<Integer> references = getReferencePositions(test, position);

		/*
			it can happen that we can delete the target statements but, when we look at
			the other statements using it, then we could not delete them :(
			in those cases, we have to recursively look at all their dependencies.
		 */

        for (Integer i : references) {

            Set<Integer> constraintDependencies = ConstraintVerifier.dependentPositions(test, i);
            if (constraintDependencies != null) {
                for (Integer j : constraintDependencies) {
                    recursiveDeleteInclusion(test, toDelete, j);
                }
            }

            recursiveDeleteInclusion(test, toDelete, i);
        }
    }

    private Set<Integer> getReferencePositions(TestCase test, int position) {
        Set<VariableReference> references = new LinkedHashSet<>();
        Set<Integer> positions = new LinkedHashSet<>();
        references.add(test.getReturnValue(position));

        for (int i = position; i < test.size(); i++) {
            Set<VariableReference> temp = new LinkedHashSet<>();
            for (VariableReference v : references) {
                if (test.getStatement(i).references(v)) {
                    temp.add(test.getStatement(i).getReturnValue());
                    positions.add(i);
                }
            }
            references.addAll(temp);
        }
        return positions;
    }

    private static final class SingletonContainer {
        private static final DefaultDeletion instance = new DefaultDeletion();
    }
}
