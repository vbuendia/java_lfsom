/*
 * 
 * This class is a specialized class of "TopographicError" class from:
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
package lfsom.layers.quality;

import lfsom.data.LFSData;
import lfsom.layers.LFSGrowingLayer;
import lfsom.layers.LFSUnit;
import lfsom.layers.metrics.HexMapDistancer;
import lfsom.util.LFSException;

/**
 * Implementation of Topographic Error Quality Measure.<br>
 * 
 * @author Gerd Platzgummer
 * @version $Id: TopographicError.java 3883 2010-11-02 17:13:23Z frank $
 */
public class LFSTopographicError implements LFSQualityMeasure {

	private double averageError = 0.0;

	// Error of each unit
	private double[][] unitError;

	// Set error
	public LFSTopographicError(LFSGrowingLayer layer, String errorInit) {
		this.averageError = Double.valueOf(errorInit);
	}

	/**
	 * Calculate topographic error
	 * 
	 * @param layer
	 * @param data
	 */
	public LFSTopographicError(LFSGrowingLayer layer, LFSData data) {

		int xSize = layer.getXSize();
		int ySize = layer.getYSize();

		int numVectors = data.numVectors();
		double sum = 0.0;
		if (numVectors > 6000) {
			numVectors = 6000;
		}

		HexMapDistancer distan = new HexMapDistancer(xSize, ySize, true);
		int[][][][] distancias = distan.map();

		for (int d = 0; d < numVectors; d++) {

			LFSUnit[] winners = layer.getWinners(data.getRandomInputDatum(), 2);

			LFSUnit bmu = winners[0];
			LFSUnit sbmu = winners[1];

			if (distancias[bmu.getXPos()][bmu.getYPos()][sbmu.getXPos()][sbmu
					.getYPos()] > 1) {
				sum++;
			}

		}
		averageError = sum / numVectors;

	}

	@Override
	public double getMapQuality(String name) throws LFSException {
		if (name.equals("TE_Map")) {
			return averageError;
		} else {
			throw new LFSException("Quality measure with name " + name
					+ " not found.");
		}
	}

	@Override
	public double[][] getUnitQualities(String name) throws LFSException {
		if (name.equals("TE_Unit")) {
			return unitError;
		} else {
			throw new LFSException("Quality measure with name " + name
					+ " not found.");
		}
	}

}
