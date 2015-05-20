package org.evosuite.runtime.javaee.javax.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

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
