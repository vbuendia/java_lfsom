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

	public static final int INIT_RANDOM = 10;

	public static final int INIT_INTERVAL_INTERPOLATE = 20;

	public static final int INIT_VECTOR = 30;

	public static final int INIT_PCA = 40;

	private int dim = 0;

	private LFSGrowingLayer layer = null;

	private double quantizationError = 0;

	private double[] weightVector = null;

	private int xPos = -1;

	private int yPos = -1;

	private ArrayList<LFSInputDatum> batchSomNeighbourhood;

	private ArrayList<Double> batchPonderaciones;

	/**
	 * Constructs a <code>Unit</code> on <code>Layer</code> specified by
	 * argument <code>layer</code> at position <code>x</code>/<code>y</code>
	 * with a given weight vector <code>vec</code>.
	 * 
	 * @param l
	 *            the layer that contains this <code>Unit</code>.
	 * @param x
	 *            the horizontal position on the <code>layer</code>.
	 * @param y
	 *            the vertical position on the <code>layer</code>.
	 * @param z
	 *            the depth position on the <code>layer</code>.
	 * @param vec
	 *            the weight vector.
	 */
	public LFSUnit(LFSGrowingLayer l, int x, int y, double[] vec) {
		layer = l;
		xPos = x;
		yPos = y;
		weightVector = vec;
		if (vec != null) {
			dim = vec.length;
		}

		batchSomNeighbourhood = new ArrayList<LFSInputDatum>();
		batchPonderaciones = new ArrayList<Double>();
	}

	public double getQError() {
		return quantizationError;
	}

	public LFSUnit(LFSData data, int x, int y, int d, Random rand,
			boolean norm, int initialisationMode) {
		layer = null;
		xPos = x;
		yPos = y;
		dim = d;
		batchSomNeighbourhood = new ArrayList<LFSInputDatum>();
		batchPonderaciones = new ArrayList<Double>();
		weightVector = new double[dim];
		if (initialisationMode == INIT_RANDOM) {
			for (int i = 0; i < dim; i++) {
				weightVector[i] = rand.nextDouble();
			}
		} else if (initialisationMode == INIT_INTERVAL_INTERPOLATE) {
			for (int i = 0; i < dim; i++) {
				double r = rand.nextDouble();
				double[][] intervals = data.getDataIntervals();
				weightVector[i] = intervals[i][0]
						+ (intervals[i][1] - intervals[i][0]) * r;
			}
		} else if (initialisationMode == INIT_VECTOR) {
			double r = rand.nextDouble();
			int index = (int) (data.numVectors() * r);
			weightVector = data.getInputDatum(index).getVector().toArray();

		}
		if (norm) {
			VectorTools.normaliseVectorToUnitLength(weightVector);
		}
	}

	LFSUnit(LFSGrowingLayer l, int x, int y, int d, Random rand, boolean norm,
			int initialisationMode) {
		layer = l;
		xPos = x;
		yPos = y;
		dim = d;
		batchSomNeighbourhood = new ArrayList<LFSInputDatum>();
		batchPonderaciones = new ArrayList<Double>();
		weightVector = new double[dim];
		if (initialisationMode == INIT_RANDOM) {
			for (int i = 0; i < dim; i++) {
				weightVector[i] = rand.nextDouble();
			}
		} else if (initialisationMode == INIT_INTERVAL_INTERPOLATE) {
			for (int i = 0; i < dim; i++) {
				double r = rand.nextDouble();
				double[][] intervals = ((LFSGrowingLayer) l).getData()
						.getDataIntervals();
				weightVector[i] = intervals[i][0]
						+ (intervals[i][1] - intervals[i][0]) * r;
			}
		} else if (initialisationMode == INIT_VECTOR) {
			double r = rand.nextDouble();
			int index = (int) (((LFSGrowingLayer) l).getData().numVectors() * r);
			weightVector = ((LFSGrowingLayer) l).getData().getInputDatum(index)
					.getVector().toArray();

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
			double dist = LFSL2Metric.distance(datum.getVector(),
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

	@Override
	public void removeMappedInput(String label) {
		super.removeMappedInput(label);
		calculateQuantizationError();
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
	 * Returns the layer of units this unit is part of.
	 * 
	 * @return the layer of units this unit is part of.
	 */
	public LFSGrowingLayer getLayer() {
		return layer;
	}

	/**
	 * Returns the width of this unit's map.
	 * 
	 * @return the width of this unit's map.
	 */
	public int getMapXSize() {
		return layer.getXSize();
	}

	/**
	 * Returns the height of this unit's map.
	 * 
	 * @return the height of this unit's map.
	 */
	public int getMapYSize() {
		return layer.getYSize();
	}

	/**
	 * Calculates and returns the mean quantization error of this unit. This is
	 * 0, if no input is mapped onto this unit.
	 * 
	 * @return the mean quantization error for this unit.
	 */
	/*
	 * public double getMeanQuantizationError() { if
	 * (mappedInputs.getNumberOfMappedInputs()>0) { return
	 * (quantizationError/mappedInputs.getNumberOfMappedInputs()); } else {
	 * return 0; } }
	 */

	/**
	 * Returns the quantization error of this unit.
	 * 
	 * @return the quantization error of this unit.
	 */
	/*
	 * public double getQuantizationError() { return quantizationError; }
	 */

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

	/**
	 * Sets the coordinates of this unit on the map, if they have changed. This
	 * happens in architectures with growing map sizes during training.
	 * 
	 * @param x
	 *            the horizontal position on the map.
	 * @param y
	 *            the vertical position on the map.
	 * @param z
	 *            the height position on the map.
	 */
	void updatePosition(int x, int y) {
		xPos = x;
		yPos = y;
	}

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

	void getWeightVectorFromBatchSomNeighbourhood() {

		double acum = 0;

		// Primero se mira si tiene ponderaciones
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

	@Override
	public String toString() {
		return "Unit[" + printCoordinates() + "]";
	}

	private String printCoordinates() {
		return xPos + "/" + yPos;
	}

	public int getDim() {
		return dim;
	}

	public boolean isTopLeftUnit() {
		return xPos == 0 && yPos == 0;
	}

}
