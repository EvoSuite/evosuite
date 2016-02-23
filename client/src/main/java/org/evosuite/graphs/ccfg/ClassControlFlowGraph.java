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
package org.evosuite.graphs.ccfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.graphs.EvoSuiteGraph;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.ccg.ClassCallGraph;
import org.evosuite.graphs.ccg.ClassCallNode;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.ControlFlowEdge;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.evosuite.utils.JdkPureMethodsList;
import org.objectweb.asm.Type;

/**
 * This class computes the Class Control Flow Graph (CCFG) of a CUT.
 * 
 * Given the ClassCallGraph the CCFG is generated as follows:
 * 
 * The RawControlFlowGraph (CFG) of each method in the target class is retrieved
 * from the GraphPool and imported into this CCFG. BytecodeInstructions are
 * imported as CCFGCodeNodes and ControlFlowEdges as CCFGCodeEdges. Additionally
 * each CFG is enclosed by a CCFGMethodEntryNode and CCFGMethodExitNode with an
 * edge from the entry node to the first instruction in the CFG and an edge from
 * each exit instruction in the CFG to the exit node.
 * 
 * After that each method call instruction as defined in
 * BytecodeInstruction.isMethodCall() is replaced by two new nodes
 * CCFGMethodCallNode and CCFGMethodReturnNode that are labeled with that call
 * instruction. Each incoming edge to the previous CCFGCodeNode is redirected to
 * the CCFGMethodCallNode and each outgoing edge from the previous node is
 * redirected to the CCFGMethodReturnNode. Then two CCFGMethodCallEdges are
 * added. One from the CCFGMethodCallNode to the CCFGMethodEntryNode of the
 * called method and one from that methods CCFGMethodExitNode to the
 * CCFGMethodReturnNode. NOTE: Not every method call is replaced like this. Only
 * calls to methods of the class this CCFG is created for that are either static
 * methods - as defined by BytecodeInstruction.isStaticMethodCall() - or calls
 * to methods on the same object (this) as defined by
 * BytecodeInstruction.isMethodCallOnSameObject().
 * 
 * All this is enclosed by a frame consisting of five CCFGFrameNodes of
 * different types. This frame has two dedicated ENTRY and EXIT nodes connected
 * via a third node LOOP. The LOOP node has an outgoing edge to CALL which in
 * turn has an outgoing edge to each CCFGMethodEntryNode of each public method
 * in this graph. Analogously the CCFGMethodExitNode of each public method has
 * an outgoing edge to the CCFGFrameNode RETURN which in turn has an outgoing
 * edge back to LOOP. All these edges are CCFGFrameEdges.
 * 
 * The frame simulates the possible calls to the CUT a test can potentially
 * make. After starting (ENTRY->LOOP) a test can make arbitrary calls to public
 * methods (LOOP->CALL) that can in turn call other methods of the class
 * (CCFGMethodCallEdges). After returning from a public method call
 * (RETURN->LOOP) the test can either make more calls to the class (LOOP->CALL)
 * or stop (LOOP->EXIT).
 * 
 * The construction of the CCFG is inspired by: Proc. of the Second ACM SIGSOFT
 * Symp. on the Foundations of Softw. Eng., December 1994, pages 154-164
 * "Performing Data Flow Testing on Classes" Mary Jean Harrold and Gregg
 * Rothermel, Section 5. The resulting CCFG should be as described in the paper
 * but our construction differs a little (we don't import the CCG and then
 * replace method nodes with CFGs but rather import CFGs and connect them
 * directly).
 * 
 * @author Andre Mis
 */
public class ClassControlFlowGraph extends EvoSuiteGraph<CCFGNode, CCFGEdge> {

	public enum FrameNodeType {
		ENTRY, EXIT, LOOP, CALL, RETURN
	};

	private final String className;
	private final ClassCallGraph ccg;
	private final ClassLoader classLoader;
	

	private Map<String, CCFGMethodEntryNode> methodEntries = new HashMap<String, CCFGMethodEntryNode>();
	private Map<String, CCFGMethodExitNode> methodExits = new HashMap<String, CCFGMethodExitNode>();

	public Set<CCFGMethodEntryNode> publicMethods = new HashSet<CCFGMethodEntryNode>();

	private Map<FrameNodeType, CCFGFrameNode> frameNodes = new HashMap<FrameNodeType, CCFGFrameNode>();

	// cache of already analyzed methods that are known to be pure or impure
	// respectively
	private Set<String> pureMethods = new HashSet<String>();
	private Set<String> impureMethods = new HashSet<String>();

