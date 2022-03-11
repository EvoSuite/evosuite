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

public class ShowConfirmDialogExample {

    public ShowConfirmDialogExample() {

    }

    public int showConfirmDialogs() {
        int count = 0;
        final int ret_code0 = JOptionPane.showConfirmDialog(null, "message0");
        if (ret_code0 == JOptionPane.YES_OPTION) {
            count++;
        } else {
            count--;
        }

        final int ret_code1 = JOptionPane.showConfirmDialog(null, "message0", "title0",
                JOptionPane.YES_NO_CANCEL_OPTION);
        if (ret_code1 == JOptionPane.YES_OPTION) {
            count++;
        } else {
            count--;
        }

        final int ret_code2 = JOptionPane.showConfirmDialog(null, "message0", "title0",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
        if (ret_code2 == JOptionPane.YES_OPTION) {
            count++;
        } else {
            count--;
        }

        final int ret_code3 = JOptionPane.showConfirmDialog(null, "message0", "title0",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null);
        if (ret_code3 == JOptionPane.YES_OPTION) {
            count++;
        } else {
            count--;
        }

        return count;
    }

}
