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

import org.evosuite.runtime.annotation.Constraints;
import org.evosuite.runtime.annotation.EvoSuiteClassExclude;
import org.evosuite.runtime.annotation.EvoSuiteInclude;

import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * When a a Servlet is started in a container, its method "init" is called in
 * which a configuration is used as input
 *
 * @author foo
 *
 */
@EvoSuiteClassExclude
public class EvoServletConfig implements ServletConfig{

	private EvoServletContext context;

	public EvoServletConfig(){
		context = new EvoServletContext();
	}

	@Override
	public String getInitParameter(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletContext getServletContext() {
		return context;
	}

	@Override
	public String getServletName() {
		// TODO Auto-generated method stub
		return null;
	}


	//------------ EvoSuite test methods -----------------

	@EvoSuiteInclude
	@Constraints(noNullInputs = true)
	public void createDispatcher(String name){
		context.createDispatcher(name);
	}
}
