package org.evosuite.continuous.job.schedule;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.continuous.job.JobDefinition;
import org.evosuite.continuous.job.JobScheduler;
import org.evosuite.continuous.project.ProjectGraph;

/**
 * Choose a precise order in which the CUTs will be targeted.
 * Test cases for a CUT can be used for seeding in the search
 * of the following CUTs in the schedule
 * 
 * @author arcuri
 *
 */
public class SeedingSchedule extends OneTimeSchedule{

	protected final OneTimeSchedule base;
	
	public SeedingSchedule(JobScheduler scheduler) {
		this(scheduler, new SimpleSchedule(scheduler));
	}
	
	protected SeedingSchedule(JobScheduler scheduler, OneTimeSchedule base) {
		super(scheduler);
		this.base = base;
	}
	

	@Override
	protected List<JobDefinition> createScheduleOnce() {
		List<JobDefinition> jobs = base.createScheduleOnce();
		return addDependenciesForSeeding(jobs);
	}
	
	@Override
	protected List<JobDefinition> createScheduleForWhenNotEnoughBudget(){
		/*
		 * even if we do not have enough budget to target all CUTs, we
		 * still want to use seeding.
		 */
		List<JobDefinition> jobs = super.createScheduleForWhenNotEnoughBudget(); 
		return addDependenciesForSeeding(jobs);
	}
	
	/**
	 * For each input job, identify all the others jobs we want to generate
	 * test cases first.
	 * The scheduler will use this information to first try to generate
	 * the test cases for the "dependency" jobs.
	 * 
	 * <p>
	 * There can be different strategies to define a dependency:
	 * - ancestor, non-interface classes
	 * - classes used as input objects
	 * - subtypes of classes used as input objects
	 * 
	 * @param jobs
	 * @return the given input list, but with new jobs objects
	 */
	protected List<JobDefinition> addDependenciesForSeeding(List<JobDefinition> jobs){

		for(int i=0; i<jobs.size(); i++){
			JobDefinition job = jobs.get(i);
						
			Set<String> inputs = calculateInputClasses(job);
			Set<String> parents = calculateAncestors(job);
			
			jobs.set(i, job.getByAddingDependencies(inputs,parents));
		}
		
		return jobs; 
	}

	/**
	 * If CUT A takes as input an object B, then to cover A we might need B
	 * set in a specific way. Using the test cases generated for B can give
	 * us a pool of interesting instances of B. 
	 * 
	 * @param job
	 * @param dep
	 */
	private Set<String>  calculateInputClasses(JobDefinition job) {
		
		Set<String> dep = new LinkedHashSet<String>();
		
		ProjectGraph graph = scheduler.getProjectData().getProjectGraph();
		for(String input : graph.getCUTsDirectlyUsedAsInput(job.cut, true)){
			if(graph.isInterface(input)){
				continue;
			}
			dep.add(input);
		}	
		
		return dep;
	}

	/**
	 * The motivation for adding ancestors is as follows:
	 * consider CUT A extends B. If B is not an interface,
	 * then likely it will have an internal state.
	 * Test cases for B might bring it to some interesting/hard
	 * to reach configurations. 
	 * If the methods in A rely on those states in the upper class,
	 * then such test cases from B "might" be helpful.
	 * But, to do so, the seeded test cases need to change the concrete class.
	 * For example, if we have:
	 * 
	 *  <p> <code>B foo = new B(); </br>
	 *  foo.doSomething(x,y); </code>
	 * 
	 *  <p> then we should transform it into:
	 *  
	 *  <p> <code>B foo = new A(); </br>
	 *  foo.doSomething(x,y); </code>
	 *  
	 *  <p> There is a potentially tricky case.
	 *  Consider for example if B is abstract and, 
	 *  when test cases were generated for it, a subclass
	 *  C was chosen instead of A. This would mean the test
	 *  case for B would look like:
	 *  
	 *  <p> <code>B foo = new C(); </br>
	 *  foo.doSomething(x,y); </code>
	 *  
	 *  <p> However, it is actually not a big deal, as it is safe
	 *  to modify <code>new C()</code> with <code>new A()</code>.
	 * 
	 * <p> Note: if the test cases for B are in the 
	 * form <code>C foo = new C();</code>, then that would rather
	 * be a bug/problem in how EvoSuite generated test cases for B
	 * 
	 * @param job
	 */
	private Set<String> calculateAncestors(JobDefinition job) {

		Set<String> dep = new LinkedHashSet<String>();
		
		ProjectGraph graph = scheduler.getProjectData().getProjectGraph();
		
		if(graph.isInterface(job.cut)){
			/*
			 * even if an interface has code, it will have no class state (ie fields).
			 * so, no point in looking at its ancestors 
			 */
			return dep;
		}
		
		for(String parent :  graph.getAllCUTsParents(job.cut)){
			if(graph.isInterface(parent)){
				continue;
			}
			dep.add(parent);
		}
		return dep;
	}
	
}
