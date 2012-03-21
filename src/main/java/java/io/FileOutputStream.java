/*
 * %W% %E%
 * 
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved. ORACLE
 * PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.io;

import java.nio.channels.FileChannel;

import org.apache.commons.vfs2.FileSystemException;

import sun.nio.ch.FileChannelImpl;

/**
 * A file output stream is an output stream for writing data to a
 * <code>File</code> or to a <code>FileDescriptor</code>. Whether or not a file
 * is available or may be created depends upon the underlying platform. Some
 * platforms, in particular, allow a file to be opened for writing by only one
 * <tt>FileOutputStream</tt> (or other file-writing object) at a time. In such
 * situations the constructors in this class will fail if the file involved is
 * already open.
 * 
 * <p>
 * <code>FileOutputStream</code> is meant for writing streams of raw bytes such
 * as image data. For writing streams of characters, consider using
 * <code>FileWriter</code>.
 * 
 * @author Arthur van Hoff
 * @version %I%, %G%
 * @see java.io.File
 * @see java.io.FileDescriptor
 * @see java.io.FileInputStream
 * @since JDK1.0
 */
public class FileOutputStream extends OutputStream {
	/**
	 * The system dependent file descriptor. The value is 1 more than actual
	 * file descriptor. This means that the default value 0 indicates that the
	 * file is not open.
	 */
	private FileDescriptor fd;

	private FileChannel channel = null;

	private boolean append = false;

	// new; gets set in constructor
	private OutputStream ramOutputStream;

	// flag, which decides, if the FileOutputStream instance shall use new or original implementation
	private boolean isOriginal = true;

	private final Object closeLock = new Object();
	private volatile boolean closed = false;

	private static final ThreadLocal<Boolean> runningFinalize = new ThreadLocal<Boolean>();

	// switch between original and new implementation
	public static boolean USE_SIMULATION = false;

	private static boolean isRunningFinalize() {
		Boolean val;
		if ((val = runningFinalize.get()) != null)
			return val.booleanValue();
		return false;
	}

	/**
	 * Creates an output file stream to write to the file with the specified
	 * name. A new <code>FileDescriptor</code> object is created to represent
	 * this file connection.
	 * <p>
	 * First, if there is a security manager, its <code>checkWrite</code> method
	 * is called with <code>name</code> as its argument.
	 * <p>
	 * If the file exists but is a directory rather than a regular file, does
	 * not exist but cannot be created, or cannot be opened for any other reason
	 * then a <code>FileNotFoundException</code> is thrown.
	 * 
	 * @param name
	 *            the system-dependent filename
	 * @exception FileNotFoundException
	 *                if the file exists but is a directory rather than a
	 *                regular file, does not exist but cannot be created, or
	 *                cannot be opened for any other reason
	 * @exception SecurityException
	 *                if a security manager exists and its
	 *                <code>checkWrite</code> method denies write access to the
	 *                file.
	 * @see java.lang.SecurityManager#checkWrite(java.lang.String)
	 */
	public FileOutputStream(String name) throws FileNotFoundException {
		this(name != null ? new File(name) : null, false);
	}

	/**
	 * Creates an output file stream to write to the file with the specified
	 * <code>name</code>. If the second argument is <code>true</code>, then
	 * bytes will be written to the end of the file rather than the beginning. A
	 * new <code>FileDescriptor</code> object is created to represent this file
	 * connection.
	 * <p>
	 * First, if there is a security manager, its <code>checkWrite</code> method
	 * is called with <code>name</code> as its argument.
	 * <p>
	 * If the file exists but is a directory rather than a regular file, does
	 * not exist but cannot be created, or cannot be opened for any other reason
	 * then a <code>FileNotFoundException</code> is thrown.
	 * 
	 * @param name
	 *            the system-dependent file name
	 * @param append
	 *            if <code>true</code>, then bytes will be written to the end of
	 *            the file rather than the beginning
	 * @exception FileNotFoundException
	 *                if the file exists but is a directory rather than a
	 *                regular file, does not exist but cannot be created, or
	 *                cannot be opened for any other reason.
	 * @exception SecurityException
	 *                if a security manager exists and its
	 *                <code>checkWrite</code> method denies write access to the
	 *                file.
	 * @see java.lang.SecurityManager#checkWrite(java.lang.String)
	 * @since JDK1.1
	 */
	public FileOutputStream(String name, boolean append) throws FileNotFoundException {
		this(name != null ? new File(name) : null, append);
	}

