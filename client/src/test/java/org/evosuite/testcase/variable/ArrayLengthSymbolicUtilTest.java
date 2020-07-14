package org.evosuite.testcase.variable;

import org.evosuite.Properties;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArrayLengthSymbolicUtilTest {

  public static final String ARRAY_REFERENCE_TEST_NAME = "testName";

  @Test
  public void buildArraySymbolicLengthExpression() {
    ArraySymbolicLengthName arraySymbolicLengthName = mock(ArraySymbolicLengthName.class);
    when(arraySymbolicLengthName.getSymbolicName()).thenReturn("test_var");
    IntegerValue integerValue;

    //When Arrays enabled, it should create an IntegerVariable
    Properties.IS_DSE_ARRAYS_SUPPORT_ENABLED = true;
    integerValue = ArrayLengthSymbolicUtil.buildArraySymbolicLengthExpression(0, arraySymbolicLengthName);
    assertTrue(integerValue.containsSymbolicVariable());

    //When Arrays disabled, it should create an IntegerConstant so it won't propagate later on
    Properties.IS_DSE_ARRAYS_SUPPORT_ENABLED = false;
    integerValue = ArrayLengthSymbolicUtil.buildArraySymbolicLengthExpression(0, arraySymbolicLengthName);
    assertFalse(integerValue.containsSymbolicVariable());
  }

  @Test
  public void isSymbolicArraysSupportEnabled() {
    Properties.IS_DSE_ARRAYS_SUPPORT_ENABLED = true;
    assertTrue(ArrayLengthSymbolicUtil.isSymbolicArraysSupportEnabled());

    Properties.IS_DSE_ARRAYS_SUPPORT_ENABLED = false;
    assertFalse(ArrayLengthSymbolicUtil.isSymbolicArraysSupportEnabled());
  }
}