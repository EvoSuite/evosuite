package org.evosuite.symbolic.expr;

import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.token.NewTokenizerExpr;
import org.evosuite.symbolic.expr.token.StringNextTokenExpr;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class StringNextTokenExprTest {

    @Test
    public void testEquals() {
        StringConstant helloWorldStr = new StringConstant("Hello World");
        StringConstant delimiterStr = new StringConstant(" ");
        NewTokenizerExpr newTokenizerExpr = new NewTokenizerExpr(helloWorldStr, delimiterStr);
        StringNextTokenExpr left = new StringNextTokenExpr(newTokenizerExpr, helloWorldStr.getConcreteValue());
        StringNextTokenExpr right = new StringNextTokenExpr(newTokenizerExpr, helloWorldStr.getConcreteValue());
        assertEquals(left, right);
    }

    @Test
    public void testNotEquals() {
        StringConstant helloWorldStr = new StringConstant("Hello World");
        StringConstant goodByeWorldStr = new StringConstant("Goodbye World");
        StringConstant delimiterStr = new StringConstant(" ");
        NewTokenizerExpr leftNewTokenizerExpr = new NewTokenizerExpr(helloWorldStr, delimiterStr);
        StringNextTokenExpr left = new StringNextTokenExpr(leftNewTokenizerExpr, helloWorldStr.getConcreteValue());

        NewTokenizerExpr rightNewTokenizerExpr = new NewTokenizerExpr(goodByeWorldStr, delimiterStr);
        StringNextTokenExpr right = new StringNextTokenExpr(rightNewTokenizerExpr, helloWorldStr.getConcreteValue());
        assertNotEquals(left, right);
    }
}
