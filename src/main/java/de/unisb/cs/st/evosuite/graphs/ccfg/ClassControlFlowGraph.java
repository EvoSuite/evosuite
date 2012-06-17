/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.graphs.ccfg;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageTestFitness;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageTestFitness.DefUsePairType;
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

	private final int UPPER_PAIR_SEARCH_INVOCATION_BOUND = 2000000;
	private boolean warnedAboutAbortion = false;

	private String className;
	private ClassCallGraph ccg;

	private Map<String, CCFGMethodEntryNode> methodEntries = new HashMap<String, CCFGMethodEntryNode>();
	private Map<String, CCFGMethodExitNode> methodExits = new HashMap<String, CCFGMethodExitNode>();

	// map methods to Sets of definitions that can be active at method
	// return. map according to defined variables name
	private Map<String, Set<Map<String, BytecodeInstruction>>> determinedActiveDefs = new HashMap<String, Set<Map<String, BytecodeInstruction>>>();
	// map methods to Sets of Uses that have a definition-free path
	// from their methods entry
	private Map<String, Set<BytecodeInstruction>> determinedFreeUses = new HashMap<String, Set<BytecodeInstruction>>();

	private Set<CCFGMethodEntryNode> analyzedMethods = new HashSet<CCFGMethodEntryNode>();

	private Set<CCFGMethodEntryNode> publicMethods = new HashSet<CCFGMethodEntryNode>();

	private Map<FrameNodeType, CCFGFrameNode> frameNodes = new HashMap<FrameNodeType, CCFGFrameNode>();

	// debug profiling
	private long timeSpentMingling = 0l;

	/**
	 * Represents a single invocation of a method during the Inter-Method pair
	 * search.
	 * 
	 * This class is used to keep track of the current call stack during the
	 * search and to differentiate different method calls to the same method.
	 * 
	 * @author Andre Mis
	 */
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

		public static MethodCall constructForCallNode(
				CCFGMethodCallNode callNode) {
			if (callNode == null)
				throw new IllegalArgumentException("given call node was null");
			return new MethodCall(callNode, callNode.getCalledMethod());
		}
	}

	/**
	 * A VariableDefinition consisting of a defining BytecodeInstruction and a
	 * MethodCall.
	 * 
	 * Used in Inter-Method pair search algorithm to differentiate between
	 * Intra-Method pairs and Inter-Method Pairs.
	 * 
	 * More or less just a pair of a BytecodeInstruction and a Methodcall.
	 * 
	 * @author Andre Mis
	 */
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
	 * corresponding CCFG using the RCFGs from the GraphPool.
	 */
	public ClassControlFlowGraph(ClassCallGraph ccg) {
		super(CCFGEdge.class);
		this.className = ccg.getClassName();
		this.ccg = ccg;
		nicenDotOutput();
		compute();
	}

	// Definition-Use Pair computation

	/**
	 * Makes a run of determineInterMethodPairs() for each public method. If you
	 * reach a use for which you have no def yet, remember that also remember
	 * activeDefs after each run then create intra-class pairs from these uses
	 * and defs and during each single run we detect intra and inter method
	 * pairs
	 */
	public Set<DefUseCoverageTestFitness> determineDefUsePairs() {

		// TODO clinit? id say uses dont count, defs do

		Set<DefUseCoverageTestFitness> r = preAnalyzeMethods();
		// create inter-method-pairs
		for (CCFGMethodEntryNode publicMethodEntry : publicMethods) {
			if (analyzedMethods.contains(publicMethodEntry)) {
				continue;
			}
			if (publicMethodEntry.getEntryInstruction() == null)
				throw new IllegalStateException(
						"expect each CCFGMethodEntryNode to have its entryInstruction set");
			
			r.addAll(determineInterMethodPairs(publicMethodEntry));
		}
		// create intra-method pairs
		r.addAll(createIntraMethodPairs());

		freeMemory();
		return r;
	}

	/**
	 * Checks if there are methods in the CCG that dont call any other methods
	 * except for maybe itself. For these we can predetermine free uses and
	 * activeDefs prior to looking for inter_method_pairs. After that we can
	 * even repeat this process for methods we now have determined free uses and
	 * activeDefs! that way you can save a lot of computation. Map activeDefs
	 * and freeUses according to the variable so you can easily determine which
	 * defs will be active and which uses are free once you encounter a
	 * methodCall to that method without looking at its part of the CCFG
	 */
	private Set<DefUseCoverageTestFitness> preAnalyzeMethods() {

		// TODO after preanalyze, order the remaining methods as follows:
		// first order each method by the number of instructions within them in
		// ascending order. after that for each method check if there is a
		// method that calls this one and also has to be analyzed still. if you
		// find such a pair move the calling method in front of the called one

		Set<DefUseCoverageTestFitness> r = new HashSet<DefUseCoverageTestFitness>();

		LinkedList<ClassCallNode> toAnalyze = new LinkedList<ClassCallNode>();
		toAnalyze.addAll(getInitialPreAnalyzeableMethods());

		while (!toAnalyze.isEmpty()) {
			ClassCallNode currentMethod = toAnalyze.poll();
			CCFGMethodEntryNode analyzeableEntry = getMethodEntryNodeForClassCallNode(currentMethod);
			if (analyzedMethods.contains(analyzeableEntry))
				continue;

			r.addAll(determineInterMethodPairs(analyzeableEntry));

			// check if we can pre-analyze further methods now
			Set<ClassCallNode> parents = ccg.getParents(currentMethod);
			for (ClassCallNode parent : parents) {
				if (toAnalyze.contains(parent))
					continue; // will be analyzed anyway
				if (analyzedMethods
						.contains(getMethodEntryNodeForClassCallNode(parent)))
					continue; // was already analyzed

				Set<ClassCallNode> parentsChildren = ccg.getChildren(parent);
				boolean canAnalyzeNow = true;
				for (ClassCallNode parentsChild : parentsChildren) {
					if (!parentsChild.equals(parent)
							&& !(toAnalyze.contains(parentsChild) || analyzedMethods
									.contains(getMethodEntryNodeForClassCallNode(parentsChild)))) {
						// found child of parent that will not be pre-analyzed
						canAnalyzeNow = false;
						break;
					}
				}
				if (canAnalyzeNow) {
					toAnalyze.offer(parent);
				}
			}
		}

		return r;
	}

	/**
	 * Every CCGNode that has no children except for maybe itself can be
	 * initially pre-analyzed
	 */
	private Set<ClassCallNode> getInitialPreAnalyzeableMethods() {
		Set<ClassCallNode> preAnalyzeable = new HashSet<ClassCallNode>();
		for (ClassCallNode ccgNode : ccg.vertexSet()) {
			boolean add = true;
			for (ClassCallNode child : ccg.getChildren(ccgNode))
				if (!child.equals(ccgNode))
					add = false;

			if (add)
				preAnalyzeable.add(ccgNode);
		}

		return preAnalyzeable;
	}

	// intra-class pairs

	private Set<DefUseCoverageTestFitness> createIntraMethodPairs() {
		Set<DefUseCoverageTestFitness> r = new HashSet<DefUseCoverageTestFitness>();

		for (String method : determinedFreeUses.keySet()) {
			if (!isPublicMethod(method)) {
				// System.out.println("Skipping free uses in non-public method "
				// + method);
				continue;
			}
			for (BytecodeInstruction freeUse : determinedFreeUses.get(method))
				r.addAll(createIntraMethodPairsForFreeUse(freeUse));
		}

		return r;
	}

	private Set<DefUseCoverageTestFitness> createIntraMethodPairsForFreeUse(
			BytecodeInstruction freeUse) {
		checkFreeUseSanity(freeUse);

		Set<DefUseCoverageTestFitness> r = new HashSet<DefUseCoverageTestFitness>();
		for (String method : determinedActiveDefs.keySet()) {
			if (!isPublicMethod(method)) {
				// System.out.println("Skipping activeDefs in non-public method "
				// + method);
				continue;
			}
			Set<Map<String, BytecodeInstruction>> activeDefss = determinedActiveDefs
					.get(method);
			for (Map<String, BytecodeInstruction> activeDefs : activeDefss) {
				// checkActiveDefsSanity(activeDefs);
				// if (activeDefs.get(freeUse.getDUVariableName()) == null)
				// continue;

				BytecodeInstruction activeDef = activeDefs.get(freeUse
						.getDUVariableName());
				if (activeDef == null)
					continue;
				addNewGoalToFoundPairs(null, activeDef, freeUse,
						DefUsePairType.INTRA_CLASS, r);
			}
		}
		return r;
	}

	// inter-method pair search

	private Set<DefUseCoverageTestFitness> determineInterMethodPairs(
			CCFGMethodEntryNode methodEntry) {

		long start = System.currentTimeMillis();
		long mingled = timeSpentMingling;

		// TODO get a logger and replace System.outs with logger.debug

		System.out.print("* Searching for pairs in " + methodEntry.getMethod()
				+ " ... ");
		
		warnedAboutAbortion = false;

		// initialize variables
		Set<DefUseCoverageTestFitness> foundPairs = new HashSet<DefUseCoverageTestFitness>();
		Set<Map<String, VariableDefinition>> activeDefs = createInitialActiveDefs();
		Set<BytecodeInstruction> freeUses = new HashSet<BytecodeInstruction>();
		Stack<MethodCall> callStack = createInitialCallStack(methodEntry);

		// search
		Integer calls = determineInterMethodPairs(methodEntry,
				methodEntry.getEntryInstruction(), new HashSet<CCFGNode>(),
				new HashSet<CCFGEdge>(), activeDefs, freeUses, foundPairs,
				callStack, 0, true);

		long spentTime = System.currentTimeMillis() - start;

		// check if search was aborted
		Integer rerunCalls = 0;
//		if (calls >= UPPER_PAIR_SEARCH_INVOCATION_BOUND) {
//			System.out.println();
//			System.out.println("* ABORTED pairSearch for method"
//					+ methodEntry.getMethod());
//			
//			System.out.print("* Re-Searching for pairs without concidering loops in " + methodEntry.getMethod()
//					+ " ... ");
//			// if we previously tried to analyze this method but had to abort
//			// try to rerun without handling loops
//			activeDefs = createInitialActiveDefs();
//			Set<BytecodeInstruction> freeUses2 = new HashSet<BytecodeInstruction>();
//			callStack = createInitialCallStack(methodEntry);
//			rerunCalls = determineInterMethodPairs(methodEntry,
//					methodEntry.getEntryInstruction(), new HashSet<CCFGNode>(),
//					new HashSet<CCFGEdge>(), activeDefs, freeUses2, foundPairs,
//					callStack, 0, false);
//			freeUses.addAll(freeUses2);
//
//			spentTime = System.currentTimeMillis() - start;
//		}

		mingled = timeSpentMingling - mingled;

		System.out.println("  invocations: " + (calls + rerunCalls) + " took "
				+ spentTime + "ms (" + mingled + ") found " + foundPairs.size()
				+ " pairs");

		analyzedMethods.add(methodEntry);

		return foundPairs;
	}

	private int determineInterMethodPairs(
			CCFGMethodEntryNode investigatedMethod, CCFGNode node,
			Set<CCFGNode> handled, Set<CCFGEdge> handledBackEdges,
			Set<Map<String, VariableDefinition>> activeDefs,
			Set<BytecodeInstruction> freeUses,
			Set<DefUseCoverageTestFitness> foundPairs,
			Stack<MethodCall> callStack, int invocationCount,
			boolean handleLoops) {

//		System.out.println("  processing "+node.toString());

		handleHandledNodesSet(node, handled);

		invocationCount++;
		if (checkInvocationBound(invocationCount, callStack))
			return invocationCount;

		if (node instanceof CCFGCodeNode)
			handleCodeNode(investigatedMethod, node, callStack, activeDefs,
					freeUses, foundPairs);
		else if (node instanceof CCFGMethodCallNode)
			handled = handleMethodCallNode(node, callStack, handled);
		else if (node instanceof CCFGMethodReturnNode)
			handleMethodReturnNode(node, callStack);
		else if (node instanceof CCFGFrameNode)
			handleFrameNode();
		else if (node instanceof CCFGMethodExitNode)
			handleMethodExitNode(node, investigatedMethod, activeDefs, freeUses);

		node = determineNextRelevantNode(node, handled);

		Set<CCFGNode> children = getChildren(node);

		boolean skipChildren = shouldSkipChildren(node, handledBackEdges,
				children, handleLoops);

		if (!skipChildren)
			for (CCFGNode child : children) {
				if (!shouldProcessChild(node, child, handled, handledBackEdges,
						handleLoops))
					continue;

//				System.out.println("  nextChild of "+node.toString()+" is "+child.toString());
//				for(MethodCall mc : callStack)
//					System.out.println("    "+mc.toString());
				
				
				// we don't want to take every child into account all the time
				// for example if we previously found a methodCallNode and then
				// later visit a MethodExitNode we do want to follow the edge
				// from that node to the MethodReturnNode of our previous
				// methodCallNode. However we do not want to follow the edge
				// back to Frame.RETURN. on the other hand if we did not visit a
				// methodCallNode and find a MethodExitNode we do not want to
				// follow the edges from there to methodReturnNodes

				Stack<MethodCall> nextCallStack = callStack;
				
				if (child instanceof CCFGMethodReturnNode) {
					if (handleReturnNodeChild(child, callStack))
						continue;
				} else if (child instanceof CCFGFrameNode) {
					handleFrameNodeChild(child);
					continue;
				} else if (child instanceof CCFGMethodCallNode) {
					CCFGMethodCallNode callNode = (CCFGMethodCallNode) child;
					if (alreadyAnalzedMethod(callNode.getCalledMethod())) {

						nextCallStack = copyCallStack(callStack);
						
						// use previously stored information
						activeDefs = handleMethodCallNodeChild(callNode,
								activeDefs, freeUses, foundPairs, nextCallStack,
								investigatedMethod);
						// now we can continue our search with the
						// CCFGMethodReturnNode of our call
						child = callNode.getReturnNode();
					}
				}

				// if (children.size() > 1)
				// System.out.println("  found branching point: "
				// + node.toString());

				// only have to copy stuff if current node has more than one
				// child
				if (children.size() > 1)
					invocationCount = determineInterMethodPairs(
							investigatedMethod, child, new HashSet<CCFGNode>(
									handled), new HashSet<CCFGEdge>(
									handledBackEdges),
							copyActiveDefs(activeDefs),
							new HashSet<BytecodeInstruction>(freeUses),
							foundPairs, copyCallStack(nextCallStack),
							invocationCount, handleLoops);
				else
					invocationCount = determineInterMethodPairs(
							investigatedMethod, child, handled,
							handledBackEdges, activeDefs, freeUses, foundPairs,
							nextCallStack, invocationCount, handleLoops);

				if (invocationCount >= UPPER_PAIR_SEARCH_INVOCATION_BOUND)
					continue;
			}
		return invocationCount;
	}

	/**
	 * If the child is a CCFGMethodCallNode check if we previously determined
	 * reachable DUs in there and if so handle that call separately in order to
	 * minimize computation. make sure to update activeDefs and freeUses
	 * accordingly. after that just proceed with the child of that
	 * CCFGMethodCallNode
	 * 
	 * @param investigatedMethod
	 * 
	 */
	private Set<Map<String, VariableDefinition>> handleMethodCallNodeChild(
			CCFGMethodCallNode callNode,
			Set<Map<String, VariableDefinition>> activeDefs,
			Set<BytecodeInstruction> freeUses,
			Set<DefUseCoverageTestFitness> foundPairs,
			Stack<MethodCall> callStack, CCFGMethodEntryNode investigatedMethod) {
		// create MethodCall object to avoid weird special cases
		MethodCall call = MethodCall.constructForCallNode(callNode);
		// since we already analyzed the called method we will
		// use the previously stored information in determinedActiveDefs
		// and determinedFreeUses
		activeDefs = useStoredInformationForMethodCall(investigatedMethod,
				callNode, activeDefs, freeUses, foundPairs, call);
		// and push a MethodCall onto the Stack in order to avoid special
		// cases in handleMethodReturnNode()
		updateCallStackForCall(callStack, call);

		return activeDefs;
	}
	
	/**
	 * If the given Set of handled nodes already contains the given node, an
	 * IllegalStateException is thrown, because that should not happen.
	 * Otherwise the given node is added to the given Set.
	 * 
	 * Well ... funny method name i know
	 */
	private void handleHandledNodesSet(CCFGNode node, Set<CCFGNode> handled) {
		if (handled.contains(node))
			throw new IllegalStateException(
					"visiting already handled node, should not happen");
		handled.add(node);
	}

	/**
	 * Pushes a MethodCall according to the given MethodCallNode onto the
	 * callStack and filters Set of handled nodes to no longer contain nodes of
	 * the called method except the method call itself.
	 * 
	 * Filtering the handled Set is due to the fact, that we will have to visit
	 * some nodes more than once in case of a recursive method call for example
	 */
	private Set<CCFGNode> handleMethodCallNode(CCFGNode node,
			Stack<MethodCall> callStack, Set<CCFGNode> handled) {

		CCFGMethodCallNode callNode = (CCFGMethodCallNode) node;
		updateCallStackForCallNode(callStack, callNode);

		return filterHandledMapForMethodCallNode(callNode, handled);
	}

	/**
	 * If we encounter a CCFGMethodReturnNode during pair search but we no
	 * longer had MethodCalls on our callStack except the initial MethodCall, we
	 * throw an IllegalStateException. We also do this if the top of the
	 * callStack is from a method different to the one from our
	 * CCFGMethodReturnNode.
	 * 
	 * Otherwise we pop the top of our callStack.
	 */
	private void handleMethodReturnNode(CCFGNode node,
			Stack<MethodCall> callStack) {
		if (callStack.peek().isInitialMethodCall())
			throw new IllegalStateException(
					"found method return but had no more method calls on stack");

		CCFGMethodReturnNode retrn = (CCFGMethodReturnNode) node;
		if (!callStack.peek().isMethodCallFor(retrn.getCallInstruction()))
			throw new IllegalStateException(
					"visiting MethodReturnNode even though lastly visited MethodCallNode was from a different method");

		callStack.pop();
	}

	private void handleFrameNode() {
		throw new IllegalStateException(
				"visiting CCFGFrameNode during pair search, which should not happen");
	}

	/**
	 * If this is the methodExit of our investigated public method we remember
	 * our current activeDefs for intra-class pairs
	 * 
	 * @param freeUses
	 */
	private void handleMethodExitNode(CCFGNode node,
			CCFGMethodEntryNode investigatedMethod,
			Set<Map<String, VariableDefinition>> activeDefs,
			Set<BytecodeInstruction> freeUses) {

		CCFGMethodExitNode exitNode = (CCFGMethodExitNode) node;
		if (exitNode.isExitOfMethodEntry(investigatedMethod)) {
			rememberActiveDefs(exitNode.getMethod(), activeDefs);
			rememberFreeUses(exitNode.getMethod(), freeUses);
		}
	}

	private void handleCodeNode(CCFGMethodEntryNode investigatedMethod,
			CCFGNode node, Stack<MethodCall> callStack,
			Set<Map<String, VariableDefinition>> activeDefs,
			Set<BytecodeInstruction> freeUses,
			Set<DefUseCoverageTestFitness> foundPairs) {

		BytecodeInstruction code = ((CCFGCodeNode) node).getCodeInstruction();

		checkCallStackSanity(callStack, code);

		if (code.isUse())
			handleUseInstruction(investigatedMethod, code, callStack,
					activeDefs, freeUses, foundPairs);

		if (code.isDefinition())
			handleDefInstruction(code, callStack, activeDefs);
	}

	private void handleDefInstruction(BytecodeInstruction code,
			Stack<MethodCall> callStack,
			Set<Map<String, VariableDefinition>> activeDefMaps) {

		VariableDefinition def = new VariableDefinition(code, callStack.peek());
		for (Map<String, VariableDefinition> activeDefMap : activeDefMaps) {
			activeDefMap.put(code.getDUVariableName(), def);
			// System.out.println("  setting activeDef:" + def.toString());
		}
	}

	private void handleUseInstruction(CCFGMethodEntryNode investigatedMethod,
			BytecodeInstruction code, Stack<MethodCall> callStack,
			Set<Map<String, VariableDefinition>> activeDefMaps,
			Set<BytecodeInstruction> freeUses,
			Set<DefUseCoverageTestFitness> foundPairs) {

		for (Map<String, VariableDefinition> activeDefs : activeDefMaps) {

			VariableDefinition activeDef = activeDefs.get(code
					.getDUVariableName());
			if (activeDef != null) {

				// we have an intraMethodPair iff use and definition are in
				// the
				// same method and executed during a single invocation of
				// that method
				boolean isIntraPair = activeDef.getMethodCall().equals(
						callStack.peek());
				DefUseCoverageTestFitness.DefUsePairType type;
				if (isIntraPair)
					type = DefUseCoverageTestFitness.DefUsePairType.INTRA_METHOD;
				else {
					type = DefUseCoverageTestFitness.DefUsePairType.INTER_METHOD;
				}

				if (!activeDef.getDefinition().isLocalDU()
						|| type.equals(DefUsePairType.INTRA_METHOD))
					addNewGoalToFoundPairs(investigatedMethod, activeDef, code,
							type, foundPairs);
			} else {
				// if we encounter a use here but have no activeDef yet we know
				// the
				// use has a definition-free path from method start
				if (code.isFieldUse()) {
					freeUses.add(code);
					// System.out.println("  adding free use: " +
					// code.toString());
					;
				}
			}
		}
	}
	
	/**
	 * When we go back to previously visited nodes we do not have to visit nodes
	 * after our current node again. If we follow backEdges we do that so we
	 * find all intra-method pairs within loops, so we go through loops twice so
	 * to speak. however the possible activeDefs are already determined after
	 * the first walk through the loop. so after we make our two runs through
	 * the loop we don't have to walk through everything after the loop again
	 */
	private boolean shouldSkipChildren(CCFGNode node,
			Set<CCFGEdge> handledBackEdges, Set<CCFGNode> children,
			boolean handleLoops) {
		boolean skipChildren = false;
		if (handleLoops) {
			for (CCFGNode child : children) {
				CCFGEdge currentEdge = getEdge(node, child);
				if (handledBackEdges.contains(currentEdge)) {
					skipChildren = true;
					// System.out.println("Skipping nodes. Found already handled backEdge between "+node.toString()+" and "+child.toString());
					break;
				}
			}
		}
		return skipChildren;
	}

	private boolean shouldProcessChild(CCFGNode node, CCFGNode child,
			Set<CCFGNode> handled, Set<CCFGEdge> handledBackEdges,
			boolean handleLoops) {

		if (handleLoops) {
			CCFGEdge currentEdge = getEdge(node, child);
			if (handledBackEdges.contains(currentEdge))
				throw new IllegalStateException(
						"should have been detected earlier");
			if (handled.contains(child)) {
				// whenever we encounter an edge back to a previously handled
				// node we need to clear handled set once, otherwise we are
				// missing
				// intra-method pairs in loops
				handledBackEdges.add(currentEdge);
				handled.clear();
			}
		} else {
			if (handled.contains(child))
				return false;
		}

		return true;
	}

	/**
	 * While a node has exactly 1 further child which is a CCFGCodeNode and not
	 * a DefUse-instruction that child does not need to be processed explicitly
	 * and can be skipped
	 */
	private CCFGNode determineNextRelevantNode(CCFGNode node,
			Set<CCFGNode> handled) {
		CCFGNode nextNode;
		while (outDegreeOf(node) == 1
				&& (nextNode = getSingleChild(node)) instanceof CCFGCodeNode
				&& !handled.contains(nextNode)
				&& !((CCFGCodeNode) nextNode).getCodeInstruction().isDefUse())
			node = nextNode;

		return node;
	}

	private Set<Map<String, VariableDefinition>> copyActiveDefs(
			Set<Map<String, VariableDefinition>> activeDefs) {

		HashSet<Map<String, VariableDefinition>> r = new HashSet<Map<String, VariableDefinition>>();
		for (Map<String, VariableDefinition> activeDef : activeDefs)
			r.add(new HashMap<String, VariableDefinition>(activeDef));
		return r;
	}

	private Set<Map<String, VariableDefinition>> useStoredInformationForMethodCall(
			CCFGMethodEntryNode investigatedMethod,
			CCFGMethodCallNode callNode,
			Set<Map<String, VariableDefinition>> activeDefMapsInCaller,
			Set<BytecodeInstruction> freeUses,
			Set<DefUseCoverageTestFitness> foundPairs, MethodCall call) {

		//
		Set<BytecodeInstruction> freeUsesInCalledMethod = determinedFreeUses
				.get(callNode.getCalledMethod());

		for (BytecodeInstruction freeUseInCalledMethod : freeUsesInCalledMethod) {
			for (Map<String, VariableDefinition> activeDefMap : activeDefMapsInCaller) {
				VariableDefinition activeDef = activeDefMap
						.get(freeUseInCalledMethod.getDUVariableName());

				if (activeDef == null) {
					// there was a path to the calledMethod that did not define
					// the variable of our freeUse, so it is still free
					freeUses.add(freeUseInCalledMethod);
				} else {
					// checkActiveDefsSetSanity(activeDefs);

					// otherwise, we have an active definition for a variable
					// that is free in the called method so we have a new
					// inter-method pair
					if (freeUseInCalledMethod.isFieldUse()) {
						addNewGoalToFoundPairs(investigatedMethod, activeDef,
								freeUseInCalledMethod,
								DefUsePairType.INTER_METHOD, foundPairs);
					}
				}
			}
		}

		Set<Map<String, BytecodeInstruction>> activeDefMapsInCallee = determinedActiveDefs
				.get(callNode.getCalledMethod());

		Set<Map<String, VariableDefinition>> activeDefMapsAfterCurrentCall = new HashSet<Map<String, VariableDefinition>>();

		long start = System.currentTimeMillis();

		// since every defMap in my previously determined activeDefMaps
		// represents one possible configuration of activeDefs i will have to
		// mingle each of these maps with each of the currently active maps
		for (Map<String, BytecodeInstruction> activeDefMapInCallee : activeDefMapsInCallee) {
			for (Map<String, VariableDefinition> activeDefMapInCaller : activeDefMapsInCaller) {
				Set<String> relevantVariables = new HashSet<String>(
						activeDefMapInCallee.keySet());
				relevantVariables.addAll(activeDefMapInCaller.keySet());

				// mingle both activeDefMaps from prior to the call and when
				// returning from call to a new one that will be true after the
				// call
				Map<String, VariableDefinition> activeDefMapAfterCurrentCall = new HashMap<String, VariableDefinition>();
				for (String variable : relevantVariables) {
					BytecodeInstruction activeDefAfterCall = activeDefMapInCallee
							.get(variable);
					VariableDefinition activeDefPriorToCall = activeDefMapInCaller
							.get(variable);

					if (activeDefAfterCall == null) {
						if (activeDefPriorToCall == null)
							throw new IllegalStateException(
									"expect activeDefMaps not to map to null values");

						// variable was not overwritten in called
						// method, so the activeDef prior to the call stays
						// active
						activeDefMapAfterCurrentCall.put(variable,
								activeDefPriorToCall);
					} else {
						// variable was overwritten in call, so we will make a
						// new VariableDefinition and keep that active in the
						// newly created map
						VariableDefinition overwritingDefinition = new VariableDefinition(
								activeDefAfterCall, call);
						activeDefMapAfterCurrentCall.put(variable,
								overwritingDefinition);
					}
				}

				// System.out.println("mingled map:");
				// printVDDefMap(activeDefMapAfterCurrentCall);

				activeDefMapsAfterCurrentCall.add(activeDefMapAfterCurrentCall);
			}
		}

		// System.out.println("Finished mingling. #Resulting-Maps: "+activeDefMapsAfterCurrentCall.size());

		timeSpentMingling += System.currentTimeMillis() - start;

		return activeDefMapsAfterCurrentCall;
	}

	private boolean alreadyAnalzedMethod(String method) {

		if (determinedFreeUses.get(method) != null) {
			if (determinedActiveDefs.get(method) == null)
				throw new IllegalStateException(
						"found already determined freeUse but no activeDefs for method "
								+ method);
			return true;
		}
		return false;
	}
	
	/**
	 * Creates a new MethodCall object for the given MethodCallNode and pushes
	 * it onto the given callStack.
	 */
	private void updateCallStackForCallNode(Stack<MethodCall> callStack,
			CCFGMethodCallNode callNode) {

		MethodCall call = MethodCall.constructForCallNode(callNode);
		updateCallStackForCall(callStack,call);
	}

	/**
	 * Pushes the given MethodCall object onto the given callStack 
	 */
	private void updateCallStackForCall(Stack<MethodCall> callStack,
			MethodCall call) {

		callStack.push(call);
	}

	private void addNewGoalToFoundPairs(CCFGMethodEntryNode investigatedMethod,
			VariableDefinition activeDef, BytecodeInstruction code,
			DefUsePairType type, Set<DefUseCoverageTestFitness> foundPairs) {

		addNewGoalToFoundPairs(investigatedMethod, activeDef.getDefinition(),
				code, type, foundPairs);
	}

	private void addNewGoalToFoundPairs(CCFGMethodEntryNode investigatedMethod,
			BytecodeInstruction activeDef, BytecodeInstruction freeUse,
			DefUsePairType type, Set<DefUseCoverageTestFitness> foundPairs) {

		checkDefinitionSanity(activeDef);
		checkUseSanity(freeUse);

		if (type.equals(DefUsePairType.INTER_METHOD)
				&& !isPublicMethod(investigatedMethod))
			return;

		DefUseCoverageTestFitness goal = DefUseCoverageFactory.createGoal(
				activeDef, freeUse, type);
		if (goal != null) {
			foundPairs.add(goal);
			// System.out.println("  created goal: " + goal.toString());
		}
	}

	private boolean handleReturnNodeChild(CCFGNode child,
			Stack<MethodCall> callStack) {

		if (callStack.peek().isInitialMethodCall())
			return true;
		CCFGMethodReturnNode retrn = (CCFGMethodReturnNode) child;
		if (!callStack.peek().isMethodCallFor(retrn.getCallInstruction()))
			return true;

		return false;
	}

	private void handleFrameNodeChild(CCFGNode child) {
		CCFGFrameNode frameNode = (CCFGFrameNode) child;
		if (!frameNode.getType().equals(FrameNodeType.RETURN))
			throw new IllegalStateException(
					"found CCFGFrameNode that was not of type RETURN. should not be possible");
	}

	private void rememberActiveDefs(String method,
			Set<Map<String, VariableDefinition>> activeDefMaps) {

		if (determinedActiveDefs.get(method) == null)
			determinedActiveDefs.put(method,
					new HashSet<Map<String, BytecodeInstruction>>());

		Set<Map<String, BytecodeInstruction>> defMaps = toRememberableBytecodeInstructionMap(activeDefMaps);

		for (Map<String, BytecodeInstruction> defMap : defMaps) {
			determinedActiveDefs.get(method).add(defMap);
		}
	}

	private void rememberFreeUses(String method,
			Set<BytecodeInstruction> freeUses) {
		if (determinedFreeUses.get(method) == null)
			determinedFreeUses.put(method, new HashSet<BytecodeInstruction>());
		determinedFreeUses.get(method).addAll(freeUses);
	}

	private Stack<MethodCall> copyCallStack(Stack<MethodCall> callStack) {
		Stack<MethodCall> r = new Stack<MethodCall>();
		r.setSize(callStack.size());
		Collections.copy(r, callStack);
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

	private void freeMemory() {
		determinedActiveDefs = null;
		determinedFreeUses = null;
		analyzedMethods = null;
	}

	// sanity functions

	/**
	 * Returns true iff the given invocationCount has exceeded the upper limit
	 * defined by UPPER_PAIR_SEARCH_INVOCATION_BOUND
	 */
	private boolean checkInvocationBound(int invocationCount,
			Stack<MethodCall> callStack) {

		if (invocationCount % (UPPER_PAIR_SEARCH_INVOCATION_BOUND / 10) == 0) {
			int percent = invocationCount
					/ (UPPER_PAIR_SEARCH_INVOCATION_BOUND / 10);
			System.out.print(percent + "0% .. ");
		}

		if (invocationCount >= UPPER_PAIR_SEARCH_INVOCATION_BOUND) {
			if (!warnedAboutAbortion) {
				System.out.println();
				System.out.println("* ABORTED inter method pair search in "
						+ callStack.peek()
						+ "! Reached maximum invocation limit: "
						+ UPPER_PAIR_SEARCH_INVOCATION_BOUND);
				warnedAboutAbortion = true;
			}
			return true;
		}
		return false;
	}

	/**
	 * If the method on top of the callStack differs from the method of the
	 * given BytecodeInstruction this methods throws an IllegalStateException
	 */
	private void checkCallStackSanity(Stack<MethodCall> callStack,
			BytecodeInstruction code) {

		if (!callStack.peek().getCalledMethodName()
				.equals(code.getMethodName())) {
			
			for(MethodCall mc : callStack) {
				System.out.println("  "+mc.toString());
			}
			
			throw new IllegalStateException(
					"insane callStack: peek is in method "
							+ callStack.peek().getCalledMethodName()
							+ " and i encountered code of method "
							+ code.getMethodName());
		}
	}

	private void checkFreeUseSanity(BytecodeInstruction freeUse) {
		checkUseSanity(freeUse);

		if (!freeUse.isFieldUse())
			throw new IllegalStateException(
					"expect all freeUses to be Use instructions for field variable");
	}

	private void checkUseSanity(BytecodeInstruction freeUse) {
		if (freeUse == null)
			throw new IllegalStateException(
					"null values not allowed in freeUses map");
		else if (!freeUse.isUse())
			throw new IllegalStateException(
					"expect all freeUses to be Use instructions");
	}

	private void checkDefinitionSanity(BytecodeInstruction activeDef) {
		if (activeDef == null)
			throw new IllegalStateException(
					"null values not allowed in activeDef map");
		else if (!activeDef.isDefinition())
			throw new IllegalStateException(
					"expect all activeDefs to be Definition instructions");
	}

	/**
	 * Copies the given Maps to VariableDefinitions to correpsonding Maps to
	 * BytecodeInstructions and filters out local variables.
	 */
	private Set<Map<String, BytecodeInstruction>> toRememberableBytecodeInstructionMap(
			Set<Map<String, VariableDefinition>> activeDefMaps) {

		Set<Map<String, BytecodeInstruction>> r = new HashSet<Map<String, BytecodeInstruction>>();

		for (Map<String, VariableDefinition> activeDefMap : activeDefMaps) {
			Map<String, BytecodeInstruction> instructionMap = new HashMap<String, BytecodeInstruction>();
			for (String var : activeDefMap.keySet()) {
				VariableDefinition activeDef = activeDefMap.get(var);
				if (activeDef.getDefinition().isLocalDU())
					continue;
				instructionMap.put(var, activeDef.getDefinition());
			}
			r.add(instructionMap);
		}
		return r;
	}

	private boolean isPublicMethod(String method) {
		if (method == null)
			return false;
		CCFGMethodEntryNode entry = getMethodEntryOf(method);
		return isPublicMethod(entry);
	}

	private boolean isPublicMethod(CCFGMethodEntryNode node) {
		if (node == null)
			return false;
		return publicMethods.contains(node);
	}
	
	private Set<Map<String, VariableDefinition>> createInitialActiveDefs() {
		Set<Map<String, VariableDefinition>> activeDefs = new HashSet<Map<String, VariableDefinition>>();
		// add initial activeDefMap
		activeDefs.add(new HashMap<String, VariableDefinition>());
		return activeDefs;
	}

	private Stack<MethodCall> createInitialCallStack(
			CCFGMethodEntryNode publicMethodEntry) {
		Stack<MethodCall> callStack = new Stack<MethodCall>();
		// null will represent the public method call itself
		callStack.add(new MethodCall(null, publicMethodEntry.getMethod()));

		return callStack;
	}

	// convenience getters

	private CCFGMethodEntryNode getMethodEntryNodeForClassCallNode(
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
		return GraphPool.getRawCFG(className, ccgNode.getMethod());
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
			if(e.isExceptionEdge())
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

	@Override
	public String getName() {
		return "CCFG_" + className;
	}

	@Override
	protected String dotSubFolder() {
		return toFileString(className) + "/";
	}
}
