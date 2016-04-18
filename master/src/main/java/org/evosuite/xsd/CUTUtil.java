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
package org.evosuite.xsd;

/**
 * 
 * @author José Campos
 */
public abstract class CUTUtil {

  public static Generation getLatestSuccessfulGeneration(CUT cut) {

    if (cut.getGeneration().isEmpty()) {
      return null;
    }

    for (int i = cut.getGeneration().size() - 1; i >= 0; i--) {
      Generation g = cut.getGeneration().get(i);

      // if a test generation failed and the class under test
      // was modified, we can argue that there is not a valid
      // last successful generation
      if (g.isFailed() && g.isModified()) {
        return null;
      } else if (!g.isFailed() && g.getSuite() != null) {
        // however, if there is generation that ended successfully
        // and has a test suite, return it.
        return g;
      }
    }

    return null;
  }
}
