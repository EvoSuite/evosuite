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
 * A <code>FileInputStream</code> obtains input bytes from a file in a file
 * system. What files are available depends on the host environment.
 * 
 * <p>
 * <code>FileInputStream</code> is meant for reading streams of raw bytes such
 * as image data. For reading streams of characters, consider using
 * <code>FileReader</code>.
 * 
 * @author Arthur van Hoff
 * @version %I%, %G%
 * @see java.io.File
 * @see java.io.FileDescriptor
 * @see java.io.FileOutputStream
 * @since JDK1.0
 */
public class FileInputStream extends InputStream {
	/* File Descriptor - handle to the open file */
	private FileDescriptor fd;

	private FileChannel channel = null;

	private final Object closeLock = new Object();
	private volatile boolean closed = false;

	// new; gets set in constructor
	private InputStream ramInputStream;

	// flag, which decides, if the FileOutputStream instance shall use new or original implementation
	private boolean isOriginal = true;

	private static final ThreadLocal<Boolean> runningFinalize = new ThreadLocal<Boolean>();

	// switch between new and original implementation
	public static boolean USE_SIMULATION = false;

	private static boolean isRunningFinalize() {
		Boolean val;
		if ((val = runningFinalize.get()) != null)
			return val.booleanValue();
		return false;
	}

	/**
	 * Creates a <code>FileInputStream</code> by opening a connection to an
	 * actual file, the file named by the path name <code>name</code> in the
	 * file system. A new <code>FileDescriptor</code> object is created to
	 * represent this file connection.
	 * <p>
	 * First, if there is a security manager, its <code>checkRead</code> method
	 * is called with the <code>name</code> argument as its argument.
	 * <p>
	 * If the named file does not exist, is a directory rather than a regular
	 * file, or for some other reason cannot be opened for reading then a
	 * <code>FileNotFoundException</code> is thrown.
	 * 
	 * @param name
	 *            the system-dependent file name.
	 * @exception FileNotFoundException
	 *                if the file does not exist, is a directory rather than a
	 *                regular file, or for some other reason cannot be opened
	 *                for reading.
	 * @exception SecurityException
	 *                if a security manager exists and its
	 *                <code>checkRead</code> method denies read access to the
	 *                file.
	 * @see java.lang.SecurityManager#checkRead(java.lang.String)
	 */
	public FileInputStream(String name) throws FileNotFoundException {
		this(name != null ? new File(name) : null);
	}

