package org.evosuite.runtime.vfs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A virtual folder
 * 
 * @author arcuri
 *
 */
public class VFolder extends FSObject{

	private final List<FSObject> children;
		
	public VFolder(String path, VFolder parent) {
		super(path, parent);
		
		children = new CopyOnWriteArrayList<FSObject>();
	}
	
	@Override
	public boolean delete(){
		if(children.size()>0){
			return false;
		}
		
		return super.delete();
	}
	
	public boolean isRoot(){
		return parent == null && path==null;
	}
	
	public void addChild(FSObject child){
		children.add(child);
	}
	
	public boolean removeChild(String name) throws IllegalArgumentException{
		
		if(name==null || name.isEmpty()){
			throw new IllegalArgumentException("Empty name");
		}
		
		for(FSObject element : children){
			if(name.equals(element.getName())){
				return children.remove(element);				 
			}
		}
		
		return false;
	}
	
	public boolean hasChild(String name){
		return getChild(name) != null; 
	}
	
	public String[] getChildrenNames(){
		List<String> list = new ArrayList<String>(children.size());
		Iterator<FSObject> iter = children.iterator();
		while(iter.hasNext()){
			list.add(iter.next().getName());
		}		
		return list.toArray(new String[0]);
	}
	
	public FSObject getChild(String name){
		if(name==null || name.isEmpty()){
			throw new IllegalArgumentException("Empty name");
		}
		
		Iterator<FSObject> iter = children.iterator();
		while(iter.hasNext()){
			FSObject current = iter.next();
			if(name.equals(current.getName())){
				return current;		
			}
		}
		
		return null;
	}
}
