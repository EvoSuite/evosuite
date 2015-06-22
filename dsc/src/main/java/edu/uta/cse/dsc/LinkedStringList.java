package edu.uta.cse.dsc;

import java.util.LinkedList;
import java.util.ListIterator;

/*
    This class is taken and adapted from the DSC tool developed by Christoph Csallner.
    Link at :
    http://ranger.uta.edu/~csallner/dsc/index.html
 */

/**
 * Linked list of Strings
 *  
 * @author csallner@uta.edu (Christoph Csallner)
 */
public class LinkedStringList extends LinkedList<String> {

  /**
   * Constructor
   */
  public LinkedStringList(String... args) {
    for (String arg: args)
      add(arg.trim());  // remove outer white-spaces
  }
  
  /**
   * Constructor
   * 
   * Parse arg as comma separated string list. For example:
   * "a, b b, c" --> {"a", "b b", "c"}
   */
  public LinkedStringList(String arg) {
    this(arg.split(","));
  }  
  
  /**
   * Comma separated list, e.g.: foo,bar bar,baz
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    ListIterator<String> it = listIterator();
    while (it.hasNext()) {
      sb.append(it.next());
      if (it.hasNext())
        sb.append(", ");
    }
    return sb.toString();
  }
}