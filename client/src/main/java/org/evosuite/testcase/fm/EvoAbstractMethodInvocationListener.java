package org.evosuite.testcase.fm;

import org.evosuite.utils.generic.GenericClass;

import java.lang.reflect.Type;

public class EvoAbstractMethodInvocationListener extends EvoInvocationListener {
    private static final long serialVersionUID = -8389668702984369978L;

    public EvoAbstractMethodInvocationListener(Type retvalType) {
        super(retvalType);
    }

    public EvoAbstractMethodInvocationListener(GenericClass retvalType) {
        super(retvalType);
    }

    @Override
    protected boolean onlyMockAbstractMethods() {
        return true;
    }
}
