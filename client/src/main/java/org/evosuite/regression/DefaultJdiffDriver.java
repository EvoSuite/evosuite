package org.evosuite.regression;

/* 
 * Originally developed by Taweesup Apiwattanapong
 * Copyright (C) 1997-2005 Georgia Institute of Technology
 *
 * This file is part of jdiff.
 *
 * jdiff is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139,
 * USA.
 *
 * $Id: JdiffDriver.java,v 1.4 2005/10/13 21:50:12 term Exp $
 */

import jaba.graph.Edge;
import jaba.graph.Graph;
import jaba.graph.StatementNode;
import jaba.main.DottyOutputSpec;
import jaba.main.Options;
import jaba.main.ResourceFileI;
import jaba.sym.Class;
import jaba.sym.Interface;
import jaba.sym.Method;
import jaba.sym.Program;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.evosuite.Properties;

import jdiff.ClassPair;
import jdiff.DefaultHammockGraphDiffStrategy;
import jdiff.DiffRelationship;
import jdiff.HammockGraphDiffStrategy;
import jdiff.LaskiAndSzermerHammockGraphDiffStrategy;
import jdiff.MethodPair;
import jdiff.NamedReferenceTypePair;
import jdiff.NodePair;
import jdiff.ProgramPair;
import jdiff.strategyImpl.DefaultClassMatchingStrategy;
import jdiff.strategyImpl.DefaultFieldMatchingStrategy;
import jdiff.strategyImpl.DefaultInterfaceMatchingStrategy;
import jdiff.strategyImpl.DefaultMethodMatchingStrategy;
import jdiff.strategyImpl.FromFileClassMatchingStrategy;
import jdiff.strategyImpl.FromFileFieldMatchingStrategy;
import jdiff.strategyImpl.FromFileInterfaceMatchingStrategy;
import jdiff.strategyImpl.FromFileMethodMatchingStrategy;
import jdiff.strategyImpl.InteractiveClassMatchingStrategy;
import jdiff.strategyImpl.InteractiveFieldMatchingStrategy;
import jdiff.strategyImpl.InteractiveInterfaceMatchingStrategy;
import jdiff.strategyImpl.InteractiveMethodMatchingStrategy;
import edu.gatech.cc.aristotle.util.ArgumentParser;


import jaba.tools.Factory;

