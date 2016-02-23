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
package org.evosuite.runtime.javaee.javax.servlet;

import org.evosuite.runtime.annotation.EvoSuiteClassExclude;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Created by Andrea Arcuri on 21/05/15.
 */
@EvoSuiteClassExclude
public class EvoAsyncContext implements AsyncContext {

	@Override
	public void addListener(AsyncListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListener(AsyncListener arg0, ServletRequest arg1,
			ServletResponse arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void complete() {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends AsyncListener> T createListener(Class<T> arg0)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispatch() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispatch(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispatch(ServletContext arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public ServletRequest getRequest() {
		return EvoServletState.getRequest();
	}

	@Override
	public ServletResponse getResponse() {
		return EvoServletState.getResponse();
	}

	@Override
	public long getTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasOriginalRequestAndResponse() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setTimeout(long arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(Runnable arg0) {
		// TODO Auto-generated method stub

	}
}
