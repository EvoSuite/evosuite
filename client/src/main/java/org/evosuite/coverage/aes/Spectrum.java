	package org.evosuite.coverage.aes;


	import java.util.ArrayList;
	import java.util.BitSet;
	import java.util.HashSet;
	import java.util.LinkedHashMap;
	import java.util.Set;
	import java.io.*;

	//mycode starts
	class Aj{
        double vrd;
        double vcd;
        public Aj(double a, double b)
        {
            vrd = a;
            vcd = b;
        }
        public double getvrd()
        {
            return vrd;
        }
        public double getvcd()
        {
            return vcd;
        }
    }
    //mycode ends


	public class Spectrum {

		private ArrayList<BitSet> transactions;
		private int numComponents = 0;

		public static double[] buffer = new double[10];
		public static double[] mean_buffer = new double[10];
		public static int pointer_buffer = 0;
		public static int pointer_mean = 0;
		public static int flag = 0;
		public static int file_status = 0;
		public static int file_variable =0;

		public Spectrum() {
		}

		public Spectrum(int transactions, int components) {
			this.setSize(transactions, components);
		}

		public void setSize(int transactions, int components) {
			this.setNumComponents(components);
			this.setNumTransactions(transactions);
		}

		public void setNumComponents(int size) {
			this.numComponents = size;
		}

		public void setNumTransactions(int size) {
			if (transactions == null) {
				transactions = new ArrayList<BitSet>(size);
			}
			else {
				transactions.clear();
				transactions.ensureCapacity(size);
			}

			for (int i = 0; i < size; i++) {
				transactions.add(new BitSet(numComponents));
			}
		}

		public int getNumComponents() {
			return this.numComponents;
		}

		public int getNumTransactions() {
			if (transactions == null) {
				return 0;
			}
			return this.transactions.size();
		}

		public void setInvolved(int transaction, int component, boolean involvement) {
			if (transaction >= 0 && transaction < this.getNumTransactions() &&
				component   >= 0 && component   < this.getNumComponents()) {
				this.transactions.get(transaction).set(component, involvement);
			}
		}

		public void setInvolved(int transaction, int component) {
			this.setInvolved(transaction, component, true);
		}

		public boolean getInvolved(int transaction, int component) {
			if (transaction >= 0 && transaction < this.getNumTransactions() &&
				component   >= 0 && component   < this.getNumComponents()) {
				return this.transactions.get(transaction).get(component);
			}
			return false;
		}

		protected boolean isValidMatrix() {
			return this.getNumComponents() > 0 && this.getNumTransactions() > 0;

		}
		// New function for get rho vector

		public double getVrho() {
			//Vector v_each_transaction = new Vector();
		      //Vector v_ideal_transaction = new Vector();






		      System.out.println("******************* The program works **********************");
		      if (!this.isValidMatrix()) return 0d;

		      double activityCounter = 0d;
		      double sum_of_squares_transactions = 0d;
		      double sum_of_squares_components = 0d;
		      for (BitSet transaction : this.transactions) {
		        activityCounter += transaction.cardinality();
		        sum_of_squares_transactions += ((double)transaction.cardinality())*((double)transaction.cardinality());
		      }
		      double rho_transaction = 0d;
		      if(activityCounter==0)
		      		rho_transaction = 0;
		      else
		      		rho_transaction = activityCounter/( Math.sqrt( (double) this.getNumTransactions() ) * Math.sqrt((double)sum_of_squares_transactions) );


		      int components = this.getNumComponents();
		      int[] comp = new int[components];
		      for(int c=0; c < components; c++)
		        comp[c]=0;
		      //BitSet activations = new BitSet(components);
		      for (BitSet transaction : this.transactions) {
		        for (int c = 0; c < components; c++) {
		          comp[c] += transaction.get(c) ? 1 : 0;
		        }
		      }
		      for (int c = 0; c < components; c++) {
		        sum_of_squares_components += comp[c]*comp[c];
		      }
		      double rho_component =0d;
		      if(activityCounter ==0)
		      		rho_component =0;
		      else
		      		rho_component = activityCounter/( Math.sqrt( (double) this.getNumComponents() ) * Math.sqrt(sum_of_squares_components) );
		     return 0.5*(rho_transaction)*(rho_component);
		      //double rho = rho_transaction/2;
		      //return rho;

		}

		    //mycode starts
        public  Aj getVrho2() {
            //Vector v_each_transaction = new Vector();
            //Vector v_ideal_transaction = new Vector();






            System.out.println("******************* The program works **********************");
            if (!this.isValidMatrix()) return new Aj(0d, 0d);

            double activityCounter = 0d;
            double sum_of_squares_transactions = 0d;
            double sum_of_squares_components = 0d;
            for (BitSet transaction : this.transactions) {
                activityCounter += transaction.cardinality();
                sum_of_squares_transactions += ((double)transaction.cardinality())*((double)transaction.cardinality());
            }
            double rho_transaction = 0d;
            if(activityCounter==0)
                rho_transaction = 0;
            else
                rho_transaction = activityCounter/( Math.sqrt( (double) this.getNumTransactions() ) * Math.sqrt((double)sum_of_squares_transactions) );


            int components = this.getNumComponents();
            int[] comp = new int[components];
            for(int c=0; c < components; c++)
                comp[c]=0;
            //BitSet activations = new BitSet(components);
            for (BitSet transaction : this.transactions) {
                for (int c = 0; c < components; c++) {
                    comp[c] += transaction.get(c) ? 1 : 0;
                }
            }
            for (int c = 0; c < components; c++) {
                sum_of_squares_components += comp[c]*comp[c];
            }
            double rho_component =0d;
            if(activityCounter ==0)
                rho_component =0;
            else
                rho_component = activityCounter/( Math.sqrt( (double) this.getNumComponents() ) * Math.sqrt(sum_of_squares_components) );
            return new Aj(rho_transaction, rho_component);
            //double rho = rho_transaction/2;
            //return rho;

        }

    //mycode ends
		
		public double getVCrho() {
			//Vector v_each_transaction = new Vector();
		      //Vector v_ideal_transaction = new Vector();

		      System.out.println("******************* The program works **********************");
		      if (!this.isValidMatrix()) return 0d;

		      double activityCounter = 0d;
		      double sum_of_squares_transactions = 0d;
		      double sum_of_squares_components = 0d;
		      for (BitSet transaction : this.transactions) {
		        activityCounter += transaction.cardinality();
		        sum_of_squares_transactions += ((double)transaction.cardinality())*((double)transaction.cardinality());
		      }
		      double rho_transaction = 0d;
		      if(activityCounter==0)
		      		rho_transaction = 0;
		      else
		      		rho_transaction = activityCounter/( Math.sqrt( (double) this.getNumTransactions() ) * Math.sqrt((double)sum_of_squares_transactions) );


		      int components = this.getNumComponents();
		      int[] comp = new int[components];
		      for(int c=0; c < components; c++)
		        comp[c]=0;
		      //BitSet activations = new BitSet(components);
		      for (BitSet transaction : this.transactions) {
		        for (int c = 0; c < components; c++) {
		          comp[c] += transaction.get(c) ? 1 : 0;
		        }
		      }
		      for (int c = 0; c < components; c++) {
		        sum_of_squares_components += comp[c]*comp[c];
		      }
		      double rho_component =0d;
		      if(activityCounter ==0)
		      		rho_component =0;
		      else
		      		rho_component = activityCounter/( Math.sqrt( (double) this.getNumComponents() ) * Math.sqrt(sum_of_squares_components) );




			// File file = new File("/home/harsh/Values.txt");
	  //       FileWriter fr = null;
	  //       BufferedWriter br = null;
	  //       try{
		 //        if(!file.exists()){
		 //    	   file.createNewFile();
		 //    	}
	  //   	}
	  //    	catch (IOException e) {
	  //           e.printStackTrace();
	  //       }
	  //       try{
	  //           fr = new FileWriter(file,true);
	  //           br = new BufferedWriter(fr);
	  //           br.write("VCDDU"+"\n");
	  //           br.write("transaction(test cases) "+ this.getNumTransactions() + "\n" );
	  //           br.write("activityCounter "+ activityCounter + "\n");
			//     br.write("sum_of_squares_transactions " + sum_of_squares_transactions+"\n");
			//     br.write("rho_transaction " + rho_transaction+"\n");
			//     br.write("components "+components+"\n");
			//     br.write("sum_of_squares_components "+sum_of_squares_components +"\n");
			//     br.write("rho_component " + rho_component +"\n");
			//     br.write("\n\n\n");


	  //       } catch (IOException e) {
	  //           e.printStackTrace();
	  //       }finally{
	  //           try {
	  //               br.close();
	  //               fr.close();
	  //           } catch (IOException e) {
	  //               e.printStackTrace();
	  //           }
	  //       }



		     return 0.5*(rho_component);
		      //double rho = rho_transaction/2;
		      //return rho;

		}

			public double getVCMrho1() {
			//Vector v_each_transaction = new Vector();
		      //Vector v_ideal_transaction = new Vector();






		      System.out.println("******************* The program works **********************");
		      if (!this.isValidMatrix()) return 0d;

		      double activityCounter = 0d;
		      double sum_of_squares_transactions = 0d;
		      double sum_of_squares_components = 0d;
		      for (BitSet transaction : this.transactions) {
		        activityCounter += transaction.cardinality();
		        sum_of_squares_transactions += ((double)transaction.cardinality())*((double)transaction.cardinality());
		      }
		      double rho_transaction = 0d;
		      if(activityCounter==0)
		      		rho_transaction = 0;
		      else
		      		rho_transaction = activityCounter/( Math.sqrt( (double) this.getNumTransactions() ) * Math.sqrt((double)sum_of_squares_transactions) );


		      int components = this.getNumComponents();
		      int[] comp = new int[components];
		      for(int c=0; c < components; c++)
		        comp[c]=0;
		      //BitSet activations = new BitSet(components);
		      for (BitSet transaction : this.transactions) {
		        for (int c = 0; c < components; c++) {
		          comp[c] += transaction.get(c) ? 1 : 0;
		        }
		      }
		      for (int c = 0; c < components; c++) {
		        sum_of_squares_components += comp[c]*comp[c];
		      }
		      double rho_component =0d;
		      if(activityCounter ==0)
		      		rho_component =0;
		      else
		      		rho_component = activityCounter/( Math.sqrt( (double) this.getNumComponents() ) * Math.sqrt(sum_of_squares_components) );

		      double rho = activityCounter / ( ((double) this.getNumComponents()) * ((double) this.getNumTransactions()) );

		      double vmrho = 0.5*((rho_component) - Math.abs(0.5 - rho));


			// File file = new File("/home/harsh/Values.txt");
	  //       FileWriter fr = null;
	  //       BufferedWriter br = null;
	  //       try{
		 //        if(!file.exists()){
		 //    	   file.createNewFile();
		 //    	}
	  //   	}
	  //    	catch (IOException e) {
	  //           e.printStackTrace();
	  //       }
	  //       try{
	  //           fr = new FileWriter(file,true);
	  //           br = new BufferedWriter(fr);
	  //           br.write("VCMDDU1"+"\n");
	  //           br.write("transaction(test cases) "+ this.getNumTransactions() + "\n" );
	  //           br.write("activityCounter "+ activityCounter + "\n");
			//     br.write("sum_of_squares_transactions " + sum_of_squares_transactions+"\n");
			//     br.write("rho_transaction " + rho_transaction+"\n");
			//     br.write("components "+components+"\n");
			//     br.write("sum_of_squares_components "+sum_of_squares_components +"\n");
			//     br.write("rho_component " + rho_component +"\n");
			//     br.write("rho " + rho +"\n");
			//     br.write("vcmrho "+ vmrho +"\n");
			//     br.write("\n\n\n");


	  //       } catch (IOException e) {
	  //           e.printStackTrace();
	  //       }finally{
	  //           try {
	  //               br.close();
	  //               fr.close();
	  //           } catch (IOException e) {
	  //               e.printStackTrace();
	  //           }
	  //       }



		     return vmrho;
		      //double rho = rho_transaction/2;
		      //return rho;

		}

		public double getVCMrho2() {
			//Vector v_each_transaction = new Vector();
		      //Vector v_ideal_transaction = new Vector();






		     System.out.println("******************* The program works **********************");
		      if (!this.isValidMatrix()) return 0d;

		      double activityCounter = 0d;
		      double sum_of_squares_transactions = 0d;
		      double sum_of_squares_components = 0d;
		      for (BitSet transaction : this.transactions) {
		        activityCounter += transaction.cardinality();
		        sum_of_squares_transactions += ((double)transaction.cardinality())*((double)transaction.cardinality());
		      }
		      double rho_transaction = 0d;
		      if(activityCounter==0)
		      		rho_transaction = 0;
		      else
		      		rho_transaction = activityCounter/( Math.sqrt( (double) this.getNumTransactions() ) * Math.sqrt((double)sum_of_squares_transactions) );


		      int components = this.getNumComponents();
		      int[] comp = new int[components];
		      for(int c=0; c < components; c++)
		        comp[c]=0;
		      //BitSet activations = new BitSet(components);
		      for (BitSet transaction : this.transactions) {
		        for (int c = 0; c < components; c++) {
		          comp[c] += transaction.get(c) ? 1 : 0;
		        }
		      }
		      // for (int c = 0; c < components; c++) {
		      //   sum_of_squares_components += comp[c]*comp[c];
		      // }
		      double[] c_vec = new double[components];
		      double[] a_vec = new double[components];
		      for(int c=0;c < components; c++)
		      {
		      		c_vec[c] = comp[c]/( (double) this.getNumTransactions() );
		      }

		      //Read array from file
		      File file = new File("/home/harsh/comp_rho.txt");

		      if(!file.exists())
		      {
		      		for(int c=0; c < components; c++)
		      		{
		      			a_vec[c]=0.5;
		      		}
		      }
		      else
		      {
		      	try{
				  BufferedReader br = new BufferedReader(new FileReader(file));

				  String st;
				  int c=0;
				  while ((st = br.readLine()) != null) {
				    	a_vec[c] = Double.parseDouble(st);
				    	c=c+1;
				  		}

				  		}
				  catch(IOException e) {
	            e.printStackTrace();
	        }
				}
		      double rho = 1;

		      for(int c=0;c< components;c++)
		      {
		      	 rho = rho*(1 - Math.abs(a_vec[c] - c_vec[c]));
		      }





			File file2 = new File("/home/harsh/Values.txt");
	        FileWriter fr = null;
	        BufferedWriter br = null;
	        try{
		        if(!file2.exists()){
		    	   file2.createNewFile();
		    	}
	    	}
	     	catch (IOException e) {
	            e.printStackTrace();
	        }
	        try{
	            fr = new FileWriter(file2,true);
	            br = new BufferedWriter(fr);
	            br.write("VCMDDU2"+"\n");
	            br.write("transaction(test cases) "+ this.getNumTransactions() + "\n" );
	            br.write("activityCounter "+ activityCounter + "\n");
			    br.write("sum_of_squares_transactions " + sum_of_squares_transactions+"\n");
			    br.write("rho_transaction " + rho_transaction+"\n");
			    br.write("components "+components+"\n");
			    br.write("rho " + rho +"\n");
			    br.write("\n\n\n");


	        } catch (IOException e) {
	            e.printStackTrace();
	        }finally{
	            try {
	                br.close();
	                fr.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }



		     return 0.5 *rho;
		      //double rho = rho_transaction/2;
		      //return rho;

		}

		public double getVMrho() {
			//Vector v_each_transaction = new Vector();
		      //Vector v_ideal_transaction = new Vector();






		      System.out.println("******************* The program works **********************");
		      if (!this.isValidMatrix()) return 0d;

		      double activityCounter = 0d;
		      double sum_of_squares_transactions = 0d;
		      double sum_of_squares_components = 0d;
		      for (BitSet transaction : this.transactions) {
		        activityCounter += transaction.cardinality();
		        sum_of_squares_transactions += ((double)transaction.cardinality())*((double)transaction.cardinality());
		      }
		      double rho_transaction = 0d;
		      if(activityCounter==0)
		      		rho_transaction = 0;
		      else
		      		rho_transaction = activityCounter/( Math.sqrt( (double) this.getNumTransactions() ) * Math.sqrt((double)sum_of_squares_transactions) );


		      int components = this.getNumComponents();
		      int[] comp = new int[components];
		      for(int c=0; c < components; c++)
		        comp[c]=0;
		      //BitSet activations = new BitSet(components);
		      for (BitSet transaction : this.transactions) {
		        for (int c = 0; c < components; c++) {
		          comp[c] += transaction.get(c) ? 1 : 0;
		        }
		      }
		      for (int c = 0; c < components; c++) {
		        sum_of_squares_components += comp[c]*comp[c];
		      }
		      double rho_component =0d;
		      if(activityCounter ==0)
		      		rho_component =0;
		      else
		      		rho_component = activityCounter/( Math.sqrt( (double) this.getNumComponents() ) * Math.sqrt(sum_of_squares_components) );

		      double rho = activityCounter / ( ((double) this.getNumComponents()) * ((double) this.getNumTransactions()) );

		      double vmrho = 0.5*((rho_component*rho_transaction) - Math.abs(0.5 - rho));


			// File file = new File("/home/harsh/Values.txt");
	  //       FileWriter fr = null;
	  //       BufferedWriter br = null;
	  //       try{
		 //        if(!file.exists()){
		 //    	   file.createNewFile();
		 //    	}
	  //   	}
	  //    	catch (IOException e) {
	  //           e.printStackTrace();
	  //       }
	  //       try{
	  //           fr = new FileWriter(file,true);
	  //           br = new BufferedWriter(fr);
	  //           br.write("VMDDU"+"\n");
	  //           br.write("transaction(test cases) "+ this.getNumTransactions() + "\n" );
	  //           br.write("activityCounter "+ activityCounter + "\n");
			//     br.write("sum_of_squares_transactions " + sum_of_squares_transactions+"\n");
			//     br.write("rho_transaction " + rho_transaction+"\n");
			//     br.write("components "+components+"\n");
			//     br.write("sum_of_squares_components "+sum_of_squares_components +"\n");
			//     br.write("rho_component " + rho_component +"\n");
			//     br.write("rho " + rho +"\n");
			//     br.write("vmrho "+ vmrho +"\n");
			//     br.write("\n\n\n");


	  //       } catch (IOException e) {
	  //           e.printStackTrace();
	  //       }finally{
	  //           try {
	  //               br.close();
	  //               fr.close();
	  //           } catch (IOException e) {
	  //               e.printStackTrace();
	  //           }
	  //       }



		     return vmrho;
		      //double rho = rho_transaction/2;
		      //return rho;

		}

		// public double getVRrho() {
		// 	//Vector v_each_transaction = new Vector();
		//       //Vector v_ideal_transaction = new Vector();

		// 	int max = 1000;
	 //        int min = 1;
	 //        int range = max - min + 1;

	 //        int rand = (int)(Math.random() * range) + min;

	 //        if(rand%2==0){
		//       System.out.println("******************* The program works **********************");
		//       if (!this.isValidMatrix()) return 0d;

		//       double activityCounter = 0d;
		//       double sum_of_squares_transactions = 0d;
		//       double sum_of_squares_components = 0d;
		//       for (BitSet transaction : this.transactions) {
		//         activityCounter += transaction.cardinality();
		//         sum_of_squares_transactions += ((double)transaction.cardinality())*((double)transaction.cardinality());
		//       }
		//       double rho_transaction = 0d;
		//       if(activityCounter==0)
		//       		rho_transaction = 0;
		//       else
		//       		rho_transaction = activityCounter/( Math.sqrt( (double) this.getNumTransactions() ) * Math.sqrt((double)sum_of_squares_transactions) );


		//       int components = this.getNumComponents();
		//       int[] comp = new int[components];
		//       for(int c=0; c < components; c++)
		//         comp[c]=0;
		//       //BitSet activations = new BitSet(components);
		//       for (BitSet transaction : this.transactions) {
		//         for (int c = 0; c < components; c++) {
		//           comp[c] += transaction.get(c) ? 1 : 0;
		//         }
		//       }
		//       for (int c = 0; c < components; c++) {
		//         sum_of_squares_components += comp[c]*comp[c];
		//       }
		//       double rho_component =0d;
		//       if(activityCounter ==0)
		//       		rho_component =0;
		//       else
		//       		rho_component = activityCounter/( Math.sqrt( (double) this.getNumComponents() ) * Math.sqrt(sum_of_squares_components) );




		// 	// File file = new File("/home/harsh/Values.txt");
	 //  //       FileWriter fr = null;
	 //  //       BufferedWriter br = null;
	 //  //       try{
		//  //        if(!file.exists()){
		//  //    	   file.createNewFile();
		//  //    	}
	 //  //   	}
	 //  //    	catch (IOException e) {
	 //  //           e.printStackTrace();
	 //  //       }
	 //  //       try{
	 //  //           fr = new FileWriter(file,true);
	 //  //           br = new BufferedWriter(fr);
	 //  //           br.write("VRDDU ITERATION OF VDDU"+"\n");
	 //  //           br.write("Random No "+ rand + "\n" );
	 //  //           br.write("transaction(test cases) "+ this.getNumTransactions() + "\n" );
	 //  //           br.write("activityCounter "+ activityCounter + "\n");
		// 	//     br.write("sum_of_squares_transactions " + sum_of_squares_transactions+"\n");
		// 	//     br.write("rho_transaction " + rho_transaction+"\n");
		// 	//     br.write("components "+components+"\n");
		// 	//     br.write("sum_of_squares_components "+sum_of_squares_components +"\n");
		// 	//     br.write("rho_component " + rho_component +"\n");
		// 	//     br.write("\n\n\n");


	 //  //       } catch (IOException e) {
	 //  //           e.printStackTrace();
	 //  //       }finally{
	 //  //           try {
	 //  //               br.close();
	 //  //               fr.close();
	 //  //           } catch (IOException e) {
	 //  //               e.printStackTrace();
	 //  //           }
	 //  //       }



		//      return 0.5*(rho_transaction)*(rho_component);
		//       //double rho = rho_transaction/2;
		//       //return rho;
		//  }
		//  else
		//  {
		//  	if (!this.isValidMatrix()) return 0d;

		// 	double activityCounter = 0d;
		// 	for (BitSet transaction : this.transactions) {
		// 		activityCounter += transaction.cardinality();
		// 	}
		// 	System.out.println("*************In spectrum rho***********");

		// 	double rho = activityCounter / ( ((double) this.getNumComponents()) * ((double) this.getNumTransactions()) );

		// 	// File file = new File("/home/harsh/Values.txt");
	 //  //       FileWriter fr = null;
	 //  //       BufferedWriter br = null;
	 //  //       try{
		//  //        if(!file.exists()){
		//  //    	   file.createNewFile();
		//  //    	}
	 //  //   	}
	 //  //    	catch (IOException e) {
	 //  //           e.printStackTrace();
	 //  //       }
	 //  //       try{
	 //  //           fr = new FileWriter(file,true);
	 //  //           br = new BufferedWriter(fr);
	 //  //           br.write("VMDDU ITEARTION OF DDU"+"\n");
	 //  //           br.write("Random No "+ rand + "\n" );
	 //  //           br.write("transaction(test cases) "+ this.getNumTransactions() + "\n" );
	 //  //           br.write("components "+ this.getNumComponents() + "\n" );
	 //  //           br.write("activityCounter "+ activityCounter + "\n");
	 //  //           br.write("rho "+rho+"\n");
		// 	//     br.write("\n\n\n");


	 //  //       } catch (IOException e) {
	 //  //           e.printStackTrace();
	 //  //       }finally{
	 //  //           try {
	 //  //               br.close();
	 //  //               fr.close();
	 //  //           } catch (IOException e) {
	 //  //               e.printStackTrace();
	 //  //           }
	 //  //       }





		// 	return rho;
		//  }

		// }


		public double getRho() {

			if (!this.isValidMatrix()) return 0d;

			double activityCounter = 0d;
			for (BitSet transaction : this.transactions) {
				activityCounter += transaction.cardinality();
			}
			System.out.println("*************In spectrum rho***********");

			double rho = activityCounter / ( ((double) this.getNumComponents()) * ((double) this.getNumTransactions()) );

			// File file = new File("/home/harsh/Values.txt");
	  //       FileWriter fr = null;
	  //       BufferedWriter br = null;
	  //       try{
		 //        if(!file.exists()){
		 //    	   file.createNewFile();
		 //    	}
	  //   	}
	  //    	catch (IOException e) {
	  //           e.printStackTrace();
	  //       }
	  //       try{
	  //           fr = new FileWriter(file,true);
	  //           br = new BufferedWriter(fr);
	  //           br.write("DDU"+"\n");
	  //           br.write("transaction(test cases) "+ this.getNumTransactions() + "\n" );
	  //           br.write("components "+ this.getNumComponents() + "\n" );
	  //           br.write("activityCounter "+ activityCounter + "\n");
	  //           br.write("rho "+rho+"\n");
			//     br.write("\n\n\n");


	  //       } catch (IOException e) {
	  //           e.printStackTrace();
	  //       }finally{
	  //           try {
	  //               br.close();
	  //               fr.close();
	  //           } catch (IOException e) {
	  //               e.printStackTrace();
	  //           }
	  //       }

			return rho;
			//return 0;
		}

		public double getVRrho()
		{
			if (!this.isValidMatrix()) return 0d;



			if(flag ==0 )  //VDDU will run
			{

				double activityCounter = 0d;
		      	double sum_of_squares_transactions = 0d;
		      	double sum_of_squares_components = 0d;
		      	for (BitSet transaction : this.transactions) {
		        	activityCounter += transaction.cardinality();
		        	sum_of_squares_transactions += ((double)transaction.cardinality())*((double)transaction.cardinality());
		      	}
		      	double rho_transaction = 0d;
		      	if(activityCounter==0)
		      			rho_transaction = 0;
		      	else
		      			rho_transaction = activityCounter/( Math.sqrt( (double) this.getNumTransactions() ) * Math.sqrt((double)sum_of_squares_transactions) );


			      int components = this.getNumComponents();
			      int[] comp = new int[components];
			      for(int c=0; c < components; c++)
			        comp[c]=0;
			      //BitSet activations = new BitSet(components);
			      for (BitSet transaction : this.transactions) {
			        for (int c = 0; c < components; c++) {
			          comp[c] += transaction.get(c) ? 1 : 0;
			        }
			      }
			      for (int c = 0; c < components; c++) {
			        sum_of_squares_components += comp[c]*comp[c];
			      }
			      double rho_component =0d;
			      if(activityCounter ==0)
			      		rho_component =0;
			      else
			      		rho_component = activityCounter/( Math.sqrt( (double) this.getNumComponents() ) * Math.sqrt(sum_of_squares_components) );
			double vrho = 0.5*(rho_transaction)*(rho_component);
	        double vsimp = (1.0 - getSimpson());
	        double vambig = getAmbiguity();
	        double metric = vrho*vsimp*vambig;

			    
	        
	        
	        int point_buffer = pointer_buffer%10;
	        double stand_devi =0 ;
	        double mean_v = 0;
	        buffer[point_buffer] = metric;
	        if(pointer_buffer >=9){
		        double sum=0;
		        for(int i=0;i<=9;i++)
		        {
		        	sum = sum + buffer[i];
		        	
		        }
		        mean_v = sum/10;
		        int mean_circular_buffer;
		        mean_circular_buffer = pointer_mean%10;
		        mean_buffer[mean_circular_buffer] = mean_v;
		        
		        if(pointer_mean >= 9){
		        	double sum1 =0;
		        	for(int i=0;i<=9;i++)
		        	{
		        		sum1 = sum1 +mean_buffer[i];
		        	}
		        	double mean_std = sum1/10;
		        	stand_devi = 0;
		        	for(int i=0;i<=9;i++)
		        	{
		        		stand_devi = stand_devi + (double)(Math.pow((double)(mean_buffer[i] - mean_std),2));
		        	}
		        	stand_devi = Math.sqrt(stand_devi / (float)(9));

		        	if(stand_devi < 0.005)
		        	{
		        		flag=1;
		        	}
		        }
		        pointer_mean = pointer_mean + 1;
		    }
		        pointer_buffer = pointer_buffer + 1;
		        
		        int counter = 0;
			    while(file_status==0){

				File file = new File("/tmp/Values"+counter+".csv");
					if(!file.exists()){
						file_variable = counter;
						try{
						file.createNewFile();
						file_status = 1;
					}
					catch(IOException e) {
	            e.printStackTrace();
					}
				}
				counter = counter +1;
				}
				File file = new File("/tmp/Values"+file_variable+".csv");
		        FileWriter fr = null;
		        BufferedWriter br = null;
		        
		        try{
		            fr = new FileWriter(file,true);
		            br = new BufferedWriter(fr);
					br.write(rho_transaction+","+rho_component+","+ vrho+"," + vsimp+","+vambig + "," +metric + ","+ mean_v+"," + stand_devi +"\n");

	        } catch (IOException e) {
	            e.printStackTrace();
	        }finally{
	            try {
	                br.close();
	                fr.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }

		        return metric;

			}
			else
			{
				double activityCounter = 0d;
				for (BitSet transaction : this.transactions) {
					activityCounter += transaction.cardinality();
				}
				System.out.println("*************In spectrum rho***********");

				double rho = activityCounter / ( ((double) this.getNumComponents()) * ((double) this.getNumTransactions()) );
				double vsimp = (1.0 - getSimpson());
	        	double vambig = getAmbiguity();
	        	double metric = rho*vsimp*vambig;

				File file = new File("/tmp/Values"+file_variable+".csv");

				FileWriter fr = null;
		        BufferedWriter br = null;
				try{
			            fr = new FileWriter(file,true);
			            br = new BufferedWriter(fr);
						br.write(rho+"," + vsimp+","+vambig + "," +metric +"\n");

		        	} catch (IOException e) {
		            e.printStackTrace();
		        	}finally{
		            try {
		                br.close();
		                fr.close();
		            } catch (IOException e){
		                e.printStackTrace();
		            }
	        	}
	        	return metric;
			}
		}

		public double getDistinctTransactionsRho() {

			if (!this.isValidMatrix()) return 0d;

			Set<BitSet> distinctTransactionsSet = new HashSet<BitSet>(this.transactions);

			double activityCounter = 0d;
			for (BitSet transaction : distinctTransactionsSet) {
				activityCounter += transaction.cardinality();
			}

			double rho = activityCounter / ( ((double) this.getNumComponents()) * ((double) distinctTransactionsSet.size()) );
			return rho;
		}

		public double getSimpson() {

			if (!this.isValidMatrix()) return 0d;

			LinkedHashMap<Integer, Integer> species = new LinkedHashMap<Integer, Integer>();
			for (BitSet transaction : transactions) {
				int hash = transaction.hashCode();
				if (species.containsKey(hash)) {
					species.put(hash, species.get(hash) + 1);
				}
				else {
					species.put(hash, 1);
				}
			}

			double n = 0.0;
			double N = 0.0;

			for (int s : species.keySet()) {
				double ni = species.get(s);
				n += ni * (ni - 1);
				N += ni;
			}

			double diversity = ( (N == 0.0) || ((N - 1) == 0) ) ? 1.0 : n / (N * (N - 1));
			return diversity;
		}

		public double getAmbiguity() {

			if (!this.isValidMatrix()) return 0d;

			Set<Integer> ambiguityGroups = new HashSet<Integer>();

			int transactions = this.getNumTransactions();
			int components = this.getNumComponents();

			for (int c = 0; c < components; c++) {
				BitSet bs = new BitSet(transactions);

				for (int t = 0; t < transactions; t++) {
					if (this.getInvolved(t, c)) {
						bs.set(t);
					}
				}

				ambiguityGroups.add(bs.hashCode());
			}

			double ambiguity = (double) ambiguityGroups.size() / (double) components;
			return ambiguity;
		}

		public double basicCoverage() {
			if (!this.isValidMatrix()) return 0d;
			int components = this.getNumComponents();

			BitSet activations = new BitSet(components);
			for (BitSet transaction : transactions) {
				for (int c = 0; c < components; c++) {
					if(transaction.get(c)) {
						activations.set(c);
					}
				}
			}

			double coverage = (double) activations.cardinality() / (double) components;
			return coverage;
		}
	}
