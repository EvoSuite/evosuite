package org.evosuite.testcase.environmentdata;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.PrimitiveStatement;

import java.lang.reflect.Type;

/**
 * Created by arcuri on 12/12/14.
 */
public abstract class EnvironmentDataStatement<T> extends PrimitiveStatement<T> {

	private static final long serialVersionUID = -348689954506405873L;

	protected EnvironmentDataStatement(TestCase tc, Type clazz, T value) {
        super(tc,clazz,value);
    }

    public abstract String getTestCode(String varName);
}
