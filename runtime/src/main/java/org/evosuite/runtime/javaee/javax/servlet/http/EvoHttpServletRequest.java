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
package org.evosuite.runtime.javaee.javax.servlet.http;

import org.evosuite.runtime.annotation.Constraints;
import org.evosuite.runtime.annotation.EvoSuiteAssertionOnly;
import org.evosuite.runtime.annotation.EvoSuiteClassExclude;
import org.evosuite.runtime.annotation.EvoSuiteInclude;
import org.evosuite.runtime.javaee.TestDataJavaEE;
import org.evosuite.runtime.javaee.javax.servlet.EvoAsyncContext;
import org.evosuite.runtime.javaee.javax.servlet.EvoServletState;
import org.evosuite.runtime.vnet.VirtualNetwork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

/**
 * Class used to create HttpServletRequest to unit test Java EE servlet applications.
 *
 * Created by Andrea Arcuri on 20/05/15.
 */
@EvoSuiteClassExclude
public class EvoHttpServletRequest implements HttpServletRequest {

	public static final String TEXT_XML_CONTENT_FORMAT = "text/xml";
	public static final String TEXT_HTML_CONTENT_FORMAT = "text/html";
	public static final String MULTIPART_FORM_CONTENT_FORMAT = "multipart/form-data";

    private String contextPath;
    private String servletPath;
    private Integer requestedSessionId;
    private HttpMethod method;
    private String contentType;
	private String protocol;

	//network related
	private String localAddr;
	private String localName;
	private int localPort;
	private String remoteAddr;
	private String remoteHost;
	private int remotePort;

	private Map<String, String[]> parameters;
	private AsyncContext asyncContext;
	private Map<String, EvoPart> parts;
	private String principalName;

    public EvoHttpServletRequest(){
        /*
            Note: quite a few of these fields could be good to be set by the test
            cases directly, but they might just increase the search space without
            any real benefit. Should carefully check what is actually used in
            practice
         */
        contextPath = "/EvoSuiteContext";
        servletPath = "/EvoSuiteServlet";
        requestedSessionId = null;
        asGET();
		contentType = null; //TODO check unsure if null only in GET
		protocol = "HTTP/1.1";

		localName = "MockedJavaEEServer";
		localAddr = VirtualNetwork.getInstance().dnsResolve(localName);
		localPort = 80;
		remoteHost = "MockedRemoteEvoSuiteRequestClient";
		remoteAddr = VirtualNetwork.getInstance().dnsResolve(remoteHost);
		remotePort = 61386; //any would do

		parameters = new LinkedHashMap<>();
		asyncContext = null;
		parts = new LinkedHashMap<>();
	}

    // ------- super classes overridden methods  ---------------

	@Override
	public AsyncContext getAsyncContext() throws IllegalStateException{
		if(asyncContext == null){
			throw new IllegalStateException("Async context not initialized");
		}
		return asyncContext;
	}

	@EvoSuiteAssertionOnly
	@Override
	public boolean isAsyncStarted() {
		if(asyncContext==null){
			//TODO check on asyncContext if "dispatch" or "complete" were called (need new is* method for that)
			return false;
		}
		return true;
	}

	@EvoSuiteAssertionOnly
	@Override
	public boolean isAsyncSupported() {
		Servlet sut = EvoServletState.getServlet();
		WebServlet annotation = sut.getClass().getAnnotation(WebServlet.class);
		if(annotation == null){
			return false; //TODO: unsure if really correct. need more investigation
		}
		return annotation.asyncSupported();
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		if(!isAsyncSupported() || EvoServletState.getResponse().isCommitted()){
		/*
			TODO Need also to handle:
			- this request is within the scope of a filter
			- this method is called again without any asynchronous dispatch (resulting from one of the AsyncContext.dispatch methods),
			  is called outside the scope of any such dispatch, or is called again within the scope of the same dispatch
		 */
			throw new IllegalStateException("Cannot start async");
		}
		if(asyncContext==null){
			asyncContext = new EvoAsyncContext();
		} else {
			//should re-init, as per JavaDoc
		}
		return asyncContext;
	}

