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
