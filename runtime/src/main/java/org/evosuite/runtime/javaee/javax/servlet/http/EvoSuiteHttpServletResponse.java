package org.evosuite.runtime.javaee.javax.servlet.http;

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
public class EvoSuiteHttpServletResponse implements HttpServletResponse{

	public static final String WARN_NO_COMMITTED = "WARN: the response was not committed";

	private final List<Byte> buffer;

	private int bufferSize;
	private boolean committed;
	private String encoding;

	private ServletOutputStream stream;
	private PrintWriter writer;

	public EvoSuiteHttpServletResponse(){
		buffer = new ArrayList<>();
		bufferSize = 1024;
		committed = false;
		encoding = "ISO-8859-1";
		stream = null;
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

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if(writer != null){
			return null; //TODO unclear what to do if writer is already on
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
	public PrintWriter getWriter() throws IOException {
		//TODO somehow we should use "encoding" here, but maybe not so important for unit testing
		if(writer != null){
			return writer;
		}
		if(stream != null){
			return null; //TODO unclear what to do here
		}
		writer = new PrintWriter(getOutputStream());
		return writer;
	}

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
		// TODO Auto-generated method stub

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

	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void sendError(int arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendError(int arg0, String arg1) throws IOException {
		// TODO Auto-generated method stub

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
