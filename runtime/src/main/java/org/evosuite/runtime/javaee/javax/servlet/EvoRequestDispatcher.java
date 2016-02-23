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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

@EvoSuiteClassExclude
public class EvoRequestDispatcher implements RequestDispatcher{

	private final String name;

	public EvoRequestDispatcher(String name) throws IllegalArgumentException{
		if(name==null || name.trim().isEmpty()){
			throw new IllegalArgumentException("Invalid name");
		}
		this.name = name;
	}

	@Override
	public void forward(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		if(arg1.isCommitted()){
			throw new IllegalStateException("Request already committed");
		}

		PrintWriter out = arg1.getWriter();
		out.println("[Simulated request handled by dispatcher: "+name+"]");
		out.close();
	}

	@Override
	public void include(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {

		//TODO
	}

}