	/**
	 * Creates a file output stream to write to the file represented by the
	 * specified <code>File</code> object. A new <code>FileDescriptor</code>
	 * object is created to represent this file connection.
	 * <p>
	 * First, if there is a security manager, its <code>checkWrite</code> method
	 * is called with the path represented by the <code>file</code> argument as
	 * its argument.
	 * <p>
	 * If the file exists but is a directory rather than a regular file, does
	 * not exist but cannot be created, or cannot be opened for any other reason
	 * then a <code>FileNotFoundException</code> is thrown.
	 * 
	 * @param file
	 *            the file to be opened for writing.
	 * @exception FileNotFoundException
	 *                if the file exists but is a directory rather than a
	 *                regular file, does not exist but cannot be created, or
	 *                cannot be opened for any other reason
	 * @exception SecurityException
	 *                if a security manager exists and its
	 *                <code>checkWrite</code> method denies write access to the
	 *                file.
	 * @see java.io.File#getPath()
	 * @see java.lang.SecurityException
	 * @see java.lang.SecurityManager#checkWrite(java.lang.String)
	 */
	public FileOutputStream(File file) throws FileNotFoundException {
		this(file, false);
	}

	/**
	 * Creates a file output stream to write to the file represented by the
	 * specified <code>File</code> object. If the second argument is
	 * <code>true</code>, then bytes will be written to the end of the file
	 * rather than the beginning. A new <code>FileDescriptor</code> object is
	 * created to represent this file connection.
	 * <p>
	 * First, if there is a security manager, its <code>checkWrite</code> method
	 * is called with the path represented by the <code>file</code> argument as
	 * its argument.
	 * <p>
	 * If the file exists but is a directory rather than a regular file, does
	 * not exist but cannot be created, or cannot be opened for any other reason
	 * then a <code>FileNotFoundException</code> is thrown.
	 * 
	 * @param file
	 *            the file to be opened for writing.
	 * @param append
	 *            if <code>true</code>, then bytes will be written to the end of
	 *            the file rather than the beginning
	 * @exception FileNotFoundException
	 *                if the file exists but is a directory rather than a
	 *                regular file, does not exist but cannot be created, or
	 *                cannot be opened for any other reason
	 * @exception SecurityException
	 *                if a security manager exists and its
	 *                <code>checkWrite</code> method denies write access to the
	 *                file.
	 * @see java.io.File#getPath()
	 * @see java.lang.SecurityException
	 * @see java.lang.SecurityManager#checkWrite(java.lang.String)
	 * @since 1.4
	 */
	public FileOutputStream(File file, boolean append) throws FileNotFoundException {
		if (USE_SIMULATION && !file.isOriginal()) {
			this.isOriginal = false;
			String name = (file != null ? file.getPath() : null);
			// leave out security manager check here //
			if (name == null) {
				throw new NullPointerException();
			}
			fd = new FileDescriptor(); // should also work with virtual file
			fd.incrementAndGetUseCount(); // same
			this.append = append;
			try {
				/*
				 * PROBLEM: There may only be a single RamFileOutputStream open for a file at any time, but it is allowed to have multiple
				 * FileOutputStream instances to the same file open! So this will throw an exception, when the real implementation does not, and all
				 * further actions will throw a nullpointer exception for ramOutputStream! // TODO somehow resolve this issue
				 */
				ramOutputStream = file.getRamFile().getContent().getOutputStream(append);
			} catch (FileSystemException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else { // original code
			String name = (file != null ? file.getPath() : null);
			SecurityManager security = System.getSecurityManager();
			if (security != null) {
				security.checkWrite(name);
			}
			if (name == null) {
				throw new NullPointerException();
			}
			fd = new FileDescriptor();
			fd.incrementAndGetUseCount();
			this.append = append;
			if (append) {
				openAppend(name);
			} else {
				open(name);
			}
		}

	}

	/**
	 * Creates an output file stream to write to the specified file descriptor,
	 * which represents an existing connection to an actual file in the file
	 * system.
	 * <p>
	 * First, if there is a security manager, its <code>checkWrite</code> method
	 * is called with the file descriptor <code>fdObj</code> argument as its
	 * argument.
	 * 
	 * @param fdObj
	 *            the file descriptor to be opened for writing
	 * @exception SecurityException
	 *                if a security manager exists and its
	 *                <code>checkWrite</code> method denies write access to the
	 *                file descriptor
	 * @see java.lang.SecurityManager#checkWrite(java.io.FileDescriptor)
	 */
	public FileOutputStream(FileDescriptor fdObj) {
		// TODO check, what to do with this
		// if I see it correctly, FileDescriptor.sync() has possibly to be adjusted // TODO check that
		SecurityManager security = System.getSecurityManager();
		if (fdObj == null) {
			throw new NullPointerException();
		}
		if (security != null) {
			security.checkWrite(fdObj);
		}
		fd = fdObj;

		/*
		 * FileDescriptor is being shared by streams. Ensure that it's GC'ed only when all the streams/channels are done using it.
		 */
		fd.incrementAndGetUseCount();
	}

	/**
	 * Opens a file, with the specified name, for writing.
	 * 
	 * @param name
	 *            name of file to be opened
	 */
	private native void open(String name) throws FileNotFoundException; // not used by simulation

	/**
	 * Opens a file, with the specified name, for appending.
	 * 
	 * @param name
	 *            name of file to be opened
	 */
	private native void openAppend(String name) throws FileNotFoundException; // not used by simulation

	/**
	 * Writes the specified byte to this file output stream. Implements the
	 * <code>write</code> method of <code>OutputStream</code>.
	 * 
	 * @param b
	 *            the byte to be written.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public void write(int b) throws IOException {
		if (USE_SIMULATION && !isOriginal) {
			ramOutputStream.write(b);
		} else {
			// writeOriginal(b); // this will throw an UnsatisfiedLinkError (see writeOriginal()); the following code should solve that problem
			byte[] buffer = new byte[1];
			buffer[0] = (byte) b;
			write(buffer);
		}
	}

	// /**
	// * Writes the specified byte to this file output stream. Implements the <code>write</code> method of <code>OutputStream</code>.
	// *
	// * @param b
	// * the byte to be written.
	// * @exception IOException
	// * if an I/O error occurs.
	// */
	// private native void writeOriginal(int b) throws IOException; // this will throw an UnsatisfiedLinkError, since the C code expects a native
	// method 'write(int b)'

	/**
	 * Writes a sub array as a sequence of bytes.
	 * 
	 * @param b
	 *            the data to be written
	 * @param off
	 *            the start offset in the data
	 * @param len
	 *            the number of bytes that are written
	 * @exception IOException
	 *                If an I/O error has occurred.
	 */
	private native void writeBytes(byte b[], int off, int len) // private and implementation specific - does not need to be simulated
	        throws IOException;

	/**
	 * Writes <code>b.length</code> bytes from the specified byte array to this
	 * file output stream.
	 * 
	 * @param b
	 *            the data.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public void write(byte b[]) throws IOException {
		if (USE_SIMULATION && !isOriginal) {
			ramOutputStream.write(b);
		} else {
			writeBytes(b, 0, b.length);
		}
	}

	/**
	 * Writes <code>len</code> bytes from the specified byte array starting at
	 * offset <code>off</code> to this file output stream.
	 * 
	 * @param b
	 *            the data.
	 * @param off
	 *            the start offset in the data.
	 * @param len
	 *            the number of bytes to write.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public void write(byte b[], int off, int len) throws IOException {
		if (USE_SIMULATION && !isOriginal) {
			ramOutputStream.write(b, off, len);
		} else {
			writeBytes(b, off, len);
		}
	}

	/**
	 * Closes this file output stream and releases any system resources
	 * associated with this stream. This file output stream may no longer be
	 * used for writing bytes.
	 * 
	 * <p>
	 * If this stream has an associated channel then the channel is closed as
	 * well.
	 * 
	 * @exception IOException
	 *                if an I/O error occurs.
	 * 
	 * @revised 1.4
	 * @spec JSR-51
	 */
	@Override
	public void close() throws IOException {
		synchronized (closeLock) {
			if (closed) {
				return;
			}
			closed = true;
		}

		if (channel != null) {
			/*
			 * Decrement FD use count associated with the channel The use count is incremented whenever a new channel is obtained from this stream.
			 */
			fd.decrementAndGetUseCount();
			channel.close();
		}

		/*
		 * Decrement FD use count associated with this stream
		 */
		int useCount = fd.decrementAndGetUseCount();

		if (USE_SIMULATION && !isOriginal) {
			ramOutputStream.close();
		} else {
			/*
			 * If FileDescriptor is still in use by another stream, the finalizer will not close it.
			 */
			if ((useCount <= 0) || !isRunningFinalize()) {
				close0();
			}
		}
	}

	/**
	 * Returns the file descriptor associated with this stream.
	 * 
	 * @return the <code>FileDescriptor</code> object that represents the
	 *         connection to the file in the file system being used by this
	 *         <code>FileOutputStream</code> object.
	 * 
	 * @exception IOException
	 *                if an I/O error occurs.
	 * @see java.io.FileDescriptor
	 */
	public final FileDescriptor getFD() throws IOException {
		if (fd != null)
			return fd;
		throw new IOException();
	}

	/**
	 * Returns the unique {@link java.nio.channels.FileChannel FileChannel}
	 * object associated with this file output stream. </p>
	 * 
	 * <p>
	 * The initial {@link java.nio.channels.FileChannel#position()
	 * </code>position<code>} of the returned channel will be equal to the
	 * number of bytes written to the file so far unless this stream is in
	 * append mode, in which case it will be equal to the size of the file.
	 * Writing bytes to this stream will increment the channel's position
	 * accordingly. Changing the channel's position, either explicitly or by
	 * writing, will change this stream's file position.
	 * 
	 * @return the file channel associated with this file output stream
	 * 
	 * @since 1.4
	 * @spec JSR-51
	 */
	public FileChannel getChannel() { // TODO simulate FileChannel
		synchronized (this) {
			if (channel == null) {
				channel = FileChannelImpl.open(fd, false, true, this, append);

				/*
				 * Increment fd's use count. Invoking the channel's close() method will result in decrementing the use count set for the channel.
				 */
				fd.incrementAndGetUseCount();
			}
			return channel;
		}
	}

	/**
	 * Cleans up the connection to the file, and ensures that the
	 * <code>close</code> method of this file output stream is called when there
	 * are no more references to this stream.
	 * 
	 * @exception IOException
	 *                if an I/O error occurs.
	 * @see java.io.FileInputStream#close()
	 */
	@Override
	protected void finalize() throws IOException { // should also work with simulation
		if (fd != null) {
			if (fd == FileDescriptor.out || fd == FileDescriptor.err) {
				flush();
			} else {

				/*
				 * Finalizer should not release the FileDescriptor if another stream is still using it. If the user directly invokes close() then the
				 * FileDescriptor is also released.
				 */
				runningFinalize.set(Boolean.TRUE);
				try {
					close();
				} finally {
					runningFinalize.set(Boolean.FALSE);
				}
			}
		}
	}

	private native void close0() throws IOException;

	// not sure, if this could compromise simulation // TODO check that
	private static native void initIDs();

	static {
		initIDs();
	}

}
