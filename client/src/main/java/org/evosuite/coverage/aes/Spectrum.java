	package org.evosuite.coverage.aes;


	//import jdk.jfr.Threshold;

	import java.util.ArrayList;
	import java.util.BitSet;
	import java.util.HashSet;
	import java.util.LinkedHashMap;
	import java.util.Set;
	import java.io.*;
    import java.lang.Math;


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

		public double getRho() {

			if (!this.isValidMatrix()) return 0d;

			double activityCounter = 0d;
			for (BitSet transaction : this.transactions) {
				activityCounter += transaction.cardinality();
			}

			double rho = activityCounter / ( ((double) this.getNumComponents()) * ((double) this.getNumTransactions()) );

			return rho;

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

		public double[] getDistances(){
            double distances[] = {0.0,0.0,0.0};
            if (!this.isValidMatrix()) return distances;

		    int rowsize = this.getNumTransactions();
		    int colsize = this.getNumComponents();

		    double myspectrum[][] = new double[rowsize][colsize];
            int counter = 0;
            for (BitSet transaction : transactions)
            {
                for(int j=0;j<colsize;j++)
                {
                    if(transaction.get(j))
                        myspectrum[counter][j] = 1.0;

                    else
                        myspectrum[counter][j] = 0.0;

                }
                counter++;
            }
            counter=0;
            double sum_eucledian = 0.0;
            double sum_manhattan = 0.0;
            double sum_hamming = 0.0;
            for(int i=0;i<rowsize;i++)
            {
                for(int j=i+1;j<rowsize;j++)
                {
                    double temp1 = 0.0;
                    for(int k=0;k<colsize;k++)
                    {
                        if(myspectrum[i][k] != myspectrum[j][k]) {
                            temp1 = temp1 + 1.0;
                        }
                    }
                    sum_eucledian = sum_eucledian + Math.sqrt(temp1);
                    sum_manhattan = sum_manhattan + temp1;
                    sum_hamming = sum_hamming + (temp1 / colsize);
                    counter++;

                }
            }

            if(counter == 0)
                return distances;
            distances[0] = (sum_eucledian / counter);
            distances[1] = (sum_manhattan / counter);
            distances[2] = (sum_hamming / counter);
            return distances;
		}

        public double[][] compute_ochiai()
        {
            if (!this.isValidMatrix()) return null;

            ArrayList<BitSet> A = this.transactions;
            int rows = this.getNumTransactions();
            int cols = this.getNumComponents();
            double[][] result = new double[cols][cols];
            for(int i=0;i<cols;i++)
            {
                for(int j=0;j<cols;j++)
                {
                    double ef = 0d;
                    double ep = 0d;
                    double nf = 0d;

                    for(int k=0;k<rows;k++)
                    {
                        if((A.get(k).get(i) == true) && (A.get(k).get(j) == true))
                            ef++;
                        else if((A.get(k).get(i) == false) && (A.get(k).get(j) == true))
                            ep++;
                        else if((A.get(k).get(i) == true) && (A.get(k).get(j) == false))
                            nf++;
                    }
                    double denominator = Math.sqrt((ef + ep) * (ef + nf));
                    if(denominator != 0)
                        result[i][j] = ef / denominator;
                }

            }

            return result;
        }

	}
