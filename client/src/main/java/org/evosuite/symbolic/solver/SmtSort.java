package org.evosuite.symbolic.solver;

public enum SmtSort {
  INT("Int"),
  REAL("Real"),
  ARRAY("Array"),
  STRING("String");

  SmtSort(String name){
    this.name = name;
  }

  private String name;

  public String getName(){
    return this.name;
  }

  public String toString() {
    return this.name;
  }
}