	@Override
	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1)
			throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAttribute(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getContentLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getContentLengthLong() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getContentType() {
		TestDataJavaEE.getInstance().accessContentType();
		return contentType;
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalAddr() {
		return localAddr;
	}

	@Override
	public String getLocalName() {
		return localName;
	}

	@Override
	public int getLocalPort() {
		return localPort;
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<Locale> getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getParameter(String arg0) {
		if(arg0 != null){
			TestDataJavaEE.getInstance().accessedHttpRequestParameter(arg0);
		}

		String[] params = getParameterValues(arg0);
		if(params != null){
			return params[0];
		}
		return null;
	}

	@Override
	public Map<String, String[]> getParameterMap() {

		/*
			here all the fuzz is to check if the existence of any specific property is checked for.
			this is actually done in some of the tested SUTs
		 */

		return Collections.unmodifiableMap(new LinkedHashMap<String, String[]>() {
			{
				this.putAll(parameters);
			}

			@Override
			public Set<String> keySet() {
				final Set<String> set = super.keySet();
				return Collections.unmodifiableSet(new LinkedHashSet<String>() {
					{
						this.addAll(set);
					}

					@Override
					public boolean contains(Object k) {
						if (!(k instanceof String)) {
							return false;
						}
						String val = (String) k;
						if (val != null && !val.trim().isEmpty()) {
							TestDataJavaEE.getInstance().accessedHttpRequestParameter(val);
						}
						return super.contains(val);
					}
				});
			}

			@Override
			public boolean containsKey(Object k) {
				if (!(k instanceof String)) {
					return false;
				}
				String key = (String) k;
				if (key != null && !key.trim().isEmpty()) {
					TestDataJavaEE.getInstance().accessedHttpRequestParameter(key);
				}
				return super.containsKey(key);
			}
		});
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(parameters.keySet());
	}

	@Override
	public String[] getParameterValues(String arg0) {
		return parameters.get(arg0);
	}

	@Override
	public String getProtocol() {
		return protocol;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteAddr() {
		return remoteAddr;
	}

	@Override
	public String getRemoteHost() {
		return remoteHost;
	}

	@Override
	public int getRemotePort() {
		return remotePort;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getScheme() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getServerPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeAttribute(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		// TODO Auto-generated method stub

	}



	@Override
	public boolean authenticate(HttpServletResponse arg0) throws IOException,
			ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String changeSessionId() throws IllegalStateException{
		if(requestedSessionId == null){
			new IllegalStateException("No current session id");
		}
		requestedSessionId++;
		return requestedSessionId.toString();
	}

	@Override
	public String getAuthType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContextPath() {
		return contextPath;
	}

	@Override
	public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getDateHeader(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getHeader(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getHeaders(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIntHeader(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMethod() {
		return method.toString();
	}

	@Override
	public Part getPart(String arg0) throws IOException, ServletException {
		if(! MULTIPART_FORM_CONTENT_FORMAT.equals(contentType)){
			throw new ServletException("Cannot access parts if request is not of type "+MULTIPART_FORM_CONTENT_FORMAT);
		}
		TestDataJavaEE.getInstance().accessPart(arg0);
		return parts.get(arg0);
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		if(! MULTIPART_FORM_CONTENT_FORMAT.equals(contentType)){
			throw new ServletException("Cannot access parts if request is not of type "+MULTIPART_FORM_CONTENT_FORMAT);
		}

		TestDataJavaEE.getInstance().accessPart(null);
		return new ArrayList<Part>(){{addAll(parts.values());}};
	}

	@Override
	public String getPathInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getQueryString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestURI() {
		return getContextPath()+getServletPath();
	}

	@Override
	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		return requestedSessionId == null ? null : requestedSessionId.toString();
	}

	@Override
	public String getServletPath() {
		return servletPath;
	}

	@Override
	public HttpSession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpSession getSession(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		if(principalName == null || principalName.trim().isEmpty()){
			return null;
		}

		return new Principal() {
			@Override
			public String getName() {
				return principalName;
			}
		};
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void login(String arg0, String arg1) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout() throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> arg0)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String toString(){
		//TODO: might need more stuff
		return EvoHttpServletRequest.class.getSimpleName() + " [ " + getMethod() + " " + getRequestURI() + " ]";
	}

    // ---------  public methods used only in EvoSuite -------------------


	@EvoSuiteInclude
	@Constraints(atMostOnce = true , excludeOthers = {"asGET","asPUT","as"})
    public void asPOST(){
        setHttpMethod(HttpMethod.POST);
    }

	@EvoSuiteInclude
	@Constraints(atMostOnce = true , excludeOthers = {"asPOST","asPUT","as"})
    public void asGET(){
        setHttpMethod(HttpMethod.GET);
    }

	@EvoSuiteInclude
	@Constraints(atMostOnce = true , excludeOthers = {"asGET","asPOST","as"})
	public void asPUT(){
		setHttpMethod(HttpMethod.PUT);
	}


	@EvoSuiteInclude
	@Constraints(atMostOnce = true , excludeOthers = {"asGET","asPOST","asPUT"}, noNullInputs = true)
	public void as(HttpMethod m){
		setHttpMethod(m);
	}


	@EvoSuiteInclude
	@Constraints(atMostOnce = true , excludeOthers = {"asMultipartFormData","asTextHtml"}, dependOnProperties = TestDataJavaEE.HTTP_REQUEST_CONTENT_TYPE)
    public void asTextXml(){
        setContentType(TEXT_XML_CONTENT_FORMAT);
    }

	@EvoSuiteInclude
	@Constraints(atMostOnce = true , excludeOthers = {"asTextXml","asMultipartFormData"}, dependOnProperties = TestDataJavaEE.HTTP_REQUEST_CONTENT_TYPE)
	public void asTextHtml(){
		setContentType(TEXT_HTML_CONTENT_FORMAT);
	}

	@EvoSuiteInclude
	@Constraints(atMostOnce = true , excludeOthers = {"asTextXml","asTextHtml"}, dependOnProperties = TestDataJavaEE.HTTP_REQUEST_CONTENT_TYPE, after="asPOST")
	public void asMultipartFormData(){
        setContentType(MULTIPART_FORM_CONTENT_FORMAT);
    }

	@EvoSuiteInclude
	@Constraints(dependOnProperties = TestDataJavaEE.HTTP_REQUEST_PARAM, after = "asMultipartFormData")
	public void addParam(String key, String value) throws IllegalArgumentException{
		if(key==null || value==null){
			throw new IllegalArgumentException("Null input");
		}
		String[] params = parameters.get(key);
		if(params==null){
			params = new String[]{value};
		} else {
			params = Arrays.copyOf(params,params.length+1);
			params[params.length-1] = value;
		}

		parameters.put(key, params);

	}

	@EvoSuiteInclude
	@Constraints(dependOnProperties = TestDataJavaEE.HTTP_REQUEST_PART)
	public void addPart(EvoPart p){
		parts.put(p.getName(), p);
	}


	@EvoSuiteInclude
	@Constraints(dependOnProperties = TestDataJavaEE.HTTP_REQUEST_PRINCIPAL)
	public void setPrincipalName(String principalName) {
		this.principalName = principalName;
	}

	// --------- private methods -----------------------------------------

    private void setHttpMethod(HttpMethod m) throws IllegalArgumentException{
        if(m == null){
            throw new IllegalArgumentException("Null input");
        }
        method = m;
    }

    /**
     *
     * @param type can be null
     */
    private void setContentType(String type) {
        contentType = type;
    }
}