/**
 * @author term
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class DefaultJdiffDriver implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8468325166825252348L;
	protected static final int DEFAULT_MAXLOOKAHEAD = 20;
	protected static final float DEFAULT_THRESHOLD = 0.2f;
	public static final String CMFNAME = "classmatching.txt";
	public static final String IMFNAME = "interfacematching.txt";
	public static final String MMFNAME = "methodmatching.txt";
	public static final String FMFNAME = "fieldmatching.txt";
	
	public static boolean canExit = true;
	
	public Map<String,List> npMethods;
	
	public static boolean successful = false;
	public int matchedMethods = 0;
	
	public static int numAddedMethods = 0;
	public static int numDeletedMethods = 0;
	public static int numChangedMethods = 0;
	public static int numChangedmethodsAcceptable = 0;
	public static int numSameMethods = 0;
	public static int numFailedMethods = 0;
	public static int numMatchedMethods = 0;

	protected void printUsage() {
		if(true)
		return;
		System.out
				.println("Usage: JABADriver DefaultJdiffDriver [JABA options] -- [options] <old_version_rcfile> "
						+ "<new_version_rcfile>");
		System.out
				.println("Options [-l <level>] [[-f|-i] -d <dir>] [-m <int>] [-t <float>] [-a] [-d] [-f] [-u] [-h] [-q]");
		System.out
				.println("\t -l <level> \t specify level of detail (default:3)");
		System.out.println("\t\t\t 0: program");
		System.out.println("\t\t\t 1: class and interface");
		System.out.println("\t\t\t 2: method and field");
		System.out.println("\t\t\t 3: node (default)");
		System.out.println("\t -f \t\t use mapping information from files");
		System.out
				.println("\t -i \t\t use mapping information from files and ask user when an unmatched entity is found");
		System.out
				.println("\t -d \t\t specify the directory where the files containing mapping information resides");
		System.out
				.println("\t\t\t Files containing mapping information for classes, interfaces, methods, and fields");
		System.out
				.println("\t\t\t must be named 'classmatching.txt', 'interfacematching.txt', 'methodmatching.txt, and");
		System.out.println("\t\t\t 'fieldmatching.txt,' respectively");
		System.out
				.println("\t -a, -d, -f, -u  do NOT print specified types of changes");
		System.out.println("\t\t\t -a: added entities");
		System.out.println("\t\t\t -e: deleted entities");
		System.out.println("\t\t\t -f: modified entity pairs");
		System.out.println("\t\t\t -u: unmodified entity pairs");
		System.out.println("\t -m <int> \t maximum lookahead (default:20)");
		System.out
				.println("\t -t <float> \t hammock similarity threshold [0.0f-1.0f] (default:0.2f)");
		System.out.println("\t -h \t\t print this help message");
		System.out
				.println("\t -q \t\t quiet mode (not asking whether to create dotty files)");
	}

	/*
	 * This method is the starting point for your application. You can think of
	 * it as main() and refer to argument lists from <code> rcs </code> variable
	 * 
	 * @see jaba.main.JABADriver#run()
	 */

	protected void run() throws Throwable {
		try {
			ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(false);
			if (rcs.length < 2 || rcs.length == 1 && rcs[0].equals("-h")) {
				printUsage();
				System.exit(0);
			}
			String[] args = new String[rcs.length - 2];
			System.arraycopy(rcs, 0, args, 0, rcs.length - 2);
			ArgumentParser ap = new ArgumentParser(args);
			String oldRcfile = rcs[rcs.length - 2];
			String newRcfile = rcs[rcs.length - 1];
			int level = Integer.parseInt(ap.getOption("l") == null ? "3" : ap
					.getOption("l"));
			boolean printAdded = ap.getOption("a") == null;
			boolean printDeleted = ap.getOption("d") == null;
			boolean printModified = ap.getOption("f") == null;
			boolean printUnchanged = ap.getOption("u") == null;
			boolean quiet = ap.getOption("q") != null;
			assert options != null;
			ProgramPair pPair = getProgramPair(oldRcfile, newRcfile, args);
	
			System.out.println();
			System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			
			Collection classPairs = pPair.getClassPairs();
			
			for(Object c:classPairs){
				try{
					ClassPair cp = (ClassPair) c;
					System.out.println("Classpair: " + cp.getOldClass().getName().replace('/', '.')  + " | CUT: " + Properties.getTargetClass().getName() + " | Same class: " + cp.getOldClass().getName().replace('/', '.').equals(Properties.getTargetClass().getName()));
					if(!cp.getOldClass().getName().replace('/', '.').equals(Properties.getTargetClass().getName()))
						continue;
					Collection methodPairs = cp.getMethodPairs();
					 
					for(Object m:methodPairs){
						try{
							MethodPair mp = (MethodPair) m;
							//mp.getDiffRel(). 
							System.out.println(mp.getOldMethod().getFullyQualifiedName().replace('/', '.') + mp.getOldMethod().getDescriptor());
							List<RegressionNodePair> rnodePairs = new ArrayList<RegressionNodePair>();
							
							try{
								forbidSystemExitCall();
								Collection nodePairs = mp.getNodePairs();
								
								for(Object n:nodePairs){
									try{
										NodePair np = (NodePair) n;
										if(!(np.getOldNode() instanceof StatementNode)) continue;
										if(!np.getOldNode().getNodeTypeName().equals("Predicate")) continue;
										StatementNode stn1 = (StatementNode) np.getOldNode();
										StatementNode stn2 = (StatementNode) np.getNewNode();
										RegressionNodePair rnp = new RegressionNodePair(stn1.getByteCodeOffset(), stn2.getByteCodeOffset(), np.getDiffRel().getRelationship());
										rnodePairs.add(rnp);
										System.out.println("-----> Node1: getByteCodeOffset-" +  stn1.getByteCodeOffset() + " | nodeNumber-" + np.getOldNode().getNodeNumber() + " -- Node2: getByteCodeOffset-" +  stn2.getByteCodeOffset() + " | nodeNumber-"  + np.getNewNode().getNodeNumber() + " #  Diffrel: " + np.getDiffRel().getRelationship() + "" );
										System.out.println("-----> Node1: getSourceLineNumber-" +  stn1.getSourceLineNumber() + " | nodeNumber-" + np.getOldNode().getNodeNumber() + " -- Node2: getSourceLineNumber-" +  stn2.getSourceLineNumber() + " | nodeNumber-"  + np.getNewNode().getNodeNumber() + " #  Diffrel: " + np.getDiffRel().getRelationship() + "" );
									} catch (Exception e){
										System.out.println("exection on nodepair" + e);
									}
									
								}
							}catch(Exception e){
								System.out.println("Exception on getNodePairs!" + e);
								continue;
							}finally{
								enableSystemExitCall();
							}
							npMethods.put(mp.getOldMethod().getFullyQualifiedName().replace('/', '.') + mp.getOldMethod().getDescriptor(), rnodePairs);
						} catch (Exception e){
							System.out.println("exection on methodpair " + e);
							continue;
						}
					}
				} catch(Exception e){
					System.out.println("exection on classpair " + e);
					continue;
				}
			}
			System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			
			// NodePair np = new NodePair(_oldNode, _newNode, _progPair)
			System.out.println("Checking Interfaces --------------------");
			
			for (Object c : classPairs) {
				ClassPair cp = (ClassPair) c;
				System.out.println("Classpair: "
						+ cp.getOldClass().getName().replace('/', '.')
						+ " | CUT: "
						+ Properties.getTargetClass().getName()
						+ " | Same class: "
						+ cp.getOldClass().getName().replace('/', '.')
								.equals(Properties.getTargetClass().getName()));
				if (!cp.getOldClass().getName().replace('/', '.')
						.equals(Properties.getTargetClass().getName()))
					continue;
				Collection deletedMethods = cp.getDeletedMethods();
				for (Object dm : deletedMethods) {
					numDeletedMethods++;
					Method x = (Method) dm;
					System.out.println("Deleted Method: " + x.getName());
				}
				Collection addedMethods = cp.getAddedMethods();
				for (Object am : addedMethods) {
					numAddedMethods++;
					Method x = (Method) am;
					System.out.println("Added Method: " + x.getName());
				}

				Collection methodPairs = cp.getMethodPairs();

				for (Object m : methodPairs) {
					try {
						forbidSystemExitCall();
						MethodPair mp = (MethodPair) m;
						matchedMethods++;
						if (mp.getDiffRel().getRelationship() == DiffRelationship.PERFECT_MATCH)
							numSameMethods++;
						if (mp.getDiffRel().getRelationship() == DiffRelationship.METHODS_DIFF)
							numChangedmethodsAcceptable++;
						if (mp.getDiffRel().getRelationship() != DiffRelationship.PERFECT_MATCH)
							numChangedMethods++;

						System.out.println("method pair: "
								+ mp.getOldMethod().getName()
								+ " | diff: "
								+ (mp.getDiffRel().getRelationship())
								+ " | slightly changed or same: "
								+ (mp.getDiffRel().getRelationship() == mp
										.getDiffRel().METHODS_DIFF || mp
										.getDiffRel().getRelationship() == mp
										.getDiffRel().PERFECT_MATCH));
						// mp.toString();
					} catch (Exception e) {
						numFailedMethods++;
						try{
						MethodPair mp = (MethodPair) m;
						System.out.println("Exception on getDiffRel of method " + mp.getOldMethod().getName() + " - Error:" + e);
						e.printStackTrace();
						} catch(Exception e2){
							e.printStackTrace();
							System.out.println("problematic methodpair");
							e2.printStackTrace();
						}
						
						continue;
					} finally {
						enableSystemExitCall();
					}
				}

			}
			
			System.out.println("---------------------------------------");
			
			// run getDiffRel() to initiate differencing process
			pPair.getDiffRel();
			// print out differencing status as directed by these parameters
			forbidSystemExitCall();
			try{
			pPair.print(level, printAdded, printDeleted, printModified,
					printUnchanged);
			}catch(Exception e){
				e.printStackTrace();
			}
			enableSystemExitCall();
			// ask for which program pair, class pair, or method pair you want to
			// create dotty file
			if (!quiet) {
				try {
					askDotty(pPair);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
			ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		} catch(Throwable t) {
			System.out.println("JDiff died on me!" + t);
		}
	}
	private static class ExitTrappedException extends SecurityException { }

	public static SecurityManager oldSM;

	private static void forbidSystemExitCall() {

		final SecurityManager securityManager = new SecurityManager() {
			@Override
			public void checkExit(int status) {

				throw new SecurityException();
			}

			public void checkPermission(Permission permission) {
				if ("exitVM".equals(permission.getName())) {
					throw new ExitTrappedException();
				}
			}
		};
		System.setSecurityManager(securityManager);
	}

	private static void enableSystemExitCall() {
		System.setSecurityManager(oldSM);
	}

	static {
		boolean assertsEnabled = false;
		assert assertsEnabled = true; // Intentional side effect!!!
		File file = new File("./.noassertions");
		/*if ((!assertsEnabled) && (!file.exists())) {
			throw new RuntimeException(
					"\n"
							+ "\n********************************************************************"
							+ "\n*                                                                  *"
							+ "\n* JDK >= 1.4 must be used and asserts must be enabled to run JABA! *"
							+ "\n* either use the 'jaba' script provided with the distribution      *"
							+ "\n* or provide the '-ea' switch when invoking java                   *"
							+ "\n*                                                                  *"
							+ "\n********************************************************************"
							+ "\n");
		}*/
	}

	/**
	 * Main driver for the java analysis system. It Creates an object of the
	 * subclass of {@link #JABADriver} specified in the command line and invokes
	 * three methods on the created object: (1) method {@link #init(String[])},
	 * passing all but the first command-line parameters to it; (2) method
	 * {@link #run()}; and (4) method {@link #quit()}
	 * 
	 * @param argv
	 *            Command line arguments. The first argument must be the type of
	 *            the user-defined driver. Although the second argument is
	 *            generally the resouce file for the program to be analyzed, the
	 *            set of parameters from the second on may actually depend on
	 *            the user-defined driver.
	 */
	public static final void main(String[] argv) throws Throwable {
		/*
		 * checkVersion ();
		 * 
		 * for (int arg = 0; arg < argv.length; arg++) { arguments += argv [arg]
		 * + " "; }
		 */
		if (false) {
			try {
				/*
				 * if (argv.length == 0) { System.err.println
				 * ("Please specify the main class"); throw (new
				 * IllegalArgumentException ()); }
				 */

				DefaultJdiffDriver jdriver = (DefaultJdiffDriver) java.lang.Class
						.forName(argv[0]).newInstance();

				String params[] = new String[argv.length - 1];
				for (int i = 0; i < params.length; i++) {
					params[i] = argv[i + 1];
				}
				System.out.println(Arrays.toString(params));

				jdriver.factory = new Factory(true);
				jdriver.options = jdriver.factory.getOptions();
				jdriver.init(params);
				try{
				jdriver.run();
				} catch(Exception e){
					System.out.println("Failed: " + e);
				}
				jdriver.quit();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		DefaultJdiffDriver d = new DefaultJdiffDriver(argv);
		
	}
	
	public DefaultJdiffDriver(String[] argv) {
		//oldSM = System.getSecurityManager();
		
		npMethods = new HashMap<String, List>();
		try {
			/*
			 * if (argv.length == 0) { System.err.println
			 * ("Please specify the main class"); throw (new
			 * IllegalArgumentException ()); }
			 */
			


			/*DefaultJdiffDriver jdriver = (DefaultJdiffDriver) java.lang.Class
					.forName(argv[0]).newInstance();*/
			// [jdiff.main.DefaultJdiffDriver, -if, -l, --, -q, -m, 5, example/loop.v1.jrc, example/loop.v2.jrc]

			String params[] = new String[argv.length - 1];
			for (int i = 0; i < params.length; i++) {
				params[i] = argv[i + 1];
			}
			//System.out.println("HELOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			System.out.println(Arrays.toString(params));

			this.factory = new Factory(true);
			this.options = this.factory.getOptions();
			this.init(params);
			//System.out.print(Properties.SANDBOX_MODE);
			/*
			boolean turnBackOn = false;
			if(Properties.SANDBOX_MODE.equals(SandboxMode.RECOMMENDED)){
				Properties.SANDBOX_MODE = SandboxMode.OFF;
				turnBackOn = true;
			}
			*/
			canExit = false;
			
			    try {
					DefaultJdiffDriver.this.run();
				} catch(Exception e){
					e.printStackTrace();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			   
			 canExit = true;
			 /*if(turnBackOn)
			 Properties.SANDBOX_MODE = SandboxMode.RECOMMENDED;
			*/
			this.quit();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/** Prints the version of JABA. */
	protected static String version() {
		return jaba.tools.local.Factory.getVersion();
	}

	/**
	 * Initializes the java analysis system. Reads in a resource file specifying
	 * all of the class files to be analyzed, loads those classfiles, creates a
	 * {@link Program} object for the system under analysis, saves a reference
	 * to such object in attribute {@link #program}, and saves any unused
	 * argument int array {@link #rcs}.
	 * 
	 * @param argv
	 *            Command line arguments. The first argument specifies the
	 *            resouce file. Additional argument can be used by other methods
	 *            of the class through array {@link #rcs}.
	 */
	protected void init(String[] argv) {
		int i;

		for (i = 0; i < argv.length; i++) {
			if (argv[i].equals("--")) {
				break;
			} else if (argv[i].equals("-l")) {
				options.setCreateLVT(true);
			} else if (argv[i].equals("-re")) {
				options.setAnalyzeRuntimeException(true);
			} else if (argv[i].equals("-le")) {
				options.setAnalyzeLibraryException(true);
			} else if (argv[i].equals("-if")) {
				options.setInlineFinally(true);
			} else if (argv[i].equals("-cr")) {
				options.setCheckForReflection(true);
			} /*else if (argv[i].equals("-rrf")) {
				options.setReadReflection(true);
			} else if (argv[i].equals("-wrf")) {
				options.setWriteReflection(true);
			}*/ else if (argv[i].equals("-v")) {
				System.out.println(version());
				System.exit(0);
			} else if (argv[i].startsWith("-")) {
				usage();
				System.exit(0);
			} else {
				resourceFileName = argv[i];
				ResourceFileI rcFile = factory.getResources(resourceFileName);
				// create and load program object
				program = factory.getProgram(rcFile, options);
			}
		}

		i++;
		int numopts = argv.length - i;
		if (numopts <= 0) {
			rcs = new String[0];
		} else {
			rcs = new String[numopts];
			for (int l = 0; l < numopts; i++, l++) {
				rcs[l] = argv[i];
			}
		}
	}

	/** Prints usage information. */
	protected static void usage() {
		System.out
				.println("\nUsage: <your driver> [OPTION]* [<resource file>]* -- [<user-defined option>]*");
		System.out.println("\nOPTION is any of these:");
		System.out.println("  -v                       "
				+ "output version information and exit.");
		System.out.println("  -l                       "
				+ "recreate local-variable information; without\n"
				+ "                           "
				+ "this flag, the subject must be compiled with\n"
				+ "                           " + "the -g option to javac.");
		System.out.println("  -le                      "
				+ "analyze control flow caused by library exceptions.");
		System.out.println("  -re                      "
				+ "analyze control flow caused by runtime exceptions.");
		System.out.println("  -if                      "
				+ "embed the CFG for a finally block into the CFG of\n"
				+ "                           " + "the containing method.");
		System.out.println("  -cr                      "
				+ "check for and report use of reflection during CFG\n"
				+ "                           " + "construction.");
		System.out.println("  <resource file>*         "
				+ "one or more resource files for the system(s) to");
		System.out.println("                           be analyzed");
		System.out.println("  <user-defined option>*   "
				+ "one or more options used by the user-defined");
		System.out.println("                           driver");
		System.out.println();
	}

	/** Called after code analyzed, but before run () method called. */
	protected void quit() {
	}

	/**
	 * Print out a warning message if the user is not using the most up-to-date
	 * version of JABA
	 */
	private static void checkVersion() {
		String version = version();

		// validate with the server that this is the most recent version
		try {
			int timeout = 1000; // one second
			InetAddress ip = InetAddress.getByName("measure.cc.gt.atl.ga.us");
			SocketAddress socketAddress = new InetSocketAddress(ip, 80);
			Socket s = new Socket();
			s.connect(socketAddress, timeout);
			s.close();

			// Network up!

			String fileURL = "http://measure.cc.gt.atl.ga.us/jaba/versions";
			URL url = new URL(fileURL);

			URLConnection conn = url.openConnection();

			BufferedInputStream bis = new BufferedInputStream(
					conn.getInputStream());

			byte[] buff = new byte[2048];
			int bytesRead;

			String vers = new String();
			while ((bytesRead = bis.read(buff, 0, buff.length)) != -1) {
				vers += new String(buff);
			}
			StringTokenizer st = new StringTokenizer(vers);
			String token = null;
			String latest = null;
			while (st.hasMoreTokens()) {
				token = st.nextToken();
				if (token.equals(version) || token.equals("*********")) {
					latest = st.nextToken();

					if (!latest.equals(version)) {
						System.out
								.println("\nWarning: you are running an old version of JABA:\n"
										+ " your version is \""
										+ version
										+ "\"\n latest version is \""
										+ latest
										+ "\"\nContact jabaadmin@measure.cc.gt.atl.ga.us to get an updated version\n");
					}
					break;
				}
			}
		} catch (Exception e) {
			// Network down; do nothing
		}

	}

	/** ??? */
	public static Throwable exception = null;

	/** ??? */
	public static String arguments = "";

	/** ??? */
	protected static Program program = null;

	/** ??? */
	protected static String rcs[] = null;

	/** ??? */
	protected String resourceFileName = null;

	/** The factory object for retrieving attributes */
	protected Factory factory = null;

	/** The collection of options used to create the Program */
	protected Options options = null;

	protected ProgramPair getProgramPair(String oldRcfile, String newRcfile,
			String[] args) {
		ArgumentParser ap = new ArgumentParser(args);
		int maxLookAhead = ap.getOption("m") == null ? DEFAULT_MAXLOOKAHEAD
				: Integer.parseInt(ap.getOption("m"));
		float threshold = ap.getOption("t") == null ? DEFAULT_THRESHOLD : Float
				.parseFloat(ap.getOption("t"));
		Vector cMatchStrts = new Vector();
		Vector iMatchStrts = new Vector();
		Vector mMatchStrts = new Vector();
		Vector fMatchStrts = new Vector();
		cMatchStrts.add(DefaultClassMatchingStrategy.getInstance());
		iMatchStrts.add(DefaultInterfaceMatchingStrategy.getInstance());
		mMatchStrts.add(DefaultMethodMatchingStrategy.getInstance());
		fMatchStrts.add(DefaultFieldMatchingStrategy.getInstance());
		if (ap.getOption("f") != null) {
			cMatchStrts.add(FromFileClassMatchingStrategy.getInstance());
			iMatchStrts.add(FromFileInterfaceMatchingStrategy.getInstance());
			mMatchStrts.add(FromFileMethodMatchingStrategy.getInstance());
			fMatchStrts.add(FromFileFieldMatchingStrategy.getInstance());
		}
		if (ap.getOption("i") != null) {
			cMatchStrts.add(InteractiveClassMatchingStrategy.getInstance());
			iMatchStrts.add(InteractiveInterfaceMatchingStrategy.getInstance());
			mMatchStrts.add(InteractiveMethodMatchingStrategy.getInstance());
			fMatchStrts.add(InteractiveFieldMatchingStrategy.getInstance());
		}
		HammockGraphDiffStrategy hgDiff = ap.getOption("l") == null ? (HammockGraphDiffStrategy) new DefaultHammockGraphDiffStrategy(
				maxLookAhead, threshold)
				: new LaskiAndSzermerHammockGraphDiffStrategy();
		ProgramPair pp = createProgramPair(oldRcfile, newRcfile, cMatchStrts,
				iMatchStrts, mMatchStrts, fMatchStrts, hgDiff);
		if (ap.getOption("d") != null) {
			if (ap.getOption("f") != null) {
				FromFileClassMatchingStrategy.getInstance().setFilename(pp,
						ap.getOption("d") + File.separator + CMFNAME);
				FromFileInterfaceMatchingStrategy.getInstance().setFilename(pp,
						ap.getOption("d") + File.separator + IMFNAME);
				FromFileMethodMatchingStrategy.getInstance().setFilename(pp,
						ap.getOption("d") + File.separator + MMFNAME);
				FromFileFieldMatchingStrategy.getInstance().setFilename(pp,
						ap.getOption("d") + File.separator + FMFNAME);
			}
			if (ap.getOption("i") != null) {
				InteractiveClassMatchingStrategy.getInstance().setFilename(pp,
						ap.getOption("d") + File.separator + CMFNAME);
				InteractiveInterfaceMatchingStrategy.getInstance().setFilename(
						pp, ap.getOption("d") + File.separator + IMFNAME);
				InteractiveMethodMatchingStrategy.getInstance().setFilename(pp,
						ap.getOption("d") + File.separator + MMFNAME);
				InteractiveFieldMatchingStrategy.getInstance().setFilename(pp,
						ap.getOption("d") + File.separator + FMFNAME);
			}
		}
		return pp;
	}

	protected ProgramPair createProgramPair(String oldRcfile, String newRcfile,
			Vector cMatchStrts, Vector iMatchStrts, Vector mMatchStrts,
			Vector fMatchStrts, HammockGraphDiffStrategy hgDiffStrt) {
		// First, create old and new program objects
		Program oldProg = new jaba.sym.ProgramImpl(
				factory.getResources(oldRcfile), options);
		Program newProg = new jaba.sym.ProgramImpl(
				factory.getResources(newRcfile), options);
		// create the program pair
		return new ProgramPair(oldProg, newProg, cMatchStrts, iMatchStrts,
				mMatchStrts, fMatchStrts, hgDiffStrt);
	}

	/*
	 * ask whether user want to create a dotty file for any program, class,
	 * interface, or method pair This is ugly code. It is not for understanding
	 * but for functionality. The basic idea is to get the program, class or
	 * interface, and method names (signatures) along with the output file name
	 * to create the dotty file. This will loop until '!' is found.
	 */
	private void askDotty(ProgramPair pp) throws IOException {
		String input = "";
		ask: while (!input.equals("!")) {
			System.out
					.println("Which entity pair do you want to create dotty file? <Program Name['#'(ClassName|InterfaceName)['#'MethodSignature]]> or ! to quit");
			System.out.print("% ");
			BufferedReader stdin = new BufferedReader(new InputStreamReader(
					System.in));
			input = stdin.readLine();
			String[] names = input.split("#"); // split input into program,
												// class or interface, and
												// method
												// names (if available)
			if (names[0].equals(pp.getOldProgram().getName())
					|| names[0].equals(pp.getNewProgram().getName())) {
				// program name is valid
				if (names.length == 1) { // if program level dotty is asked
					System.out
							.print("Please type the name of the output file: ");
					input = stdin.readLine();
					pp.createDottyFile(input, new DottyOutputSpec());
					continue;
				} else {
					// search class/interface name in old classes
					for (Iterator ocItr = pp.getOldProgram()
							.getClassesCollection().iterator(); ocItr.hasNext();) {
						Class oldClass = (Class) ocItr.next();
						if (names[1].equals(oldClass.getName())) {
							NamedReferenceTypePair cp = pp
									.getClassPair(oldClass);
							if (cp == null) {
								System.out
										.println("No class pair is associated with "
												+ names[1]);
								continue ask;
							}
							if (names.length == 2) { // if class level dotty is
														// asked
								System.out
										.print("Please type the name of the output file: ");
								input = stdin.readLine();
								cp.createDottyFile(input, new DottyOutputSpec());
								continue ask;
							} else {
								// get method by method name
								Method oldMethod = oldClass.getMethod(names[2]);
								if (oldMethod == null) {
									System.out
											.println(names[2] + " not found.");
									continue ask;
								}
								System.out
										.print("Please type the name of the output file: ");
								input = stdin.readLine();
								MethodPair mp = cp.getMethodPair(oldMethod);
								if (mp == null) {
									System.out
											.println(names[1]
													+ " has no associated method pair.");
									continue ask;
								}
								mp.createDottyFile(input, new DottyOutputSpec());
								continue ask;
							}
						}
					}
					// search class/interface name in new classes
					for (Iterator ncItr = pp.getNewProgram()
							.getClassesCollection().iterator(); ncItr.hasNext();) {
						Class newClass = (Class) ncItr.next();
						if (names[1].equals(newClass.getName())) {
							NamedReferenceTypePair cp = pp
									.getClassPair(newClass);
							if (cp == null) {
								System.out
										.println("No class pair is associated with "
												+ names[1]);
								continue ask;
							}
							if (names.length == 2) { // if class level dotty is
														// asked
								System.out
										.print("Please type the name of the output file: ");
								input = stdin.readLine();
								pp.getClassPair(newClass).createDottyFile(
										input, new DottyOutputSpec());
								continue ask;
							} else {
								// get method by method name
								Method newMethod = newClass.getMethod(names[2]);
								if (newMethod == null) {
									System.out
											.println(names[2] + " not found.");
									continue ask;
								}
								System.out
										.print("Please type the name of the output file: ");
								input = stdin.readLine();
								MethodPair mp = cp.getMethodPair(newMethod);
								if (mp == null) {
									System.out
											.println(names[1]
													+ " has no associated method pair.");
									continue ask;
								}
								mp.createDottyFile(input, new DottyOutputSpec());
								continue ask;
							}
						}
					}
					// search class/interface name in old interfaces
					for (Iterator oiItr = pp.getOldProgram()
							.getInterfacesCollection().iterator(); oiItr
							.hasNext();) {
						Interface oldInterface = (Interface) oiItr.next();
						if (names[1].equals(oldInterface.getName())) {
							NamedReferenceTypePair ip = pp
									.getInterfacePair(oldInterface);
							if (ip == null) {
								System.out
										.println("No interface pair is associated with "
												+ names[1]);
								continue ask;
							}
							if (names.length == 2) { // if interface level dotty
														// is asked
								System.out
										.print("Please type the name of the output file: ");
								input = stdin.readLine();
								ip.createDottyFile(input, new DottyOutputSpec());
								continue ask;
							} else { // get method by method name
								Method oldMethod = oldInterface
										.getMethod(names[2]);
								if (oldMethod == null) {
									System.out
											.println(names[2] + " not found.");
									continue ask;
								}
								System.out
										.print("Please type the name of the output file: ");
								input = stdin.readLine();
								MethodPair mp = ip.getMethodPair(oldMethod);
								if (mp == null) {
									System.out
											.println(names[1]
													+ " has no associated method pair.");
									continue ask;
								}
								mp.createDottyFile(input, new DottyOutputSpec());
								continue ask;
							}
						}
					}
					// search class/interface name in new interfaces
					for (Iterator niItr = pp.getNewProgram()
							.getInterfacesCollection().iterator(); niItr
							.hasNext();) {
						Interface newInterface = (Interface) niItr.next();
						if (names[1].equals(newInterface.getName())) {
							NamedReferenceTypePair ip = pp
									.getInterfacePair(newInterface);
							if (ip == null) {
								System.out
										.println("No interface pair is associated with "
												+ names[1]);
								continue ask;
							}
							if (names.length == 2) { // if interface level dotty
														// is asked
								System.out
										.print("Please type the name of the output file: ");
								input = stdin.readLine();
								ip.createDottyFile(input, new DottyOutputSpec());
								continue ask;
							} else { // get method by method name
								Method newMethod = newInterface
										.getMethod(names[2]);
								if (newMethod == null) {
									System.out
											.println(names[2] + " not found.");
									continue ask;
								}
								System.out
										.print("Please type the name of the output file: ");
								input = stdin.readLine();
								MethodPair mp = ip.getMethodPair(newMethod);
								if (mp == null) {
									System.out
											.println(names[1]
													+ " has no associated method pair.");
									continue ask;
								}
								mp.createDottyFile(input, new DottyOutputSpec());
								continue ask;
							}
						}
					}
				}
			}
		}
	}

	protected static StatementNode getCallNode(StatementNode sn, Graph g) {
		Edge[] inEdges = g.getInEdges(sn);
		for (Edge inEdge : inEdges) {
			if (inEdge.getSource() instanceof StatementNode
					&& ((StatementNode) inEdge.getSource()).getType() == StatementNode.VIRTUAL_METHOD_CALL_NODE) {
				return (StatementNode) inEdge.getSource();
			}
		}
		return null;
	}
}