	/**
	 * Creates a <code>FileInputStream</code> by opening a connection to an
	 * actual file, the file named by the <code>File</code> object
	 * <code>file</code> in the file system. A new <code>FileDescriptor</code>
	 * object is created to represent this file connection.
	 * <p>
	 * First, if there is a security manager, its <code>checkRead</code> method
	 * is called with the path represented by the <code>file</code> argument as
	 * its argument.
	 * <p>
	 * If the named file does not exist, is a directory rather than a regular
	 * file, or for some other reason cannot be opened for reading then a
	 * <code>FileNotFoundException</code> is thrown.
	 * 
	 * @param file
	 *            the file to be opened for reading.
	 * @exception FileNotFoundException
	 *                if the file does not exist, is a directory rather than a
	 *                regular file, or for some other reason cannot be opened
	 *                for reading.
	 * @exception SecurityException
	 *                if a security manager exists and its
	 *                <code>checkRead</code> method denies read access to the
	 *                file.
	 * @see java.io.File#getPath()
	 * @see java.lang.SecurityManager#checkRead(java.lang.String)
	 */
	public FileInputStream(File file) throws FileNotFoundException {
		if (USE_SIMULATION && !file.isOriginal()) {
			this.isOriginal = false;
			String name = (file != null ? file.getPath() : null);
			// leave out security manager check here //
			if (name == null) {
				throw new NullPointerException();
			}
			fd = new FileDescriptor(); // should also work with virtual file
			fd.incrementAndGetUseCount(); // same
			try {
				/*
				 * PROBLEM: There may only be a single RamFileInputStream open for a file at any time, but it is allowed to have multiple
				 * FileInputStream instances to the same file open! So this will throw an exception, when the real implementation does not, and all
				 * further actions would throw a nullpointer exception for access on ramInputStream! // TODO somehow resolve this issue
				 */
				ramInputStream = file.getRamFile().getContent().getInputStream();
			} catch (FileSystemException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else { // original code
			String name = (file != null ? file.getPath() : null);
			SecurityManager security = System.getSecurityManager();
			if (security != null) {
				security.checkRead(name);
			}
			if (name == null) {
				throw new NullPointerException();
			}
			fd = new FileDescriptor();
			fd.incrementAndGetUseCount();
			open(name);
		}

	}

	/**
	 * Creates a <code>FileInputStream</code> by using the file descriptor
	 * <code>fdObj</code>, which represents an existing connection to an actual
	 * file in the file system.
	 * <p>
	 * If there is a security manager, its <code>checkRead</code> method is
	 * called with the file descriptor <code>fdObj</code> as its argument to see
	 * if it's ok to read the file descriptor. If read access is denied to the
	 * file descriptor a <code>SecurityException</code> is thrown.
	 * <p>
	 * If <code>fdObj</code> is null then a <code>NullPointerException</code> is
	 * thrown.
	 * 
	 * @param fdObj
	 *            the file descriptor to be opened for reading.
	 * @throws SecurityException
	 *             if a security manager exists and its <code>checkRead</code>
	 *             method denies read access to the file descriptor.
	 * @see SecurityManager#checkRead(java.io.FileDescriptor)
	 */
	public FileInputStream(FileDescriptor fdObj) {
		// TODO same issues as in FileOutputStream
		SecurityManager security = System.getSecurityManager();
		if (fdObj == null) {
			throw new NullPointerException();
		}
		if (security != null) {
			security.checkRead(fdObj);
		}
		fd = fdObj;

		/*
		 * FileDescriptor is being shared by streams. Ensure that it's GC'ed only when all the streams/channels are done using it.
		 */
		fd.incrementAndGetUseCount();
	}

	/**
	 * Opens the specified file for reading.
	 * 
	 * @param name
	 *            the name of the file
	 */
	private native void open(String name) throws FileNotFoundException; // not used by simulation

	/**
	 * Reads a byte of data from this input stream. This method blocks if no
	 * input is yet available.
	 * 
	 * @return the next byte of data, or <code>-1</code> if the end of the file
	 *         is reached.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public int read() throws IOException {
		if (USE_SIMULATION && !isOriginal) {
			return ramInputStream.read();
		} else {
			// return readOrignal(); // this will throw an UnsatisfiedLinkError (see readOriginal()); the following code should quickfix that problem
			byte[] buffer = new byte[1];
			if (read(buffer) == -1) {
				return -1;
			} else {
				return buffer[0];
			}
		}
	}

	// /**
	// * Reads a byte of data from this input stream. This method blocks if no input is yet available.
	// *
	// * @return the next byte of data, or <code>-1</code> if the end of the file is reached.
	// * @exception IOException
	// * if an I/O error occurs.
	// */
	// private native int readOriginal() throws IOException; // this will throw an UnsatisfiedLinkError, since the C code expects a native method
	// 'read()'

	/**
	 * Reads a subarray as a sequence of bytes.
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
	private native int readBytes(byte b[], int off, int len) throws IOException;

	/**
	 * Reads up to <code>b.length</code> bytes of data from this input stream
	 * into an array of bytes. This method blocks until some input is available.
	 * 
	 * @param b
	 *            the buffer into which the data is read.
	 * @return the total number of bytes read into the buffer, or
	 *         <code>-1</code> if there is no more data because the end of the
	 *         file has been reached.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public int read(byte b[]) throws IOException {
		if (USE_SIMULATION && !isOriginal) {
			return ramInputStream.read(b);
		} else {
			return readBytes(b, 0, b.length);
		}
	}

	/**
	 * Reads up to <code>len</code> bytes of data from this input stream into an
	 * array of bytes. If <code>len</code> is not zero, the method blocks until
	 * some input is available; otherwise, no bytes are read and <code>0</code>
	 * is returned.
	 * 
	 * @param b
	 *            the buffer into which the data is read.
	 * @param off
	 *            the start offset in the destination array <code>b</code>
	 * @param len
	 *            the maximum number of bytes read.
	 * @return the total number of bytes read into the buffer, or
	 *         <code>-1</code> if there is no more data because the end of the
	 *         file has been reached.
	 * @exception NullPointerException
	 *                If <code>b</code> is <code>null</code>.
	 * @exception IndexOutOfBoundsException
	 *                If <code>off</code> is negative, <code>len</code> is
	 *                negative, or <code>len</code> is greater than
	 *                <code>b.length - off</code>
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public int read(byte b[], int off, int len) throws IOException {
		if (USE_SIMULATION && !isOriginal) {
			return ramInputStream.read(b, off, len);
		} else {
			return readBytes(b, off, len);
		}
	}

	/**
	 * Skips over and discards <code>n</code> bytes of data from the input
	 * stream.
	 * 
	 * <p>
	 * The <code>skip</code> method may, for a variety of reasons, end up
	 * skipping over some smaller number of bytes, possibly <code>0</code>. If
	 * <code>n</code> is negative, an <code>IOException</code> is thrown, even
	 * though the <code>skip</code> method of the {@link InputStream} superclass
	 * does nothing in this case. The actual number of bytes skipped is
	 * returned.
	 * 
	 * <p>
	 * This method may skip more bytes than are remaining in the backing file.
	 * This produces no exception and the number of bytes skipped may include
	 * some number of bytes that were beyond the EOF of the backing file.
	 * Attempting to read from the stream after skipping past the end will
	 * result in -1 indicating the end of the file.
	 * 
	 * @param n
	 *            the number of bytes to be skipped.
	 * @return the actual number of bytes skipped.
	 * @exception IOException
	 *                if n is negative, if the stream does not support seek, or
	 *                if an I/O error occurs.
	 */
	@Override
	public long skip(long n) throws IOException {
		if (USE_SIMULATION && !isOriginal) {
			return ramInputStream.skip(n);
		} else {
			return n; // FIXME JUST A STUB - REMOVE ASAP!
			//			return skipOriginal(n); // this will throw an UnsatisfiedLinkError (see skipOriginal())
		}
	}

	/**
	 * EvoSuite redirects method calls to {@link #skip(long)} to this method by
	 * bytecode instrumentation. skipNew() then decides, if to use the new or
	 * the original implementation; normally, I would have simply replaced
	 * skip(), but then the link to the native C code for skip() gets lost.
	 * 
	 * @param n
	 *            the number of bytes to be skipped.
	 * @return the actual number of bytes skipped.
	 * @exception IOException
	 *                if n is negative, if the stream does not support seek, or
	 *                if an I/O error occurs.
	 */
	public long skipNew(long n) throws IOException {
		if (USE_SIMULATION && !isOriginal) {
			return ramInputStream.skip(n);
		} else {
			return skip(n); // this assumes, skip(long) is the original native method // TODO remove this comment, when fixed
		}
	}

	/**
	 * Skips over and discards <code>n</code> bytes of data from the input
	 * stream.
	 * 
	 * <p>
	 * The <code>skip</code> method may, for a variety of reasons, end up
	 * skipping over some smaller number of bytes, possibly <code>0</code>. If
	 * <code>n</code> is negative, an <code>IOException</code> is thrown, even
	 * though the <code>skip</code> method of the {@link InputStream} superclass
	 * does nothing in this case. The actual number of bytes skipped is
	 * returned.
	 * 
	 * <p>
	 * This method may skip more bytes than are remaining in the backing file.
	 * This produces no exception and the number of bytes skipped may include
	 * some number of bytes that were beyond the EOF of the backing file.
	 * Attempting to read from the stream after skipping past the end will
	 * result in -1 indicating the end of the file.
	 * 
	 * @param n
	 *            the number of bytes to be skipped.
	 * @return the actual number of bytes skipped.
	 * @exception IOException
	 *                if n is negative, if the stream does not support seek, or
	 *                if an I/O error occurs.
	 */
	private native long skipOriginal(long n) throws IOException; // this will throw an UnsatisfiedLinkError, since the C code
	                                                             // expects a native method 'skip(long n)'

	/**
	 * Returns an estimate of the number of remaining bytes that can be read (or
	 * skipped over) from this input stream without blocking by the next
	 * invocation of a method for this input stream. The next invocation might
	 * be the same thread or another thread. A single read or skip of this many
	 * bytes will not block, but may read or skip fewer bytes.
	 * 
	 * <p>
	 * In some cases, a non-blocking read (or skip) may appear to be blocked
	 * when it is merely slow, for example when reading large files over slow
	 * networks.
	 * 
	 * @return an estimate of the number of remaining bytes that can be read (or
	 *         skipped over) from this input stream without blocking.
	 * @exception IOException
	 *                if this file input stream has been closed by calling
	 *                {@code close} or an I/O error occurs.
	 */
	@Override
	public int available() throws IOException {
		if (USE_SIMULATION && !isOriginal) {
			return ramInputStream.available();
		} else {
			return 300; // FIXME JUST A STUB - REMOVE ASAP!
			// return availableOriginal(); // this will throw an UnsatisfiedLinkError for the same reason as skipOriginal() // TODO same as in
			// skipOriginal()
		}
	}

	/**
	 * EvoSuite redirects method calls to {@link #available()} to this method by
	 * bytecode instrumentation. availableNew() then decides, if to use the new
	 * or the original implementation; normally, I would have simply replaced
	 * available(), but then the link to the native C code for available() gets
	 * lost.
	 * 
	 * @return an estimate of the number of remaining bytes that can be read (or
	 *         skipped over) from this input stream without blocking.
	 * @throws IOException
	 */
	public int availableNew() throws IOException {
		if (USE_SIMULATION && !isOriginal) {
			return ramInputStream.available();
		} else {
			return available(); // this assumes, available() is the original native method // TODO remove this comment, when fixed
		}
	}

	/**
	 * Returns an estimate of the number of remaining bytes that can be read (or
	 * skipped over) from this input stream without blocking by the next
	 * invocation of a method for this input stream. The next invocation might
	 * be the same thread or another thread. A single read or skip of this many
	 * bytes will not block, but may read or skip fewer bytes.
	 * 
	 * <p>
	 * In some cases, a non-blocking read (or skip) may appear to be blocked
	 * when it is merely slow, for example when reading large files over slow
	 * networks.
	 * 
	 * @return an estimate of the number of remaining bytes that can be read (or
	 *         skipped over) from this input stream without blocking.
	 * @exception IOException
	 *                if this file input stream has been closed by calling
	 *                {@code close} or an I/O error occurs.
	 */
	private native int availableOriginal() throws IOException; // this will throw an UnsatisfiedLinkError for the same reason as skipOriginal()

	/**
	 * Closes this file input stream and releases any system resources
	 * associated with the stream.
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
			 * Decrement the FD use count associated with the channel The use count is incremented whenever a new channel is obtained from this
			 * stream.
			 */
			fd.decrementAndGetUseCount();
			channel.close();
		}

		/*
		 * Decrement the FD use count associated with this stream
		 */
		int useCount = fd.decrementAndGetUseCount();

		if (USE_SIMULATION && !isOriginal) {
			ramInputStream.close();
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
	 * Returns the <code>FileDescriptor</code> object that represents the
	 * connection to the actual file in the file system being used by this
	 * <code>FileInputStream</code>.
	 * 
	 * @return the file descriptor object associated with this stream.
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
	 * object associated with this file input stream.
	 * 
	 * <p>
	 * The initial {@link java.nio.channels.FileChannel#position()
	 * </code>position<code>} of the returned channel will be equal to the
	 * number of bytes read from the file so far. Reading bytes from this stream
	 * will increment the channel's position. Changing the channel's position,
	 * either explicitly or by reading, will change this stream's file position.
	 * 
	 * @return the file channel associated with this file input stream
	 * 
	 * @since 1.4
	 * @spec JSR-51
	 */
	public FileChannel getChannel() {
		synchronized (this) {
			if (channel == null) {
				channel = FileChannelImpl.open(fd, true, false, this);

				/*
				 * Increment fd's use count. Invoking the channel's close() method will result in decrementing the use count set for the channel.
				 */
				fd.incrementAndGetUseCount();

			}
			return channel;
		}
	}

	// not sure, if this could compromise simulation // TODO check that
	private static native void initIDs();

	private native void close0() throws IOException;

	static {
		initIDs();
	}

	/**
	 * Ensures that the <code>close</code> method of this file input stream is
	 * called when there are no more references to it.
	 * 
	 * @exception IOException
	 *                if an I/O error occurs.
	 * @see java.io.FileInputStream#close()
	 */
	@Override
	protected void finalize() throws IOException { // should also work with simulation
		if ((fd != null) && (fd != FileDescriptor.in)) {

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
