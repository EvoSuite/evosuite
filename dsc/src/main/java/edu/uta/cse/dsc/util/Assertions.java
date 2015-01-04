package edu.uta.cse.dsc.util;

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