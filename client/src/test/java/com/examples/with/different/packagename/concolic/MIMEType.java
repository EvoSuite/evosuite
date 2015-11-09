package com.examples.with.different.packagename.concolic;


/** Represents a MIME type.
 *
 *  @author Mikael Nilsson
 *  @version $Revision: 1.10 $
 */
public class MIMEType
{
  /** The "text/xml" MIME type.
   */
  public static final MIMEType XML = new MIMEType("text/xml", true);

  public final static MIMEType RDF = new MIMEType("text/rdf", true);

  public final static MIMEType MEM = new MIMEType("application/x.memory", true);

  /** The "application/x.layout" MIME type.
   */
  public static final MIMEType CONCEPTMAP = new MIMEType("application/x.layout", true);
  
  /** The MIME type in full.
   */
  String mimeType;

  /** The location of the slash.
   */
  int slash;

  public MIMEType(String s, boolean noThrow)
    {
      slash = s.indexOf('/');
      if(slash == -1)
	Tracer.bug("Invalid MIME Type given in private constructor: " + s);

      mimeType    = s;
    }
  
  /** Constructs a MIME type from the given string.
   *
   *  The String should be in the form "image/jpeg" etc.
   *
   *  @param ntype the String to parse.
   *  @exception MalformedMIMETypeException if parsing the string was
   *             impossible.
   */
  public MIMEType(String ntype) throws MalformedMIMETypeException
    {
      slash = ntype.indexOf('/');
      if(slash == -1)
	throw new MalformedMIMETypeException("No '/' in mime-type ("
					     + ntype + ")!", ntype);
      mimeType    = ntype;
    }

  /** Returns the main MIME type.
   *
   *  @return the main MIME type.
   */
  public String getType()
    {
      return mimeType.substring(0, slash);
    }

  /** Returns the subtype.
   *
   *  @return the subtype.
   */
  public String getSubType()
    {
      return mimeType.substring(slash + 1);
    }

  /** Returns the MIME type in original form, e.g. "image/jpeg".
   *
   *  @return the MIME type in original form.
   */
  public String toString()
    {
      return mimeType;
    }

  public boolean equals(Object o)
  {    
    if(o == null)
      return false;
    if(o instanceof MIMEType)
      {
	MIMEType ntype = (MIMEType) o;
	return ntype.mimeType.equals(mimeType);
      }
    return false;
  }
  
  public int hashCode()
    {
      return mimeType.hashCode();
    }
}
