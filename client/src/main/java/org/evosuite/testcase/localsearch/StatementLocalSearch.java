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
package org.evosuite.testcase.localsearch;

import org.evosuite.Properties;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.*;

import java.util.Set;

public abstract class StatementLocalSearch {

    private TestChromosome backup = null;

    protected void backup(TestChromosome test) {
        backup = test.clone();
    }

    protected void restore(TestChromosome test) {
        if (backup == null)
            return;

        test.setTestCase(backup.getTestCase().clone());
        test.copyCachedResults(backup);
        //test.setFitness(backup.getFitness());
        test.setFitnessValues(backup.getFitnessValues());
        test.setPreviousFitnessValues(backup.getPreviousFitnessValues());
        test.setChanged(backup.isChanged());
    }


    /**
     * <p>
     * doSearch
     * </p>
     *
     * @param test      a {@link org.evosuite.testcase.TestChromosome} object.
     * @param statement a int.
     * @param objective a {@link org.evosuite.ga.localsearch.LocalSearchObjective} object.
     */
    public abstract boolean doSearch(TestChromosome test, int statement,
                                     LocalSearchObjective<TestChromosome> objective);


    public boolean doSearch(TestChromosome test, Set<Integer> statements,
                            LocalSearchObjective<TestChromosome> objective) {
        boolean success = false;
        for (Integer statement : statements) {
            if (doSearch(test, statement, objective))
                success = true;
        }

        return success;
    }

    /**
     * If the position of the statement on which the local search was performed
     * has changed, then we need to tell this to the outside world
     *
     * @return
     */
    public int getPositionDelta() {
        return 0;
    }

    public static StatementLocalSearch getLocalSearchFor(Statement statement) {

        StatementLocalSearch search = null;
        if (statement instanceof NullStatement) {
            if (Properties.LOCAL_SEARCH_REFERENCES == false)
                return null;

            search = new ReferenceLocalSearch();
        } else if (statement instanceof PrimitiveStatement<?>) {
            Class<?> type = statement.getReturnValue().getVariableClass();
            if (type.equals(String.class)) {
                if (Properties.LOCAL_SEARCH_STRINGS)
                    search = new StringAVMLocalSearch();
            } else {
                if (Properties.LOCAL_SEARCH_PRIMITIVES == false)
                    return null;

                if (type.equals(Integer.class) || type.equals(int.class)) {
                    search = new IntegerLocalSearch<Integer>();
                } else if (type.equals(Byte.class) || type.equals(byte.class)) {
                    search = new IntegerLocalSearch<Byte>();
                } else if (type.equals(Short.class) || type.equals(short.class)) {
                    search = new IntegerLocalSearch<Short>();
                } else if (type.equals(Long.class) || type.equals(long.class)) {
                    search = new IntegerLocalSearch<Long>();
                } else if (type.equals(Character.class) || type.equals(char.class)) {
                    search = new IntegerLocalSearch<Character>();
                } else if (type.equals(Float.class) || type.equals(float.class)) {
                    search = new FloatLocalSearch<Float>();
                } else if (type.equals(Double.class) || type.equals(double.class)) {
                    search = new FloatLocalSearch<Double>();
                } else if (type.equals(Boolean.class)) {
                    search = new BooleanLocalSearch();
                } else if (statement instanceof EnumPrimitiveStatement) {
                    search = new EnumLocalSearch();
                }
            }
        } else if (statement instanceof ArrayStatement) {
            if (Properties.LOCAL_SEARCH_ARRAYS == false)
                return null;

            search = new ArrayLocalSearch();
        } else if (statement instanceof MethodStatement) {
            if (Properties.LOCAL_SEARCH_REFERENCES == false)
                return null;

            search = new ReferenceLocalSearch();
        } else if (statement instanceof ConstructorStatement) {
            if (Properties.LOCAL_SEARCH_REFERENCES == false)
                return null;

            search = new ReferenceLocalSearch();
        } else if (statement instanceof FieldStatement) {
            if (Properties.LOCAL_SEARCH_REFERENCES == false)
                return null;

            search = new ReferenceLocalSearch();
        }
        return search;
    }
}
