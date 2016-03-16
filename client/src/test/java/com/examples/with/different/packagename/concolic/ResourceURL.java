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
package com.examples.with.different.packagename.concolic;

public class ResourceURL extends URI
{
  /** Creates a "res:" URL from the given string.
   *
   *  @param nuri the string to parse.
   *  @exception MalformedURIException if the string did not parse.
   */
  public ResourceURL(String nuri) throws MalformedURIException
    {
      super(nuri);

      if(uri.length() == colonLocation + 1)
	throw new MalformedURIException("Empty path", null);
      
      if(uri.charAt(colonLocation + 1) != '/')
	throw new MalformedURIException("No leading '/' in \""+ uri + "\"",
					uri);
      if(!uri.regionMatches(0, "res", 0, colonLocation))
	throw new MalformedURIException("The identifier was no Resource URI \""
					+ uri + "\".", uri);
    }

  
  public String getResourceName()
    {
      return super.getSchemeSpecific().substring(1);
    }
  
  
  /** Returns a Java URL object pointing to the file represented by this
   *  ResourceURL.
   *
   *  @return a Java URL object.
   */
  public java.net.URL getJavaURL() throws java.net.MalformedURLException 
    {
      java.net.URL url = getClass().getClassLoader().getResource(this.getResourceName());
      if(url == null)
	throw new java.net.MalformedURLException("No such resource found: " + getResourceName());
      
      return url;
    }

  public String makeRelative(URI other, boolean allowDotDot) throws MalformedURIException
    {
      if(!(other instanceof ResourceURL))
	return other.toString();
      return genericMakeRelative(other, 4, 4, allowDotDot);
    }

  protected URI parseRelativeURI(String relstr) throws MalformedURIException
    {
      return new ResourceURL(genericParseRelativeURI(relstr, 4));
    }

}