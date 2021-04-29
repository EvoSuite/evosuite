package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph;

import org.apache.commons.lang3.tuple.Pair;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;

import java.util.*;
import java.util.stream.Collectors;

public class ControlDependenceGraph extends InstructionGraph {


    private ControlDependenceGraph(Collection<Node<ByteCodeInstruction>> nodes,
                                   Collection<Edge<ByteCodeInstruction>> edges) {
        super(nodes, edges);
    }



    /**
     * Computes the control dependence tree for a control flow graph
     *
     * @param cfg
     * @return
     */
    public static ControlDependenceGraph compute(ControlFlowGraph cfg) {
        ControlFlowGraph acfg = cfg.getACFG();
        PostDominatorTree pdt = acfg.getPDT();
        Set<Edge<ByteCodeInstruction>> edgesBetweenNotPostDominatedNodes =
                findEdgesBetweenNotPostDominatedNodes(acfg, pdt);
        Map<Edge<ByteCodeInstruction>, Node<ByteCodeInstruction>> leastCommonAncestors =
                findLeastCommonAncestors(edgesBetweenNotPostDominatedNodes, pdt);
        Map<Node<ByteCodeInstruction>, Collection<Edge<ByteCodeInstruction>>> marks = markWhileBackwardsTraverse(pdt,
                leastCommonAncestors);
        return buildControlDependenceTreeFromMarks(acfg, marks);
    }

    static ControlDependenceGraph buildControlDependenceTreeFromMarks(ControlFlowGraph cfg,
                                                                      Map<Node<ByteCodeInstruction>, Collection<Edge<ByteCodeInstruction>>> marks) {
        Node<ByteCodeInstruction> entryNode = cfg.getEntry();
        DirectedGraphBuilder<ByteCodeInstruction> controlDependenceTree =
                new MutableDirectedGraphBuilder<>(cfg.getNodes(), Collections.emptySet());
        Set<Edge<ByteCodeInstruction>> edges = new HashSet<>();
        for (Node<ByteCodeInstruction> node : cfg.getNodes()) {
            for (Edge<ByteCodeInstruction> edge : marks.get(node)) {
                if(edge instanceof EdgeLabelAdapter){
                    edges.add(new EdgeLabelAdapter<>(new UnweightedEdge<>(edge.getSource(), node),
                            ((EdgeLabelAdapter<ByteCodeInstruction>) edge).getLabel()));
                } else
                    edges.add(new UnweightedEdge<>(edge.getSource(),node));
            }
        }

        PostDominatorTree pdt = cfg.getPDT();
        controlDependenceTree = controlDependenceTree.addEdges(edges);
        for (Node<ByteCodeInstruction> node : cfg.getNodes()) {
            Collection<Node<ByteCodeInstruction>> predecessors =
                    edges.stream().filter(e -> e.getDestination().equals(node)).map(Edge::getSource).collect(Collectors.toSet());
            pdt.isPostDominated(node.getContent(), entryNode.getContent());
            if ((predecessors.size() == 1 && predecessors.iterator().next().equals(node))
                    || predecessors.size() == 0 && !entryNode.equals(node))
                controlDependenceTree = controlDependenceTree.addEdge(entryNode.getContent(), node.getContent());
        }
        return controlDependenceTree.build(ControlDependenceGraph::new);
    }

    static Map<Node<ByteCodeInstruction>, Collection<Edge<ByteCodeInstruction>>> markWhileBackwardsTraverse
            (PostDominatorTree pdt, Map<Edge<ByteCodeInstruction>, Node<ByteCodeInstruction>> leastCommonAncestors) {
        Map<Node<ByteCodeInstruction>, Collection<Edge<ByteCodeInstruction>>> marks = new HashMap<>();
        pdt.getNodes().forEach(node -> marks.put(node, new HashSet<>()));
        leastCommonAncestors.forEach((key, value) -> {
            Node<ByteCodeInstruction> B = key.getDestination();
            Node<ByteCodeInstruction> A = key.getSource();
            Node<ByteCodeInstruction> mark = B;
            while (!mark.equals(value)) {
                marks.get(mark).add(key);
                mark = pdt.getPredecessors(mark).iterator().next();
            }
            if (value.equals(A))
                marks.get(value).add(key);
        });
        return marks;
    }

