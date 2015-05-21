package org.evosuite.runtime.javaee.javax.servlet;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Created by Andrea Arcuri on 21/05/15.
 */
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
