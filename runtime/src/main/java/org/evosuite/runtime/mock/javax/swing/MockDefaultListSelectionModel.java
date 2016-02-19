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
package org.evosuite.runtime.mock.javax.swing;

import org.evosuite.runtime.mock.OverrideMock;

import javax.swing.*;
import java.lang.reflect.Field;

/**
 * Created by gordon on 28/01/2016.
 */
public class MockDefaultListSelectionModel extends DefaultListSelectionModel implements OverrideMock {

    public String toString() {
        String s =  ((getValueIsAdjusting()) ? "~" : "=");
        try {
            // Value is private...
            Field f = DefaultListSelectionModel.class.getField("value");
            f.setAccessible(true);
            Object value = f.get(this);
            if(value != null)
                s += value.toString();
        } catch (Throwable t) {
           // ignore
        }
        // Integer.toString(System.identityHashCode(this))  -- Ignoring hash code in string
        return DefaultListSelectionModel.class.getName() + " " +  s;
    }

}
