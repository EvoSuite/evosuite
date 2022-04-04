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
package org.evosuite.symbolic.vm;

import org.evosuite.dse.AbstractVM;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.constraint.IntegerConstraint;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;

import java.util.Vector;

/**
 * Java byte codes we group together as "jump-related"
 * <p>
 * Explicit intra-procedural control transfer, conditional or unconditional:
 * Goto, jump, etc.
 *
 * @author csallner@uta.edu (Christoph Csallner)
 */
public final class JumpVM extends AbstractVM {
    private final SymbolicEnvironment env;

    private final PathConditionCollector pc;

    /**
     * Constructor
     */
    public JumpVM(SymbolicEnvironment env, PathConditionCollector pc) {
        this.env = env;
        this.pc = pc;
    }

    /**
     * (p == 0) is just ((p == right) with right==0). (p != 0) is just (not (p ==
     * 0)).
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#ifcond
     */
    @Override
    public void IFEQ(String className, String methName, int branchIndex, int p) {
        env.topFrame().operandStack.pushBv32(ExpressionFactory.ICONST_0); // right
        // hand
        // side
        // argument
        // of
        // EQ comparison
        IF_ICMPEQ(className, methName, branchIndex, p, 0); // use general
        // implementation
    }

    @Override
    public void IFNE(String className, String methName, int branchIndex, int p) {
        IFEQ(className, methName, branchIndex, p);
    }

    /**
     * (p < 0) is just ((p < right) with right==0). (p >= 0) is just (not (p < 0)).
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#ifcond
     */

    @Override
    public void IFLT(String className, String methName, int branchIndex, int p) {
        env.topFrame().operandStack.pushBv32(ExpressionFactory.ICONST_0); // right
        // hand
        // side
        // argument
        // of
        // LT comparison
        IF_ICMPLT(className, methName, branchIndex, p, 0); // use general
        // implementation
    }

    @Override
    public void IFGE(String className, String methName, int branchIndex, int p) {
        IFLT(className, methName, branchIndex, p);
    }

    /**
     * (p > 0) is just (0 < p). (0 < p) is just ((left < p) with left==0).
     * <p>
     * (p <= 0) is just (0 >= p). (0 >= p) is just (not (0 < p )).
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#ifcond
     */
    @Override
    public void IFGT(String className, String methName, int branchIndex, int p) {
        IntegerValue rightBv = env.topFrame().operandStack.popBv32(); // symbolic
        // version
        // of
        // p
        env.topFrame().operandStack.pushBv32(ExpressionFactory.ICONST_0); // left
        // hand
        // side
        // argument
        // of
        // LT comparison
        env.topFrame().operandStack.pushBv32(rightBv); // right hand side
        // argument of LT
        // comparison
        IF_ICMPLT(className, methName, branchIndex, 0, p);
    }

    @Override
    public void IFLE(String className, String methName, int branchIndex, int p) {
        IFGT(className, methName, branchIndex, p);
    }

    /**
     * (left == right). (left != right) is just (not (left == right)).
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#if_icmpcond
     */
    @Override
    public void IF_ICMPEQ(String className, String methName, int branchIndex, int left, int right) {
        IntegerValue rightOp = env.topFrame().operandStack.popBv32();
        IntegerValue leftOp = env.topFrame().operandStack.popBv32();

        IntegerConstraint cnstr;
        boolean isTrueBranch;
        if (left == right) {
            isTrueBranch = true;
            cnstr = ConstraintFactory.eq(leftOp, rightOp); // "True" branch
        } else {
            isTrueBranch = false;
            cnstr = ConstraintFactory.neq(leftOp, rightOp); // "False" branch
        }
        // add branch condition iif local constraint is concrete
        if (cnstr.getLeftOperand().containsSymbolicVariable() || cnstr.getRightOperand().containsSymbolicVariable())
            pc.appendIfBranchCondition(className, methName, branchIndex, isTrueBranch, cnstr);
    }

    @Override
    public void IF_ICMPNE(String className, String methName, int branchIndex, int left, int right) {
        IF_ICMPEQ(className, methName, branchIndex, left, right);
    }

