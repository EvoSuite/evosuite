/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.concurrency;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;


/**
 * @author Sebastian Steenbuck
 *
 */
public class ConcurrencyTracer {
	private static Logger logger = Logger.getLogger(ConcurrencyTracer.class);
	
	private List<SchedulingDecisionTuple> seen;

	public ConcurrencyTracer(){
		seen=new LinkedList<SchedulingDecisionTuple>();
	}
	
	//#TODO global naming for scheduleID (the IDs written to the source code at each schedule point). This is named requestID in other places
	public synchronized void passedScheduleID(int threadID, int scheduleID){
		seen.add(new SchedulingDecisionTuple(threadID, scheduleID));
	}

	public int getDistance(List<SchedulingDecisionTuple> target){
		for(SchedulingDecisionTuple t : target){
			if(!seen.contains(t))throw new AssertionError();

		}

		List<SchedulingDecisionTuple> seen2 = new ArrayList<SchedulingDecisionTuple>();
		for(SchedulingDecisionTuple s : seen){
			if(target.contains(s)){
				seen2.add(s);
			}
		}

		return computeLevenshteinDistance(seen2, target);

	}
	
	private boolean noNull(){
		for(SchedulingDecisionTuple t : seen){
			try{
			assert(t!=null);
			}catch(Throwable e){
				logger.fatal("oh nooo", e);
				System.exit(1);
			}
		}
		return true;
	}
	
	public List<SchedulingDecisionTuple> getTrace(){
		assert(seen!=null);
		assert(noNull());
		return seen;
	}


	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	public static int computeDistance(List<SchedulingDecisionTuple> goal, List<SchedulingDecisionTuple> run){
		return test(goal, run);
	}
	

	private static final int defDistance = 200;
	
	private static int test(List<SchedulingDecisionTuple> goal, List<SchedulingDecisionTuple> run){
		if(goal.size()>run.size()){
			return defDistance;
		}
		
		if(goal.size()==1){
			if(run.contains(goal.get(0))){
				return 0;
			}else{
				return defDistance;
			}
		}
		
		class searchState{
			public Integer pos=1;
			public Integer cost=0;
			boolean finished=false;
		}
		
		Set<searchState> states = new HashSet<searchState>();
		for(SchedulingDecisionTuple t : run){
			for(searchState s : states){
				//assert(goal.size()>s.pos) : "goal size is only " + goal.size() + " but we will request pos " + s.pos;
				if(!s.finished && goal.get(s.pos).equals(t)){
					s.pos++;
					if(s.pos==goal.size()){
						s.finished=true;
						if(s.cost==0)
							return 0;
					}
				}else if(!s.finished){
					s.cost++;
				}
			}
				
			if(goal.get(0).equals(t)){
				states.add(new searchState());
			}
			
			Map<Integer, searchState> posToMinCost = new HashMap<Integer, searchState>();
			Set<searchState> toDrop = new HashSet<searchState>();
			for(searchState s : states){
				if(posToMinCost.containsKey(s.pos)){
					if(posToMinCost.get(s.pos).cost>s.cost){
						toDrop.add(posToMinCost.get(s.pos));
						posToMinCost.put(s.pos, s);
					}else{
						toDrop.add(s);
					}
				}
			}
			states.removeAll(toDrop);
		}
		
		searchState min = new searchState();
		min.pos=0;
		min.cost=Integer.MAX_VALUE;
		for(searchState s : states){
			if(min.pos<s.pos){
				min = s;
			}else if(min.pos==s.pos && min.cost>s.cost){
				min = s;
			}
		}
		
		int distance;
		assert(min.pos<=goal.size());
		if(min.pos==goal.size()){
			distance= min.cost;
		}else{
			distance=defDistance;
		}
		
		
		//System.out.println("XXXXXXXXXXXXXXXXXXXXXXx");
		//System.out.println("compare: " + distance);
		//System.out.println("goal: " + ConcurrencySuitCoverage.printList(goal));
		//System.out.println("reality:" + ConcurrencySuitCoverage.printList(run));
		
		return distance;
	}
	
	private static int computeLevenshteinDistance(List<SchedulingDecisionTuple> str1,
			List<SchedulingDecisionTuple> str2) {
		int[][] distance = new int[str1.size() + 1][str2.size() + 1];

		for (int i = 0; i <= str1.size(); i++)
			distance[i][0] = i;
		for (int j = 0; j <= str2.size(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= str1.size(); i++)
			for (int j = 1; j <= str2.size(); j++){


				if(!str1.get(i - 1).equals(str2.get(j - 1))){
					int cost = (str1.get(i - 1).equals(str2.get(j - 1))) ? 0
							: 1;

					distance[i][j] = minimum(
							distance[i - 1][j] + 1, //delete
							distance[i][j - 1] + 1, //insert
							distance[i - 1][j - 1]
							                + cost); //substitute
					if(i>1 && j<str2.size() && i<str1.size() &&
							j>1 && 
							str1.get(i).equals(str2.get(j-1)) && 
							str1.get(i-1).equals(str2.get(j))){
						//System.out.println("transpose " + distance[i][j] + " " + (distance[i-2][j-2]+cost));
						distance[i][j] = Math.min(distance[i][j], (distance[i-2][j-2])); //transpose
					}
				}else{
					distance[i][j]=distance[i - 1][j - 1];
				}

			}

		System.out.println("XXXXXXXXXXXXXXXXXXXXXXx");
		System.out.println("compare: " + distance[str1.size()][str2.size()]);
		System.out.println("goal: " + ConcurrencySuitCoverage.printList(str2));
		System.out.println("reality:" + ConcurrencySuitCoverage.printList(str1));
		return distance[str1.size()][str2.size()];
	}


	//#TODO inserts should cost someting
	public static int getLevenshteinDistance (List<SchedulingDecisionTuple> s, List<SchedulingDecisionTuple> t) {
		if (s == null || t == null) {
			throw new IllegalArgumentException("Strings must not be null");
		}	

		int n = s.size(); // length of s
		int m = t.size(); // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		int p[] = new int[n+1]; //'previous' cost array, horizontally
		int d[] = new int[n+1]; // cost array, horizontally
		int _d[]; //placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		SchedulingDecisionTuple t_j; // jth character of t

		int cost; // cost

		for (i = 0; i<=n; i++) {
			p[i] = i;
		}

		for (j = 1; j<=m; j++) {
			t_j = t.get(j-1);
			d[0] = j;

			for (i=1; i<=n; i++) {
				cost = s.get(i-1).equals(t_j) ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left and up +cost				
				d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);  
			}

			// copy current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		} 

		// our last action in the above loop was to switch d and p, so p now 
		// actually has the most recent cost counts
		return p[n];
	}

}
