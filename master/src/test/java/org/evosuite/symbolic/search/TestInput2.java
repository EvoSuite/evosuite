package org.evosuite.symbolic.search;

public class TestInput2 {

	/**
	 * 
	 * @param int0==5
	 * @param int1==16
	 * @param int2==16
	 * @param int3==22
	 * @param int4==22
	 */
	public static void test(int int0, int int1, int int2, int int3, int int4) {
	      int[] intArray0 = new int[int0];
	      intArray0[0] = int1;
	      intArray0[1] = int2;
	      intArray0[2] = int3;
	      tracepoint1(intArray0, int4);
	}


	private static void tracepoint1(int[] A, int p) {
		int n = 0;
		int ic = 0;

		loop_invariant(ic, A, p, n);

		while (A[ic] != 0) {
			
	         loop_invariant(ic, A, p, n);

			if (p == 0 || A[ic] == p) {
				n++;
			}
			ic++;
			
	         loop_invariant(ic, A, p, n);

		}
		
        loop_invariant(ic, A, p, n);

	}


	private static void loop_invariant(int ic, int[] A, int p, int n) {
	      try
	      {
	         if ( !(ic >= 0) )
	         {
	            inv_violation();
	         }
	      }
	      catch (ThreadDeath t_instrument)
	      {
	         throw t_instrument;
	      }
	      catch (Throwable t_instrument)
	      {
	      }
	      try
	      {
	         if ( !(A != null) )
	         {
		            inv_violation();
	         }
	      }
	      catch (ThreadDeath t_instrument)
	      {
	         throw t_instrument;
	      }
	      catch (Throwable t_instrument)
	      {
	      }
	      try
	      {
	         if ( !(n >= 0) )
	         {
		            inv_violation();
	         }
	      }
	      catch (ThreadDeath t_instrument)
	      {
	         throw t_instrument;
	      }
	      catch (Throwable t_instrument)
	      {
	      }
	      try
	      {
	         if ( !(ic >= n) )
	         {
		            inv_violation();
	         }
	      }
	      catch (ThreadDeath t_instrument)
	      {
	         throw t_instrument;
	      }
	      catch (Throwable t_instrument)
	      {
	      }
	      try
	      {
	         if ( !(ic <= daikon_Quant_size(A) - 1) )
	         {
		            inv_violation();
	         }
	      }
	      catch (ThreadDeath t_instrument)
	      {
	         throw t_instrument;
	      }
	      catch (Throwable t_instrument)
	      {
	      }
	      try
	      {
	         if ( !(p != daikon_Quant_size(A)) )
	         {
		            inv_violation();
	         }
	      }
	      catch (ThreadDeath t_instrument)
	      {
	         throw t_instrument;
	      }
	      catch (Throwable t_instrument)
	      {
	      }
	      try
	      {
	         if ( !(p != daikon_Quant_size(A) - 1) )
	         {
		            inv_violation();
	         }
	      }
	      catch (ThreadDeath t_instrument)
	      {
	         throw t_instrument;
	      }
	      catch (Throwable t_instrument)
	      {
	      }
	      try
	      {
	         if ( !(n <= daikon_Quant_size(A) - 1) )
	         {
		            inv_violation();
	         }
	      }
	      catch (ThreadDeath t_instrument)
	      {
	         throw t_instrument;
	      }
	      catch (Throwable t_instrument)
	      {
	      }
	      try
	      {
	         if ( !(daikon_Quant_size(A) != daikon_Quant_getElement_int(A, ic)) )
	         {
		            inv_violation();
	         }
	      }
	      catch (ThreadDeath t_instrument)
	      {
	         throw t_instrument;
	      }
	      catch (Throwable t_instrument)
	      {
	      }
	      try
	      {
	         if ( !(daikon_Quant_size(A) != daikon_Quant_getElement_int(A, n)) )
	         {
		            inv_violation();
	         }
	      }
	      catch (ThreadDeath t_instrument)
	      {
	         throw t_instrument;
	      }
	      catch (Throwable t_instrument)
	      {
	      }

	}


	private static int daikon_Quant_getElement_int(int[] a, int ic) {
		return a[ic];
	}


	private static int daikon_Quant_size(int[] a) {
		return a.length;
	}


	private static void inv_violation() {
		// TODO Auto-generated method stub
		
	}

}
