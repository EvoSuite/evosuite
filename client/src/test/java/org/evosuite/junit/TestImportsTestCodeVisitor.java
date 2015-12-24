package org.evosuite.junit;

import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.ImportsTestCodeVisitor;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCodeVisitor;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by gordon on 24/12/2015.
 */
public class TestImportsTestCodeVisitor {

    @Test
    public void testBasicImports() throws NoSuchMethodException {
        TestCase test = new DefaultTestCase();
        GenericConstructor gc = new GenericConstructor(Object.class.getConstructor(), Object.class);
        VariableReference callee = test.addStatement(new ConstructorStatement(test, gc, new ArrayList<>()));
        GenericMethod gm = new GenericMethod(Object.class.getMethod("toString"), Object.class);
        test.addStatement(new MethodStatement(test, gm, callee, new ArrayList<>()));

        ImportsTestCodeVisitor visitor = new ImportsTestCodeVisitor();
        test.accept(visitor);
        assertTrue(visitor.getImports().contains(Object.class));
        assertEquals(1, visitor.getImports().size());
    }

    @Test
    public void testImportException() throws NoSuchMethodException {
        TestCase test = new DefaultTestCase();
        GenericConstructor gc = new GenericConstructor(Object.class.getConstructor(), Object.class);
        VariableReference callee = test.addStatement(new ConstructorStatement(test, gc, new ArrayList<>()));
        GenericMethod gm = new GenericMethod(Object.class.getMethod("toString"), Object.class);
        test.addStatement(new MethodStatement(test, gm, callee, new ArrayList<>()));

        ImportsTestCodeVisitor visitor = new ImportsTestCodeVisitor();
        Map<Integer, Throwable> exceptionMap = new HashMap<>();
        exceptionMap.put(1, new NullPointerException());
        visitor.setExceptions(exceptionMap);
        test.accept(visitor);
        assertTrue(visitor.getImports().contains(Object.class));
        assertTrue(visitor.getImports().contains(NullPointerException.class));
        assertEquals(2, visitor.getImports().size());
    }

}