	// auxilary set for purity analysis to keep track of methods that are
	// currently
	// being analyzed across several CCFGs. elements are of the form
	// <className>.<methodName>
	private static Set<String> methodsInPurityAnalysis = new HashSet<String>();


	/**
	 * Given the ClassCallGraph of a class this constructor will build up the
	 * corresponding CCFG using the RCFGs from the GraphPool.
	 * 
	 * @param ccg
	 *            a {@link org.evosuite.graphs.ccg.ClassCallGraph} object.
	 */
	public ClassControlFlowGraph(ClassCallGraph ccg) {
		super(CCFGEdge.class);
		this.className = ccg.getClassName();
		this.ccg = ccg;
		this.classLoader = ccg.getClassLoader();
		nicenDotOutput();
		compute();
	}





	// purity analysis

	public boolean isPure(String methodName) {
		if (pureMethods.contains(methodName))
			return true;
		else if (impureMethods.contains(methodName))
			return false;

		boolean isPure = analyzePurity(methodName);
		if (isPure) {
			pureMethods.add(methodName);
			return true;
		} else {
			impureMethods.add(methodName);
			return false;
		}
	}

	private boolean analyzePurity(String methodName) {

		if (!methodEntries.containsKey(methodName)) {
			// workaround to deal with abstract methods for now
			// default behaviour for unknown things is "pure" for now
			return true;
		}

		CCFGMethodEntryNode entry = getMethodEntryOf(methodName);
		Set<CCFGNode> handled = new HashSet<CCFGNode>();

		// LoggingUtils.getEvoLogger().info(
		// "Starting purity analysis of " + methodName);

		// add methodName to set of currently analyzed methods
		methodsInPurityAnalysis.add(className + "." + methodName);
		boolean r = analyzePurity(methodName, entry, handled);
		// remove methodName from set of currently analyzed methods
		methodsInPurityAnalysis.remove(className + "." + methodName);

		return r;
	}

	private boolean analyzePurity(String analyzedMethod, CCFGNode currentNode,
			Set<CCFGNode> handled) {

		if (handled.contains(currentNode)) {
			// if we already handled the node we know it is pure otherwise we
			// would have returned
			return true;
		}
		handled.add(currentNode);

		// the node at which analysis is supposed to continue
		// used for skipping intermediate nodes for CCFGMethodCallNodes
		CCFGNode nextNode = currentNode;
		
		if (currentNode instanceof CCFGFieldClassCallNode) {
			CCFGFieldClassCallNode fieldCall = (CCFGFieldClassCallNode) currentNode;
			// TODO for now we will have to ignore classes that we are not able
			// to analyze.
			// this should only happen for classes in java.*
			String toAnalyze = fieldCall.getClassName() + "."
					+ fieldCall.getMethodName();
			if (GraphPool.getInstance(classLoader).canMakeCCFGForClass(fieldCall.getClassName())) {
				
				if (!methodsInPurityAnalysis.contains(toAnalyze)) {
					ClassControlFlowGraph ccfg = GraphPool.getInstance(classLoader).getCCFG(fieldCall
							.getClassName());
					if (!ccfg.isPure(fieldCall.getMethodName())) {
						// if fieldCall is impure this method is also impure
						return false;
					}
				}
			}
			
			else{

				//The format that ASM for types and the one used in my data file is different: in particular ASM uses the 
				//Class.getName format for types see http://docs.oracle.com/javase/6/docs/api/java/lang/Class.html#getName(), while the data
				//file with the pure methods uses the qualified name. 
				//For instance, in my file is: java.blabla.ClassExample.method(java.util.List,java.lang.Class[])
				//ASM returns java.blabla.ClassExample.method(Ljava.util.List;[Ljava.lang.Class)V.
				//The conversion from qualified name to the JVM/ASM format is not so straightforward, 
				//well it's not a very complicate problem but there some corner cases that I have to check. 
				//In the mean time this method convert the ASM/JVM format into the normal one, using an utility 
				//of ASM.
				//The file with the method list is in src/resources, it SHOULD be accurate but not perfect, some methods are missing for sure.
				
				if(toAnalyze.startsWith("java.")){
					 
					Type[] parameters = org.objectweb.asm.Type.getArgumentTypes(fieldCall.getOnlyParameters());
					String newParams = "";
					if(parameters.length!=0){
						for (Type i : parameters) {
							newParams = newParams + "," + i.getClassName();
						}
						newParams = newParams.substring(1, newParams.length());
					}
					toAnalyze=fieldCall.getClassName() + "." + fieldCall.getOnlyMethodName()+"("+newParams+")";
					
					return JdkPureMethodsList.instance.checkPurity(toAnalyze);
				}
			}
			
			// otherwise proceed
		} else if (currentNode instanceof CCFGCodeNode) {
			CCFGCodeNode codeNode = (CCFGCodeNode) currentNode;
			// it this node alters the state of this object this method is
			// impure
			if (codeNode.getCodeInstruction().isFieldDefinition())
				return false;
			// otherwise proceed
		} else if (currentNode instanceof CCFGMethodExitNode) {
			CCFGMethodExitNode methodExit = (CCFGMethodExitNode) currentNode;
			// if we encounter the end of the analyzed method and have not
			// detected
			// impurity yet then the method is pure
			if (methodExit.getMethod().equals(analyzedMethod))
				return true;
			else
				throw new IllegalStateException(
						"MethodExitNodes from methods other then the currently analyzed one should not be reached");
		} else if (currentNode instanceof CCFGMethodCallNode) {
			CCFGMethodCallNode callNode = (CCFGMethodCallNode) currentNode;
			// avoid loops in analysis
			String toAnalyze = className + "." + callNode.getCalledMethod();
			if (!methodsInPurityAnalysis.contains(toAnalyze)) {
				// if another method of this class is called check that
				// method
				// it the called method is impure then this method is impure
				if (!isPure(callNode.getCalledMethod()))
					return false;
			}
			// otherwise proceed after the method call has taken place
			nextNode = callNode.getReturnNode();
		} else if (currentNode instanceof CCFGMethodEntryNode) {
			// do nothing special
		} else
			throw new IllegalStateException(
					"purity analysis should not reach this kind of CCFGNode: "
							+ currentNode.getClass().toString());

		Set<CCFGNode> children = getChildren(nextNode);
		for (CCFGNode child : children) {
			if (!analyzePurity(analyzedMethod, child, handled))
				return false;
		}

		// no child was impure so this method is pure
		return true;
	}

