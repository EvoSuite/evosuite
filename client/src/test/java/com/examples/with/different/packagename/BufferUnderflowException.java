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
package com.examples.with.different.packagename;

import java.util.NoSuchElementException;

/**
 * The BufferUnderflowException is used when the buffer is already empty.
 * <p>
 * NOTE: From version 3.0, this exception extends NoSuchElementException.
 * 
 * @since Commons Collections 2.1
 * @version $Revision: 646777 $ $Date: 2008-04-10 13:33:15 +0100 (Thu, 10 Apr 2008) $
 *
 * @author Avalon
 * @author Berin Loritsch
 * @author Jeff Turner
 * @author Paul Jack
 * @author Stephen Colebourne
 */
public class BufferUnderflowException extends NoSuchElementException {
    
    /** The root cause throwable */
    private final Throwable throwable;

    /**
     * Constructs a new <code>BufferUnderflowException</code>.
     */
    public BufferUnderflowException() {
        super();
        throwable = null;
    }

    /** 
     * Construct a new <code>BufferUnderflowException</code>.
     * 
     * @param message  the detail message for this exception
     */
    public BufferUnderflowException(String message) {
        this(message, null);
    }

    /** 
     * Construct a new <code>BufferUnderflowException</code>.
     * 
     * @param message  the detail message for this exception
     * @param exception  the root cause of the exception
     */
    public BufferUnderflowException(String message, Throwable exception) {
        super(message);
        throwable = exception;
    }

    /**
     * Gets the root cause of the exception.
     *
     * @return the root cause
     */
    public final Throwable getCause() {
        return throwable;
    }
    
}
