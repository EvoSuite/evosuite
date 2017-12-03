package org.evosuite.ga;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Construction of a grid and the neighbourhood models 
 * 
 * @author Nasser Albunian
 */
public class Neighbourhood<T extends Chromosome> implements NeighborModels,Serializable{

	private static final long serialVersionUID = 1L;

	/** Size of collection **/
	private int chromCollectionSize;
	
	/** Position values of different neighbourhood based on the direction **/
	private int _L, _R, _N, _S, _W, _E, _NE, _NW, _SE, _SW, _NN, _SS, _EE, _WW;
	
	/** An array that represents the grid **/
	int neighbour[][];
	
	/** Number of chromosomes per one row of a grid **/
	int columns;
	
	/**
	 * Collection of cells will be returned by different models of neighbourhood 
	 */
	private List<T> chromosomes = new ArrayList<>();
	
	
	public Neighbourhood (int populationSize){
		
		chromCollectionSize = populationSize;
		
		neighbour = new int[chromCollectionSize][0];
		
		columns = (int)Math.sqrt(chromCollectionSize);
		
		constructNeighbour();
	}
	
	/**
	 * Construct the grid and define positions of neighbours for each individual
	 */
	public void constructNeighbour(){

		for(int i=0; i<chromCollectionSize; i++){
			neighbour[i] = new int[8];
		}
		
		for(int i=0; i<chromCollectionSize; i++){
			
			//~~~~ NORTH ~~~~//
			if (i > columns - 1)
			{
				neighbour[i][Positions.N.ordinal()] = i - columns;
			}
			else
			{
				int mod = chromCollectionSize % columns; 
				if(mod != 0){
					int thisPosition = ((i - columns + chromCollectionSize) % chromCollectionSize);
					if(i == 0){
						neighbour[i][Positions.N.ordinal()] = chromCollectionSize - (mod);
					}else{
						if(mod > 1){
							if(i >= mod){
								neighbour[i][Positions.N.ordinal()] = thisPosition - mod;
							}else{
								neighbour[i][Positions.N.ordinal()] = thisPosition + 1;
							}
						}else{
							neighbour[i][Positions.N.ordinal()] = thisPosition - 1;
						}
					}
				}else{
					neighbour[i][Positions.N.ordinal()] = (i - columns + chromCollectionSize) % chromCollectionSize;
				}
			}
			
			//~~~~ SOUTH ~~~~//
			int thisPosition = (i + columns) % chromCollectionSize;
			if(chromCollectionSize % columns != 0 && i+columns>=chromCollectionSize){
				neighbour[i][Positions.S.ordinal()] = i % columns;
			}else{
				neighbour[i][Positions.S.ordinal()] = thisPosition;
			}
			
			//~~~~ EAST ~~~~//
			if ((i + 1) % columns == 0)
			{
				neighbour[i][Positions.E.ordinal()] = i - (columns - 1);
			}
			else 
			{
				if(chromCollectionSize % columns != 0 && i == chromCollectionSize-1){
					neighbour[i][Positions.E.ordinal()] = (i % columns) + 1;
				}else{
					neighbour[i][Positions.E.ordinal()] = i + 1;
				}
			}

			//~~~~ WEST ~~~~//
			if (i % columns == 0)
			{
				int westPosition = i + (columns - 1);
				if(westPosition >= chromCollectionSize){
					neighbour[i][Positions.W.ordinal()] = neighbour[i][Positions.E.ordinal()];
				}else{
					neighbour[i][Positions.W.ordinal()] = westPosition;
				}
			}
			else
			{
				neighbour[i][Positions.W.ordinal()] = i - 1;
			}
		}
		
		//~~~~ NW, SW, NE, SE ~~~~//
		for(int i=0; i<chromCollectionSize; i++){
			neighbour[i][Positions.NW.ordinal()] = neighbour[neighbour[i][Positions.N.ordinal()]][Positions.W.ordinal()];
			
			neighbour[i][Positions.SW.ordinal()] = neighbour[neighbour[i][Positions.S.ordinal()]][Positions.W.ordinal()];
			
			neighbour[i][Positions.NE.ordinal()] = neighbour[neighbour[i][Positions.N.ordinal()]][Positions.E.ordinal()];
			
			neighbour[i][Positions.SE.ordinal()] = neighbour[neighbour[i][Positions.S.ordinal()]][Positions.E.ordinal()];
		}
		
	}

	/**
	 * Retrieve neighbours of a chromosome according to the ring topology (i.e. 1D)
	 * @param collection The current collection of chromosomes
	 * @param position   The position of a chromosome which its neighbours will be retrieved
	 * @return collection of neighbours 
	 */
	@SuppressWarnings("unchecked")
	public List<T> ringTopology(List<? extends Chromosome> collection, int position) {
		
		if (position-1 < 0){
			_L = collection.size() - 1;
		}else{
			_L = position - 1;
		}
		
		if (position+1 > collection.size() - 1){
			_R = 0;
		}else{
			_R = position + 1;
		}
		
		chromosomes.add((T) collection.get(_L));
		chromosomes.add((T) collection.get(_R));
		chromosomes.add((T) collection.get(position));
		
		return chromosomes;
	}

