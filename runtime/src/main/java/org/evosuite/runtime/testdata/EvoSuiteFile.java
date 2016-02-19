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
/**
 * 
 */
package org.evosuite.runtime.testdata;

import java.io.File;
import java.io.Serializable;

/**
 * A object wrapper for file paths accessed by the SUTs.
 *   
 * @author fraser
 */
public class EvoSuiteFile implements Serializable{

	private static final long serialVersionUID = -4900126189189434483L;

	private final String path;

	/**
	 * <p>Constructor for EvoSuiteFile.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public EvoSuiteFile(String path) {
		
		if(path==null){
			this.path = null;
		} else {
			this.path = (new File(path)).getAbsolutePath();
		}
	}

	/**
	 * <p>Getter for the field <code>path</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPath() {
		return path;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return path;
	}
}
