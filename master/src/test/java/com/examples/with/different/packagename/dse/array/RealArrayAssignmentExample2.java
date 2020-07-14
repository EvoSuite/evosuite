package com.examples.with.different.packagename.dse.array;

public class RealArrayAssignmentExample2 {

  public static int realAssignment(double b, double[] array) {
    double a = array[0];

    if (a > 2.3d) {
      array[0] = b;

      if (array[0] > 5.1d && array[0] < 5.8d) {
        if (array[0] == 5.6d) {
          return 6;
        } else {
          return 7;
        }
      }

      if (array[2] == 0.2d) {
        return -1;
      } else {
        return 0;
      }
    } else {
      return 2;
    }
  }

}
