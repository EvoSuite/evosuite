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
package org.evosuite.runtime.mock.java.util.logging;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.ErrorManager;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.sandbox.MSecurityManager;
import org.evosuite.runtime.vfs.VirtualFileSystem;

/**
 * Mock class for FileHandler.
 * In this mock, all the logging is ignored.
 * In theory, we could redirect all logs to the VFS.
 * We do not do it for the following reasons:
 * 
 * <ul>
 * 		<li> Performance
 *      <li> Anyway we shouldn't have assertions on log status
 *      <li> We do not want the log files to become part of the search
 *           once they get accessed the first time
 * 
 * @author arcuri
 *
 */
public class MockFileHandler extends FileHandler  implements OverrideMock{

    private static final int offValue = Level.OFF.intValue();

    private Filter filter;
    private Formatter formatter;
    private Level logLevel = Level.ALL;
    private ErrorManager errorManager = new ErrorManager();
    private String encoding;

    /* Default constructor of parent that we do have to handle
     * as it will be always called 
     * 
    public FileHandler() throws IOException, SecurityException {
        checkPermission();  //this is package level
        configure();  // private
        openFiles();  //private
    }    
	*/
    
    
    //---- constructors -------

	public MockFileHandler() throws IOException, SecurityException {
		super(MSecurityManager.FILE_HANDLER_NAME_PATTERN,true); //we have to create one file
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(null);
	}

	public MockFileHandler(String pattern) throws IOException, SecurityException {
		this();
		if (pattern.length() < 1 ) {
			throw new IllegalArgumentException();
		}
	}

	public MockFileHandler(String pattern, boolean append) throws IOException, SecurityException {
		this();
		if (pattern.length() < 1 ) {
			throw new IllegalArgumentException();
		}
	}

	public MockFileHandler(String pattern, int limit, int count)
			throws IOException, SecurityException {
		this();
		if (limit < 0 || count < 1 || pattern.length() < 1) {
			throw new IllegalArgumentException();
		}
	}

	public MockFileHandler(String pattern, int limit, int count, boolean append)
			throws IOException, SecurityException {
		this();
		if (limit < 0 || count < 1 || pattern.length() < 1) {
			throw new IllegalArgumentException();
		}
	}
	
	//------- methods of FileHandler ---------

	@Override
	public synchronized void publish(LogRecord record) {
		// nothing to do
	}

	@Override
	public synchronized void close() throws SecurityException {
		//nothing to do
	}

	//----- methods from StreamHandler ----------


	@Override
	public boolean isLoggable(LogRecord record) {
        if(record==null){
        		return false;
        }
		int levelValue = getLevel().intValue();
        if (record.getLevel().intValue() < levelValue || levelValue == offValue) {
            return false;
        }
        Filter filter = getFilter();
        if (filter == null) {
            return true;
        }
        return filter.isLoggable(record);
	}

	@Override
	public synchronized void flush() {
		//nothing to do
	}

	//------- methods from Handler ----------
	
	@Override
    public void setFormatter(Formatter newFormatter) throws SecurityException {
        // Check for a null pointer:
        newFormatter.getClass();
        formatter = newFormatter;
    }

	@Override
    public Formatter getFormatter() {
        return formatter;
    }

	@Override
    public void setEncoding(String encoding)
                        throws SecurityException, java.io.UnsupportedEncodingException {
        if (encoding != null) {
            try {
                if(!java.nio.charset.Charset.isSupported(encoding)) {
                    throw new UnsupportedEncodingException(encoding);
                }
            } catch (java.nio.charset.IllegalCharsetNameException e) {
                throw new UnsupportedEncodingException(encoding);
            }
        }
        this.encoding = encoding;
    }

	@Override
    public String getEncoding() {
        return encoding;
    }

	@Override
    public void setFilter(Filter newFilter) throws SecurityException {
        filter = newFilter;
    }

	@Override
    public Filter getFilter() {
        return filter;
    }

	@Override
    public void setErrorManager(ErrorManager em) {
        if (em == null) {
           throw new NullPointerException();
        }
        errorManager = em;
    }

	@Override
    public ErrorManager getErrorManager() {
        return errorManager;
    }


	@Override
    public synchronized void setLevel(Level newLevel) throws SecurityException {
        if (newLevel == null) {
            throw new NullPointerException();
        }
        logLevel = newLevel;
    }

	@Override
    public synchronized Level getLevel() {
        return logLevel;
    }
}