	// sanity functions


	public boolean isPublicMethod(String method) {
		if (method == null)
			return false;
		CCFGMethodEntryNode entry = getMethodEntryOf(method);
		return isPublicMethod(entry);
	}

	public boolean isPublicMethod(CCFGMethodEntryNode node) {
		if (node == null)
			return false;
		return publicMethods.contains(node);
	}


	// convenience getters

	public CCFGMethodEntryNode getMethodEntryNodeForClassCallNode(
			ClassCallNode ccgNode) {
		CCFGMethodEntryNode r = methodEntries.get(ccgNode.getMethod());
		if (r == null)
			throw new IllegalStateException(
					"expect the CCFG to contain a CCFGMethodEntryNode for each node in the corresponding CCG "
							+ ccgNode.getMethod());
		return r;
	}

	private CCFGMethodEntryNode getMethodEntryOf(String method) {
		CCFGMethodEntryNode r = methodEntries.get(method);
		if (r == null)
			throw new IllegalArgumentException("unknown method: " + method);
		return r;

	}

	private RawControlFlowGraph getRCFG(ClassCallNode ccgNode) {
		return GraphPool.getInstance(classLoader).getRawCFG(className, ccgNode.getMethod());
	}

	/**
	 * <p>
	 * getMethodExitOf
	 * </p>
	 * 
	 * @param methodEntry
	 *            a {@link org.evosuite.graphs.ccfg.CCFGMethodEntryNode} object.
	 * @return a {@link org.evosuite.graphs.ccfg.CCFGMethodExitNode} object.
	 */
	public CCFGMethodExitNode getMethodExitOf(CCFGMethodEntryNode methodEntry) {
		if (methodEntry == null)
			return null;

		return methodExits.get(methodEntry.getMethod());
	}

	/**
	 * <p>
	 * getMethodEntryOf
	 * </p>
	 * 
	 * @param methodExit
	 *            a {@link org.evosuite.graphs.ccfg.CCFGMethodExitNode} object.
	 * @return a {@link org.evosuite.graphs.ccfg.CCFGMethodEntryNode} object.
	 */
	public CCFGMethodEntryNode getMethodEntryOf(CCFGMethodExitNode methodExit) {
		if (methodExit == null)
			return null;

		return methodEntries.get(methodExit.getMethod());
	}

