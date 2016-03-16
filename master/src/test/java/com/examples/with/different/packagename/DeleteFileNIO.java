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
package com.examples.with.different.packagename;

import java.io.IOException;

/**
 * @author Gordon Fraser
 * 
 */
public class DeleteFileNIO {
	public void testMe(String x) throws IOException {
		String tmpdir = System.getProperty("java.io.tmpdir");
		/*
		 * TODO: This only works with Java 7, so for now it is commented out
		java.nio.file.Path p = java.nio.file.FileSystems.getDefault().getPath(tmpdir,
		                                          "this_file_should_not_be_deleted_by_evosuite");
		java.nio.file.Files.delete(p);
		*/
	}
}
