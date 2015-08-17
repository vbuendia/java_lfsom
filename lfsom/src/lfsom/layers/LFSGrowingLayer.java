/*
 * 
 * This class is a variation of "GrowingLayer" class from:
 * Java SOMToolbox
 * http://www.ifs.tuwien.ac.at/dm/somtoolbox/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.livingforsom.com/license.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ---------------------------------------------------------------
 * 
 * Original license from Java SOMToolbox:
 * 
 * Copyright 2004-2010 Information & Software Engineering Group (188/1)
 *                     Institute of Software Technology and Interactive Systems
 *                     Vienna University of Technology, Austria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.ifs.tuwien.ac.at/dm/somtoolbox/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package lfsom.layers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import lfsom.data.LFSData;
import lfsom.data.LFSInputDatum;
import lfsom.layers.metrics.HexMapDistancer;
import lfsom.layers.metrics.LFSL2Metric;
import lfsom.layers.quality.LFSKLDistance;
import lfsom.layers.quality.LFSQualityMeasure;
import lfsom.layers.quality.LFSQuantizationError;
import lfsom.layers.quality.LFSTopographicError;
import lfsom.models.LFSGrowingSOM;
import lfsom.properties.LFSSOMProperties;
import lfsom.util.LFSException;
import cern.colt.matrix.DoubleMatrix1D;

/**
 * Implementation of a growing Self-Organizing Map layer that can also be static
 * in size. Layer growth is based on the quantization errors of the units and
 * the distance to their respective neighboring units.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: GrowingLayer.java 4245 2012-01-23 15:30:11Z mayer $
 */
//
//

public class LFSGrowingLayer {

	// Quality measures
	private LFSQualityMeasure QError = null;

	private LFSQualityMeasure TError = null;

	private LFSQualityMeasure KError = null;

	// To know if training has to finish
	private boolean qualityReached = false;

	private Random rand = null;

	// Units of the layer
	private LFSUnit[][] units = null;

	// Precalculed distances among units
	private int[][][][] distancesHex = null;

	// Precalculed kernel depending on neighbour function
	private double[][] distNeigh = null;

	// Different ranges of disNeigh, assigning sigma decay
	private int nRangos = 3;

	private int xSize = 0;

	private int ySize = 0;

	// The "worst" error
	private double maxQe = 0;

	// The unif having maxQe
	private LFSUnit unitmaxQe;

	// Neighbour function to apply training
	private int neighbourFunc = NEIGH_GAUSS;

	// Width of neighbour to apply in bubble and cut gauss
	private float neighbourWidth = 15;

	// Declaration of the neighbour functions
	public static final int NEIGH_GAUSS = 10;

	public static final int NEIGH_CUTGAUSS = 20;

	public static final int NEIGH_BUBBLE = 30;

	public static final int NEIGH_EP = 40;

	private int maxYSize, maxXSize;

	// Control to avoid growing always rows, or always cols
	private boolean ultFilas = false;

	// Link to SOM this belongs to
	private LFSGrowingSOM gSOM;

	// Data to train
	private LFSData data;

