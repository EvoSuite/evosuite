package org.evosuite.testcase;

import org.evosuite.testcase.statements.*;

import java.util.LinkedList;
import java.util.List;

public class CalledMethodsTestVisitor extends TestVisitor {
    private final List<EntityWithParametersStatement> calledMethods = new LinkedList<>();
    private final int index;

    public List<EntityWithParametersStatement> getCalledMethods() {
        return calledMethods;
    }

    public CalledMethodsTestVisitor(int index) {
        this.index = index;
    }

    @Override
    public void visitTestCase(TestCase test) {
        // empty
    }

    @Override
    public void visitPrimitiveStatement(PrimitiveStatement<?> statement) {
        // empty
    }

    @Override
    public void visitFieldStatement(FieldStatement statement) {
        // empty
    }

    @Override
    public void visitMethodStatement(MethodStatement statement) {
        if (statement.getPosition() < index) {
            calledMethods.add(statement);
        }
    }

    @Override
    public void visitConstructorStatement(ConstructorStatement statement) {
        if (statement.getPosition() < index) {
            calledMethods.add(statement);
        }
    }

    @Override
    public void visitArrayStatement(ArrayStatement statement) {
        // empty
    }

    @Override
    public void visitAssignmentStatement(AssignmentStatement statement) {
        // empty
    }

    @Override
    public void visitNullStatement(NullStatement statement) {
        // empty
    }

    @Override
    public void visitPrimitiveExpression(PrimitiveExpression primitiveExpression) {
        // empty
    }

    @Override
    public void visitFunctionalMockStatement(FunctionalMockStatement functionalMockStatement) {
        // empty
    }
}
