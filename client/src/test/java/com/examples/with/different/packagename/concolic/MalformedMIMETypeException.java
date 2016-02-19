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