    /**
     * (left < right). (left >= right) is just (not (left < right)).
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#if_icmpcond
     */
    @Override
    public void IF_ICMPLT(String className, String methName, int branchIndex, int left, int right) {
        IntegerValue rightBv = env.topFrame().operandStack.popBv32();
        IntegerValue leftBv = env.topFrame().operandStack.popBv32();

        IntegerConstraint cnstr;
        boolean isTrueBranch;
        if (left < right) {
            cnstr = ConstraintFactory.lt(leftBv, rightBv); // True Branch
            isTrueBranch = true;
        } else {
            cnstr = ConstraintFactory.gte(leftBv, rightBv); // False branch
            isTrueBranch = false;
        }

        // add branch condition iif local constraint is concrete
        if (cnstr.getLeftOperand().containsSymbolicVariable() || cnstr.getRightOperand().containsSymbolicVariable())
            pc.appendIfBranchCondition(className, methName, branchIndex, isTrueBranch, cnstr);
    }

    @Override
    public void IF_ICMPGE(String className, String methName, int branchIndex, int left, int right) {
        IF_ICMPLT(className, methName, branchIndex, left, right);
    }

    /**
     * (left > right) is just (right < left). (left <= right) is just (not (left >
     * right)).
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#if_icmpcond
     */
    @Override
    public void IF_ICMPGT(String className, String methName, int branchIndex, int left, int right) {
        // FIXME: Replace following five instructions with SWAP

        IntegerValue rightBv = env.topFrame().operandStack.popBv32();
        IntegerValue leftBv = env.topFrame().operandStack.popBv32();

        env.topFrame().operandStack.pushBv32(rightBv);
        env.topFrame().operandStack.pushBv32(leftBv);

        IF_ICMPLT(className, methName, branchIndex, right, left);
    }

    @Override
    public void IF_ICMPLE(String className, String methName, int branchIndex, int left, int right) {
        IF_ICMPGT(className, methName, branchIndex, left, right);
    }

    /* Reference equality */

    /**
     * (left == right). (left != right) is just (not (left == right)).
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#if_acmpcond
     */
    @Override
    public void IF_ACMPEQ(String className, String methName, int branchIndex, Object conc_left, Object conc_right) {
        ReferenceExpression right_ref = env.topFrame().operandStack.popRef();
        ReferenceExpression left_ref = env.topFrame().operandStack.popRef();

        env.heap.initializeReference(conc_left, left_ref);
        env.heap.initializeReference(conc_right, right_ref);
    }

    @Override
    public void IF_ACMPNE(String className, String methName, int branchIndex, Object left, Object right) {
        IF_ACMPEQ(className, methName, branchIndex, left, right);
    }

    /**
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#ifnull
     */
    @Override
    public void IFNULL(String className, String methName, int branchIndex, Object p) {
        // right hand side argument of EQ
        env.topFrame().operandStack.pushNullRef();

        // comparison
        IF_ACMPEQ(className, methName, branchIndex, p, null); // use general
        // implementation
    }

    @Override
    public void IFNONNULL(String className, String methName, int branchIndex, Object p) {
        IFNULL(className, methName, branchIndex, p);
    }

    /* switch */

    /**
     * <b>switch</b> statement that has consecutively numbered cases. I.e., there
     * are no holes (missing targets) between the lowest and highest target. Hence
     * the compiler does not need to translate the case values to offsets.
     *
     * <p>
     * We treat the switch statement as a nested if in order lowest to highest index
     * as follows.
     *
     * <pre>
     * if (x==lowest) ..
     * else
     * {
     *   if (x==second_lowest) ..
     *   else
     *   {
     *     if ..
     * </pre>
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc14.html#tableswitch
     */
    @Override
    public void TABLESWITCH(String className, String methName, int branchIndex, int goalConcrete, int minGoalValue,
                            int maxGoalValue) {
        final IntegerValue value = env.topFrame().operandStack.popBv32();

        Vector<IntegerConstraint> constraints = new Vector<>();

        // process each time in the same order: lowest to highest target
        boolean isDefaultGoal = true;
        for (int goalValue = minGoalValue; goalValue <= maxGoalValue; goalValue++) {
            IntegerConstant literal = ExpressionFactory.buildNewIntegerConstant(goalValue);
            IntegerConstraint constraint;
            if (goalConcrete == goalValue) {
                constraint = ConstraintFactory.eq(value, literal);
                constraints.add(constraint);
                isDefaultGoal = false;
                break;
            } else {
                constraint = ConstraintFactory.neq(value, literal);
                constraints.add(constraint);
            }
        }

        for (int i = 0; i < constraints.size() - 1; i++) {
            IntegerConstraint cnstrt = constraints.get(i);
            if (cnstrt.getLeftOperand().containsSymbolicVariable()
                    || cnstrt.getRightOperand().containsSymbolicVariable())
                pc.appendSupportingConstraint(cnstrt);
        }
        // add branch condition iif local constraint is concrete
        IntegerConstraint cnstr = constraints.get(constraints.size() - 1);
        if (cnstr.getLeftOperand().containsSymbolicVariable() || cnstr.getRightOperand().containsSymbolicVariable()) {
            if (isDefaultGoal) {
                pc.appendDefaultSwitchBranchCondition(className, methName, branchIndex, cnstr);
            } else {
                pc.appendSwitchBranchCondition(className, methName, branchIndex, cnstr, goalConcrete);

            }
        }
    }

