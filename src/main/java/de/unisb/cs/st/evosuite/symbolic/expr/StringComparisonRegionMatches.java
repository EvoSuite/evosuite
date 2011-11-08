/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.expr;

/**
 * @author krusev
 *
 */
public class StringComparisonRegionMatches extends StringComparison {

	private static final long serialVersionUID = -3643819326898534052L;

	private final Expression<Long> offset1;
	private final Expression<Long> offset2;
	private final Expression<Long> length;
	private final Expression<Long> ignore_case;
	
	public StringComparisonRegionMatches(
			Expression<String> left, Operator op, Expression<String> right, 
			Expression<Long> _offset1, 
			Expression<Long> _offset2, 
			Expression<Long> _length, 
			Expression<Long> _ignore_case, 
			Long con) {
		super(left, op, right, con);
		
		this.offset1 = _offset1;
		this.offset2 = _offset2;
		this.length = _length;
		this.ignore_case = _ignore_case;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof StringComparisonRegionMatches) {
			StringComparisonRegionMatches other = (StringComparisonRegionMatches) obj;
			return this.op.equals(other.op) && this.con.equals(other.con) 
					&& this.left.equals(other.left) 
					&& this.right.equals(other.right)
					&& this.ignore_case.equals(other.ignore_case)
					&& this.offset1.equals(other.offset1)
					&& this.offset2.equals(other.offset2)
					&& this.length.equals(other.length);
		}

		return false;
	}

	@Override
	public String toString() {
		return "(" + left + op.toString() + ignore_case + " " + offset1 + " " + right + " " + offset2 + " " + length  + ")";
	}
	
	
	/**
	 * @return the offset1
	 */
	public Expression<Long> getOffset1() {
		return offset1;
	}

	/**
	 * @return the offset2
	 */
	public Expression<Long> getOffset2() {
		return offset2;
	}

	/**
	 * @return the length
	 */
	public Expression<Long> getLength() {
		return length;
	}

	/**
	 * @return the ignore_case
	 */
	public Expression<Long> isIgnore_case() {
		return ignore_case;
	}
	
}