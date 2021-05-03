package org.evosuite.instrumentation.certainty_transformation.boolean_transformation;

import org.objectweb.asm.MethodVisitor;

import static org.evosuite.instrumentation.certainty_transformation.boolean_transformation.BooleanToIntTransformer.*;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class StaticMethodsLogic implements BooleanToIntLogic {

    private final MethodVisitor mv;

    public StaticMethodsLogic(MethodVisitor mv){
        this.mv = mv;
    }

    private void callWithNameAndDescriptor(UtilFunctionNameAndDescriptor nameAndDescriptor){
        mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_TO_INT_UTIL_NAME, nameAndDescriptor.getName(),
                nameAndDescriptor.getDescriptor(),false);
    }

    /**
     * Logical and operation on the int array currently on the top of the operand stack.
     *
     * Operand stack: {..[I} -> {..I}
     */
    @Override
    public void callLogicalOr(){
        callWithNameAndDescriptor(LOGICAL_OR_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callBinaryLogicalOr() {
        callWithNameAndDescriptor(BIN_LOGICAL_OR_NAME_AND_DESCRIPTOR);
    }

    /**
     * Logical and operation on the int array currently on the top of the operand stack.
     *
     * Operand stack: {..[I} -> {..I}
     */
    @Override
    public void callLogicalAnd(){
        callWithNameAndDescriptor(LOGICAL_AND_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callBinaryLogicalAnd() {
        callWithNameAndDescriptor(BIN_LOGICAL_AND_NAME_AND_DESCRIPTOR);
    }

    /**
     * Negates the int currently on the top of the stack.
     *
     * Operand stacks is not changed.
     */
    @Override
    public void callLogicalNeg(){
        callWithNameAndDescriptor(NEG_NAME_AND_DESCRIPTOR);
    }

    /**
     * Inserts logic of dcmpg.
     */
    @Override
    public void callDCMPG(){
        callWithNameAndDescriptor(DCMPG_NAME_AND_DESCRIPTOR);
    }

    /**
     * Inserts logic of dcmpl
     */
    @Override
    public void callDCMPL() {
        callWithNameAndDescriptor(DCMPL_NAME_AND_DESCRIPTOR);
    }

    /**
     * Inserts logic of fcmpl
     */
    @Override
    public void callFCMPL(){
        callWithNameAndDescriptor(FCMPL_NAME_AND_DESCRIPTOR);
    }

    /**
     * Inserts logic of fcmpg
     */
    @Override
    public void callFCMPG(){
        callWithNameAndDescriptor(FCMPG_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callIfCmpEq() {
        callWithNameAndDescriptor(IFEQ_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callIfCmpNe() {
        callWithNameAndDescriptor(IFNE_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callIfCmpGt() {
        callWithNameAndDescriptor(IFGT_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callIfCmpGe() {
        callWithNameAndDescriptor(IFGE_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callIfCmpLe() {
        callWithNameAndDescriptor(IFLE_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callIfCmpLt() {
        callWithNameAndDescriptor(IFLT_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callIfIntCmpEq() {
        callWithNameAndDescriptor(INT_CMP_EQ_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callIfIntCmpNe() {
        callWithNameAndDescriptor(INT_CMP_NE_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callIfIntCmpGt() {
        callWithNameAndDescriptor(INT_CMP_GT_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callIfIntCmpGe() {
        callWithNameAndDescriptor(INT_CMP_GE_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callIfIntCmpLe() {
        callWithNameAndDescriptor(INT_CMP_LE_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callIfIntCmpLt() {
        callWithNameAndDescriptor(INT_CMP_LT_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callIfACmpEq() {
        callWithNameAndDescriptor(A_CMP_EQ_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callIfACmpNe() {
        callWithNameAndDescriptor(A_CMP_NE_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callUpdate(){
        callWithNameAndDescriptor(UPDATE_II_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callUpdateWithReassignmentValue() {
        callWithNameAndDescriptor(UPDATE_III_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callFromInt() {
        callWithNameAndDescriptor(FROM_INT_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callToInt() {
        callWithNameAndDescriptor(TO_INT_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callIfNull() {
        callWithNameAndDescriptor(IF_NULL_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callIfNonNull() {
        callWithNameAndDescriptor(IF_NON_NULL_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callBooleanEq() {
        callWithNameAndDescriptor(CMP_BOOLEAN_EQ_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void callBooleanNe() {
        callWithNameAndDescriptor(CMP_BOOLEAN_NE_NAME_AND_DESCRIPTOR);
    }

    @Override
    public void enforcePositiveConstraint() {
        callWithNameAndDescriptor(ENFORCE_POSITIVE_CONSTRAINT);
    }

    @Override
    public void enforceNegativeConstraint() {
        callWithNameAndDescriptor(ENFORCE_NEGATIVE_CONSTRAINT);
    }

}
