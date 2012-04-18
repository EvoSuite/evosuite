package de.unisb.cs.st.evosuite.graphs.ccfg;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageTestFitness;
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
 * 
 * @author Andre Mis
 */
public class ClassControlFlowGraph extends EvoSuiteGraph<CCFGNode, CCFGEdge> {

	public enum FrameNodeType {
		ENTRY, EXIT, LOOP, CALL, RETURN
	};

	private String className;
	private ClassCallGraph ccg;

	private Map<String, CCFGMethodEntryNode> methodEntries = new HashMap<String, CCFGMethodEntryNode>();
	private Map<String, CCFGMethodExitNode> methodExits = new HashMap<String, CCFGMethodExitNode>();

	private Set<CCFGMethodEntryNode> publicMethods = new HashSet<CCFGMethodEntryNode>();

	private Map<FrameNodeType, CCFGFrameNode> frameNodes = new HashMap<FrameNodeType, CCFGFrameNode>();

	private static class MethodCall {
		private static int invocations = 0;
		private final CCFGMethodCallNode methodCall;
		private final int invocationNumber;
		private final String calledMethod;

		public MethodCall(CCFGMethodCallNode methodCall, String calledMethod) {
			this.methodCall = methodCall;
			invocations++;
			this.invocationNumber = invocations;
			this.calledMethod = calledMethod;
		}

		public boolean isInitialMethodCall() {
			return methodCall == null;
		}

		public boolean isMethodCallFor(BytecodeInstruction callInstruction) {
			if (methodCall == null)
				return callInstruction == null;
			return methodCall.getCallInstruction().equals(callInstruction);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + invocationNumber;
			result = prime * result
					+ ((methodCall == null) ? 0 : methodCall.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MethodCall other = (MethodCall) obj;
			if (invocationNumber != other.invocationNumber)
				return false;
			if (methodCall == null) {
				if (other.methodCall != null)
					return false;
			} else if (!methodCall.equals(other.methodCall))
				return false;
			return true;
		}

		public String toString() {
			if (methodCall == null)
				return "initCall for " + calledMethod + " " + invocationNumber;
			return methodCall.getCalledMethod() + " " + invocationNumber;
		}

		public String getCalledMethodName() {
			return calledMethod;
		}
	}

	private static class VariableDefinition {
		private final BytecodeInstruction definition;
		private final MethodCall call;

		public VariableDefinition(BytecodeInstruction definition,
				MethodCall call) {
			this.definition = definition;
			this.call = call;
		}

		public BytecodeInstruction getDefinition() {
			return definition;
		}

