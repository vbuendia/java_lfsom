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

	// Link to SOM it belongs
	private LFSGrowingSOM gSOM;

	// Data to train
	private LFSData data;

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

	private LFSGrowingLayer(int xSize, int ySize, long seed,
			LFSGrowingSOM growSOM) {

		rand = new Random(seed);
		new Random();
		this.xSize = xSize;
		this.ySize = ySize;
		this.setgSOM(growSOM);
	}

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
					units[i][j] = new LFSUnit(this, i, j,
							unitsAssign[i][j].getWeightVectorVal());
				}
			}
		} else {

			for (int j = 0; j < this.ySize; j++) {
				for (int i = 0; i < this.xSize; i++) {
					units[i][j] = new LFSUnit(this, i, j, data.dim(), rand,
							normalized, initialisationMode);
				}
			}
		}

	}

	/**
	 * Load a list of values into weightvector of a unit
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
				if (units[i][j].getMappedSOM() != null) {
					units[i][j].getMappedSOM().getLayer().clearMappedInput();
				}
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
	 * Calculates distances among units in a hexagonal grid
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
							units[i][j].getWeightVector(), vec);
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
							newUnits[i][j] = new LFSUnit(this, i, j,
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
							newUnits[i][j] = new LFSUnit(this, i, j,
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

	private void mapCompleteDataAfterTraining(LFSData data, boolean Calc) {

		LFSInputDatum datum = null;
		LFSUnit winner = null;
		int numVectors = data.numVectors();
		for (int x = 0; x < xSize; x++) {
			for (int y = 0; y < ySize; y++) {
				try {
					this.getUnit(x, y).clearMappedInput();
				} catch (LFSException e) {
					// TODO Auto-generated catch block
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

	// Lo mismo que el train pero para un mapa fijo y calculando varias medidas
	// de calidad

	private double calcQualityQError(LFSData data) {
		double QErr;

		// calc QualityMeasure
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

	public void calcQuality(LFSData data) {
		// calc QualityMeasure
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
							&& this.getUnit(i, j).getNumberOfMappedInputs() > minDatosExp;// ||
																							// this.getUnit(i,
																							// j).getNumberOfMappedInputs()
																							// >
																							// numTotalDat/(xSize*ySize);

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

	private boolean controlTamanyo(LFSSOMProperties props, LFSData datos1,
			int i, int numIterations) {
		// Ajuste de crecimiento o decrecimiento de la red
		// Si la red no cumple con una medida de calidad determinada,
		// insertar un nuevo nodo.

		boolean okCalc = false;

		try {

			if (this.xSize < this.maxXSize || this.ySize < this.maxYSize) {
				// grabaTMP();
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
				// grabaTMP();
				props.setXYSize(this.xSize, this.ySize);
			}

			// Borrado de filas y columnas poco representativas
			// okCalc = borrado(i, numIterations) || okCalc;

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return okCalc;
	}

	public void trainNormal(LFSData data, int nIterations, int startIteration,
			LFSSOMProperties trainingProps, double initialLearnrate,
			double initialSigma) {

		double expParam = nIterations / 5.0; // TODO: hidden parameter

		int iteraControl = data.numVectors() / 30; // Iteraciones para control
													// de tamaño (aumento o
													// disminucion)

		int numIterations = nIterations;
		double currentLearnrate = initialLearnrate;

		/*
		 * if (trainingProps.batchSom()) { numIterations = nRangos; }
		 */
		float iniPcNeighWidth = trainingProps.pcNeighbourWidth();

		double sigmaAct = initialSigma;

		float xmulti = (numIterations - startIteration) / 100;
		int i = startIteration;
		int controlI = 400;
		int ultControl = i;

		while (i < numIterations && !qualityReached) {

			if (trainingProps.batchSom()) {
				almacenaBatch(data, numIterations, i, iteraControl,
						trainingProps);
			} else {
				trainFinalOnline(i, numIterations, iteraControl, expParam,
						trainingProps);
			}

			boolean hazControl = false;
			if (i > 0 && (i - ultControl) % controlI == 0) {
				hazControl = true;
				ultControl = i;
				if (controlI < this.xSize * this.ySize * 1.2) {
					controlI = (int) (this.xSize * this.ySize * 1.2);
				}
			}

			if (trainingProps.batchSom() && (hazControl || i == numIterations)) {
				finalBatch(data, i, trainingProps);
			}

			boolean calcNuevoNeigh = false;
			// Control del learnRate para los no growing
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

			// Decay del neighbourwidth, para el no gauss
			if (trainingProps.getNeighbourFunc() != LFSGrowingLayer.NEIGH_GAUSS
					&& hazControl) {
				int neighDecay = 25;
				if (trainingProps.isGrowing()) {
					neighDecay = 1;
				}
				// Se modifica el neighwidth para ir afinando
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
				// Se pone por separado para evitar posibilidad de calcularlo 2
				// veces seguidas.
				calcDistancesNEIGH(this.xSize, this.ySize, currentLearnrate,
						sigmaAct);
			}

			if (trainingProps.isHier() && hazControl) {
				growAndHier(i, numIterations, trainingProps);
			}

			// Control del tamaño para el growing
			if (hazControl && !qualityReached
					&& (trainingProps.isGrowing() || trainingProps.isHier())
					&& i < 0.85 * numIterations) {
				// grabaTMP();
				// Ojo, para el caso del batch no puede ejecutarse a mitad. Debe
				// ejecutarse siempre justo despues de
				// algun finalBatch
				// pinch();
				compruebaErrUnits();
				boolean okCalc = controlTamanyo(trainingProps, data, i,
						numIterations);
				if (okCalc) {
					calcDistancesNEIGH_add(this.xSize, this.ySize,
							currentLearnrate, sigmaAct);
				}
				// grabaTMP();
			}

			if (i == numIterations) {
				qualityReached = true;
			}

			i++;

		}
		clearMappedInput();

	}

	private void almacenaBatch(LFSData data, int numIterations, int i,
			int iteraControl, LFSSOMProperties trainingProps) {

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

	private void finalBatch(LFSData data, int i, LFSSOMProperties trainingProps) {

		for (int z = 0; z < ySize; z++) {
			for (int l = 0; l < xSize; l++) {
				units[l][z].getWeightVectorFromBatchSomNeighbourhood();
				units[l][z].clearBatchSomList();
			}
		}

	}

	private void trainFinalOnline(int i, int numIterations, int iteraControl,
			double expParam, LFSSOMProperties trainingProps) {
		// get new input
		LFSInputDatum currentInput = data.getRandomInputDatum();

		// grabaTMP();
		// get winner & update weight vectors
		final LFSUnit winner = getWinner(currentInput);

		int rango = (int) Math.floor(i / Math.ceil(numIterations / nRangos));
		if (rango >= nRangos) {
			rango = nRangos - 1;
		}

		updateUnits(winner, currentInput, rango);

	}

	private void compruebaErr(LFSUnit U) {
		double err = U.getQError();

		if (err >= this.maxQe) {
			this.maxQe = err;
			this.unitmaxQe = U;
		}

	}

	private void compruebaErrUnits() {

		this.maxQe = 0;
		this.unitmaxQe = units[0][0];
		for (int j = 0; j < ySize; j++) {
			for (int l = 0; l < xSize; l++) {
				compruebaErr(units[l][j]);

			}
		}
	}

	private void growAndHier(int i, int numIterations,
			LFSSOMProperties trainingProps) {
		// Pasos comunes que se siguen en el caso de growing y en el de
		// jerarquico
		mapSomeDataAfterTraining(data, data.numVectors() / 20);
		this.calcQualityQError(data);
		QError = this.getQError();// ualityMeasure("QError");
		try {

			double errActualQuan = QError.getMapQuality("mqe");
			double compru = trainingProps.tau * trainingProps.qRef;
			qualityReached = errActualQuan < compru;

		} catch (LFSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Updates the weight vectors of the all map units with respect to the input
	 * datum and the according winner unit.
	 * 
	 * @param winner
	 *            the winner unit.
	 * @param input
	 *            the input datum.
	 * @param learnrate
	 *            the learnrate.
	 * @param sigma
	 *            the width of the Gaussian determining the neighborhood radius.
	 */
	private void updateUnits(LFSUnit winner, LFSInputDatum input, int rango) {
		double[] inputVector = input.getVector().toArray();

		updateUnitsInArea(winner, inputVector, 0, xSize, 0, ySize, rango);

	}

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
	 * in the same column or row of the SOM, thus this method returns at most
	 * six neighbours (two for each of the x, y and z dimensions).
	 */
	private ArrayList<LFSUnit> getNeighbouringUnits(LFSUnit u)
			throws LFSException {
		return getNeighbouringUnits(u.getXPos(), u.getYPos(), 1.0);
	}

	/**
	 * Convenience method for
	 * {@link #getNeighbouringUnits(int, int, int, double)}
	 */

	/**
	 * Gets neighbours within a certain radius; uses
	 * {@link #getMapDistance(int, int, int, int, int, int)} for map distance
	 * computation
	 */
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
				newLayer.units[x][y] = new LFSUnit(newLayer, x, y,
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

	public int getNDatosCluster(int i) {
		int ndatos = 0;

		ArrayList<LFSUnit> ExpUnits = getUnitsCluster(i);

		for (int w = 0; w < ExpUnits.size(); w++) {
			ndatos += ExpUnits.get(w).getNumberOfMappedInputs();
		}
		return ndatos;
	}

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

	public ArrayList<Integer> getLabelCluster(int z) {

		ArrayList<Integer> arrl = new ArrayList<Integer>();
		int[] labelAgrupados = this.getgSOM().getLabelAgrupados();
		for (int k = 0; k < labelAgrupados.length; k++) {
			if (labelAgrupados[k] == z) {
				arrl.add(k);
			}
		}
		HexMapDistancer HexMap = new HexMapDistancer(this.getYSize(),
				this.getXSize());
		return HexMap.traspon(arrl);
	}

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
