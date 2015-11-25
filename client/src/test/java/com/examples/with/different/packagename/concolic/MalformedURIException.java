package com.examples.with.different.packagename.concolic;

public class MalformedURIException extends Exception
{
  /** The URI that could not be parsed.
   */
  String uri;

  /** Constructs an exception with the given detail message.
   *
   * @param message the detail message.
   * @param nuri the malformed URI.
   */
  public MalformedURIException(String message, String nuri)
    {
      super(message);
      uri = nuri;
    }

  /** Returns the URI that could not be parsed.
   *
   * @return the URI that could not be parsed.
   */
  public String getURI()
  {
    return uri;
  }
}
