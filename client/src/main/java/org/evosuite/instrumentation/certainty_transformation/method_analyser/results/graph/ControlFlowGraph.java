package org.evosuite.instrumentation.certainty_transformation.method_analyser.results.graph;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.MethodEnter;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.MethodExit;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.variables.VariableTable;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations.*;
import org.objectweb.asm.Type;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class ControlFlowGraph extends InstructionGraph implements Serializable {


    /**
     * Caches for costly computations, that should not be done more than once.
     */
    private transient Map<ByteCodeInstruction, Set<ByteCodeInstruction>> postDominators = null;
    private transient Map<ByteCodeInstruction, ByteCodeInstruction> iPostDominators = null;
    private transient DirectedGraph<ByteCodeInstruction> postDominatorTree_dep = null;
    private transient PostDominatorTree postDominatorTree = null;
    private transient ControlDependenceGraph controlDependenceGraph = null;
    private transient ControlFlowGraph augmentedControlFlowGraph = null;

    public ControlFlowGraph(Collection<Node<ByteCodeInstruction>> nodes, Collection<Edge<ByteCodeInstruction>> edges) {
        super(nodes, edges);
    }

    public ControlFlowGraph(DirectedGraph<ByteCodeInstruction> graph) {
        super(graph.getNodes(), graph.getEdges());
    }

    @Deprecated
    public StackTypeSet computeTypeOfTopAt(ByteCodeInstruction instruction, VariableTable table) {
        Objects.requireNonNull(instruction);
        Objects.requireNonNull(table);
        Optional<Path<ByteCodeInstruction>> dijkstra = this.dijkstra(entryNode, instruction);
        Path<ByteCodeInstruction> path = dijkstra.get();
        TypeStack stack = new TypeStack(table);
        for (Edge<ByteCodeInstruction> byteCodeInstructionEdge : path) {
            ByteCodeInstruction source = byteCodeInstructionEdge.getSource().getContent();
            System.out.println("APPLYING INSTRUCTION: " + source);
            ByteCodeInstruction dest = byteCodeInstructionEdge.getDestination().getContent();
            TypeStackManipulation stackManipulation = source.getStackManipulation(table, dest);
            System.out.println("    MANIPULATION: ");
            System.out.println("    " + stackManipulation);
            stack = stackManipulation.apply(stack);
            System.out.println("    NEW STACK: ");
            System.out.println("    " + stack);
        }
        return stack.getOrDefault(0, Type.VOID);
    }

    public Map<ByteCodeInstruction, InstructionInputOutputFrames> computeStackFrameLayouts(VariableTable table) {
        Map<ByteCodeInstruction, InstructionInputOutputFrames> init = computeStackFrameLayoutsInit(table);
        ByteCodeInstruction methodEnter = getFilteredNodeSet(n -> n.getOrder() == -1).iterator().next();
        init.put(methodEnter, init.get(methodEnter).updateInput(new FrameLayout(Collections.emptyList(),
                false)));
        while (init.values().stream().anyMatch(InstructionInputOutputFrames::hasChanged)) {
            try {
                init = computeStackFrameLayoutsIteration(init);
            } catch (Exception e){
                throw new IllegalStateException("Failed to compute Frames for class: " + entryNode.getClassName(),e);
            }
        }
        return init;
    }

    /**
     * Computes the next Iteration of InstructionInputOutputFrames.
     *
     * @param map the current mapping of InstructionInputOutputFrames.
     * @return the new mapping of InstructionInputOutputFrames.
     */
    Map<ByteCodeInstruction, InstructionInputOutputFrames> computeStackFrameLayoutsIteration(Map<ByteCodeInstruction,
            InstructionInputOutputFrames> map) {
        Map<ByteCodeInstruction, InstructionInputOutputFrames> next = new HashMap<>();
        map.forEach((key, value) -> next.putIfAbsent(key, value.resetChanged()));
        for (Map.Entry<ByteCodeInstruction, InstructionInputOutputFrames> entry : map.entrySet()) {
            ByteCodeInstruction key = entry.getKey();
            InstructionInputOutputFrames value = entry.getValue();
            // InputFrame has changed.
            if (value.isInputFrameChanged()) {
                Collection<ByteCodeInstruction> predecessors = getPredecessors(key);
                // Update all predecessors
                for (ByteCodeInstruction predecessor : predecessors) {
                    InstructionInputOutputFrames frames = next.get(predecessor);
                    InstructionInputOutputFrames newFrames = frames.updateOutput(value.getInputFrameLayout(), key);
                    next.put(predecessor, newFrames);
                }
            }
            // For every OutputFrame, that has changed. Update the successors
            for (ByteCodeInstruction successor : value.getOutputFrameChanged()) {
                InstructionInputOutputFrames frames = next.get(successor);
                // if frames == null, the successor would be the Method Exit node
                if (frames != null) {
                    InstructionInputOutputFrames newFrames = frames.updateInput(value.getOutputFrames().get(successor));
                    next.put(successor, newFrames);
                }
            }
        }
        map.forEach((k,v) -> next.put(k,InstructionInputOutputFrames.computeChanges(v,next.get(k))));
        return next;
    }

    @Override
    public Set<Path<ByteCodeInstruction>> getAllAcyclicPathsFromTo(ByteCodeInstruction from, ByteCodeInstruction to) {
        if(from instanceof MethodEnter || to instanceof MethodExit){
            return Collections.emptySet();
        }
        return super.getAllAcyclicPathsFromTo(from, to);
    }

    /**
     * Compute the initial input/outputs stack frame layouts. (No information gained by analysing the predecessors
     * and successors is used yet)
     *
     * @param table the local variable table.
     * @return A map containing a InstructionInputOutputFrames object for every Instruction in the CFG.
     */
    Map<ByteCodeInstruction, InstructionInputOutputFrames> computeStackFrameLayoutsInit(VariableTable table) {
        Map<ByteCodeInstruction, InstructionInputOutputFrames> init = new HashMap<>();
        for (ByteCodeInstruction instruction : this.getFilteredNodeSet(n -> !(n instanceof MethodExit))) {
            Collection<ByteCodeInstruction> successors = getSuccessors(instruction);
            Map<ByteCodeInstruction, TypeStackManipulation> manipulationMap = new HashMap<>();
            for (ByteCodeInstruction successor : successors) {
                manipulationMap.put(successor, instruction.getStackManipulation(table, successor));
            }
            init.put(instruction, constructMinimalFrames(instruction, manipulationMap));
        }
        return init;
    }

    /**
     * Constructs the minimal input and output frames for a Instruction in the CFG.
     *
     * @param instruction     The instruction for which the minimal frames should be computed.
     * @param manipulationMap A map containing a TypeStackManipulation object for every successor of
     *                        {@param instruction}
     * @return the minimal input and output frames.
     */
    static InstructionInputOutputFrames constructMinimalFrames(ByteCodeInstruction instruction,
                                                               Map<ByteCodeInstruction, TypeStackManipulation> manipulationMap) {
        Set<FrameLayout> collect =
                manipulationMap.values().stream().map(TypeStackManipulation::computeMinimalBefore).collect(Collectors.toSet());
        FrameLayout[] layouts = new FrameLayout[collect.size()];
        FrameLayout merge = InstructionInputOutputFrames.merge(collect.toArray(layouts));
        return new InstructionInputOutputFrames(instruction, merge, manipulationMap);
    }

    private void computeAugmentedControlFlowGraph(){
        DirectedGraphBuilder<ByteCodeInstruction> acfgBuilder = new MutableDirectedGraphBuilder<>(this);
        acfgBuilder = acfgBuilder.addEdge(this.getEntry(), this.getExit());
        augmentedControlFlowGraph = acfgBuilder.build(ControlFlowGraph::new);
    }

    public ControlFlowGraph getACFG(){
        if(augmentedControlFlowGraph == null){
            computeAugmentedControlFlowGraph();
        }
        return augmentedControlFlowGraph;
    }

    public PostDominatorTree getPDT() {
        if (postDominatorTree == null)
            postDominatorTree = PostDominatorTree.computePostDominanceTree(this);
        return postDominatorTree;
    }

    public ControlDependenceGraph getCDT() {
        if (controlDependenceGraph == null)
            controlDependenceGraph = ControlDependenceGraph.compute(this);
        return controlDependenceGraph;
    }


    /**************************************************************
     * ************************************************************
     * ******** Deprecated, use getPDT()/ getCDT() ****************
     * ************************************************************
     **************************************************************/

    /**
     * Whether {@param b} is control dependent on {@param a}.
     * b is control dependent on a if:
     * - There exists a direct path from a to b in the control flow graph
     * where all nodes are postdominated by b.
     * - a is not post-dominated by b.
     *
     * @param b is b in the definition of control dependence
     * @param a is a in the definition of control dependence
     * @return Whether {@param b} is control dependent on {@param a}
     */
    @Deprecated
    boolean isControlDependent(ByteCodeInstruction b, ByteCodeInstruction a) {
        if (!getContents().contains(b) || !getContents().contains(a)) {
            throw new IllegalArgumentException("Instruction not contained in CFG");
        }
        Set<ByteCodeInstruction> postDominators = getPostDominators(b);
        Set<Path<ByteCodeInstruction>> allAcyclicPathsFromTo = getAllAcyclicPathsFromTo(a, b);
        Optional<Path<ByteCodeInstruction>> any =
                allAcyclicPathsFromTo
                        .stream().filter(p -> p.getContents().stream().map(Node::getContent)
                        .filter(x -> !x.equals(a) && !x.equals(b))
                        .map(this::getPostDominators).allMatch(pDoms -> pDoms.contains(b))).findAny();
        return any.isPresent() && !postDominators.contains(a);
    }

    @Deprecated
    ByteCodeInstruction getMostCommonPostDominator(ByteCodeInstruction first, ByteCodeInstruction second,
                                                   boolean strict) {
        if (postDominatorTree_dep == null)
            computePostDominanceTree();
        ByteCodeInstruction nextFirstAncestor = first;
        ByteCodeInstruction nextSecondAncestor = second;
        Set<ByteCodeInstruction> firstAncestors;
        Set<ByteCodeInstruction> secondAncestors;
        if (!strict) {
            firstAncestors = new HashSet<>(Collections.singleton(first));
            secondAncestors = new HashSet<>(Collections.singleton(second));
        } else {
            firstAncestors = new HashSet<>();
            secondAncestors = new HashSet<>();
        }
        while (!firstAncestors.contains(nextSecondAncestor) && !secondAncestors.contains(nextFirstAncestor)) {
            Iterator<ByteCodeInstruction> nextFirstIterator =
                    postDominatorTree_dep.getPredecessors(nextFirstAncestor).iterator();
            Iterator<ByteCodeInstruction> nextSecondIterator =
                    postDominatorTree_dep.getPredecessors(nextSecondAncestor).iterator();
            nextFirstAncestor = nextFirstIterator.hasNext() ? nextFirstIterator.next() : nextFirstAncestor;
            nextSecondAncestor = nextSecondIterator.hasNext() ? nextSecondIterator.next() : nextSecondAncestor;
            firstAncestors.add(nextFirstAncestor);
            secondAncestors.add(nextSecondAncestor);
        }
        firstAncestors.retainAll(secondAncestors);
        return firstAncestors.iterator().next();
    }

    /**
     * Computes the post-dominance tree for this CFG.
     *
     * @return The post-dominance tree for this ControlFlowGraph.
     */
    @Deprecated
    DirectedGraph<ByteCodeInstruction> computePostDominanceTree() {
        if (postDominatorTree_dep == null) {
            postDominatorTree_dep = PostDominatorTree.computePostDominanceTree(this);
            final Collection<ByteCodeInstruction> instructions = getContents();

            Map<ByteCodeInstruction, ByteCodeInstruction> iDominators = getImmediatePostDominators();

            final DirectedGraphBuilder<ByteCodeInstruction> postDominanceTree = new MutableDirectedGraphBuilder<>();
            instructions.forEach(postDominanceTree::addContent);
            instructions.forEach(
                    node -> {
                        if (iDominators.containsKey(node)) {
                            postDominanceTree.addEdge(iDominators.get(node), node);
                        }
                    });
            postDominatorTree_dep = postDominanceTree.build();
        }
        return postDominatorTree_dep;
    }

    @Deprecated
    Map<ByteCodeInstruction, ByteCodeInstruction> getImmediatePostDominators() {
        if (iPostDominators == null) {
            Map<ByteCodeInstruction, ByteCodeInstruction> map = new HashMap<>();
            getContents().forEach(i -> {
                ByteCodeInstruction immediatePostDominator = getImmediatePostDominator(i);
                if (immediatePostDominator != null) map.put(i, immediatePostDominator);
            });
            iPostDominators = map;
        }
        return new HashMap<>(iPostDominators);
    }

    @Deprecated
    ByteCodeInstruction getImmediatePostDominator(ByteCodeInstruction instruction) {
        if (!getContents().contains(instruction))
            throw new IllegalArgumentException("Instruction not contained in CFG");
        if (iPostDominators == null) {
            Set<ByteCodeInstruction> sDominators =
                    getPostDominators(instruction).stream().filter(i -> !i.equals(instruction)).collect(Collectors.toSet());
            Collection<ByteCodeInstruction> visiting = getSuccessors(instruction);
            Set<ByteCodeInstruction> visited = new HashSet<>();
            ByteCodeInstruction iDom = null;
            while (!visiting.isEmpty()) {
                Set<ByteCodeInstruction> foundStrictDominators =
                        visiting.stream().filter(sDominators::contains).collect(Collectors.toSet());
                if (foundStrictDominators.isEmpty()) {
                    visited.addAll(visiting);
                    Collection<ByteCodeInstruction> temp = new HashSet<>();
                    visiting.forEach(el -> temp.addAll(getSuccessors(el)));
                    visiting = temp.stream().filter(el -> !visited.contains(el)).collect(Collectors.toSet());
                } else if (foundStrictDominators.size() == 1) {
                    iDom = foundStrictDominators.iterator().next();
                    visiting.clear();
                } else {
                    throw new IllegalStateException("Found multiple strict Dominators with same distance!");
                }
            }
            return iDom;
        } else
            return iPostDominators.get(instruction);
    }

    /**
     * Compute the post-dominator set for each instruction in the graph.
     *
     * @return a Map containing the post-dominator set for each key.
     */
    @Deprecated
    Map<ByteCodeInstruction, Set<ByteCodeInstruction>> computeAllPostDominators() {
        if (postDominators != null) {
            return new HashMap<>(postDominators);
        }
        Map<ByteCodeInstruction, Set<ByteCodeInstruction>> dominators = new HashMap<>();
        dominators.put(exitNode, Collections.singleton(exitNode));
        Collection<ByteCodeInstruction> contents = getContents().stream().filter(n -> !n.equals(exitNode)).collect(Collectors.toList());
        contents.forEach(n -> {
            dominators.put(n,
                    new HashSet<>(contents));
        });
        boolean changed = true;
        while (changed) {
            changed = false;
            for (ByteCodeInstruction n : getFilteredNodeSet(x -> !x.equals(exitNode))) {
                Set<ByteCodeInstruction> currentDominators = dominators.get(n);
                final Set<ByteCodeInstruction> newDominators = new HashSet<>(Collections.singleton(n));
                Collection<ByteCodeInstruction> successors = getSuccessors(n);
                final Set<ByteCodeInstruction> successorDominators =
                        new HashSet<>(dominators.get(successors.iterator().next()));
                successors.forEach(
                        successor -> successorDominators.retainAll(dominators.get(successor)));
                newDominators.addAll(successorDominators);
                if (!currentDominators.equals(newDominators)) {
                    dominators.put(n, newDominators);
                    changed = true;
                }
            }
        }
        postDominators = new HashMap<>(dominators);
        return dominators;
    }

    @Deprecated
    Set<ByteCodeInstruction> getPostDominators(ByteCodeInstruction instruction) {
        Objects.requireNonNull(instruction);
        if (!getContents().contains(instruction))
            throw new IllegalArgumentException("Instruction is not contained in CFG");
        return computeAllPostDominators().get(instruction);
    }

    @Deprecated
    Set<ByteCodeInstruction> getPostDominators(Node<ByteCodeInstruction> node) {
        return getPostDominators(node.getContent());
    }



}