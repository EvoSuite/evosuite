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
package org.evosuite.testcarver.codegen;


import org.evosuite.testcarver.capture.CaptureLog;


public interface ICodeGenerator<T> {

    void before(final CaptureLog log);

    void createFieldReadAccessStmt(final CaptureLog log, final int currentLogRecNo);

    void createFieldWriteAccessStmt(final CaptureLog log, final int currentLogRecNo);

    void createMethodCallStmt(final CaptureLog log, final int currentLogRecNo);

    void createPlainInitStmt(final CaptureLog log, final int currentLogRecNo);

    void createUnobservedInitStmt(final CaptureLog log, final int currentLogRecNo);

    void createArrayInitStmt(final CaptureLog log, final int currentLogRecNo);

    void createCollectionInitStmt(final CaptureLog log, final int currentLogRecNo);

    void createMapInitStmt(final CaptureLog log, final int currentLogRecNo);

    boolean isMaximumLengthReached();

    void after(final CaptureLog log);

    T getCode();

    void clear();

}
