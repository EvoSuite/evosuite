package org.evosuite.testcase.environmentdata;

import org.evosuite.runtime.testdata.EvoSuiteFile;
import org.evosuite.testcase.PrimitiveStatement;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.VariableReference;
import org.evosuite.utils.GenericClass;

import java.lang.reflect.Type;

/**
 * Created by arcuri on 12/12/14.
 */
public abstract class EnvironmentDataStatement<T> extends PrimitiveStatement<T> {

    protected EnvironmentDataStatement(TestCase tc, Type clazz, T value) {
        super(tc,clazz,value);
    }

    public abstract String getTestCode(String varName);
}
