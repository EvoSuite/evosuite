package org.evosuite.instrumentation.certainty_transformation.method_analyser.results.graph;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;

import java.util.*;
import java.util.stream.Collectors;

public class PostDominatorTree extends DirectedGraph<ByteCodeInstruction> {

    private final static Map<ControlFlowGraph, Map<Node<ByteCodeInstruction>, Set<Node<ByteCodeInstruction>>>> dominatorCache =
            new HashMap<>();

    PostDominatorTree(Collection<Node<ByteCodeInstruction>> nodes,
                      Collection<Edge<ByteCodeInstruction>> edges) {
        super(nodes, edges);
    }

    public static void clearCaches(){
        dominatorCache.clear();
    }

    /**
     * Computes a PostDominatorTree from a ControlFlowGraph
     *
     * @param cfg
     * @return
     */
    public static PostDominatorTree computePostDominanceTree(ControlFlowGraph cfg) {
        final Collection<ByteCodeInstruction> instructions = cfg.getContents();

        Map<Node<ByteCodeInstruction>, Node<ByteCodeInstruction>> iDominators = computeImmediatePostDominators(cfg,
                computeStrictDominators(cfg));

        final DirectedGraphBuilder<ByteCodeInstruction> postDominanceTree = new MutableDirectedGraphBuilder<>();
        instructions.forEach(postDominanceTree::addContent);
        iDominators.forEach((key,value) -> postDominanceTree.addEdge(value,key));
        return postDominanceTree.build(PostDominatorTree::new);
    }


    /**
     * Computes the dominators of all nodes in the CFG
     *
     * @param pCFG the control flow graph for which the dominators should be computed.
     * @return A map, such map.get(node) returns all dominators of node in the given CFG
     */
    private static Map<Node<ByteCodeInstruction>, Set<Node<ByteCodeInstruction>>> computePostDominators(ControlFlowGraph pCFG) {
        if(!dominatorCache.containsKey(pCFG)) {
            String methodName = pCFG.getEntry().getContent().getMethodName();
            String className = pCFG.getEntry().getContent().getClassName();
            Collection<Node<ByteCodeInstruction>> nodes = pCFG.getNodes();
            final Node<ByteCodeInstruction> exitNode = pCFG.getExit();
            final Set<Node<ByteCodeInstruction>> innerNodes =
                    nodes.stream().filter(c -> !c.equals(exitNode)).collect(Collectors.toSet());
            final Map<Node<ByteCodeInstruction>, Set<Node<ByteCodeInstruction>>> successorMap = new HashMap<>();
            final Map<Node<ByteCodeInstruction>, Set<Node<ByteCodeInstruction>>> dominators = new HashMap<>();
            dominators.put(exitNode, Collections.singleton(exitNode));
            innerNodes.forEach(node -> dominators.put(node, new HashSet<>(nodes)));
            nodes.forEach(node -> successorMap.put(node, new HashSet<>(pCFG.getSuccessors(node))));
            boolean changed = true;
            int i = 0;
            while (changed) {
                changed = false;
                for (Node<ByteCodeInstruction> node : innerNodes) {
                    final Set<Node<ByteCodeInstruction>> currentDominators = dominators.get(node);
                    final Set<Node<ByteCodeInstruction>> newDominators = new HashSet<>();
                    newDominators.add(node);
                    final Collection<Node<ByteCodeInstruction>> successors = successorMap.get(node);
                    final Set<Node<ByteCodeInstruction>> successorDominators =
                            new HashSet<>(dominators.get(successors.iterator().next()));
                    successors.forEach(
                            successor -> successorDominators.retainAll(dominators.get(successor)));
                    newDominators.addAll(successorDominators);
                    if (!currentDominators.equals(newDominators)) {
                        dominators.put(node, newDominators);
                        changed = true;
                    }
                }
            }
            dominatorCache.put(pCFG,dominators);
        }
        return dominatorCache.get(pCFG);
    }

    private static Map<Node<ByteCodeInstruction>, Set<Node<ByteCodeInstruction>>> computeStrictDominators(ControlFlowGraph cfg) {
        final Collection<Node<ByteCodeInstruction>> nodes = cfg.getNodes();
        final Map<Node<ByteCodeInstruction>, Set<Node<ByteCodeInstruction>>> dominators = computePostDominators(cfg);
        final Map<Node<ByteCodeInstruction>, Set<Node<ByteCodeInstruction>>> sDominators = new HashMap<>();
        nodes.forEach(node -> sDominators.put(node, computeStrictDominator(node, dominators)));
        return sDominators;
    }

    /**
     * Computes the strict dominators for a node
     *
     * @param node       The node, the strict dominators are requested.
     * @param dominators A map of all Dominators for a node.
     * @return All strict dominators for {@param node}
     */
    private static Set<Node<ByteCodeInstruction>> computeStrictDominator(Node<ByteCodeInstruction> node, Map<Node<ByteCodeInstruction>,
            Set<Node<ByteCodeInstruction>>> dominators) {
        Set<Node<ByteCodeInstruction>> sDOM = new HashSet<>(dominators.get(node));
        sDOM.remove(node);
        Set<Node<ByteCodeInstruction>> unstrictDominators = new HashSet<>();
        for (Node<ByteCodeInstruction> p : sDOM) {
            if (sDOM.stream().anyMatch(s -> !s.equals(p) && dominators.get(s).contains(p)))
                unstrictDominators.add(p);
        }
        sDOM.removeAll(unstrictDominators);
        return sDOM;
    }

