package com.examples.with.different.packagename.concolic;

import java.beans.PropertyChangeSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Used to make sure that a correct postcode is entered
 * more complex validation on postcode needed to meet
 * bS7666
 * @author Tom Nolan (tom.nolan@sinbadsoftware.com)
 * @version 1.0
 * @since 28/05/2003 
 *
 *
 */

public final class PostCodeValidator  extends Validator
{
  
 /**
  * Defines the pattern of a UK Postcode that is used by the validator to match
  * against
  */   
 private static final Pattern pattern =  Pattern.compile("(GIR 0AA)|((([A-Z][0-9][0-9]?)|(([A-Z][A-HJ-Y][0-9][0-9]?)|(([A-Z][0-9][A-Z])|([A-Z][A-HJ-Y][0-9]?[A-Z])))) [0-9][A-Z]{2})");
 
 /**
  * for gui
  */
  public PostCodeValidator()
  {
      propertySupport = new PropertyChangeSupport( this );
  }
     
  /** Constructor
   * State if you want a MADATORY or NONMADATORY Validation
   * NONMADATORY Validation only takes place if something is entered
   * returns true for null and ""
   * @param blnMand Whether or not we want this validation to be MANDATORY
   */ 
 public PostCodeValidator(boolean blnMand)
 {
    propertySupport = new PropertyChangeSupport( this );
    setMandatory(blnMand);
    setErrorText("Please enter a valid postcode");
 }
 
 
 /** Checks whether the input is a valid UK Postcode
  * @return True if the input is a valid UK Postcode
  * @param str The String to be checked
  */ 
 public  boolean isValid(String str) 
  {
     String strMatch = str; 
     Matcher matcher = null;
     
        if(strMatch == null)
      {
        if(getMandatory()==MANDATORY)
        {    
            return false;   
        }
        else
        {
            return true;
        }
      }
      else
      {      
        strMatch = strMatch.trim();
        System.out.println("strMatch = " + strMatch);
     
          if(strMatch.equals(""))
          {
            System.out.println("1");  
            if(getMandatory()==MANDATORY)
            {    
                 System.out.println("2");
                return false;   
            }
            else
            {
                 System.out.println("3");
                return true;
            }
          }
          else //test
          {
             System.out.println("strMatch = " + strMatch);
          
              //lets put in a space 
              // if the user hasn't before we validate
              strMatch =  insertRequiredSpace(strMatch).toUpperCase(); 

              System.out.println("validating:"+strMatch);

              matcher = pattern.matcher(strMatch);

              return matcher.matches(); 
          }
        
      }  
      
   }

 /** If the user has omitted a space in the
  * postcode this method inserts a space in
  * the right place
  * @param str The postcode String
  * @return The Postcode with a space inserted (if required)
  */ 
 public static String insertRequiredSpace(String str)
 {
     String strResult="";
    //lets put in a space 
      // if the user hasn't before we validate
    
      if(str.indexOf(" ")== -1) // not found a space
      {
        try
        {  
            strResult = str.substring(0,str.length()-3) + " " + 
                   str.substring(str.length()-3);
            System.out.println("inserting Space2 : " + strResult);
        }
        catch(StringIndexOutOfBoundsException ex)
        {
         System.out.println(ex.getMessage()); 
         strResult = str;
        }
        
      }
      else
      {
          strResult = str;
      }
 
      return strResult;
 }
 
}
