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
package org.evosuite.symbolic.expr.ref;

import org.evosuite.symbolic.expr.ExpressionVisitor;
import org.evosuite.symbolic.expr.Variable;
import org.objectweb.asm.Type;

import java.util.Set;

public final class GetFieldExpression extends ReferenceExpression {


    private static final long serialVersionUID = 4517401722564017247L;

    private final ReferenceExpression receiverExpr;

    private final String fieldName;

    /**
     * Creates a symbolic expression of the form "expr.F" where expr is the
     * <code>ReferenceExpression</code>, F is the string <code>fieldName</code>
     * and the concrete value of the symbolic expression "expr.F" is
     * <code>concreteValue</code>
     *
     * @param receiverExpr  the symbolic expression of the receiver object
     * @param fieldName     the field name
     * @param concreteValue the concrete object for the symbolic expression expr.F
     */
    public GetFieldExpression(Type objectType, int instanceId, ReferenceExpression receiverExpr, String fieldName,
                              Object concreteValue) {
        super(objectType, instanceId, 1 + receiverExpr.getSize(), receiverExpr.containsSymbolicVariable());
        this.receiverExpr = receiverExpr;
        this.fieldName = fieldName;
        this.initializeReference(concreteValue);
    }

    /**
     * Returns the set of all the variables in the receiver expression (the expr
     * in expr.F)
     *
     * @return
     */
    @Override
    public Set<Variable<?>> getVariables() {
        return this.receiverExpr.getVariables();
    }

    @Override
    public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
        return v.visit(this, arg);
    }

    /**
     * Returns the receiver expression (the expr in expr.F)
     *
     * @return
     */
    public ReferenceExpression getReceiverExpr() {
        return receiverExpr;
    }

    /**
     * Returns the field name (the F in expr.F)
     *
     * @return
     */
    public String getFieldName() {
        return fieldName;
    }

}