    /**
     * Computes the immediate dominator for a Node in a CFG.
     *
     * @param pCFG        The CFG.
     * @param node        The node, the immediate dominator is required.
     * @param sDominators All strict dominators of {@param node} in {@param pCFG}
     * @return the immediate dominator of {@param node} in {@param pCFG}
     */
    static private Node<ByteCodeInstruction> computeImmediatePostDominator(ControlFlowGraph pCFG, Node<ByteCodeInstruction> node, Set<Node<ByteCodeInstruction>> sDominators) {
        Collection<Node<ByteCodeInstruction>> visiting = pCFG.getSuccessors(node);
        Set<Node<ByteCodeInstruction>> visited = new HashSet<>();
        Node<ByteCodeInstruction> iDom = null;
        while (!visiting.isEmpty()) {
            Set<Node<ByteCodeInstruction>> foundStrictDominators =
                    visiting.stream().filter(sDominators::contains).collect(Collectors.toSet());
            if (foundStrictDominators.isEmpty()) {
                // search in all unvisited predecessors of the currently visited nodes.
                visited.addAll(visiting);
                Collection<Node<ByteCodeInstruction>> temp = new HashSet<>();
                visiting.forEach(el -> temp.addAll(pCFG.getSuccessors(el)));
                visiting = temp.stream().filter(el -> !visited.contains(el)).collect(Collectors.toSet());
            } else if (foundStrictDominators.size() == 1) {
                iDom = foundStrictDominators.iterator().next();
                visiting.clear();
            } else {
                throw new IllegalStateException("Found multiple strict Dominators with same distance!");
            }
        }
        return iDom;
    }

    /**
     * Computes the immediate dominator for every node in a CFG
     *
     * @param pCFG        the CFG, for which the immediate dominators are requested.
     * @param sDominators The strict dominators for each node.
     * @return A Map, such Map.get(node) returns the immediate dominator of node for every node in
     * {@param pCFG}.
     */
    static Map<Node<ByteCodeInstruction>, Node<ByteCodeInstruction>> computeImmediatePostDominators(ControlFlowGraph pCFG,
                                                                                                    Map<Node<ByteCodeInstruction>,
                                                                                                        Set<Node<ByteCodeInstruction>>> sDominators) {
        Collection<Node<ByteCodeInstruction>> nodes = pCFG.getNodes();
        final Map<Node<ByteCodeInstruction>, Node<ByteCodeInstruction>> iDominators = new HashMap<>();
        for (Node<ByteCodeInstruction> node : nodes) {
            Node<ByteCodeInstruction> iDom = computeImmediatePostDominator(pCFG, node, sDominators.get(node));
            if (iDom != null)
                iDominators.put(node, iDom);
        }
        return iDominators;
    }


    public boolean isPostDominated(ByteCodeInstruction a, ByteCodeInstruction b){
        return dijkstra(b,a).isPresent();
    }

    /**
     * Queries the immediate post dominators of the Byte code instruction {@param a}
     * The optional will be empty, if no immediate post dominator exists, e.g. for Method exit.
     *
     * @param a
     * @return
     */
    public Optional<ByteCodeInstruction> getImmediatePostDominator(ByteCodeInstruction a){
        Collection<ByteCodeInstruction> predecessors = getPredecessors(a);
        if(predecessors.size() == 0)
            return Optional.empty();
        else if(predecessors.size() == 1)
            return Optional.of(predecessors.iterator().next());
        else{
            throw new IllegalStateException("A PDT should be a tree, but Node a has more than one parent");
        }
    }

    public Optional<ByteCodeInstruction> getMostCommonDominator(ByteCodeInstruction a, ByteCodeInstruction b,
                                                                boolean strict){
        Set<ByteCodeInstruction> visitedA = singletonIfNotStrict(a,strict);
        Set<ByteCodeInstruction> visitedB = singletonIfNotStrict(b,strict);
        Optional<ByteCodeInstruction> curA = Optional.of(a);
        Optional<ByteCodeInstruction> curB = Optional.of(b);
        while(Collections.disjoint(visitedA,visitedB) && (curA.isPresent() || curB.isPresent())){
            if(curA.isPresent())
                curA = getImmediatePostDominator(curA.get());
            if(curB.isPresent())
                curB = getImmediatePostDominator(curB.get());
            curA.ifPresent(visitedA::add);
            curB.ifPresent(visitedB::add);
        }
        return visitedA.stream().filter(visitedB::contains).findAny();
    }

    public Set<ByteCodeInstruction> getPostDominators(ByteCodeInstruction a, boolean strict){
        Set<ByteCodeInstruction> postDominators = singletonIfNotStrict(a, strict);
        Optional<ByteCodeInstruction> cur = Optional.of(a);
        while(cur.isPresent()){
            cur.ifPresent(postDominators::add);
            cur = getImmediatePostDominator(cur.get());
        }
        return postDominators;
    }

    public Set<ByteCodeInstruction> getPostDominated(ByteCodeInstruction a,boolean strict){
        Set<ByteCodeInstruction> postDominated = singletonIfNotStrict(a,strict);
        Collection<ByteCodeInstruction> successors = getSuccessors(a);
        while(!successors.isEmpty()){
            postDominated.addAll(successors);
            successors =
                    successors.stream().map(this::getSuccessors).flatMap(Collection::stream).collect(Collectors.toSet());
        }
        return postDominated;
    }

    private static Set<ByteCodeInstruction> singletonIfNotStrict(ByteCodeInstruction elem, boolean strict){
        return  new HashSet<>(strict ? Collections.emptySet() : Collections.singleton(elem));
    }
}
