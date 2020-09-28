/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package com.examples.with.different.packagename.papers.vfs;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Scanner;

public class EnvExample {

    public boolean checkContent() throws Exception {

        Scanner console = new Scanner(System.in);
        String fileName = console.nextLine();
        console.close();

        File file = new File(fileName);
        if (!file.exists())
            return false;

        Scanner fromFile = new Scanner(new FileInputStream(file));
        String fileContent = fromFile.nextLine();
        fromFile.close();

        String date = DateFormat.getDateInstance(DateFormat.SHORT).format(new Date());
        if (fileContent.equals(date))
            return true;

        return false;
    }
}