    static Set<Edge<ByteCodeInstruction>> findEdgesBetweenNotPostDominatedNodes(ControlFlowGraph cfg,
                                                                                PostDominatorTree pdt) {
        if(true)
            return cfg.getEdges().stream().filter(e -> pdt.dijkstra(e.getDestination().getContent(),
                    e.getSource().getContent()).isEmpty()).collect(Collectors.toSet());
        Collection<Edge<ByteCodeInstruction>> edges = cfg.getEdges();
        List<Edge<ByteCodeInstruction>> discard = new ArrayList<>();
        for (Edge<ByteCodeInstruction> edge : edges) {
            Iterator<Node<ByteCodeInstruction>> it = pdt.getPredecessors(edge.getSource()).iterator();
            while (it.hasNext()) {
                Node<ByteCodeInstruction> nextPredecessor = it.next();
                if (nextPredecessor.equals(edge.getDestination())) {
                    discard.add(edge);
                    break;
                } else {
                    it = pdt.getPredecessors(nextPredecessor).iterator();
                }
            }
        }
        edges.removeAll(discard);
        return new HashSet<>(edges);
    }

    static Node<ByteCodeInstruction> findLeastCommonAncestors(Edge<ByteCodeInstruction> edge,
                                                              PostDominatorTree pdt) {
        Node<ByteCodeInstruction> nextFromAncestor = edge.getSource();
        Node<ByteCodeInstruction> nextToAncestor = edge.getDestination();
        Set<Node<ByteCodeInstruction>> fromAncestors = new HashSet<>(Collections.singleton(nextFromAncestor));
        Set<Node<ByteCodeInstruction>> toAncestors = new HashSet<>(Collections.singleton(nextToAncestor));
        while (!fromAncestors.contains(nextToAncestor) && !toAncestors.contains(nextFromAncestor)) {
            Iterator<Node<ByteCodeInstruction>> nextFromIterator =
                    pdt.getPredecessors(nextFromAncestor).iterator();
            Iterator<Node<ByteCodeInstruction>> nextToIterator =
                    pdt.getPredecessors(nextToAncestor).iterator();
            if(!nextToIterator.hasNext() && !nextFromIterator.hasNext())
                throw new IllegalStateException("Could not find an ancestor in the PDT");
            nextFromAncestor = nextFromIterator.hasNext() ? nextFromIterator.next() : nextFromAncestor;
            nextToAncestor = nextToIterator.hasNext() ? nextToIterator.next() : nextToAncestor;
            fromAncestors.add(nextFromAncestor);
            toAncestors.add(nextToAncestor);
        }
        fromAncestors.retainAll(toAncestors);
        return fromAncestors.iterator().next();
    }

    static Map<Edge<ByteCodeInstruction>, Node<ByteCodeInstruction>> findLeastCommonAncestors(
            Collection<Edge<ByteCodeInstruction>> edges, PostDominatorTree pdt) {
        return edges.stream().map(edge -> Pair.of(edge, findLeastCommonAncestors(edge, pdt))).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    public Set<ByteCodeInstruction> getControlDependencies(ByteCodeInstruction a, boolean strict){
        Set<ByteCodeInstruction> controlDependencies = strict ? new HashSet<>() :
                new HashSet<>(Collections.singleton(a));
        Set<ByteCodeInstruction> predecessors = getPredecessors(a).stream().filter(e -> !e.equals(a)).collect(Collectors.toSet());
        while(!predecessors.isEmpty()){
            controlDependencies.addAll(predecessors);
            predecessors =
                    predecessors.stream().map(this::getPredecessors).flatMap(Collection::stream)
                            .filter(p-> !controlDependencies.contains(p) && !p.equals(a)).collect(Collectors.toSet());

        }
        return controlDependencies;
    }

    public Set<ByteCodeInstruction> getImmediateControlDependencies(ByteCodeInstruction a, boolean strict){
        return getPredecessors(a).stream().filter(p -> !strict || !p.equals(a)).collect(Collectors.toSet());
    }

    /**
     * Whether {@param b} is control dependent on {@param a}.
     * b is control dependent on a if:
     * - There exists a direct path from a to b in the control flow graph
     *   where all nodes are postdominated by b.
     * - a is not post-dominated by b.
     *
     * @param b is b in the definition of control dependence
     * @param a is a in the definition of control dependence
     * @return Whether {@param b} is control dependent on {@param a}
     */
    boolean isControlDependent(ByteCodeInstruction a, ByteCodeInstruction b){
        return this.dijkstra(b,a).isPresent();
    }
}
