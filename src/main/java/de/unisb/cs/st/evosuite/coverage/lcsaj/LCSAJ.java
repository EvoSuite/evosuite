package de.unisb.cs.st.evosuite.coverage.lcsaj;

import java.util.HashMap;

import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;

public class LCSAJ {
	
	private HashMap<Integer,AbstractInsnNode> instructions;
	private HashMap<AbstractInsnNode,Integer> instructions_reverse;
	
	private AbstractInsnNode lastNodeAccessed;
	private AbstractInsnNode lastJump;
	
	private int id;
	
	private String className;
	private String methodName;
	
	private static int lcsaj_counter = 0;
	
	public LCSAJ(String className,String methodName){
		this.id = lcsaj_counter++;
		this.className = className;
		this.methodName = methodName;
		this.instructions = new HashMap<Integer,AbstractInsnNode>();
		this.instructions_reverse = new HashMap<AbstractInsnNode,Integer>();
	}
	
	//Copy constructor
	public LCSAJ(String className,String methodName, LCSAJ l){
		this.id = lcsaj_counter++;
		this.className = l.getClassName();
		this.methodName = l.getMethodName();
		this.instructions = l.getInstructions();
		this.lastNodeAccessed = l.getLastNodeAccessed();
		this.instructions_reverse = l.getInstructionsReverse();
	}
	
	public int getID(){
		return this.id;
	}
	
	public HashMap<Integer,AbstractInsnNode> getInstructions(){
		return this.instructions;
	}
	
	protected HashMap<AbstractInsnNode,Integer> getInstructionsReverse(){
		return instructions_reverse;
	}
	
	public AbstractInsnNode getInstruction(int id){
		return instructions.get(id);
	}
	
	public int getInstructionID(AbstractInsnNode node){
		if (instructions_reverse.containsKey(node)){
			return instructions_reverse.get(node);
		}
		else
			return -1;
	}
	
	public AbstractInsnNode getLastNodeAccessed(){
		return lastNodeAccessed;
	}
	
	public AbstractInsnNode getLastJump(){
		return lastJump;
	}
	
	public String getClassName(){
		return this.className;
	}
	
	public String getMethodName(){
		return this.methodName;
	}
	
	public boolean containssInstruction(int id){
		return instructions.containsKey(id);
	}
	
	public void addInstruction(int id, AbstractInsnNode node, boolean LCSAJStart){
		lastNodeAccessed = node;
		if (node instanceof JumpInsnNode || LCSAJStart)
			instructions.put(id, node);
	}
	
	public void removeInstruction(int id){
		instructions.remove(id);
	}

}
