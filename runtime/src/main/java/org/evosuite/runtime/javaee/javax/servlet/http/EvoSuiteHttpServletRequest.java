package org.evosuite.runtime.javaee.javax.servlet.http;

import org.evosuite.runtime.javaee.TestDataJavaEE;
import org.evosuite.runtime.vnet.VirtualNetwork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
public class EvoSuiteHttpServletRequest implements HttpServletRequest {

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


    public EvoSuiteHttpServletRequest(){
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
	}

    // ------- super classes overridden methods  ---------------

	@Override
	public AsyncContext getAsyncContext() {
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
		return Collections.unmodifiableMap(new LinkedHashMap<String, String[]>() {{
			this.putAll(parameters);
		}});
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
	public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		return false;
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
	public AsyncContext startAsync() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1)
			throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
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
		return EvoSuiteHttpServletRequest.class.getSimpleName() + " [ " + getMethod() + " " + getRequestURI() + " ]";
	}

    // ---------  public methods used only in EvoSuite -------------------


    public void asPOST(){
        setHttpMethod(HttpMethod.POST);
    }

    public void asGET(){
        setHttpMethod(HttpMethod.GET);
    }

    public void asPUT(){
        setHttpMethod(HttpMethod.PUT);
    }

    public void asTextXml(){
        setContentType("text/xml");
    }

    public void asMultipartFormData(){
        //TODO only if POST?
        setContentType("multipart/form-data");
    }

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
