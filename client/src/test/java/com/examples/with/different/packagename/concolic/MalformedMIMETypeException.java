package com.examples.with.different.packagename.concolic;

/** Thrown if a MIME type was not possible to parse.
 *  @author Mikael Nilsson
 *  @version $Revision$
 */
public class MalformedMIMETypeException extends Exception
{
  /** The MIME type that could not be parsed.
   */
  String type;
  
  /** Constructs an exception with the given detail message.
   *
   * @param nreason the detail message.
   * @param type the malformed MIME type.
   */
  MalformedMIMETypeException(String nreason, String type)
    {
      super(nreason);
      this.type = type;
    }

  /** Returns the MIME type that could not be parsed.
   *
   * @return the MIME type that could not be parsed.
   */
  public String getType()
  {
    return type;
  }
}