	// CCFG computation from CCG and CFGs

	private void compute() {
		importCFGs();
		addFrame();
	}

	private void importCFGs() {
		Map<RawControlFlowGraph, Map<BytecodeInstruction, CCFGCodeNode>> tempMap = new HashMap<RawControlFlowGraph, Map<BytecodeInstruction, CCFGCodeNode>>();
		// replace each class call node with corresponding CFG
		for (ClassCallNode ccgNode : ccg.vertexSet()) {
			RawControlFlowGraph cfg = getRCFG(ccgNode);
			tempMap.put(cfg, importCFG(cfg));
		}
		connectCFGs(tempMap);
	}

	private void connectCFGs(
			Map<RawControlFlowGraph, Map<BytecodeInstruction, CCFGCodeNode>> tempMap) {

		for (RawControlFlowGraph cfg : tempMap.keySet()) {
			List<BytecodeInstruction> calls = cfg
					.determineMethodCallsToOwnClass();
			for (BytecodeInstruction call : calls) {
				// we do not want to connect every method call to the target
				// class, but only those that are called on the same object or
				// are static
				if (!(call.isCallToStaticMethod() || call
						.isMethodCallOnSameObject())) {
					// call.printFrameInformation();
					continue;
				}
				connectCFG(cfg, call, tempMap);
			}
		}
	}

	private void connectCFG(
			RawControlFlowGraph cfg,
			BytecodeInstruction call,
			Map<RawControlFlowGraph, Map<BytecodeInstruction, CCFGCodeNode>> tempMap) {

		// add MethodCallNode and MethodReturnNode
		CCFGMethodReturnNode returnNode = new CCFGMethodReturnNode(call);
		CCFGMethodCallNode callNode = new CCFGMethodCallNode(call, returnNode);
		addVertex(callNode);
		addVertex(returnNode);

		// connect with method entry and exit nodes of called method
		CCFGNode calleeEntry = methodEntries.get(call.getCalledMethod());
		CCFGNode calleeExit = methodExits.get(call.getCalledMethod());

		CCFGMethodCallEdge callEdge = new CCFGMethodCallEdge(call, true);
		CCFGMethodCallEdge returnEdge = new CCFGMethodCallEdge(call, false);

		addEdge(callNode, calleeEntry, callEdge);
		addEdge(calleeExit, returnNode, returnEdge);

		// redirect edges from the original CodeNode to the new nodes
		CCFGNode origCallNode = tempMap.get(cfg).get(call);
		if (!redirectEdges(origCallNode, callNode, returnNode)
				|| !graph.removeVertex(origCallNode))
			throw new IllegalStateException(
					"internal error while connecting cfgs during CCFG construction");
	}

	private Map<BytecodeInstruction, CCFGCodeNode> importCFG(
			RawControlFlowGraph cfg) {
		Map<BytecodeInstruction, CCFGCodeNode> temp = new HashMap<BytecodeInstruction, CCFGCodeNode>();

		importCFGNodes(cfg, temp);
		importCFGEdges(cfg, temp);

		// enclose with CCFGMethodEntryNode and CCFGMethodExitNode
		encloseCFG(cfg, temp);

		return temp;
	}

	/**
	 * import CFGs nodes. If the node is a method call to a method of a field
	 * class, a new CCFGFieldClassCallNode is created. Otherwise, a normal
	 * CCFGCodeNode is created
	 * 
	 * @param cfg
	 * @param temp
	 */
	private void importCFGNodes(RawControlFlowGraph cfg,
			Map<BytecodeInstruction, CCFGCodeNode> temp) {

		// add BytecodeInstructions as CCFGCodeNodes
		for (BytecodeInstruction code : cfg.vertexSet()) {
			CCFGCodeNode node;
			if (code.isMethodCallOfField()) {
				node = new CCFGFieldClassCallNode(code,
						code.getCalledMethodsClass(), code.getCalledMethodName(), code.getMethodCallDescriptor());
			} else {
				node = new CCFGCodeNode(code);
			}
			addVertex(node);
			temp.put(code, node);
		}
	}

	private void importCFGEdges(RawControlFlowGraph cfg,
			Map<BytecodeInstruction, CCFGCodeNode> temp) {

		// add ControlFlowEdges as CCFGCodeEdges
		for (ControlFlowEdge e : cfg.edgeSet()) {
			if (e.isExceptionEdge())
				continue;
			CCFGCodeNode src = temp.get(cfg.getEdgeSource(e));
			CCFGCodeNode target = temp.get(cfg.getEdgeTarget(e));
			addEdge(src, target, new CCFGCodeEdge(e));
		}
	}

