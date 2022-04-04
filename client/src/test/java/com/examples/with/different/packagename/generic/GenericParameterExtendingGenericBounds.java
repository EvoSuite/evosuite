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

package com.examples.with.different.packagename.generic;

import java.util.LinkedList;
import java.util.Map;

/**
 * @author Gordon Fraser
 */
public class GenericParameterExtendingGenericBounds<T extends Map<String, ?>> extends
        LinkedList<T> {

    private static final long serialVersionUID = -5120901091724267526L;

    public boolean testMe() {
        if (size() == 2)
            return true;
        else
            return false;
    }

}
