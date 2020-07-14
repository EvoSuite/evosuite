package org.evosuite.testcase;

import java.util.function.Function;

public enum DefaultValueChecker {
    INTEGER((value) -> ((Integer) value).equals(0)),
    BYTE((value) -> ((Byte) value).equals(0)),
    SHORT((value) -> ((Short) value).equals(0)),
    LONG((value) -> ((Long) value).equals(0L)),
    CHAR((value) -> ((Character) value).equals('\u0000')),
    BOOLEAN((value) -> ((Boolean) value)),
    STRING((value) -> !(((String)value) == null)),
    FLOAT((value) -> ((Float) value).equals(0.0f)),
    DOUBLE((value) -> ((Double) value).equals(0.0d));

    private final Function<Object, Boolean> checker;

    DefaultValueChecker(final Function<Object, Boolean> statementCheck) {
      this.checker = statementCheck;
    }

    public static boolean isDefaultValue(Object value) {
      if (Integer.class.equals(value.getClass())) {
        return INTEGER.checker.apply(value);
      } else if (Byte.class.equals(value.getClass())) {
        return BYTE.checker.apply(value);
      } else if (Short.class.equals(value.getClass())) {
        return SHORT.checker.apply(value);
      } else if (Long.class.equals(value.getClass())) {
        return LONG.checker.apply(value);
      } else if (Boolean.class.equals(value.getClass())) {
        return BOOLEAN.checker.apply(value);
      } else if (String.class.equals(value.getClass())) {
        return STRING.checker.apply(value);
      } else if (Float.class.equals(value.getClass())) {
        return FLOAT.checker.apply(value);
      } else if (Double.class.equals(value.getClass())) {
        return DOUBLE.checker.apply(value);
      }
      throw new IllegalStateException("Unexpected value: " + value.getClass());
    }
}
