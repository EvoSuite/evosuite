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

public class ShowInputDialogExample {

    public int showInputDialogs() {
        int count = 0;

        final String string3 = JOptionPane.showInputDialog("message0");
        if (string3 == null) {
            count++;
        }

        final String string0 = JOptionPane.showInputDialog(null, "message0");
        if (string0 == null) {
            count++;
        }

        String string4 = JOptionPane.showInputDialog("message0", "initialValue0");
        if (string4 == null) {
            count++;
        }

        String string1 = JOptionPane.showInputDialog(null, "message0", "initialValue0");
        if (string1 == null) {
            count++;
        }

        String string2 = JOptionPane.showInputDialog(null, "message0", "title0", JOptionPane.ERROR_MESSAGE);
        if (string2 == null) {
            count++;
        }

        Object object0 = JOptionPane.showInputDialog(null, "message0", "title0", JOptionPane.ERROR_MESSAGE, null,
                new Object[]{"val0", "val1"}, "val0");
        if (object0 == null) {
            count++;
        }

        return count;

    }
}