		public MethodCall getMethodCall() {
			return call;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((call == null) ? 0 : call.hashCode());
			result = prime * result
					+ ((definition == null) ? 0 : definition.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			VariableDefinition other = (VariableDefinition) obj;
			if (call == null) {
				if (other.call != null)
					return false;
			} else if (!call.equals(other.call))
				return false;
			if (definition == null) {
				if (other.definition != null)
					return false;
			} else if (!definition.equals(other.definition))
				return false;
			return true;
		}

		public String toString() {
			return definition.toString() + " in " + call.toString();
		}
	}

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
					.determineMethodCallsToClass(className);
			for (BytecodeInstruction call : calls) {
				// we do not want to connect every method call to the target
				// class, but only those that are called on the same object or
				// are static
				if (!(call.isStaticMethodCall() || call
						.isMethodCallOnSameObject())) {
					// System.out.println("excluded method call: "
					// + call.toString());
					// System.out.println("DESC: "+call.getCalledMethodsArgumentCount());
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
		CCFGMethodCallNode callNode = new CCFGMethodCallNode(call);
		CCFGMethodReturnNode returnNode = new CCFGMethodReturnNode(call);
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

	private void importCFGNodes(RawControlFlowGraph cfg,
			Map<BytecodeInstruction, CCFGCodeNode> temp) {

		// add BytecodeInstructions as CCFGCodeNodes
		for (BytecodeInstruction code : cfg.vertexSet()) {
			CCFGCodeNode node = new CCFGCodeNode(code);
			addVertex(node);
			temp.put(code, node);
		}
	}

	private void importCFGEdges(RawControlFlowGraph cfg,
			Map<BytecodeInstruction, CCFGCodeNode> temp) {

		// add ControlFlowEdges as CCFGCodeEdges
		for (ControlFlowEdge e : cfg.edgeSet()) {
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

	private RawControlFlowGraph getRCFG(ClassCallNode ccgNode) {
		return GraphPool.getRawCFG(className, ccgNode.getMethod());
	}

	// Definition-Use Pair computation

	public Set<DefUseCoverageTestFitness> determineDefUsePairs() {

		Set<DefUseCoverageTestFitness> r = new HashSet<DefUseCoverageTestFitness>();

		// make a run for each public method
		// if you reach a use for which you have no def yet, remember that
		// also remember activeDefs after each run
		// then create intra-class pairs from these uses and defs
		// and during each single run we detect intra and inter method pairs

		// TODO clinit? id say uses dont count, defs do

		Set<Set<BytecodeInstruction>> freeUseses = new HashSet<Set<BytecodeInstruction>>();

		// determine inter-method-pairs
		for (CCFGMethodEntryNode publicMethodEntry : publicMethods) {
			if (publicMethodEntry.getEntryInstruction() == null)
				throw new IllegalStateException(
						"expect each CCFGMethodEntryNode to have its entryInstruction set");

			System.out.print("looking for pairs in "
					+ publicMethodEntry.getMethod() + " ... ");
			Map<String, VariableDefinition> activeDefs = new HashMap<String, VariableDefinition>();
			Set<DefUseCoverageTestFitness> temp = new HashSet<DefUseCoverageTestFitness>();
			Set<BytecodeInstruction> freeUses = new HashSet<BytecodeInstruction>();
			Stack<MethodCall> callStack = new Stack<MethodCall>();
			// null will represent the public method call itself
			callStack.add(new MethodCall(null, publicMethodEntry.getMethod()));
			int calls = determineInterMethodPairs(publicMethodEntry,
					publicMethodEntry.getEntryInstruction(),
					new HashSet<CCFGNode>(), activeDefs, freeUses, temp,
					callStack);
			System.out.println("needed invocations: " + calls);
			r.addAll(temp);

			freeUseses.add(freeUses);
		}

		r.addAll(createIntraMethodPairs(freeUseses));

		// free memory
		forgetAllActiveDefs();

		return r;
	}

	private void forgetAllActiveDefs() {
		for (CCFGMethodExitNode exit : methodExits.values()) {
			exit.forgetActiveDefs();
		}
	}

	private int determineInterMethodPairs(
			CCFGMethodEntryNode investigatedPublicMethod, CCFGNode node,
			Set<CCFGNode> handled, Map<String, VariableDefinition> activeDefs,
			Set<BytecodeInstruction> freeUses,
			Set<DefUseCoverageTestFitness> foundPairs,
			Stack<MethodCall> callStack) {

		// TODO complexity too high

		// looking at scs.Stemmer and PairTestClass.pairExplosion() i have to
		// decrease the complexity of this algorithm a lot!
		// somehow i need to do a BFS instead of DFS and propagate free uses and
		// active defs to nodes within this graph or something. right now the
		// runtime just explodes if the search space gets too big and wide

		// guess i have too look at implementations of reaching definition after
		// all .. sad face

		if (handled.contains(node))
			throw new IllegalStateException(
					"visiting already handled node, should not happen");
		handled.add(node);

		int r = 1;
		if (node instanceof CCFGCodeNode) {
			BytecodeInstruction code = ((CCFGCodeNode) node)
					.getCodeInstruction();
			if (!callStack.peek().getCalledMethodName()
					.equals(code.getMethodName()))
				throw new IllegalStateException(
						"insane callStack: peek is in method "
								+ callStack.peek().getCalledMethodName()
								+ " and i encountered code of method "
								+ code.getMethodName());

			if (code.isUse()) {
				VariableDefinition activeDef = activeDefs.get(code
						.getDUVariableName());
				if (activeDef != null) {

					// we have an intraMethodPair iff use and def are in the
					// same method and executed during a single invocation of
					// that method
					boolean isIntraPair = activeDef.getMethodCall().equals(
							callStack.peek());
					DefUseCoverageTestFitness.DefUsePairType type;
					if (isIntraPair)
						type = DefUseCoverageTestFitness.DefUsePairType.INTRA_METHOD;
					else
						type = DefUseCoverageTestFitness.DefUsePairType.INTER_METHOD;

					DefUseCoverageTestFitness goal = DefUseCoverageFactory
							.createGoal(activeDef.getDefinition(), code, type);
					if (goal != null)
						foundPairs.add(goal);
				} else {
					if (code.isFieldUse())
						freeUses.add(code);
				}
			}
			if (code.isDefinition()) {
				activeDefs.put(code.getDUVariableName(),
						new VariableDefinition(code, callStack.peek()));
			}
		} else if (node instanceof CCFGMethodCallNode) {
			CCFGMethodCallNode callNode = (CCFGMethodCallNode) node;
			callStack
					.push(new MethodCall(callNode, callNode.getCalledMethod()));

			// so now we have a problem: we will have to visit some nodes twice
			// here, in case of a recursive method call for example
			// here is what we do: we will remove each node in handle from the
			// called method except the callNode itself.
			handled = filterHandledMapForMethodCallNode(callNode, handled);
		} else if (node instanceof CCFGMethodReturnNode) {
			if (callStack.peek().isInitialMethodCall())
				throw new IllegalStateException(
						"found method return but had no more method calls on stack");
			CCFGMethodReturnNode retrn = (CCFGMethodReturnNode) node;
			if (callStack.peek().isMethodCallFor(retrn.getCallInstruction()))
				callStack.pop();
			else
				throw new IllegalStateException(
						"visiting MethodReturnNode even though lastly visited MethodCallNode was from a different method");

		} else if (node instanceof CCFGFrameNode) {
			throw new IllegalStateException(
					"visiting CCFGFrameNode, which should not happen");
		} else if (node instanceof CCFGMethodExitNode) {
			// if this is the methodExit of our public method we add our
			// activeDefs before returning for intra-class pairs
			CCFGMethodExitNode exitNode = (CCFGMethodExitNode) node;
			if (exitNode.isExitOfMethodEntry(investigatedPublicMethod)) {
				exitNode.addActiveDefs(toBytecodeInstructionMap(activeDefs));
			}
		}

		// DONE we don't want to take every child into account all the time
		// for example if we previously found a methodCallNode and then later
		// visit a MethodExitNode we do want to follow the edge from that node
		// to the MethodReturnNode of our previous methodCallNode. However we
		// do not want to follow the edge back to Frame.RETURN
		// on the other hand if we did not visit a methodCallNode and find a
		// MethodExitNode we do not want to follow the edges from there to
		// methodReturnNodes

		Set<CCFGNode> children = getChildren(node);
		for (CCFGNode child : children) {
			if (handled.contains(child))
				continue;

			if (child instanceof CCFGMethodReturnNode) {
				if (callStack.peek().isInitialMethodCall())
					continue;
				CCFGMethodReturnNode retrn = (CCFGMethodReturnNode) child;
				if (!callStack.peek().isMethodCallFor(
						retrn.getCallInstruction()))
					continue;
			} else if (child instanceof CCFGFrameNode) {
				CCFGFrameNode frameNode = (CCFGFrameNode) child;
				if (frameNode.getType().equals(FrameNodeType.RETURN)) {
					// if (!callStack.empty())
					continue;
				} else {
					throw new IllegalStateException(
							"found CCFGFrameNode that was not of type RETURN. should not be possible");
				}
			}

			Stack<MethodCall> copiedStack = new Stack<MethodCall>();
			copiedStack.setSize(callStack.size());
			Collections.copy(copiedStack, callStack);

			r += determineInterMethodPairs(investigatedPublicMethod, child,
					new HashSet<CCFGNode>(handled),
					new HashMap<String, VariableDefinition>(activeDefs),
					freeUses, foundPairs, copiedStack);
		}
		return r;
	}

	private Map<String, BytecodeInstruction> toBytecodeInstructionMap(
			Map<String, VariableDefinition> activeDefs) {
		Map<String, BytecodeInstruction> r = new HashMap<String, BytecodeInstruction>();
		for (String key : activeDefs.keySet()) {
			r.put(key, activeDefs.get(key).getDefinition());
		}
		return r;
	}

	private Set<CCFGNode> filterHandledMapForMethodCallNode(
			CCFGMethodCallNode callNode, Set<CCFGNode> handled) {
		Set<CCFGNode> r = new HashSet<CCFGNode>();
		for (CCFGNode node : handled)
			if (!nodeBelongsToMethod(node, callNode.getCalledMethod())
					|| (node instanceof CCFGMethodCallNode))
				r.add(node);

		r.add(callNode);
		return r;
	}

	private boolean nodeBelongsToMethod(CCFGNode node, String method) {
		if (node instanceof CCFGCodeNode)
			return ((CCFGCodeNode) node).getMethod().equals(method);
		else if (node instanceof CCFGMethodCallNode)
			return ((CCFGMethodCallNode) node).getMethod().equals(method);
		else if (node instanceof CCFGMethodReturnNode)
			return ((CCFGMethodReturnNode) node).getMethod().equals(method);
		else if (node instanceof CCFGMethodEntryNode)
			return ((CCFGMethodEntryNode) node).getMethod().equals(method);
		else if (node instanceof CCFGMethodExitNode)
			return ((CCFGMethodExitNode) node).getMethod().equals(method);
		// frame nodes belong to no method
		return false;
	}

	private Set<DefUseCoverageTestFitness> createIntraMethodPairs(
			Set<Set<BytecodeInstruction>> freeUseses) {
		Set<Map<String, BytecodeInstruction>> activeDefss = gatherActiveDefsAtPublicMethodExits();
		return createIntraMethodPairs(activeDefss, freeUseses);
	}

	private Set<Map<String, BytecodeInstruction>> gatherActiveDefsAtPublicMethodExits() {
		Set<Map<String, BytecodeInstruction>> activeDefss = new HashSet<Map<String, BytecodeInstruction>>();
		for (CCFGMethodEntryNode publicMethodEntry : publicMethods) {
			CCFGMethodExitNode exit = getMethodExitOf(publicMethodEntry);
			Set<Map<String, BytecodeInstruction>> activeDefs = exit
					.getActiveDefs();
			activeDefss.addAll(activeDefs);
		}
		return activeDefss;
	}

	public CCFGMethodExitNode getMethodExitOf(CCFGMethodEntryNode methodEntry) {
		if (methodEntry == null)
			return null;

		return methodExits.get(methodEntry.getMethod());
	}

	public CCFGMethodEntryNode getMethodEntryOf(CCFGMethodExitNode methodExit) {
		if (methodExit == null)
			return null;

		return methodEntries.get(methodExit.getMethod());
	}

	private Set<DefUseCoverageTestFitness> createIntraMethodPairs(
			Set<Map<String, BytecodeInstruction>> activeDefss,
			Set<Set<BytecodeInstruction>> freeUseses) {

		Set<DefUseCoverageTestFitness> r = new HashSet<DefUseCoverageTestFitness>();

		for (Set<BytecodeInstruction> freeUses : freeUseses)
			for (BytecodeInstruction freeUse : freeUses)
				for (Map<String, BytecodeInstruction> activeDefs : activeDefss) {
					if (!freeUse.isUse())
						throw new IllegalStateException(
								"expect all freeUses to be Use instructions");
					BytecodeInstruction activeDef = activeDefs.get(freeUse
							.getDUVariableName());
					if (activeDef == null)
						continue;

					DefUseCoverageTestFitness intraClassGoal = DefUseCoverageFactory
							.createGoal(
									activeDef,
									freeUse,
									DefUseCoverageTestFitness.DefUsePairType.INTRA_CLASS);
					if (intraClassGoal != null) {
						r.add(intraClassGoal);
					}
				}

		return r;
	}

	// toDot utilities

	@Override
	public String getName() {
		return "CCFG_" + className;
	}

	@Override
	protected String dotSubFolder() {
		return toFileString(className) + "/";
	}
}
