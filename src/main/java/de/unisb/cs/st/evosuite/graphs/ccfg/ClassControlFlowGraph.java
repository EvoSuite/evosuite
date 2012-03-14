package de.unisb.cs.st.evosuite.graphs.ccfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unisb.cs.st.evosuite.graphs.GraphPool;
import de.unisb.cs.st.evosuite.graphs.EvoSuiteGraph;
import de.unisb.cs.st.evosuite.graphs.ccg.ClassCallGraph;
import de.unisb.cs.st.evosuite.graphs.ccg.ClassCallNode;
import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.graphs.cfg.ControlFlowEdge;
import de.unisb.cs.st.evosuite.graphs.cfg.RawControlFlowGraph;

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
 * CCFGMethodReturnNode.
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
 * 
 * @author Andre Mis
 */
public class ClassControlFlowGraph extends EvoSuiteGraph<CCFGNode, CCFGEdge> {

	private String className;
	private ClassCallGraph ccg;

	private Map<String, CCFGMethodEntryNode> methodEntries = new HashMap<String, CCFGMethodEntryNode>();
	private Map<String, CCFGMethodExitNode> methodExits = new HashMap<String, CCFGMethodExitNode>();

	
	/**
	 * Given the ClassCallGraph of a class this constructor will build up the
	 * corresponding CCFG.
	 */
	public ClassControlFlowGraph(ClassCallGraph ccg) {
		super(CCFGEdge.class);
		this.className = ccg.getClassName();
		this.ccg = ccg;

		// make .dot output pretty by visualizing different types of nodes and
		// edges with different forms and colors
		registerVertexAttributeProvider(new CCFGNodeAttributeProvider());
		registerEdgeAttributeProvider(new CCFGEdgeAttributeProvider());

		compute();
	}

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
					.determineMethodCallsToClass(className);
			for (BytecodeInstruction call : calls) {

				// add MethodCallNode and MethodReturnNode
				CCFGMethodCallNode callNode = new CCFGMethodCallNode(call);
				CCFGMethodReturnNode returnNode = new CCFGMethodReturnNode(call);
				addVertex(callNode);
				addVertex(returnNode);

				// connect with method entry and exit nodes of called method
				CCFGNode calleeEntry = methodEntries
						.get(call.getCalledMethod());
				CCFGNode calleeExit = methodExits.get(call.getCalledMethod());

				CCFGMethodCallEdge callEdge = new CCFGMethodCallEdge(call, true);
				CCFGMethodCallEdge returnEdge = new CCFGMethodCallEdge(call,
						false);

				addEdge(callNode, calleeEntry, callEdge);
				addEdge(calleeExit, returnNode, returnEdge);

				// redirect edges from the original CodeNode to the new nodes
				CCFGNode origCallNode = tempMap.get(cfg).get(call);
				if (!redirectEdges(origCallNode, callNode, returnNode)
						|| !graph.removeVertex(origCallNode))
					throw new IllegalStateException(
							"internal error while connecting cfgs during CCFG construction");
			}
		}
	}

	private Map<BytecodeInstruction, CCFGCodeNode> importCFG(
			RawControlFlowGraph cfg) {
		Map<BytecodeInstruction, CCFGCodeNode> temp = new HashMap<BytecodeInstruction, CCFGCodeNode>();

		// add BytecodeInstructions as CCFGCodeNodes
		for (BytecodeInstruction code : cfg.vertexSet()) {
			CCFGCodeNode node = new CCFGCodeNode(code);
			addVertex(node);
			temp.put(code, node);
		}

		// add ControlFlowEdges as CCFGCodeEdges
		for (ControlFlowEdge e : cfg.edgeSet()) {
			CCFGCodeNode src = temp.get(cfg.getEdgeSource(e));
			CCFGCodeNode target = temp.get(cfg.getEdgeTarget(e));
			addEdge(src, target, new CCFGCodeEdge(e));
		}

		// enclose with CCFGMethodEntryNode and CCFGMethodExitNode
		encloseCFG(cfg, temp);

		return temp;
	}

	private void encloseCFG(RawControlFlowGraph cfg,
			Map<BytecodeInstruction, CCFGCodeNode> temp) {

		// add entry node
		CCFGMethodEntryNode entry = new CCFGMethodEntryNode(cfg.getMethodName());
		addVertex(entry);
		addEdge(entry, temp.get(cfg.determineEntryPoint()));

		// add exit node
		CCFGMethodExitNode exit = new CCFGMethodExitNode(cfg.getMethodName());
		addVertex(exit);
		for (BytecodeInstruction exitPoint : cfg.determineExitPoints()) {
			addEdge(temp.get(exitPoint), exit);
		}

		// register both
		methodEntries.put(cfg.getMethodName(), entry);
		methodExits.put(cfg.getMethodName(), exit);
	}

	private void addFrame() {

		// add frame vertices
		CCFGFrameNode entry = new CCFGFrameNode(
				CCFGFrameNode.FrameNodeType.ENTRY);
		addVertex(entry);
		CCFGFrameNode exit = new CCFGFrameNode(CCFGFrameNode.FrameNodeType.EXIT);
		addVertex(exit);
		CCFGFrameNode loop = new CCFGFrameNode(CCFGFrameNode.FrameNodeType.LOOP);
		addVertex(loop);
		CCFGFrameNode call = new CCFGFrameNode(CCFGFrameNode.FrameNodeType.CALL);
		addVertex(call);
		CCFGFrameNode retrn = new CCFGFrameNode(
				CCFGFrameNode.FrameNodeType.RETURN);
		addVertex(retrn);

		// add frame edges
		addEdge(entry, loop, new CCFGFrameEdge());
		addEdge(loop, call, new CCFGFrameEdge());
		addEdge(loop, exit, new CCFGFrameEdge());
		addEdge(retrn, loop, new CCFGFrameEdge());

		connectPublicMethods(call, retrn);
	}

	/**
	 * Adds a CCFGFrameEdge from the CCFGFrameNode CALL to the
	 * CCFGMethodEntryNode of each public method and from their
	 * CCFGMethodExitNode to the CCFGFrameNode RETURN.
	 */
	private void connectPublicMethods(CCFGFrameNode callNode,
			CCFGFrameNode retrnNode) {

		for (ClassCallNode ccgNode : ccg.vertexSet()) {
			RawControlFlowGraph cfg = getRCFG(ccgNode);
			if (cfg.isPublicMethod()) {
				addEdge(callNode, methodEntries.get(ccgNode.getMethod()),
						new CCFGFrameEdge());
				addEdge(methodExits.get(ccgNode.getMethod()), retrnNode,
						new CCFGFrameEdge());
			}
		}
	}

	private RawControlFlowGraph getRCFG(ClassCallNode ccgNode) {
		return GraphPool.getRawCFG(className, ccgNode.getMethod());
	}

	// toDot util

	@Override
	public String getName() {
		return "CCFG_" + className;
	}

	@Override
	protected String dotSubFolder() {
		return toFileString(className) + "/";
	}
}
