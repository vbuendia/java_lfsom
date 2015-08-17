/*
 * 
 * This class is a variation of class of "IntrinsicDistance" class from:
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
 * Implementation of Intrinsic Distance Quality
 * 
 * @author Gerd Platzgummer
 * @version $Id: IntrinsicDistance.java 3883 2010-11-02 17:13:23Z frank $
 * 
 *          Modified for LFS by Vicente Buendia
 */
public class LFSKLDistance implements LFSQualityMeasure {

	// Whole map quality
	private double Map_Q = 0.0;

	// Quality previously assigned
	public LFSKLDistance(LFSGrowingLayer layer, String nmap) {
		Map_Q = Double.valueOf(nmap);
	}

	/**
	 * Calculate KL Index
	 * 
	 * @param layer
	 * @param data
	 */
	public LFSKLDistance(LFSGrowingLayer layer, LFSData data) {

		int xSize1 = layer.getXSize();
		int ySize1 = layer.getYSize();

		/** *********Sum1: Quantization Error */

		try {
			Map_Q = new LFSQuantizationError(layer, data).getMapQuality("mqe");
		} catch (LFSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/** *********Sum 2: Shortest path between BMU2 and BMU2 */

		HexMapDistancer distan = new HexMapDistancer(xSize1, ySize1, true);
		int[][][][] distancias = distan.map();

		int samplecount = data.numVectors();

		for (int s = 0; s < samplecount; s++) {
			LFSUnit[] winners = ((LFSGrowingLayer) layer).getWinners(
					data.getInputDatum(s), 2);

			LFSUnit bmu = winners[0];
			LFSUnit sbmu = winners[1];

			Map_Q += distancias[bmu.getXPos()][bmu.getYPos()][sbmu.getXPos()][sbmu
					.getYPos()];

		}

		Map_Q = Map_Q / samplecount;

	}

	/**
	 * Get map quality
	 */

	public double getMapQuality(String name) throws LFSException {
		if (name.equals("ID_Map")) {
			return Map_Q;
		} else {
			throw new LFSException("Quality measure with name " + name
					+ " not found.");
		}
	}

	/**
	 * Compatibility function
	 */

	public double[][] getUnitQualities(String name) throws LFSException {

		throw new LFSException("UnitQualities not implemented for KLDistance.");

	}

}
