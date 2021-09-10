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
package org.evosuite.testcase.execution.reset;

import org.evosuite.Properties;
import org.evosuite.assertion.CheapPurityAnalyzer;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.statements.FieldStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.statements.reflection.PrivateFieldStatement;
import org.evosuite.testcase.variable.FieldReference;
import org.evosuite.testcase.variable.VariableReference;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * This singleton class handles the re-initialization of classes after an
 * evosuite test execution. The re-initialization will depend on the value of
 * the set of all initialized classes plus if we should re-initializate all
 * classes or just those that were affected by GETSTATIC or PUTSTATIC
 * instructions.
 *
 * @author galeotti
 */
public class ClassReInitializer {

    private final List<String> initializedClasses = new LinkedList<>();

    private static ClassReInitializer instance = null;

    public static void resetSingleton() {
        instance = null;
    }

    public static ClassReInitializer getInstance() {
        if (instance == null) {
            instance = new ClassReInitializer();
        }
        return instance;
    }

    private ClassReInitializer() {
    }

    /*
     * TODO: I think this would be nicer if each type of statement registered
     * the classes to reset as part of their execute() method
     */
    private static HashSet<String> getMoreClassesToReset(TestCase tc, ExecutionResult result) {
        HashSet<String> moreClassesForStaticReset = new HashSet<>();
        for (int position = 0; position < result.getExecutedStatements(); position++) {
            Statement statement = tc.getStatement(position);

            // If we reset also after reads, get all fields
            if (Properties.RESET_STATIC_FIELD_GETS) {
                for (VariableReference var : statement.getVariableReferences()) {
                    if (var.isFieldReference()) {
                        FieldReference fieldReference = (FieldReference) var;
                        moreClassesForStaticReset.add(fieldReference.getField().getOwnerClass().getClassName());
                    }
                }
            }

            // Check for explicit assignments to static fields
            if (statement.isAssignmentStatement()) {
                if (statement.getReturnValue() instanceof FieldReference) {
                    FieldReference fieldReference = (FieldReference) statement.getReturnValue();
                    if (fieldReference.getField().isStatic()) {
                        moreClassesForStaticReset.add(fieldReference.getField().getOwnerClass().getClassName());
                    }
                }
            } else if (statement instanceof FieldStatement) {
                // Check if we are invoking a non-pure method on a static field
                // variable
                FieldStatement fieldStatement = (FieldStatement) statement;
                if (fieldStatement.getField().isStatic()) {
                    VariableReference fieldReference = fieldStatement.getReturnValue();
                    if (Properties.RESET_STATIC_FIELD_GETS) {
                        moreClassesForStaticReset.add(fieldStatement.getField().getOwnerClass().getClassName());

                    } else {
                        // Check if the field was passed to a non-pure method
                        for (int i = fieldStatement.getPosition() + 1; i < result.getExecutedStatements(); i++) {
                            Statement invokedStatement = tc.getStatement(i);
                            if (invokedStatement.references(fieldReference)) {
                                if (invokedStatement instanceof MethodStatement) {
                                    if (fieldReference.equals(((MethodStatement) invokedStatement).getCallee())) {
                                        if (!CheapPurityAnalyzer.getInstance()
                                                .isPure(((MethodStatement) invokedStatement).getMethod().getMethod())) {
                                            moreClassesForStaticReset
                                                    .add(fieldStatement.getField().getOwnerClass().getClassName());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (statement instanceof PrivateFieldStatement) {
                PrivateFieldStatement fieldStatement = (PrivateFieldStatement) statement;
                if (fieldStatement.isStaticField()) {
                    moreClassesForStaticReset.add(fieldStatement.getOwnerClassName());
                }
            }
        }
        return moreClassesForStaticReset;
    }

    /**
     * This method is invoked after a test execution has ended. The classes to
     * be resetted will depend on the value of the reset_a
     *
     * @param executedTestCase
     * @param testCaseResult
     */
    public void reInitializeClassesAfterTestExecution(TestCase executedTestCase, ExecutionResult testCaseResult) {
        // first collect the initialized classes during this test execution
        final ExecutionTrace trace = testCaseResult.getTrace();
        List<String> classesInitializedDuringTestExecution = trace.getInitializedClasses();
        this.addInitializedClasses(classesInitializedDuringTestExecution);

        // if no initialized classes, then there are no classes to
        // re-initialized. Therefore, we should return
        if (initializedClasses.isEmpty()) {
            return;
        } else {

            // second, re-initialize classes
            if (reset_all_observed_classes) {
                ClassReInitializeExecutor.getInstance().resetClasses(initializedClasses);
            } else {
                // reset only classes that were "observed" to have some
                // GETSTATIC/PUTSTATIC updating their state during test
                // execution
                List<String> classesToReset = new LinkedList<>(trace.getClassesWithStaticWrites());
                if (Properties.RESET_STATIC_FIELD_GETS) {
                    classesToReset.addAll(trace.getClassesWithStaticReads());
                }
                HashSet<String> moreClassesForReset = getMoreClassesToReset(executedTestCase, testCaseResult);
                classesToReset.addAll(moreClassesForReset);
                // sort classes to reset
                Collections.sort(classesToReset);

                ClassLoader loader = null;
                if (executedTestCase instanceof DefaultTestCase) {
                    DefaultTestCase defaultTestCase = (DefaultTestCase) executedTestCase;
                    ClassLoader changedClassLoader = defaultTestCase.getChangedClassLoader();
                    if (changedClassLoader != null) {
                        loader = changedClassLoader;
                    }
                }
                if (loader == null) {
                    ClassReInitializeExecutor.getInstance().resetClasses(classesToReset);
                } else {
                    ClassReInitializeExecutor.getInstance().resetClasses(classesToReset, loader);
                }
            }
        }
    }

    private boolean reset_all_observed_classes = false;

    /**
     * Indicates if we should re-initialize all observed classes of only those
     * with GETSTATIC/PUTSTATIC calls affecting their state.
     *
     * @param reInitializeAllClasses
     */
    public void setReInitializeAllClasses(boolean reInitializeAllClasses) {
        reset_all_observed_classes = reInitializeAllClasses;
    }

    /**
     * This method will add the class to the list of those classes that were
     * initialized during this context execution.
     *
     * @param classNameWithDots the initialized class name with dots
     */
    private void addInitializedClass(String classNameWithDots) {
        if (!initializedClasses.contains(classNameWithDots)) {
            initializedClasses.add(classNameWithDots);
        }
    }

    /**
     * Adds in order those classes that were not already initialized
     *
     * @param classNamesWithDots
     */
    public void addInitializedClasses(List<String> classNamesWithDots) {
        for (String classNameWithDots : classNamesWithDots) {
            this.addInitializedClass(classNameWithDots);
        }
    }

    public List<String> getInitializedClasses() {
        return new LinkedList<>(this.initializedClasses);
    }


}
