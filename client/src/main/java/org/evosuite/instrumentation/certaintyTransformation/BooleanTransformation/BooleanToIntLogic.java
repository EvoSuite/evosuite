package org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation;

/**
 * Provides boolean logic for boolean values represented as ints
 */
public interface BooleanToIntLogic {


    /**
     * Logical and operation on the int array currently on the top of the operand stack.
     *
     * Operand stack: {..[I} -> {..I}
     */
    void callLogicalOr();

    void callBinaryLogicalOr();

    /**
     * Logical and operation on the int array currently on the top of the operand stack.
     *
     * Operand stack: {..[I} -> {..I}
     */
    void callLogicalAnd();

    void callBinaryLogicalAnd();

    /**
     * Negates the int currently on the top of the stack.
     *
     * Operand stacks is not changed.
     */
    void callLogicalNeg();

    /**
     * Inserts logic of dcmpg.
     */
    void callDCMPG();

    /**
     * Inserts logic of dcmpl
     */
    void callDCMPL();

    void callFCMPL();

    void callFCMPG();

    void callIfCmpEq();

    void callIfCmpNe();

    void callIfCmpGt();

    void callIfCmpGe();

    void callIfCmpLe();

    void callIfCmpLt();

    void callIfIntCmpEq();

    void callIfIntCmpNe();

    void callIfIntCmpGt();

    void callIfIntCmpGe();

    void callIfIntCmpLe();

    void callIfIntCmpLt();

    void callIfACmpEq();

    void callIfACmpNe();

    void callUpdate();

    void callUpdateWithReassignmentValue();

    void callFromInt();

    void callToInt();

    void callIfNull();

    void callIfNonNull();

    void callBooleanEq();

    void callBooleanNe();

    void enforcePositiveConstraint();

    void enforceNegativeConstraint();
}
