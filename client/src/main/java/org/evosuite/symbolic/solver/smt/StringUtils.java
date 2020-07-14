package org.evosuite.symbolic.solver.smt;

import java.util.List;
import java.util.StringJoiner;

public class StringUtils {

  public static final String SPACE_DELIMITER = " ";

  public static String joinStrings(String delimiter, List<String> strings) {
    StringJoiner joiner = new StringJoiner(delimiter);

    for (String string : strings) {
      joiner.add(string);
    }

    return joiner.toString();
  }
}
