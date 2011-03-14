package de.unisb.cs.st.evosuite.testcase;

import java.lang.management.*;
import java.util.concurrent.*;

import org.jfree.util.Log;

public class TimeoutHandler<T> 
{
	protected FutureTask<T> task = null;
	
	public FutureTask<T> getLastTask()
	{
		return task;
	}
	
	public T execute(final Callable testcase, ExecutorService executor, long timeout, boolean timeout_based_on_cpu) throws TimeoutException, InterruptedException, ExecutionException
	{
		ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
		if(!bean.isCurrentThreadCpuTimeSupported()  && timeout_based_on_cpu)
		{
			timeout_based_on_cpu = false;
			Log.warn("Requested to use timeout_based_on_cpu, but it is not supported by the JVM/OS");
		}
			
		
		if(!timeout_based_on_cpu)
		{
			task = new FutureTask<T>(testcase);
			executor.execute(task);		
			T result = task.get(timeout, TimeUnit.MILLISECONDS);
			return result;
		}
		else
		{
			long[] other_thread_ids = bean.getAllThreadIds();
			
			task = new FutureTask<T>(testcase);
			executor.execute(task);		
			T result = null;
			
			long waiting_time = timeout;
			
			while(waiting_time > 0)
			{
				try
				{
					result = task.get(waiting_time, TimeUnit.MILLISECONDS);
				}
				catch(TimeoutException e)
				{
					//executor is still running. need to check CPU usage.
					//NOTE: this is rather tricky, because ONLY the threads that are still alive are returned.
					//if a test case generates a lot of threads and those die, then their CPU usage would not be counted
					long[] all_thread_ids = bean.getAllThreadIds();
					long cpu_usage = 0;
					
					outer: for(long id : all_thread_ids)
					{
						for(int i=0; i<other_thread_ids.length; i++)
							if(id == other_thread_ids[i])
								continue outer;
						
						//id is "new"
						
						long ns = bean.getThreadCpuTime(id);
						long ms = (long) (ns / 1000000);
						
						cpu_usage += ms;
					}
					
					final double alpha = 0.9; //used to avoid possible problems with time measurement
					if(cpu_usage < alpha * timeout)
					{
						//CPU has not been used enough
						waiting_time = timeout - cpu_usage;
					}
					else
					{
						//effectively it is a timeout
						throw e;
					}
				}
			}
			return result;

		}
	}
}

