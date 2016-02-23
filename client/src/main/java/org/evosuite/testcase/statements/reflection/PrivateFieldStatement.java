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
package org.evosuite.testcase.statements.reflection;

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.runtime.PrivateAccess;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericMethod;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Statement representing the setting of a private field, which is done through reflection in the
 * generated JUnit tests.
 *
 * Created by foo on 20/02/15.
 */
public class PrivateFieldStatement extends MethodStatement {

	private static final long serialVersionUID = 5152490398872348493L;

	private static Method setVariable;

    private transient Class<?> ownerClass;

    private String className;

    private String fieldName;

    private boolean isStaticField = false;

    static {
        try {
            //Class<T> klass, T instance, String fieldName, Object value
            setVariable = PrivateAccess.class.getMethod("setVariable",Class.class, Object.class,String.class,Object.class);
        } catch (NoSuchMethodException e) {
            //should never happen
            throw new RuntimeException("EvoSuite bug",e);
        }
    }

    public PrivateFieldStatement(TestCase tc, Class<?> klass , String fieldName, VariableReference callee, VariableReference param)
            throws NoSuchFieldException, IllegalArgumentException, ConstructionFailedException {
        super(
                tc,
                new GenericMethod(setVariable, PrivateAccess.class),
                null, //it is static
                Arrays.asList(  // setVariable(Class<T> klass, T instance, String fieldName, Object value)
                        new ConstantValue(tc, new GenericClass(Class.class), klass),  // Class<T> klass
                        //new ClassPrimitiveStatement(tc,klass).getReturnValue(),  // Class<T> klass
                        callee, // T instance
                        new ConstantValue(tc, new GenericClass(String.class), fieldName),  // String fieldName
                        param // Object value
                )
        );
        this.className = klass.getCanonicalName();
        this.fieldName = fieldName;
        this.ownerClass = klass;

        List<GenericClass> parameterTypes = new ArrayList<>();
        parameterTypes.add(new GenericClass(klass));
        this.method.setTypeParameters(parameterTypes);
        determineIfFieldIsStatic(klass, fieldName);
    }

    private void determineIfFieldIsStatic(Class<?> klass, String fieldName) {
        try {
            Field f = klass.getDeclaredField(fieldName);
            if (Modifier.isStatic(f.getModifiers()))
                isStaticField = true;
        } catch(NoSuchFieldException f) {
            // This should never happen
            throw new RuntimeException("EvoSuite bug", f);
        }
    }

    public boolean isStaticField() {
        return isStaticField;
    }

    public String getOwnerClassName() {
        return className;
    }

    @Override
    public Statement copy(TestCase newTestCase, int offset) {
        try {
            PrivateFieldStatement pf;
            VariableReference owner = parameters.get(1).copy(newTestCase, offset);
            VariableReference value = parameters.get(3).copy(newTestCase, offset);

            pf = new PrivateFieldStatement(newTestCase, ownerClass, fieldName, owner, value);

            return pf;
        } catch(NoSuchFieldException | ConstructionFailedException e) {
            throw new RuntimeException("EvoSuite bug", e);
        }
    }

    @Override
    public boolean mutate(TestCase test, TestFactory factory) {
        // just for simplicity
        return false;
        //return super.mutate(test,factory); //tricky, as should do some restrictions
    }
    
	@Override
	public boolean isReflectionStatement() {
		return true;
	}

    @Override
    public Throwable execute(Scope scope, PrintStream out) throws InvocationTargetException, IllegalArgumentException, IllegalAccessException, InstantiationException {
        if(!isStaticField) {
            try {
                Object receiver = parameters.get(1).getObject(scope);
                if (receiver == null)
                    return new CodeUnderTestException(new NullPointerException());
            } catch (CodeUnderTestException e) {
                return e;
            }

        }
        return super.execute(scope, out);
    }
}
