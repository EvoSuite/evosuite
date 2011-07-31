package de.unisb.cs.st.evosuite.cfg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.kohsuke.graphviz.Edge;
import org.kohsuke.graphviz.Graph;
import org.kohsuke.graphviz.Node;

import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJ;

public class LCSAJGraph {
	
	private RawControlFlowGraph graph;
	
	private LCSAJ lcsaj;
	
	private boolean fitnessGraph;
	
	public LCSAJGraph(LCSAJ lcsaj, boolean fitnessGraph) {
		graph = CFGPool.getRawCFG(lcsaj.getClassName(), lcsaj.getMethodName());
		this.lcsaj = lcsaj;
		this.fitnessGraph = fitnessGraph;
	}
	
	public void generate(File file){
		if (fitnessGraph)
			System.out.println("Generating Graph for uncoverd LCSAJ No: "+lcsaj.getID()+ " in " +lcsaj.getClassName() +"/"+lcsaj.getMethodName());
		else
			System.out.println("Generating Graph for LCSAJ No: "+lcsaj.getID()+ " in " +lcsaj.getClassName() +"/"+lcsaj.getMethodName());
		
		Graph lcsaj_graph = new Graph();
		ArrayList<Node> allNodes = new ArrayList<Node>();
		
		
		for (BytecodeInstruction b : graph.vertexSet()){
			Node n = new Node().attr("label", b.toString());
			lcsaj_graph = lcsaj_graph.node(n);
			allNodes.add(n);
		}
		
		for (ControlFlowEdge edge : graph.edgeSet())
			for (Node source : allNodes)
				for (Node target : allNodes){
					BytecodeInstruction b1 = graph.getEdgeSource(edge);
					BytecodeInstruction b2 = graph.getEdgeTarget(edge);
					
						if (source.attr("label").equals(b1.toString()) && target.attr("label").equals(b2.toString())){
							if (b1.isBranch()){
								Edge newEdge = new Edge(source,target).attr("label",edge.toString());
								lcsaj_graph.edge(newEdge);
							}
							else
								lcsaj_graph.edge(source, target);						
						}	
				}
			
			BytecodeInstruction l1 = lcsaj.getStartBranch().getInstruction();
			BytecodeInstruction l2 = lcsaj.getLastBranch().getInstruction();
			if (fitnessGraph)
				l2 = lcsaj.getBranch(lcsaj.getdPositionReached()).getInstruction();
			
			for (Node source : allNodes)
				for (Node target : allNodes){	
					if (source.attr("label").equals(l1.toString()) && target.attr("label").equals(l2.toString())){
						if (!fitnessGraph || lcsaj.getdPositionReached() == lcsaj.length()-1){
							Edge newEdge = new Edge(source,target).attr("color", "green").attr("label", "LCSAJ No." + lcsaj.getID());
							lcsaj_graph.edge(newEdge);
						}
						else {
							Edge newEdge = new Edge(source,target).attr("color", "red").attr("label", " LCSAJ No." + lcsaj.getID() +". Covered till:");
							lcsaj_graph.edge(newEdge);
						}
							
					}		
				}
		
		ArrayList<String> commandos = new ArrayList<String>();
		commandos.add("dot");
		try {
			lcsaj_graph.generateTo(commandos, file);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
