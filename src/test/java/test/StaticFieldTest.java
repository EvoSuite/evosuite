package test;

import java.util.Date;

public class StaticFieldTest {

    /** String class */
    public static final Class STRING_VALUE = String.class;
        
    /** Object class */
    public static final Class OBJECT_VALUE = Object.class;
                
    /** Number class */
    public static final Class NUMBER_VALUE = Number.class;
                       
    /** Date class */
    public static final Class DATE_VALUE = Date.class;
                                
    /** Class class */
    public static final Class CLASS_VALUE = Class.class;   
    
    public void testMe(Class test) {
      if(test.equals(OBJECT_VALUE)) {
      
      } else if(test.equals(CLASS_VALUE)) {
      }
    }

}