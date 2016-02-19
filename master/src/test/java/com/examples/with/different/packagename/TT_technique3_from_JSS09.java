/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package com.examples.with.different.packagename;

public class TT_technique3_from_JSS09 {
	
	public boolean simple(int x){
		if(x==123456){
			return true;
		} else {
			return false;
		}
	}
	
	public boolean complex(int x, int y, int z){
		
		if(x > 0){
			return false;
		} else {
			if(y > -100){
				return false;
			} else if (z > -10000) {
				return false;
			} else {
				return true; 
			}
		}
	}

}

/*
 * From JSS09 paper:
 * 
 * The local instrumentation is intended to assign gradual distance
 * values to the flag variables. Therefore, each right-hand operator of
 * a flag assignment will be instrumented meaning that the actual
right-hand expression is replaced by a call to a distance function.
Table 4 shows the application of this tactic. As can be seen, the
right-hand sides of the flag assignments have been replaced by
calls to functions dist or map which returns the distance for the
expression passed to it (except for the initialization when declaring
the flag). The original right-hand value becomes the first argument
of the dist function. The angle brackets used for the second argument
passed to the dist function should mean that the formal
expression as well as the actual values of the concerned variables
are passed. This short-hand notation will be used throughout the
remainder of this paper. The call of func2 has been replaced by a
call to function map (which will be explained below).
Listing 1 shows the pseudo-code of the dist function. Initially,
the conventional branch distance will be calculated based on the
passed expression. This calculation depends on the applied relational
operator; for each operator, a particular distance function
is designed (Wegener et al., 2001). Then, the distance is mapped
to a particular range using the map function. This function realizes
the idea of interval bisection which can be regarded as the inversion
of the approximation level approach. Interval bisection allows
integrating multiple information into one real value. In our case
this information consists of the actual branch distance of the condition
that controls the flag assignment and the nesting level. The
nesting level of a statement corresponds to the number of conditions
that control this statement. We use the nesting level instead
of the approximation level since the latter cannot be calculated for
the local fitness function unambiguously using static analysis. This
is due to the dynamic function binding as mentioned in Section 3.3.
The number of nesting levels may differ from function call to function
call depending on how many levels the called function
possesses.

Pseudo-code of the dist function.:

TODO

Formula (1) shows the relationship between the original
distance ðdorigÞ and the resulting mapped distance ðdmappedÞ
where l is the nesting level. The map function implements this
formula.

TODO

We will explain the idea of interval bisection using an example. Test
data for testing func1 consists of a pair (a,b) of integer input data.
Table 5 shows the nesting level, the branch distance, the flag value,
and a graphical representation of the absolute amount of the flag
value (called interval) that the test inputs (1, 1), (1, 0), (0, 1), and
(0,0) would achieve when being used as inputs for func1. The test
inputs (1,1) and (1, 0) do not satisfy the first condition of func1,
hence leading to the traversal of the alternative branch and achieving
nesting level 1. The branch distance 0.0005, calculated by the
appropriate distance function (Wegener et al., 2001), indicates
how close execution was to evaluating the first condition to true.
The flag value is negative in these cases indicating a false flag outcome.
The corresponding intervals in Table 5 show a solid lower
half which can be regarded as a reserved area for higher nesting levels.
The branch distance of 0.0005 was mapped to the upper half,
resulting in the absolute flag value 0.5002. The test input (0, 1) satisfies
the first condition of function func1 and leads to the traversal
of the alternative branch of the condition in function func2, hence
achieving a nesting level of 2. The miss of the true branch of this
condition is taken into account by the branch distance of 0.0005.
As the interval shows in this case, the lower part is bisected as compared
to the first two intervals, and the branch distance is mapped
into the upper one of these new halves. Finally, test input (0,0) satisfies
both conditions and leads to an assignment of true to the flag
variable. Therefore, the sign of the flag value is positive, indicating
the true value. The absolute value of the flag indicates how close
execution was to avoiding the true outcome. Note that in the cases
of (0,x) the map function is applied twice to the distance values,
hence leading to the effective flag values shown in Table 5 (e.g.

TODO

*/
