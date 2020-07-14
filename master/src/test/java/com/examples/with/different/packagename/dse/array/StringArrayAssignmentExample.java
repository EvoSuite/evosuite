package com.examples.with.different.packagename.dse.array;

public class StringArrayAssignmentExample {

  public static int test1(String b, String[] array) {
    String a = array[0];
    String[] arr = new String[3];
    arr[2] = array[3];

    if (arr[2].length() > 2) {
      array[0] = b;

      if (array[2].contains("aaasdd")) {
        return -1;
      } else {
        return 0;
      }
    } else {
      return 2;
    }
  }

}
