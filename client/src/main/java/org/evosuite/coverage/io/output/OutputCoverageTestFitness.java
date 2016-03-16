/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.coverage.io.output;

import static org.evosuite.coverage.io.IOCoverageConstants.*;

import org.evosuite.Properties;
import org.evosuite.assertion.Inspector;
import org.evosuite.assertion.InspectorManager;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.MethodStatement;
import org.hibernate.result.Output;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Jose Miguel Rojas
 */
public class OutputCoverageTestFitness extends TestFitnessFunction {

    private static final long serialVersionUID = 1383064944691491355L;

    protected static final Logger logger = LoggerFactory.getLogger(OutputCoverageTestFitness.class);

    /**
     * Target goal
     */
    private final OutputCoverageGoal goal;

    /**
     * Constructor - fitness is specific to a method
     *
     * @param goal the coverage goal
     * @throws IllegalArgumentException
     */
    public OutputCoverageTestFitness(OutputCoverageGoal goal) throws IllegalArgumentException {
        if (goal == null) {
            throw new IllegalArgumentException("goal cannot be null");
        }
        this.goal = goal;
    }

    /**
     * <p>
     * getClassName
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getClassName() {
        return goal.getClassName();
    }

    /**
     * <p>
     * getMethod
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMethod() {
        return goal.getMethodName();
    }

    /**
     * <p>
     * getValue
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public Type getType() {
        return goal.getType();
    }

    /**
     * <p>
     * getValue
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getValueDescriptor() {
        return goal.getValueDescriptor();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Calculate fitness
     *
     * @param individual a {@link org.evosuite.testcase.ExecutableChromosome} object.
     * @param result     a {@link org.evosuite.testcase.execution.ExecutionResult} object.
     * @return a double.
     */
    @Override
    public double getFitness(TestChromosome individual, ExecutionResult result) {
        double fitness = 1.0;

        for(Set<OutputCoverageGoal> coveredGoals : result.getOutputGoals().values()) {
            if(coveredGoals.contains(goal)) {
                fitness = 0.0;
                break;
            }
        }
        updateIndividual(this, individual, fitness);
        return fitness;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[Output]: "+goal.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int iConst = 13;
        return 51 * iConst + goal.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OutputCoverageTestFitness other = (OutputCoverageTestFitness) obj;
        return this.goal.equals(other.goal);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
     */
    @Override
    public int compareTo(TestFitnessFunction other) {
        if (other instanceof OutputCoverageTestFitness) {
            OutputCoverageTestFitness otherOutputFitness = (OutputCoverageTestFitness) other;
            return goal.compareTo(otherOutputFitness.goal);
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

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#getTargetMethod()
     */
    @Override
    public String getTargetMethod() {
        return getMethod();
    }

    /*
     * TODO: Move somewhere else into a utility class
     */
    private static final Class<?> getClassForName(String type)
    {
        try
        {
            if( type.equals("boolean"))
            {
                return Boolean.TYPE;
            }
            else if(type.equals("byte"))
            {
                return Byte.TYPE;
            }
            else if( type.equals("char"))
            {
                return Character.TYPE;
            }
            else if( type.equals("double"))
            {
                return Double.TYPE;
            }
            else if(type.equals("float"))
            {
                return Float.TYPE;
            }
            else if(type.equals("int"))
            {
                return Integer.TYPE;
            }
            else if( type.equals("long"))
            {
                return Long.TYPE;
            }
            else if(type.equals("short"))
            {
                return Short.TYPE;
            }
            else if(type.equals("String") ||type.equals("Boolean") || type.equals("Short") ||type.equals("Long") ||
                    type.equals("Integer") || type.equals("Float") || type.equals("Double") ||type.equals("Byte") ||
                    type.equals("Character") )
            {
                return Class.forName("java.lang." + type);
            }

//			if(type.endsWith(";") && ! type.startsWith("["))
//			{
//				type = type.replaceFirst("L", "");
//				type = type.replace(";", "");
//			}

            if(type.endsWith("[]"))
            {
                type = type.replace("[]", "");
                return Class.forName("[L" + type + ";");
            }
            else
            {
                return Class.forName(type);
            }
        }
        catch (final ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
}