	/**
	 * Retrieve neighbours of a chromosome according to the linear five model (i.e. L5)
	 * @param collection The current collection of chromosomes
	 * @param position   The position of a chromosome which its neighbours will be retrieved
	 * @return collection of neighbours 
	 */
	@SuppressWarnings("unchecked")
	public List<T> linearFive(List<? extends Chromosome> collection, int position) {
		
		_N = neighbour[position][Positions.N.ordinal()];
		_S = neighbour[position][Positions.S.ordinal()];
		_E = neighbour[position][Positions.E.ordinal()];
		_W = neighbour[position][Positions.W.ordinal()];
		
		chromosomes.add((T) collection.get(_N));
		chromosomes.add((T) collection.get(_S));
		chromosomes.add((T) collection.get(_E));
		chromosomes.add((T) collection.get(_W));
		chromosomes.add((T) collection.get(position));
		
		return chromosomes;
	}

	/**
	 * Retrieve neighbours of a chromosome according to the compact nine model (i.e. C9)
	 * @param collection The current collection of chromosomes
	 * @param position   The position of a chromosome which its neighbours will be retrieved
	 * @return collection of neighbours 
	 */
	@SuppressWarnings("unchecked")
	public List<T> compactNine(List<? extends Chromosome> collection, int position) {
		
		_N  = neighbour[position][Positions.N.ordinal()];
		_S  = neighbour[position][Positions.S.ordinal()];
		_E  = neighbour[position][Positions.E.ordinal()];
		_W  = neighbour[position][Positions.W.ordinal()];
		_NW = neighbour[neighbour[position][Positions.N.ordinal()]][Positions.W.ordinal()];
		_SW = neighbour[neighbour[position][Positions.S.ordinal()]][Positions.W.ordinal()];
		_NE = neighbour[neighbour[position][Positions.N.ordinal()]][Positions.E.ordinal()];
		_SE = neighbour[neighbour[position][Positions.S.ordinal()]][Positions.E.ordinal()];
		
		chromosomes.add((T) collection.get(_N));
		chromosomes.add((T) collection.get(_S));
		chromosomes.add((T) collection.get(_E));
		chromosomes.add((T) collection.get(_W));
		chromosomes.add((T) collection.get(_NW));
		chromosomes.add((T) collection.get(_SW));
		chromosomes.add((T) collection.get(_NE));
		chromosomes.add((T) collection.get(_SE));
		chromosomes.add((T) collection.get(position));
		
		return chromosomes;
	}

	/**
	 * Retrieve neighbours of a chromosome according to the linear compact thirteen (i.e. C13)
	 * @param collection The current collection of chromosomes
	 * @param position   The position of a chromosome which its neighbours will be retrieved
	 * @return collection of neighbours 
	 */
	@SuppressWarnings("unchecked")
	public List<T> CompactThirteen(List<? extends Chromosome> collection, int position) {
		
		_N  = neighbour[position][Positions.N.ordinal()];
		_S  = neighbour[position][Positions.S.ordinal()];
		_E  = neighbour[position][Positions.E.ordinal()];
		_W  = neighbour[position][Positions.W.ordinal()];
		_NW = neighbour[neighbour[position][Positions.N.ordinal()]][Positions.W.ordinal()];
		_SW = neighbour[neighbour[position][Positions.S.ordinal()]][Positions.W.ordinal()];
		_NE = neighbour[neighbour[position][Positions.N.ordinal()]][Positions.E.ordinal()];
		_SE = neighbour[neighbour[position][Positions.S.ordinal()]][Positions.E.ordinal()];
		_NN = neighbour[_N][Positions.N.ordinal()];
		_SS = neighbour[_S][Positions.S.ordinal()];
		_EE = neighbour[_E][Positions.E.ordinal()];
		_WW = neighbour[_W][Positions.W.ordinal()];
		
		chromosomes.add((T) collection.get(_N));
		chromosomes.add((T) collection.get(_S));
		chromosomes.add((T) collection.get(_E));
		chromosomes.add((T) collection.get(_W));
		chromosomes.add((T) collection.get(_NW));
		chromosomes.add((T) collection.get(_SW));
		chromosomes.add((T) collection.get(_NE));
		chromosomes.add((T) collection.get(_SE));
		chromosomes.add((T) collection.get(_NN));
		chromosomes.add((T) collection.get(_SS));
		chromosomes.add((T) collection.get(_EE));
		chromosomes.add((T) collection.get(_WW));
		chromosomes.add((T) collection.get(position));
		
		return chromosomes;
	}

}
