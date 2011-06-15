package de.unisb.cs.st.evosuite.testcase;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;

public class TimeoutTest {
	protected class FakeTestCase implements Callable<Integer> {

		@Override
		public Integer call() throws Exception {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				RESULT = 1;
				return 1;
			}

			while (true) {
				if (Thread.currentThread().isInterrupted()) {
					break;
				} else {
					Thread.yield();
				}
			}

			RESULT = 2;
			return 2;
		}
	}

	protected static int RESULT = -1;

	@Test
	public void testCPUTimeout() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Callable<Integer> call = new FakeTestCase();

		TimeoutHandler<Integer> handler = new TimeoutHandler<Integer>();

		RESULT = -1;

		try {
			handler.execute(call, executor, 2000, true);
		} catch (TimeoutException e) {
			executor.shutdownNow();
			try {
				boolean terminated = executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
				Assert.assertTrue(terminated);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			Assert.assertEquals(2, RESULT);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testNormalTimeout() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Callable<Integer> call = new FakeTestCase();

		TimeoutHandler<Integer> handler = new TimeoutHandler<Integer>();

		RESULT = -1;

		try {
			handler.execute(call, executor, 2000, false);
		} catch (TimeoutException e) {
			executor.shutdownNow();
			try {
				boolean terminated = executor.awaitTermination(2000, TimeUnit.MILLISECONDS);
				Assert.assertTrue(terminated);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			Assert.assertEquals(1, RESULT);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
