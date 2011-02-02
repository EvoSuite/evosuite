package de.unisb.cs.st.evosuite.coverage.lcsaj;

import java.util.HashMap;

import org.objectweb.asm.tree.AbstractInsnNode;

public class LCSAJ {
	
	private HashMap<Integer,AbstractInsnNode> instructions;
	
	private int id;
	
	private String className;
	private String methodName;
	
	private static int lcsaj_counter = 0;
	
	public LCSAJ(String className,String methodName){
		this.id = lcsaj_counter++;
		this.className = className;
		this.methodName = methodName;
		this.instructions = new HashMap<Integer,AbstractInsnNode>();
	}
	
	public int getID(){
		return this.id;
	}
	
	public HashMap<Integer,AbstractInsnNode> getInstructions(){
		return this.instructions;
	}
	
	public boolean containssInstruction(int id){
		return instructions.containsKey(id);
	}
	
	public AbstractInsnNode getInstruction(int id){
		return instructions.get(id);
	}
	
	public void addInstruction(int id, AbstractInsnNode node){
		instructions.put(id, node);
	}
	
	public void removeInstruction(int id){
		instructions.remove(id);
	}
	
	public String getClassName(){
		return this.className;
	}
	
	public String getMethodName(){
		return this.methodName;
	}
}
