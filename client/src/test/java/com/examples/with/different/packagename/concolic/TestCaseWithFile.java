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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class TestCaseWithFile {

	public String test(File f) throws IOException {
		if (!f.exists()) {
			return "No File";
		}
		FileInputStream fileInputStream = new FileInputStream(f);
		Scanner fromFile = new Scanner(fileInputStream);
		String str = fromFile.nextLine();
		fromFile.close();
		if (str.equals("<<FILE CONTENT>>"))
			return str;
		else
			return null;
	}

	public static boolean isZero(int value) {
		if (value == 0) {
			return true;
		} else {
			return false;
		}
	}

}