    /**
     * <b>switch</b> statement whose cases may not be numbered consecutively. I.e.,
     * there may be holes (missing targets) between the lowest and highest target.
     *
     * <p>
     * Very similar to {@link #TABLESWITCH}. The main difference is that here we are
     * given a list of explicit goals. Tableswitch defines its goals implicitly,
     * between min and max.
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc8.html#lookupswitch
     */
    @Override
    public void LOOKUPSWITCH(String className, String methName, int branchIndex, int goalConcrete,
                             int[] targetsConcrete) {
        // TODO: target array remains constant. Do we really need to create and
        // pass
        // this array every time as a paremeter?
        final IntegerValue goal = env.topFrame().operandStack.popBv32();

        Vector<IntegerConstraint> constraints = new Vector<>();

        boolean isDefaultGoal = true;
        for (int targetConcrete : targetsConcrete) {
            IntegerConstant integerConstant = ExpressionFactory.buildNewIntegerConstant(targetConcrete);
            IntegerConstraint constraint;
            if (goalConcrete == targetConcrete) {
                constraint = ConstraintFactory.eq(goal, integerConstant);
                constraints.add(constraint);
                isDefaultGoal = false;
                break;
            } else {
                constraint = ConstraintFactory.neq(goal, integerConstant);
                constraints.add(constraint);
            }
        }

        for (int i = 0; i < constraints.size() - 1; i++) {
            IntegerConstraint cnstrnt = constraints.get(i);
            if (cnstrnt.getLeftOperand().containsSymbolicVariable()
                    || cnstrnt.getRightOperand().containsSymbolicVariable())
                pc.appendSupportingConstraint(cnstrnt);
        }

        // add branch condition iif local constraint is concrete
        IntegerConstraint cnstr = constraints.get(constraints.size() - 1);
        if (cnstr.getLeftOperand().containsSymbolicVariable() || cnstr.getRightOperand().containsSymbolicVariable()) {
            if (isDefaultGoal) {
                pc.appendDefaultSwitchBranchCondition(className, methName, branchIndex, cnstr);
            } else {
                pc.appendSwitchBranchCondition(className, methName, branchIndex, cnstr, goalConcrete);

            }
        }

    }

    /**
     * Unconditional jump
     * <p>
     * No change to operand stack or local variables.
     * <p>
     * ... ==> ...
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc5.html#goto
     */
    @Override
    public void GOTO() {
        /* Concrete execution will take us to the next bytecode. */
    }

    @Override
    public void GOTO_W() {
        GOTO();
    }

    /**
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc
     * .html#athrow
     */
    @Override
    public void ATHROW(Throwable throwable) {
        /**
         * This instructions pops the operand stack and throws an exception. We only
         * update the operand stack since exceptions are not explicitly modelled in the
         * VM.
         */
        this.env.topFrame().operandStack.popRef();
        /* Concrete execution will take us to the next bytecode. */
    }

    /* Subroutine jump */

    /**
     * Unconditional jump (to sub-routine, finally block)
     * <p>
     * Pushes address onto operand stack, which finally block will astore.
     * <p>
     * ... ==> ..., address
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc7.html#jsr
     */
    @Override
    public void JSR() {
        throw new UnsupportedOperationException("Implement ME!");
    }

    @Override
    public void JSR_W() {
        JSR();
    }

    /**
     * Return from sub-routine
     * <p>
     * Operand stack and local variables remain unchanged.
     * <p>
     * ... ==> ...
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc12.html#ret
     */
    @Override
    public void RET() {
        /* Concrete execution will take us to the next bytecode. */
    }
}