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

public class AskUserShowInputDailogs {

    public boolean askInput0() {
        String message = JOptionPane.showInputDialog("message0");
        if (message == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean askInput1() {
        String message = JOptionPane.showInputDialog(null, "message0");
        if (message == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean askInput2() {
        String message = JOptionPane.showInputDialog("message0", "initialSelectionValue");
        if (message == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean askInput3() {
        String message = JOptionPane.showInputDialog(null, "message0", "initialSelectionValue0");
        if (message == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean askInput4() {
        String message = JOptionPane.showInputDialog(null, "messag0", "title0", javax.swing.JOptionPane.ERROR_MESSAGE);
        if (message == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean askInput5() {
        String message = JOptionPane.showInternalInputDialog(null, "message0");
        if (message == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean askInput6() {
        String message = JOptionPane.showInternalInputDialog(null, "messag0", "title0",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        if (message == null) {
            return false;
        } else {
            return true;
        }
    }

}
