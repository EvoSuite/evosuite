package de.unisb.cs.st.evosuite.symbolic.expr;

public class RealComparison extends RealExpression {
	private static final long serialVersionUID = 1L;

	public RealComparison(Expression<Double> left, Expression<Double> right,
			Long con) {
		super();
		this.left = left;
		this.right = right;
		this.con = con;
	}

	private Long con;
	private Expression<Double> left;
	private Expression<Double> right;
	
	@Override
	public Long getConcreteValue() {
		return con;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==this)
		{
			return true;
		}
		if(obj instanceof RealComparison)
		{
			RealComparison other=(RealComparison) obj;
			return  this.con.equals(other.con) 
//					&& this.getSize()==other.getSize() 
					&& this.left.equals(other.left) && this.right.equals(other.right);
		}

		return false;
	}

	public Expression<Double> getRightOperant() {
		return right;
	}

	public Expression<Double> getLeftOperant() {
		return left;
	}
	
	@Override
	public String toString() {
		return "("+left+" cmp "+right+")";
	}
	
//	protected int size=0;
//	@Override
//	public int getSize() {
//		if(size==0)
//		{
//			size=1+getLeftOperant().getSize()+getRightOperant().getSize();
//		}
//		return size;
//	}

	@Override
	public Double execute() {
		// this is never used
		return null;
	}
}
