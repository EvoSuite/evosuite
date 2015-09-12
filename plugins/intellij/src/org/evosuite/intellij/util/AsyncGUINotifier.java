/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.intellij.util;

/**
 * EvoSuite is run on background, in a non-blocking way.
 * Once finished, we might want some sort of notification
 *
 * Created by arcuri on 10/2/14.
 */
public interface AsyncGUINotifier {

    public void success(String message);

    public void failed(String message);

    public void attachProcess(Process process);

    public void printOnConsole(String message);

    public void clearConsole();
}
