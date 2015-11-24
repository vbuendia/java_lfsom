/*
 * 
 * This class is a specialized class of "Unit" class from:
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

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import lfsom.data.LFSData;
import lfsom.data.LFSInputDatum;
import lfsom.layers.metrics.LFSL2Metric;
import lfsom.util.LFSException;
import at.tuwien.ifs.somtoolbox.layers.InputContainer;
import at.tuwien.ifs.somtoolbox.util.VectorTools;

public class LFSUnit extends InputContainer {

	// Allowed initializations
	public static final int INIT_RANDOM = 10;

	public static final int INIT_INTERVAL_INTERPOLATE = 20;

	public static final int INIT_VECTOR = 30;

	public static final int INIT_PCA = 40;

	// Dimmension of weightVector
	private int dim = 0;

	// Weightvector
	private double[] weightVector = null;

	// QuantizationError, as it's used for many checks
	private double quantizationError = 0;

	// Position x,y
	private int xPos = -1;

	private int yPos = -1;

	// Neighbour cells, and the weight the have
	private ArrayList<LFSInputDatum> batchSomNeighbourhood;

	private ArrayList<Double> batchPonderaciones;

	/**
	 * Constructs a <code>Unit</code> at position <code>x</code>/<code>y</code>
	 * with a given weight vector <code>vec</code>.
	 * 
	 * @param x
	 *            the horizontal position on the <code>layer</code>.
	 * @param y
	 *            the vertical position on the <code>layer</code>.
	 * 
	 * @param vec
	 *            the weight vector.
	 */
	public LFSUnit(int x, int y, double[] vec) {

		xPos = x;
		yPos = y;
		weightVector = vec;
		if (vec != null) {
			dim = vec.length;
		}

		batchSomNeighbourhood = new ArrayList<LFSInputDatum>();
		batchPonderaciones = new ArrayList<Double>();
	}

	/**
	 * Constructs a Unit at position x,y. The unit will have its weights
	 * depending on the initialization mode
	 * 
	 * @param data
	 * @param x
	 * @param y
	 * @param d
	 *            dimension
	 * @param rand
	 * @param norm
	 * @param initializationMode
	 */
	public LFSUnit(LFSData data, int x, int y, int d, Random rand,
			boolean norm, int initializationMode) {

		xPos = x;
		yPos = y;
		dim = d;
		batchSomNeighbourhood = new ArrayList<LFSInputDatum>();
		batchPonderaciones = new ArrayList<Double>();
		weightVector = new double[dim];
		if (initializationMode == INIT_RANDOM) {
			for (int i = 0; i < dim; i++) {
				weightVector[i] = rand.nextDouble();
			}
		} else if (initializationMode == INIT_INTERVAL_INTERPOLATE) {
			for (int i = 0; i < dim; i++) {
				double r = rand.nextDouble();
				double[][] intervals = data.getDataIntervals();
				weightVector[i] = intervals[i][0]
						+ (intervals[i][1] - intervals[i][0]) * r;
			}
		} else if (initializationMode == INIT_VECTOR) {
			double r = rand.nextDouble();
			int index = (int) (data.numVectors() * r);
			weightVector = data.getInputDatum(index).getVector().toArray();

		}
		if (norm) {
			VectorTools.normaliseVectorToUnitLength(weightVector);
		}
	}

	/**
	 * Adds a single input datum to the unit. The method also calculates the
	 * distance between the unit's weight vector and the datum.
	 * 
	 * @param datum
	 *            the input datum to be added.
	 * @param calcQE
	 *            determines if the quantization error should be recalculated.
	 * @see #addMappedInput(String, double, boolean)
	 */
	void addMappedInput(LFSInputDatum datum, boolean calcQE) {
		try {
			double dist = LFSL2Metric.distance(datum.getVector().toArray(),
					this.weightVector);
			addMappedInput(datum.getLabel(), dist, calcQE);
		} catch (LFSException e) {
			Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage());
			System.exit(-1); // TODO: EXCEPTION HANDLING!!
		}
	}

	/**
	 * Convenience method to add an input datum specified by its name and
	 * distance. The quantization error is recalculated if argument
	 * <code>calcQE</code> is <code>true</code>.
	 * 
	 * @param name
	 *            the name of the input datum.
	 * @param dist
	 *            the precalculated distance between input datum and weight
	 *            vector
	 * @param calcQE
	 *            determines if the quantization error should be recalculated.
	 */
	private void addMappedInput(String name, double dist, boolean calcQE) {
		super.addMappedInput(name, new Double(dist));
		if (calcQE) {
			calculateQuantizationError();
		}
	}

	/**
	 * Recalculates the quantization error for this unit.
	 */
	private void calculateQuantizationError() {
		quantizationError = 0;
		for (int i = 0; i < getNumberOfMappedInputs(); i++) {
			quantizationError += getMappedInputDistance(i);
		}
	}

	/**
	 * Removes the mapped input data and sets this units quantization error to
	 * 0.
	 */
	void clearMappedInput() {
		super.clearMappedInputs();
		quantizationError = 0;
	}

	/**
	 * Returns the weight vector of this unit.
	 * 
	 * @return the weight vector of this unit.
	 */
	public double[] getWeightVectorVal() {
		double[] wv = new double[weightVector.length];
		for (int k = 0; k < weightVector.length; k++) {
			wv[k] = weightVector[k];
		}
		return wv;

	}

	public double[] getWeightVector() {
		return weightVector;
	}

	/**
	 * Sets the weight vector of this unit.
	 * 
	 * @param vector
	 *            the weight vector.
	 */

	void setWeightVector(int pos, double vectorunit) throws LFSException {
		weightVector[pos] = vectorunit;
	}

	public void setWeightVector(double[] vector) throws LFSException {
		if (vector != null && vector.length == dim) {
			weightVector = vector;
		} else {
			throw new LFSException(
					"Vector is null or has wrong dimensionality.");
		}
	}

	/**
	 * Returns the horizontal position of this unit on the map it is part of.
	 * 
	 * @return the horizontal position of this unit on the map it is part of.
	 */
	public int getXPos() {
		return xPos;
	}

	/**
	 * Returns the vertical position of this unit on the map it is part of.
	 * 
	 * @return the vertical position of this unit on the map it is part of.
	 */
	public int getYPos() {
		return yPos;
	}

	public int getPos(int xSize) {
		return xPos + yPos * (xSize);
	}

	/**
	 * Sets the coordinates of this unit on the map, if they have changed. This
	 * happens in architectures with growing map sizes during training.
	 * 
	 * @param x
	 *            the horizontal position on the map.
	 * @param y
	 *            the vertical position on the map.
	 */
	void updatePosition(int x, int y) {
		xPos = x;
		yPos = y;
	}

	/**
	 * Adds a weighted value for batch calculations
	 * 
	 * @param d
	 * @param ponderacion
	 */
	void addBatchSomNeighbour(LFSInputDatum d, double ponderacion) {
		if (ponderacion > 0) {
			batchSomNeighbourhood.add(d);
			batchPonderaciones.add(ponderacion);
		}
	}

	void clearBatchSomList() {
		batchSomNeighbourhood.clear();
		batchPonderaciones.clear();
	}

	/**
	 * Get a weightvector as a weighted sum of all batch neoghbors
	 */
	void getWeightVectorFromBatchSomNeighbourhood() {

		double acum = 0;

		// First of all, let's see if there're weighted values
		for (int j = 0; j < batchSomNeighbourhood.size(); j++) {

			Double ponder = batchPonderaciones.get(j);

			if (ponder > 0) {
				acum += ponder;
			}
		}

		if (acum > 0) {

			for (int i = 0; i < weightVector.length; i++) {

				double meanValue = 0;

				for (int j = 0; j < batchSomNeighbourhood.size(); j++) {

					Double ponder = batchPonderaciones.get(j);

					if (ponder > 0) {
						meanValue += batchSomNeighbourhood.get(j).getVector()
								.get(i)
								* ponder;
					}
				}
				meanValue = (meanValue + weightVector[i]) / (acum + 1);
				weightVector[i] += meanValue - weightVector[i];

			}

		}

	}

	public double getQError() {
		return quantizationError;
	}

}
