package de.unisb.cs.st.evosuite.testcase;

public class ArrayReference extends VariableReferenceImpl{
	protected int array_length;
	public ArrayReference(TestCase tc, GenericClass clazz, int array_length){
		super(tc, clazz);
		assert(array_length>=0);
		this.array_length=array_length;
	}
	
	public int getArrayLength(){
		return array_length;
	}

	public void setArrayLength(int l){
		assert(l>=0);
		array_length=l;
	}
}
