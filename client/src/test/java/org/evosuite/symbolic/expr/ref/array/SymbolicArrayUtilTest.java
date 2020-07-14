package org.evosuite.symbolic.expr.ref.array;

import org.junit.Test;

import static org.junit.Assert.*;

public class SymbolicArrayUtilTest {

  @Test
  public void buildArrayContentVariableNameExceptions() {
    IllegalArgumentException arrayName = null;
    IllegalArgumentException negativeindex = null;

    try {
      SymbolicArrayUtil.buildArrayContentVariableName(null, 0);
    } catch (IllegalArgumentException e) {
      arrayName = e;
    }

    try {
      SymbolicArrayUtil.buildArrayContentVariableName("var", -3);
    } catch (IllegalArgumentException e) {
      negativeindex = e;
    }

    assertNotNull(arrayName);
    assertNotNull(negativeindex);

    assertEquals(SymbolicArrayUtil.ARRAY_NAME_CANNOT_BE_NULL, arrayName.getMessage());
    assertEquals(SymbolicArrayUtil.ARRAY_INDEX_CANNOT_BE_LOWER_THAN_0, negativeindex.getMessage());
  }

  @Test
  public void isArrayContentVariableNameExceptions() {
    IllegalArgumentException arrayName = null;

    try {
      SymbolicArrayUtil.isArrayContentVariableName(null);
    } catch (IllegalArgumentException e) {
      arrayName = e;
    }

    assertNotNull(arrayName);
    assertEquals(SymbolicArrayUtil.ARRAY_NAME_CANNOT_BE_NULL, arrayName.getMessage());
  }

  @Test
  public void isArrayContentVariableName() {
    assertTrue(
      SymbolicArrayUtil.isArrayContentVariableName(
        SymbolicArrayUtil.buildArrayContentVariableName("arr0", 3)
      )
    );
  }
}