	/**
	 * Constructor gets data sample, properties of a concrete SOM, a list of
	 * precalculated units and an index to the SOM it belongs
	 * 
	 * 
	 * @param normalized
	 * @param data
	 * @param props
	 * @param units
	 * @param growSOM
	 */
	public LFSGrowingLayer(boolean normalized, LFSData data,
			LFSSOMProperties props, LFSUnit[][] units, LFSGrowingSOM growSOM) {

		this(props.xSize(), props.ySize(), props.metricName(), normalized,
				props.pca(), props.randomSeed(), data, props
						.getInitializationMode(), props.getNeighbourFunc(),
				props.pcNeighbourWidth(), props.learnrate(), props.sigma(),
				units, growSOM);

		props.setXYSize(this.xSize, this.ySize);
		props.setNumIterations(data.getData().length);

		if (props.getNeighbourFunc() == LFSGrowingLayer.NEIGH_BUBBLE) {
			// There isn't sigma decay
			nRangos = 1;
		}

		try {
			props.validatePropertyValues();
			calcDistancesNEIGH(this.xSize, this.ySize, props.learnrate(),
					props.sigma());
		} catch (LFSException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Save csv with data mapped to a list of units
	 * 
	 * @param datum
	 * @param labels
	 * @param listaCells
	 * @param DataPath
	 * @param nomFich
	 */
	public void saveMapCSVParcial(LFSData datum, String[] labels,
			ArrayList<Integer> listaCells, String DataPath, String nomFich) {
		// Se recorre la lista de cells, grabando en un fichero las mapeadas
		PrintWriter writer;
		try {
			// En primer lugar hay que validar los datos

			new File(DataPath).mkdirs();
			writer = new PrintWriter(nomFich);
			StringBuilder sb = new StringBuilder();
			for (String n : labels) {
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append(n);
			}
			writer.println(sb.toString());

			for (int j = 0; j < datum.numVectors(); j++) {
				for (int i = 0; i < listaCells.size(); i++) {
					int filaac = (int) Math.floor(listaCells.get(i) / xSize);
					int colac = listaCells.get(i) % xSize;
					if (units[colac][filaac].isMapped(datum.getInputDatum(j)
							.getLabel())) {
						String fuera = "";
						for (int w = 0; w < datum.dim(); w++) {
							fuera = fuera
									+ String.valueOf(datum.getInputDatum(j)
											.getVector().get(w));
							if (w != datum.dim() - 1) {
								fuera = fuera + ",";
							}
						}
						writer.println(fuera);
					}
				}
			}

			writer.close();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}

	}

	/**
	 * A simple LFSGrowingLayer creator
	 * 
	 * @param xSize
	 * @param ySize
	 * @param seed
	 * @param growSOM
	 */
	private LFSGrowingLayer(int xSize, int ySize, long seed,
			LFSGrowingSOM growSOM) {

		rand = new Random(seed);
		new Random();
		this.xSize = xSize;
		this.ySize = ySize;
		this.setgSOM(growSOM);
	}

	/**
	 * A constructor with parameters instead of getting a properties class
	 * 
	 * @param xSize
	 * @param ySize
	 * @param metricName
	 * @param normalized
	 * @param usePCA
	 * @param seed
	 * @param data
	 * @param initialisationMode
	 * @param neighFunc
	 * @param pcNeighWidth
	 * @param learnrate
	 * @param sigma
	 * @param unitsAssign
	 * @param growSOM
	 */
	private LFSGrowingLayer(int xSize, int ySize, String metricName,
			boolean normalized, boolean usePCA, long seed, LFSData data,
			int initialisationMode, int neighFunc, float pcNeighWidth,
			double learnrate, double sigma, LFSUnit[][] unitsAssign,
			LFSGrowingSOM growSOM) {

		this(xSize, ySize, seed, growSOM);
		this.data = data;

		this.neighbourFunc = neighFunc;
		int maxSiz = this.xSize > this.ySize ? this.xSize : this.ySize;
		this.neighbourWidth = (int) (maxSiz * pcNeighWidth);

		// Heuristic of size calculation
		double numceldas = Math.pow(data.getData().length, 0.54321);

		double ratio = Math.sqrt(data.getPCA().getFirstAxisIndex()
				/ data.getPCA().getSecondAxisIndex());
		int ySizeCalc = (int) Math.min(numceldas,
				Math.round(Math.sqrt(numceldas / ratio)));
		int xSizeCalc = (int) (numceldas / ySizeCalc);

		// Max size to grow
		maxYSize = 2 * ySizeCalc;
		maxXSize = 3 * xSizeCalc;

		if (xSize == 0 || ySize == 0) {

			this.ySize = ySizeCalc;
			this.xSize = xSizeCalc;

		}

		units = new LFSUnit[this.xSize][this.ySize];

		calcDistancesHex(maxXSize + 1, maxYSize + 1, true);

		if (unitsAssign != null) {
			for (int j = 0; j < this.ySize; j++) {
				for (int i = 0; i < this.xSize; i++) {
					units[i][j] = new LFSUnit(i, j,
							unitsAssign[i][j].getWeightVectorVal());
				}
			}
		} else {

			for (int j = 0; j < this.ySize; j++) {
				for (int i = 0; i < this.xSize; i++) {
					units[i][j] = new LFSUnit(data, i, j, data.dim(), rand,
							normalized, initialisationMode);
				}
			}
		}

	}

	/**
	 * Load a list of values into weightvectors of a units list
	 * 
	 * @param dimen
	 * @param pesos
	 */
	public void chargeWeights(int dimen, String pesos) {
		String[] strbl = pesos.split(" ");
		int temp = 0;
		for (int j = 0; j < this.ySize; j++) {
			for (int i = 0; i < this.xSize; i++) {

				try {
					this.units[i][j].setWeightVector(dimen,
							Double.valueOf(strbl[temp++]));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (LFSException e) {

					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Removes all mapped input data from the units.
	 */
	private void clearMappedInput() {
		for (int j = 0; j < ySize; j++) {
			for (int i = 0; i < xSize; i++) {
				units[i][j].clearMappedInput();
			}
		}
	}

	/**
	 * Returns all units of the layer in an array.
	 * 
	 */
	public LFSUnit[][] getUnits() {
		return units;
	}

	/**
	 * Calculates distances among units in a hexagonal grid. Updates a class
	 * containing these calculated distances
	 * 
	 * 
	 * @param xSize
	 * @param ySize
	 * @param simple
	 */
	private void calcDistancesHex(int xSize, int ySize, boolean simple) {

		HexMapDistancer HexMap = new HexMapDistancer(xSize, ySize, simple);
		distancesHex = HexMap.map();
	}

	/**
	 * Calculates kernel depending on neighbour function
	 * 
	 * @param xSize
	 * @param ySize
	 * @param learnrate
	 * @param sigma
	 */
	private void calcDistancesNEIGH(int xSize, int ySize, double learnrate,
			double sigma) {

		int diagonal = distancesHex[0][0][xSize][ySize];
		distNeigh = new double[nRangos][diagonal];

		// Se calculan las distancias segun Neighbourfunc
		for (int ran = 0; ran < nRangos; ran++) {
			for (int x = 0; x < diagonal; x++) {
				distNeigh[ran][x] = getHCI(x, learnrate, sigma / (ran + 1), ran);
			}
		}

	}

	/**
	 * If the net grows, only have to calculate new distances
	 * 
	 * @param xSize
	 * @param ySize
	 * @param learnrate
	 * @param sigma
	 */
	private void calcDistancesNEIGH_add(int xSize, int ySize, double learnrate,
			double sigma) {

		int diagonal = distancesHex[0][0][xSize][ySize];
		double[][] distNeigh_aux = new double[nRangos][diagonal];

		// System.arraycopy(distNeigh, 0, distNeigh_aux, 0, distNeigh.length);
		for (int ran = 0; ran < nRangos; ran++) {
			for (int x = 0; x < distNeigh[ran].length; x++) {
				distNeigh_aux[ran][x] = distNeigh[ran][x];
			}
		}
		// Se calculan las distancias segun Neighbourfunc
		for (int ran = 0; ran < nRangos; ran++) {
			for (int x = distNeigh[ran].length; x < diagonal; x++) {
				distNeigh_aux[ran][x] = getHCI(x, learnrate, sigma / (ran + 1),
						ran);
			}
		}
		distNeigh = distNeigh_aux;

	}

	/**
	 * Neighbour function applying
	 * 
	 * @param distancia
	 * @param learnrate
	 * @param opt1
	 * @param rango
	 * @return
	 */
	private double getHCI(double distancia, double learnrate, double opt1,
			int rango) {
		double hci = 0;
		float neigh = neighbourWidth - rango * neighbourWidth / nRangos;
		try {

			switch (neighbourFunc) {
			case NEIGH_GAUSS:
				hci = learnrate * Math.exp(-1 * distancia / opt1);
				break;
			case NEIGH_CUTGAUSS:
				if (distancia <= neigh) {
					hci = learnrate * Math.exp(-1 * distancia / opt1);
				} else {
					hci = 0;
				}
				break;
			case NEIGH_BUBBLE:
				if (distancia <= neigh) {
					hci = learnrate;
				} else {
					hci = 0;
				}
				break;
			case NEIGH_EP:
				if (distancia <= neigh) {
					hci = learnrate * (1 - distancia) / (opt1 / 2);
				} else {
					hci = 0;
				}

				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return hci;
	}

	/**
	 * Returns the most dissimilar neighbour of a given unit. useful to grow
	 * between the unit with worst error and its most dissimilar neighbour.
	 * 
	 * @param u
	 * @return
	 */
	private LFSUnit getMostDissimilarNeighbor(LFSUnit u) {
		LFSUnit neighbor = null;
		double largestDistance = 0;
		double distance = 0;
		try {
			for (LFSUnit neighbouringUnit : getNeighbouringUnits(u)) {
				try {
					distance = LFSL2Metric.distance(u.getWeightVector(),
							neighbouringUnit.getWeightVector());
				} catch (LFSException e) {
					Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
							e.getMessage());
				}
				if (distance >= largestDistance) {
					neighbor = neighbouringUnit;
					largestDistance = distance;
				}
			}
		} catch (LFSException e) {
			Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
		}

		if (neighbor != null) {
			return neighbor;
		} else {
			Logger.getLogger("at.tuwien.ifs.somtoolbox")
					.severe("A unit on a one-unit SOM has no neighbors. Something went terribly wrong ;-) Aborting.");
			System.exit(-1);
			return null;
		}
	}

	/**
	 * Get a quality measure
	 * 
	 * @param medida
	 * @return
	 */
	public LFSQualityMeasure getQualityMeasure(String medida) {
		if (medida.equals("QError")) {
			return getQError();
		}
		if (medida.equals("TError")) {
			return getTError();
		}
		if (medida.equals("KError")) {
			return getKError();
		}

		return getQError();
	}

	/**
	 * Get unit
	 * 
	 * @param x
	 * @param y
	 * @return
	 * @throws LFSException
	 */

	public LFSUnit getUnit(int x, int y) throws LFSException {
		try {
			return units[x][y];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new LFSException("Position " + x + "/" + y
					+ " is invalid. Map size is " + xSize + "x" + ySize);
		}
	}

	/**
	 * Returns the winner unit for a given input datum specified by argument
	 * <code>input</code>.
	 * 
	 * @param input
	 *            the input datum for which the winner unit will be searched.
	 * @param metric
	 *            the metric to be used.
	 * @return the winner unit.
	 */
	private LFSUnit getWinner(LFSInputDatum input) {
		LFSUnit winner = null;
		double smallestDistance = Double.MAX_VALUE;
		double[] inputVector = input.getVector().toArray();
		for (int j = 0; j < ySize; j++) {
			for (int i = 0; i < xSize; i++) {

				double distance = 0;
				try {
					distance = LFSL2Metric.distance(
							units[i][j].getWeightVector(), inputVector);
				} catch (LFSException e) {
					Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
							e.getMessage());
					System.exit(-1);
				}
				if (distance < smallestDistance) {
					smallestDistance = distance;
					winner = units[i][j];
				}
			}
		}

		return winner;
	}

	/**
	 * Returns a number of best-matching units sorted by distance (ascending)
	 * for a given input datum. If the number of best-matching units is greater
	 * than the total number of units on the map, all units of the map are
	 * returned (appropriately ranked).
	 * 
	 * @param input
	 *            the input datum for which the best-matching units will be
	 *            searched.
	 * @param num
	 *            the number of best-matching units.
	 * @return an array of Unit containing best-matching units sorted ascending
	 *         by distance from the input datum.
	 */
	public LFSUnit[] getWinners(LFSInputDatum input, int num) {
		if (num > xSize * ySize) {
			num = xSize * ySize;
		}
		LFSUnit[] res = new LFSUnit[num];
		double[] dists = new double[num];
		for (int i = 0; i < num; i++) {
			res[i] = null;
			dists[i] = Double.MAX_VALUE;
		}

		DoubleMatrix1D vec = input.getVector();

		for (int j = 0; j < ySize; j++) {
			for (int i = 0; i < xSize; i++) {
				double distance = 0;
				try {
					distance = LFSL2Metric.distance(
							units[i][j].getWeightVector(), vec.toArray());
				} catch (LFSException e) {
					Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
							e.getMessage());
					System.exit(-1);
				}
				int element = 0;
				boolean inserted = false;
				while (inserted == false && element < num) {
					if (distance < dists[element]) { // found place to
														// insert unit
						for (int m = num - 2; m >= element; m--) { // move
																	// units
																	// with
																	// greater
																	// distance
																	// to
																	// right
							res[m + 1] = res[m];
							dists[m + 1] = dists[m];
						}
						res[element] = units[i][j];
						dists[element] = distance;
						inserted = true;
					}
					element++;
				}

			}
		}

		return res;
	}

	public int getXSize() {
		return xSize;
	}

	public void setXSize(int x) {
		this.xSize = x;
	}

	public int getYSize() {
		return ySize;
	}

	public void setYSize(int y) {
		this.ySize = y;
	}

	public int getUnitCount() {
		return getXSize() * getYSize();
	}

	/**
	 * Inserts a row or column of units between units specified by argument
	 * <code>a</code> and <code>b</code>.
	 * 
	 * @param a
	 *            a unit on the layer.
	 * @param b
	 *            a unit on the layer.
	 */

	private void insertRowColumn(LFSUnit a, LFSUnit b, double learnrate,
			double sigma) {
		LFSUnit[][] newUnits = null;
		boolean ponFila = false;
		boolean inser = false;

		boolean compX = a.getXPos() != b.getXPos();
		boolean compY = a.getYPos() != b.getYPos();

		if (compX && compY) { // Si es todo diferente, lo que toque
			ponFila = !ultFilas;
		} else {
			ponFila = !compX;
		}

		inser = compX || compY;

		if (inser && !ponFila && this.xSize < this.maxXSize) { // insert column
			int insertPos = Math.max(a.getXPos(), b.getXPos());

			xSize++;
			newUnits = new LFSUnit[xSize][ySize];
			for (int i = 0; i < xSize; i++) {
				for (int j = 0; j < ySize; j++) {
					if (i < insertPos) {
						newUnits[i][j] = units[i][j];
					} else if (i == insertPos) {
						try {
							newUnits[i][j] = new LFSUnit(i, j,
									LFSL2Metric.meanVector(
											units[i - 1][j].getWeightVector(),
											units[i][j].getWeightVector()));
						} catch (Exception e) {
							Logger.getLogger("at.tuwien.ifs.somtoolbox")
									.severe(e.getMessage());
							System.exit(-1);
						}
					} else if (i > insertPos) {
						newUnits[i][j] = units[i - 1][j];
						newUnits[i][j].updatePosition(i, j);
					}
				}
			}

			units = newUnits;
			ultFilas = false;
		}

		if (inser && ponFila && this.ySize < this.maxYSize) { // insert row
			int insertPos = Math.max(a.getYPos(), b.getYPos());

			ySize++;

			newUnits = new LFSUnit[xSize][ySize];

			for (int j = ySize - 1; j >= 0; j--) {
				for (int i = 0; i < xSize; i++) {
					if (j < insertPos) {
						newUnits[i][j] = units[i][j];
					} else if (j == insertPos) {
						try {
							newUnits[i][j] = new LFSUnit(i, j,
									LFSL2Metric.meanVector(
											units[i][j - 1].getWeightVector(),
											units[i][j].getWeightVector()));
						} catch (Exception e) {
							e.printStackTrace();
							Logger.getLogger("at.tuwien.ifs.somtoolbox")
									.severe(e.getMessage());
							System.exit(-1);
						}
					} else if (j > insertPos) {
						newUnits[i][j] = units[i][j - 1];
						newUnits[i][j].updatePosition(i, j);

					}
				}
			}

			units = newUnits;
			ultFilas = true;
		}
	}

	/**
	 * Maps data onto layer without recalculating the quantization error after
	 * every single input datum.
	 * 
	 * @param data
	 *            input data to be mapped onto layer.
	 */
	public void mapCompleteDataAfterTraining(LFSData data) {

		mapCompleteDataAfterTraining(data, false);
	}

	/**
	 * Maps all sample InputDatum into cells
	 * 
	 * @param data
	 * @param Calc
	 */
	private void mapCompleteDataAfterTraining(LFSData data, boolean Calc) {

		LFSInputDatum datum = null;
		LFSUnit winner = null;
		int numVectors = data.numVectors();
		for (int x = 0; x < xSize; x++) {
			for (int y = 0; y < ySize; y++) {
				try {
					this.getUnit(x, y).clearMappedInput();
				} catch (LFSException e) {
					e.printStackTrace();
				}
			}
		}

		for (int i = 0; i < numVectors; i++) {
			datum = data.getInputDatum(i);
			winner = getWinner(datum);
			winner.addMappedInput(datum, Calc);
		}
	}

	/**
	 * Only maps a portion of sample to save time
	 * 
	 * @param data
	 * @param n
	 */
	private void mapSomeDataAfterTraining(LFSData data, int n) {
		LFSInputDatum datum = null;
		LFSUnit winner = null;
		int numVectors = data.numVectors();
		for (int x = 0; x < xSize; x++) {
			for (int y = 0; y < ySize; y++) {
				try {
					this.getUnit(x, y).clearMappedInput();
				} catch (LFSException e) {
					e.printStackTrace();
				}
			}
		}

		for (int i = 0; i < n; i++) {
			datum = data.getInputDatum(rand.nextInt(numVectors));
			winner = getWinner(datum);
			winner.addMappedInput(datum, true);
		}

	}

	/**
	 * Calculate quantization error
	 * 
	 * @param data
	 * @return
	 */
	private double calcQualityQError(LFSData data) {
		double QErr;

		try {
			this.setQError(new LFSQuantizationError(this, data));
			QErr = this.getQError().getMapQuality("mqe");
			return QErr;

		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
					"Could not instantiate quality measure.");
			System.exit(-1);
		}
		return 0;

	}

	/**
	 * Calculate the three quality measures
	 * 
	 * @param data
	 */
	public void calcQuality(LFSData data) {

		try {
			this.setQError(new LFSQuantizationError(this, data));
			this.setTError(new LFSTopographicError(this, data));
			this.setKError(new LFSKLDistance(this, data));
			// calcKTopo(data);
		} catch (Exception e) {
			Logger.getLogger("lfsom").severe(
					"Could not instantiate quality measure.");
			System.exit(-1);
		}
	}

	/**
	 * Get a list of units which have to generate new nets according to a given
	 * quality
	 * 
	 * @param qm
	 * @param qmName
	 * @param fraction
	 * @param totalQuality
	 * @param minDatosExp
	 * @param numTotalDat
	 * @return
	 */
	public ArrayList<LFSUnit> getExpandedUnits(LFSQualityMeasure qm,
			String qmName, double fraction, double totalQuality,
			int minDatosExp, long numTotalDat) {

		ArrayList<LFSUnit> ExpUnits = new ArrayList<LFSUnit>();
		double[][] quality = null;
		try {
			quality = qm.getUnitQualities(qmName);
		} catch (LFSException e) {
			Logger.getLogger("lfsom").severe(e.getMessage() + " Aborting.");
			System.exit(-1);
		}
		try {
			for (int j = 0; j < this.getYSize(); j++) {
				for (int i = 0; i < this.getXSize(); i++) {
					boolean willExpand = quality[i][j] > fraction
							* totalQuality
							&& this.getUnit(i, j).getNumberOfMappedInputs() > minDatosExp;
					if (willExpand) {
						ExpUnits.add(this.getUnit(i, j));
					}
				}
			}
		} catch (LFSException e) { /* does not happen */
			e.printStackTrace();
		}
		return ExpUnits;
	}

	/**
	 * Growing control. Gets the unit with worst error and inserts a row or a
	 * column between it and its most dissimilar unit
	 * 
	 * @param props
	 * @param datos1
	 * @param i
	 * @param numIterations
	 * @return
	 */

	private boolean controlTamanyo(LFSSOMProperties props, LFSData datos1,
			int i, int numIterations) {

		boolean okCalc = false;

		try {

			if (this.xSize < this.maxXSize || this.ySize < this.maxYSize) {
				boolean hazCol = false;
				boolean hazFil = false;
				if (this.xSize < 2 * this.ySize) {
					hazCol = true;
				}
				if (this.ySize < 2 * this.xSize) {
					hazFil = true;
				}

				if (this.ySize < 5 && this.xSize < 5) {
					hazCol = false;
					hazFil = false;
				}

				LFSUnit d = null;
				if (hazCol) {
					if (this.unitmaxQe.getXPos() > 0) {
						d = this.getUnit(this.unitmaxQe.getXPos() - 1,
								this.unitmaxQe.getYPos());
					} else {
						d = this.getUnit(1, this.unitmaxQe.getYPos());
					}
				} else if (hazFil) {
					if (this.unitmaxQe.getYPos() > 0) {
						d = this.getUnit(this.unitmaxQe.getXPos(),
								this.unitmaxQe.getYPos() - 1);
					} else {
						d = this.getUnit(this.unitmaxQe.getXPos(), 1);
					}
				} else {
					d = this.getMostDissimilarNeighbor(this.unitmaxQe);
				}
				this.insertRowColumn(this.unitmaxQe, d, props.learnrate(),
						props.sigma());
				okCalc = true;
				props.setXYSize(this.xSize, this.ySize);
			}

		} catch (Exception e1) {

			e1.printStackTrace();
		}
		return okCalc;
	}

	/**
	 * Main training bucle.
	 * 
	 * @param data
	 * @param nIterations
	 * @param startIteration
	 * @param trainingProps
	 * @param initialLearnrate
	 * @param initialSigma
	 */
	public void trainNormal(LFSData data, int nIterations, int startIteration,
			LFSSOMProperties trainingProps, double initialLearnrate,
			double initialSigma) {

		double expParam = nIterations / 5.0;

		int numIterations = nIterations;
		double currentLearnrate = initialLearnrate;

		float iniPcNeighWidth = trainingProps.pcNeighbourWidth();

		double sigmaAct = initialSigma;

		int i = startIteration;
		int controlI = 400; // Control every controlI iterations
		int ultControl = i;

		while (i < numIterations && !qualityReached) {

			if (trainingProps.batchSom()) {
				// If it is batch, only map data into units
				almacenaBatch(data, numIterations, i, trainingProps);
			} else {
				trainFinalOnline(i, numIterations, expParam, trainingProps);
			}

			boolean hazControl = false;

			// The control adapts according to net size.
			if (i > 0 && (i - ultControl) % controlI == 0) {
				hazControl = true;
				ultControl = i;
				if (controlI < this.xSize * this.ySize * 1.2) {
					controlI = (int) (this.xSize * this.ySize * 1.2);
				}
			}

			if (trainingProps.batchSom() && (hazControl || i == numIterations)) {
				// Applies modifications to units depending on their mapped data
				finalBatch(data, i, trainingProps);
			}

			boolean calcNuevoNeigh = false;

			// learnRate modification. Doesn't apply to growing
			if (hazControl && !trainingProps.isGrowing()) {
				double nLrate = initialLearnrate
						* Math.exp(-1.0 * i / expParam);
				if (nLrate < 0.01) {
					nLrate = 0.01;
				}
				if (Math.round(nLrate * 100.0) != Math
						.round(currentLearnrate * 100.0)) {
					currentLearnrate = nLrate; // exponential
					calcNuevoNeigh = true;
				}
			}

			// neighbourwidth decay, except for gauss
			if (trainingProps.getNeighbourFunc() != LFSGrowingLayer.NEIGH_GAUSS
					&& hazControl) {
				int neighDecay = 25;
				if (trainingProps.isGrowing()) {
					neighDecay = 1;
				}

				float nPcWidthAct = (float) (iniPcNeighWidth * Math.exp(-1.0
						* i / (numIterations / neighDecay)));
				int nWidthAct = (int) ((this.xSize > this.ySize ? this.xSize
						: this.ySize) * nPcWidthAct);
				if (nWidthAct < 1) {
					nWidthAct = 1;
				}

				if (nWidthAct != neighbourWidth) {
					neighbourWidth = nWidthAct;
					calcNuevoNeigh = true;
				}
			}

			if (calcNuevoNeigh) {
				// If it have been modifications in parameters, recalculate
				// kernel
				calcDistancesNEIGH(this.xSize, this.ySize, currentLearnrate,
						sigmaAct);
			}

			// Call to grow and hierarchical
			if (trainingProps.isHier() && hazControl) {
				growAndHier(i, numIterations, trainingProps);
			}

			// Size control for growing.
			// In batch case, always has to concur with "finalBatch"
			if (hazControl && !qualityReached
					&& (trainingProps.isGrowing() || trainingProps.isHier())
					&& i < 0.85 * numIterations) {
				compruebaErrUnits();
				boolean okCalc = controlTamanyo(trainingProps, data, i,
						numIterations);
				if (okCalc) {
					calcDistancesNEIGH_add(this.xSize, this.ySize,
							currentLearnrate, sigmaAct);
				}

			}

			if (i == numIterations) {
				qualityReached = true;
			}

			i++;

		}
		clearMappedInput();

	}

	// Map data in batch training
	private void almacenaBatch(LFSData data, int numIterations, int i,
			LFSSOMProperties trainingProps) {

		LFSInputDatum currentInput = data.getRandomInputDatum();
		LFSUnit winner = getWinner(currentInput);
		int posRango = numIterations / this.nRangos;
		int nRango = i / posRango;
		if (nRango >= this.nRangos) {
			nRango = nRangos - 1;
		}
		int xPos = winner.getXPos();
		int yPos = winner.getYPos();

		for (int r = 0; r < ySize; r++) {
			for (int l = 0; l < xSize; l++) {
				units[l][r].addBatchSomNeighbour(currentInput,
						getDistNeigh()[nRango][distancesHex[xPos][yPos][l][r]]);
			}
		}
	}

	// Applies modifications according to mapped data
	private void finalBatch(LFSData data, int i, LFSSOMProperties trainingProps) {

		for (int z = 0; z < ySize; z++) {
			for (int l = 0; l < xSize; l++) {
				units[l][z].getWeightVectorFromBatchSomNeighbourhood();
				units[l][z].clearBatchSomList();
			}
		}

	}

	// Train on-line mode.
	private void trainFinalOnline(int i, int numIterations, double expParam,
			LFSSOMProperties trainingProps) {
		// get new input
		LFSInputDatum currentInput = data.getRandomInputDatum();

		// get winner & update weight vectors
		final LFSUnit winner = getWinner(currentInput);

		int rango = (int) Math.floor(i / Math.ceil(numIterations / nRangos));
		if (rango >= nRangos) {
			rango = nRangos - 1;
		}

		updateUnits(winner, currentInput, rango);

	}

	// Verify err of all units
	private void compruebaErrUnits() {
		this.maxQe = 0;
		this.unitmaxQe = units[0][0];
		for (int j = 0; j < ySize; j++) {
			for (int l = 0; l < xSize; l++) {
				double err = units[l][j].getQError();

				if (err >= this.maxQe) {
					this.maxQe = err;
					this.unitmaxQe = units[l][j];
				}
			}
		}
	}

	// Common steps for Growing and Hierarchical
	private void growAndHier(int i, int numIterations,
			LFSSOMProperties trainingProps) {

		mapSomeDataAfterTraining(data, data.numVectors() / 20);
		this.calcQualityQError(data);
		QError = this.getQError();// ualityMeasure("QError");
		try {

			double errActualQuan = QError.getMapQuality("mqe");
			double compru = trainingProps.tau * trainingProps.qRef;
			qualityReached = errActualQuan < compru;

		} catch (LFSException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Updates the weight vectors of the all map units with respect to the input
	 * datum and the according winner unit.
	 * 
	 * @param winner
	 * @param input
	 * @param rango
	 */
	private void updateUnits(LFSUnit winner, LFSInputDatum input, int rango) {
		double[] inputVector = input.getVector().toArray();

		updateUnitsInArea(winner, inputVector, 0, xSize, 0, ySize, rango);

	}

	private void updateUnitsInArea(LFSUnit winner, double[] inputVector,
			int startX, int endX, int startY, int endY, int rango) {
		double[] unitVector = null;
		double hci = 0;
		for (int y = startY; y < endY; y++) {
			for (int x = startX; x < endX; x++) {
				// Euclidean metric on output layer

				hci = distNeigh[rango][distancesHex[winner.getXPos()][winner
						.getYPos()][x][y]];

				unitVector = units[x][y].getWeightVector();

				for (int ve = 0; ve < units[x][y].getWeightVector().length; ve++) {
					if (!Double.isNaN(unitVector[ve])) { // skip updating of
															// missing
															// values
						unitVector[ve] += hci
								* (inputVector[ve] - unitVector[ve]);
					}
				}
			}
		}

	}

	public LFSData getData() {
		return data;
	}

	/**
	 * Get direct neighbours of the given unit. Direct neighbours are neighbours
	 * at a distance of 1 of the SOM
	 */
	private ArrayList<LFSUnit> getNeighbouringUnits(LFSUnit u)
			throws LFSException {
		return getNeighbouringUnits(u.getXPos(), u.getYPos(), 1.0);
	}

	private ArrayList<LFSUnit> getNeighbouringUnits(int x, int y, double radius)
			throws LFSException {
		ArrayList<LFSUnit> neighbourUnits = new ArrayList<LFSUnit>();

		int rad = (int) Math.ceil(radius);

		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) {
				if (distancesHex[x][y][i][j] <= rad) {
					neighbourUnits.add(getUnit(i, j));
				}
			}
		}

		return neighbourUnits;
	}

	public int getNumberOfMappedInputs() {
		int count = 0;
		for (int j = 0; j < ySize; j++) {
			for (int i = 0; i < xSize; i++) {
				count += units[i][j].getNumberOfMappedInputs();
			}
		}

		return count;
	}

	/**
	 * Clone might not be fully functional!
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		LFSGrowingLayer newLayer = new LFSGrowingLayer(this.xSize, this.ySize,
				0, this.getgSOM());
		// newLayer.units = this.units.clone();
		newLayer.units = new LFSUnit[this.xSize][this.ySize];
		for (int x = 0; x < this.xSize; x++) {
			for (int y = 0; y < this.ySize; y++) {
				newLayer.units[x][y] = new LFSUnit(x, y,
						this.units[x][y].getWeightVector());
			}
		}

		newLayer.QError = this.QError;
		newLayer.setDistancesHex(this.distancesHex);
		newLayer.setData(this.data);
		newLayer.setDistNeigh(this.distNeigh);
		newLayer.setNRangos(this.nRangos);

		return newLayer;
	}

	public void setNRangos(int nr) {
		this.nRangos = nr;
	}

	public void setData(LFSData datos) {
		this.data = datos;
	}

	public void setDistancesHex(int[][][][] dist) {
		this.distancesHex = dist;
	}

	public void setError(String strError) {
		String[] strbl = strError.split(" ");
		this.setQError(new LFSQuantizationError(this, strbl[0]));
		this.setTError(new LFSTopographicError(this, strbl[1]));
		this.setKError(new LFSKLDistance(this, strbl[2]));
	}

	/**
	 * @return Returns the qError.
	 */
	public LFSQualityMeasure getQError() {
		return QError;
	}

	/**
	 * @param qError
	 *            The qError to set.
	 */
	public void setQError(LFSQualityMeasure qError) {
		QError = qError;
	}

	/**
	 * @return Returns the gSOM.
	 */
	public LFSGrowingSOM getgSOM() {
		return gSOM;
	}

	/**
	 * @param gSOM
	 *            The gSOM to set.
	 */
	private void setgSOM(LFSGrowingSOM gSOM) {
		this.gSOM = gSOM;
	}

	/**
	 * Get max number of clusters
	 * 
	 * @return
	 */
	public int getNClusterMax() {
		int num = 0;
		int[] labelAgrupados = this.getgSOM().getLabelAgrupados();
		for (int labelAgrupado : labelAgrupados) {
			if (num < labelAgrupado) {
				num = labelAgrupado;
			}
		}
		return num;
	}

	/**
	 * Get number of row data mapped to units of a cluster
	 * 
	 * @param i
	 * @return
	 */
	public int getNDatosCluster(int i) {
		int ndatos = 0;

		ArrayList<LFSUnit> ExpUnits = getUnitsCluster(i);

		for (int w = 0; w < ExpUnits.size(); w++) {
			ndatos += ExpUnits.get(w).getNumberOfMappedInputs();
		}
		return ndatos;
	}

	/**
	 * Get the units belonging to a cluster
	 * 
	 * @param r
	 * @return
	 */
	private ArrayList<LFSUnit> getUnitsCluster(int r) {

		ArrayList<Integer> listaCells = getLabelCluster(r);
		ArrayList<LFSUnit> ExpUnits = new ArrayList<LFSUnit>();

		for (int i = 0; i < listaCells.size(); i++) {
			int filaac = (int) Math.floor(listaCells.get(i) / xSize);
			int colac = listaCells.get(i) % xSize;
			try {
				ExpUnits.add(this.getUnit(colac, filaac));
			} catch (LFSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return ExpUnits;
	}

	/**
	 * Get list of indexes of units belonging to a cluster
	 * 
	 * @param z
	 * @return
	 */
	public ArrayList<Integer> getLabelCluster(int z) {

		ArrayList<Integer> arrl = new ArrayList<Integer>();
		int[] labelAgrupados = this.getgSOM().getLabelAgrupados();
		for (int k = 0; k < labelAgrupados.length; k++) {
			if (labelAgrupados[k] == z) {
				arrl.add(k);
			}
		}
		HexMapDistancer HexMap = new HexMapDistancer(this.getYSize(),
				this.getXSize(), true);
		return HexMap.traspon(arrl);
	}

	/**
	 * Get mean quantization error of a cluster
	 * 
	 * @param z
	 * @return
	 */
	public Double getMqeCluster(int z) {

		Double mquality = (double) 0;
		ArrayList<LFSUnit> ExpUnits = this.getUnitsCluster(z);

		LFSQualityMeasure qm = getQualityMeasure("QError");
		double[][] quality = null;
		try {
			quality = qm.getUnitQualities("mqe");
		} catch (LFSException e) {
			Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
					e.getMessage() + " Aborting.");
			System.exit(-1);
		}

		int unitsNoValen = 0;
		for (int w = 0; w < ExpUnits.size(); w++) {
			if (ExpUnits.get(w).getNumberOfMappedInputs() > 0) {
				mquality += quality[ExpUnits.get(w).getXPos()][ExpUnits.get(w)
						.getYPos()];
			} else {
				unitsNoValen++;
			}
		}

		mquality = mquality / (ExpUnits.size() - unitsNoValen);
		return mquality;
	}

	/**
	 * @return Returns the distNeigh.
	 */
	private double[][] getDistNeigh() {
		return distNeigh;
	}

	/**
	 * @param distNeigh
	 *            The distNeigh to set.
	 */
	private void setDistNeigh(double[][] distNeigh) {
		this.distNeigh = distNeigh;
	}

	/**
	 * @return the tError
	 */
	private LFSQualityMeasure getTError() {
		return TError;
	}

	/**
	 * @param tError
	 *            the tError to set
	 */
	private void setTError(LFSTopographicError tError) {
		TError = tError;
	}

	/**
	 * @return the kError
	 */
	private LFSQualityMeasure getKError() {
		return KError;
	}

	/**
	 * @param kError
	 *            the kError to set
	 */
	private void setKError(LFSQualityMeasure kError) {
		KError = kError;
	}

}