	private void encloseCFG(RawControlFlowGraph cfg,
			Map<BytecodeInstruction, CCFGCodeNode> temp) {

		addCCFGMethodEntryNode(cfg, temp);
		addCCFGMethodExitNode(cfg, temp);
	}

	private CCFGMethodEntryNode addCCFGMethodEntryNode(RawControlFlowGraph cfg,
			Map<BytecodeInstruction, CCFGCodeNode> temp) {

		CCFGCodeNode entryInstruction = temp.get(cfg.determineEntryPoint());
		CCFGMethodEntryNode entry = new CCFGMethodEntryNode(
				cfg.getMethodName(), entryInstruction);
		addVertex(entry);
		addEdge(entry, entryInstruction);
		methodEntries.put(cfg.getMethodName(), entry);
		return entry;
	}

	private CCFGMethodExitNode addCCFGMethodExitNode(RawControlFlowGraph cfg,
			Map<BytecodeInstruction, CCFGCodeNode> temp) {

		CCFGMethodExitNode exit = new CCFGMethodExitNode(cfg.getMethodName());
		addVertex(exit);
		for (BytecodeInstruction exitPoint : cfg.determineExitPoints()) {
			addEdge(temp.get(exitPoint), exit);
		}
		methodExits.put(cfg.getMethodName(), exit);
		return exit;
	}

	private void addFrame() {

		addFrameNodes();
		addFrameEdges();

		connectPublicMethodsToFrame();
	}

	private void addFrameNodes() {
		for (FrameNodeType type : FrameNodeType.values()) {
			CCFGFrameNode node = new CCFGFrameNode(type);
			addVertex(node);
			frameNodes.put(type, node);
		}
	}

	private void addFrameEdges() {
		addEdge(getFrameNode(FrameNodeType.ENTRY),
				getFrameNode(FrameNodeType.LOOP), new CCFGFrameEdge());
		addEdge(getFrameNode(FrameNodeType.LOOP),
				getFrameNode(FrameNodeType.CALL), new CCFGFrameEdge());
		addEdge(getFrameNode(FrameNodeType.LOOP),
				getFrameNode(FrameNodeType.EXIT), new CCFGFrameEdge());
		addEdge(getFrameNode(FrameNodeType.RETURN),
				getFrameNode(FrameNodeType.LOOP), new CCFGFrameEdge());
	}

	/**
	 * <p>
	 * getFrameNode
	 * </p>
	 * 
	 * @param type
	 *            a
	 *            {@link org.evosuite.graphs.ccfg.ClassControlFlowGraph.FrameNodeType}
	 *            object.
	 * @return a {@link org.evosuite.graphs.ccfg.CCFGFrameNode} object.
	 */
	public CCFGFrameNode getFrameNode(FrameNodeType type) {
		return frameNodes.get(type);
	}

	/**
	 * Adds a CCFGFrameEdge from the CCFGFrameNode CALL to the
	 * CCFGMethodEntryNode of each public method and from their
	 * CCFGMethodExitNode to the CCFGFrameNode RETURN.
	 */
	private void connectPublicMethodsToFrame() {
		for (ClassCallNode ccgNode : ccg.vertexSet()) {
			RawControlFlowGraph cfg = getRCFG(ccgNode);
			if (cfg.isPublicMethod()) {
				addEdge(getFrameNode(FrameNodeType.CALL),
						methodEntries.get(ccgNode.getMethod()),
						new CCFGFrameEdge());
				addEdge(methodExits.get(ccgNode.getMethod()),
						getFrameNode(FrameNodeType.RETURN), new CCFGFrameEdge());

				publicMethods.add(methodEntries.get(ccgNode.getMethod()));
			}
		}
	}

	// toDot utilities

	/**
	 * Makes .dot output pretty by visualizing different types of nodes and
	 * edges with different forms and colors
	 */
	private void nicenDotOutput() {
		registerVertexAttributeProvider(new CCFGNodeAttributeProvider());
		registerEdgeAttributeProvider(new CCFGEdgeAttributeProvider());
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return "CCFG_" + className;
	}

	/** {@inheritDoc} */
	@Override
	protected String dotSubFolder() {
		return toFileString(className) + "/";
	}
	
	/**
	 * @return the ccg
	 */
	public ClassCallGraph getCcg() {
		return ccg;
	}

}
