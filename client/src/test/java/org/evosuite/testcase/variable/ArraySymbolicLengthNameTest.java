package org.evosuite.testcase.variable;

import org.junit.Test;

import static org.evosuite.testcase.variable.ArraySymbolicLengthName.ARRAY_LENGTH_SYMBOLIC_NAME_INVALID_FOR_NAME_EXCEPTION;
import static org.junit.Assert.*;

public class ArraySymbolicLengthNameTest {

  public static final int DIMENSION_TEST_VALUE = 1;

  public static final String INVALID_SYMBOLIC_NAME = "test+-238132n";
  public static final String ARRAY_REFERENCE_NAME_TEST_VALUE = "var";
  public static final String SYMBOLIC_NAME_BUILT_EXPECTED_RESULT = "var_dim_1";

  @Test
  public void isArraySymbolicLengthVariableName() {
    assertTrue(
      ArraySymbolicLengthName.isArraySymbolicLengthVariableName(
        ArraySymbolicLengthName.buildSymbolicLengthDimensionName(
          ARRAY_REFERENCE_NAME_TEST_VALUE,
          DIMENSION_TEST_VALUE
        )
      )
    );
    
    assertFalse(ArraySymbolicLengthName.isArraySymbolicLengthVariableName(INVALID_SYMBOLIC_NAME));
  }


  @Test
  public void buildSymbolicLengthDimensionName() {
    String name = ArraySymbolicLengthName.buildSymbolicLengthDimensionName(ARRAY_REFERENCE_NAME_TEST_VALUE, DIMENSION_TEST_VALUE);
    assertTrue(name.equals(SYMBOLIC_NAME_BUILT_EXPECTED_RESULT));
  }


  @Test
  public void builtFromSymbolicName() {
    String symbolicName = ArraySymbolicLengthName.buildSymbolicLengthDimensionName(
      ARRAY_REFERENCE_NAME_TEST_VALUE,
      DIMENSION_TEST_VALUE
    );

    ArraySymbolicLengthName arraySymbolicLengthName = new ArraySymbolicLengthName(symbolicName);

    assertEquals(DIMENSION_TEST_VALUE, arraySymbolicLengthName.getDimension());
    assertEquals(ARRAY_REFERENCE_NAME_TEST_VALUE, arraySymbolicLengthName.getArrayReferenceName());
    assertEquals(symbolicName, arraySymbolicLengthName.getSymbolicName());
  }

  @Test
  public void builtFromReferenceNameAndDimension() {
    String symbolicName = ArraySymbolicLengthName.buildSymbolicLengthDimensionName(
      ARRAY_REFERENCE_NAME_TEST_VALUE,
      DIMENSION_TEST_VALUE
    );

    ArraySymbolicLengthName arraySymbolicLengthName = new ArraySymbolicLengthName(
      ARRAY_REFERENCE_NAME_TEST_VALUE,
      DIMENSION_TEST_VALUE
    );

    assertEquals(DIMENSION_TEST_VALUE, arraySymbolicLengthName.getDimension());
    assertEquals(ARRAY_REFERENCE_NAME_TEST_VALUE, arraySymbolicLengthName.getArrayReferenceName());
    assertEquals(symbolicName, arraySymbolicLengthName.getSymbolicName());
  }


  @Test
  public void builtFromInvalidSymbolicName() {
    IllegalArgumentException e = null;

    try {
      ArraySymbolicLengthName arraySymbolicLengthName = new ArraySymbolicLengthName(INVALID_SYMBOLIC_NAME);
    } catch(IllegalArgumentException ex) {
      e = ex;
    }

    assertNotNull(e);
    assert(e.getMessage().contains(ARRAY_LENGTH_SYMBOLIC_NAME_INVALID_FOR_NAME_EXCEPTION));
  }

}