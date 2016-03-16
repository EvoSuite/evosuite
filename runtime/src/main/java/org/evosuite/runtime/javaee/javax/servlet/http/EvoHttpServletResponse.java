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

import org.evosuite.runtime.annotation.EvoSuiteAssertionOnly;
import org.evosuite.runtime.annotation.EvoSuiteClassExclude;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

/**
 * Class used to create HttpServletResponse to unit test Java EE servlet applications.
 *
 * Created by Andrea Arcuri on 20/05/15.
 */
@EvoSuiteClassExclude
public class EvoHttpServletResponse implements HttpServletResponse{

	public static final String WARN_NO_COMMITTED = "WARN: the response was not committed";

	private final List<Byte> buffer;

	private int bufferSize;
	private boolean committed;
	private String encoding;

	private ServletOutputStream stream;
	private PrintWriter writer;
	private String contentType;

	public EvoHttpServletResponse(){
		buffer = new ArrayList<>();
		bufferSize = 1024;
		committed = false;
		encoding = "ISO-8859-1";
		stream = null;
		writer = null;
		contentType = null;
	}


	@Override
	public void flushBuffer() throws IOException {
		committed = true;
		if(stream != null){
			stream.close(); //TODO check if indeed this is the case
		}
		if(writer != null){
			writer.close();
		}
	}

	@Override
	public int getBufferSize() {
		return bufferSize;
	}

	@Override
	public String getCharacterEncoding() {
		return encoding;
	}

	@EvoSuiteAssertionOnly
	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException,IllegalStateException {
		if(writer != null){
			throw new IllegalStateException("Get stream failed because get writer has already been called");
		}

		if(stream == null) {
			stream = new ServletOutputStream() {
				@Override
				public boolean isReady() {
					return true;
				}

				@Override
				public void setWriteListener(WriteListener writeListener) {
					//TODO
				}

				@Override
				public void write(int b) throws IOException {
					buffer.add((byte) b);
				}

				@Override
				public void flush() throws IOException {
					committed = true;
					super.flush();
				}

				@Override
				public void close() throws IOException {
					committed = true;
					super.close();
				}
			};
		}
		return stream;
	}

	@Override
	public PrintWriter getWriter() throws IOException,IllegalStateException {
		//TODO somehow we should use "encoding" here, but maybe not so important for unit testing
		if(writer != null){
			return writer;
		}
		if(stream != null){
			throw new IllegalStateException("Get writer failed because get stream has already been called");
		}
		writer = new PrintWriter(getOutputStream());
		return writer;
	}

	@EvoSuiteAssertionOnly
	@Override
	public boolean isCommitted() {
		return committed;
	}

	@Override
	public void reset() {
		resetBuffer();

		//TODO reset headers and state
	}

	@Override
	public void resetBuffer() {
		if(isCommitted()){
			throw new IllegalStateException("Already committed");
		}
		buffer.clear();
		writer = null;
		stream = null;
	}

	@Override
	public void setBufferSize(int arg0) {

		if(isCommitted() || buffer.size() > 0){
			throw new IllegalStateException("Can only set buffer before writing any output");
		}

		if(arg0 > 0){
			bufferSize = arg0;
		}
	}

	@Override
	public void setCharacterEncoding(String arg0) {
		encoding = arg0;
	}

	@Override
	public void setContentLength(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContentLengthLong(long arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContentType(String arg0) {
		contentType = arg0;
	}

	@Override
	public void setLocale(Locale arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCookie(Cookie arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean containsHeader(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String encodeRedirectURL(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeRedirectUrl(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeURL(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeUrl(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHeader(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaders(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@EvoSuiteAssertionOnly
	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void sendError(int arg0) throws IOException, IllegalStateException {
		sendError(arg0,"");
	}

	@Override
	public void sendError(int arg0, String arg1) throws IOException, IllegalStateException {
		if(isCommitted()){
			throw new IllegalStateException("Response already committed");
		}
		contentType = "text/html";

		resetBuffer();
		PrintWriter out = getWriter();
		out.println("ERROR page. Code: "+arg0+". Message: "+arg1);
		out.close();
	}

	@Override
	public void sendRedirect(String arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStatus(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStatus(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}


	// -----------  public used only in EvoSuite tests -----------

	/**
	 * This method should be used to see what the SUT has written in the
	 * body of the response.
	 * @return
	 */
	@EvoSuiteAssertionOnly
	public String getBody(){

		if(!isCommitted()){
			return WARN_NO_COMMITTED;
		}


		byte[] data = new byte[buffer.size()];
		for(int i=0; i<data.length; i++){
			data[i] = buffer.get(i);
		}
		return new String(data);
	}


	// -----------  private --------------------------------------


}
