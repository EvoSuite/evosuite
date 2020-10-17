/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.dse;

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


	private static final long serialVersionUID = 5757225099467215983L;

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