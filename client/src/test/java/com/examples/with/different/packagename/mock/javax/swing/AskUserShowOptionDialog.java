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
package com.examples.with.different.packagename.mock.javax.swing;

import javax.swing.*;

public class AskUserShowOptionDialog {


    public int showOptionDialog() {

        int ret = JOptionPane.showOptionDialog(null, "message0", "title0 ", JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE, null, new Object[]{"Hello", "Goodbye"}, "Goodbye");
        if (ret == -1) {
            return 0;
        } else if (ret == 0) {
            return 1;
        } else {
            return 3;
        }
    }

    public int showInternalOptionDialog() {

        int ret = JOptionPane.showInternalOptionDialog(null, "message0", "title0 ", JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE, null, new Object[]{"Hello", "Goodbye", "Hallo"}, "Goodbye");
        if (ret == -1) {
            return 0;
        } else if (ret == 0) {
            return 1;
        } else if (ret == 1) {
            return 2;
        } else {
            return 3;
        }
    }

    public Object showInputDialog() {

        Object ret = JOptionPane.showInputDialog(null, "message0", "title0 ", JOptionPane.DEFAULT_OPTION,
                null, new Object[]{"Hello", "Goodbye", "Hallo"}, "Goodbye");
        if (ret == null) {
            return null;
        } else if (ret.equals("Hello")) {
            return "Hello";
        } else if (ret.equals("Goodbye")) {
            return "Goodbye";
        } else {
            return null;
        }
    }

}
