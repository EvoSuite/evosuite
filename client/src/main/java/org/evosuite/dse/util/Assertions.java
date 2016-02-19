/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.dse.util;

/*
    This class is taken and adapted from the DSC tool developed by Christoph Csallner.
    Link at :
    http://ranger.uta.edu/~csallner/dsc/index.html
 */

/**
 * Makes assertion checking more elegant than explicit if statements. Avoids the
 * -disableassertions problem of Java's assert statements.
 * 
 * @author csallner@uta.edu (Christoph Csallner)
 */
public class Assertions {
	
  /**
   * @throws NullPointerException iff (t==null)
   */
  public static <T> T notNull(final T t) {
    if (t == null) {
      NullPointerException npe = new NullPointerException();
      npe.printStackTrace();
      throw npe;
    }
    
    return t;
  }

  public static void notNull(final Object a, final Object b) {
    notNull(a);
    notNull(b);
  }
  
  public static void notNull(final Object a, final Object b, final Object c) {
    notNull(a);
    notNull(b);
    notNull(c);
  }  

  
  /**
   * @throws IndexOutOfBoundsException iff (i<0)
   */
  public static int notNegative(final int i) {
    if (i < 0) {
    	IndexOutOfBoundsException e = new IndexOutOfBoundsException();
      e.printStackTrace();
      throw e;
    }
    
    return i;
  }
  
  public static void notNegative(final int a, final int b) {
  	notNegative(a);
  	notNegative(b);
  }
  
  
  
  /**
   * Checks if b holds. Call this method to check assertions like
   * pre- and post-conditions.
   * 
   * @throws IllegalStateException iff (b==false)
   */
  public static void check(final boolean b) {
    check(b, "");
  }

  public static void check(final boolean b, Throwable t) {
    if (b == false) {
      IllegalStateException ise = new IllegalStateException(t);
      throw ise;
    }
  }
  
  /**
   * @param msg for exception, in case b==false
   */
  public static void check(final boolean b, String msg) {
    if (b == false) {
      IllegalStateException ise = new IllegalStateException(msg);
      throw ise;
    }
  }  
}