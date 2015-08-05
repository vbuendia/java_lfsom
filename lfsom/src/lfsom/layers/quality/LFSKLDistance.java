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
 */
public class LFSKLDistance implements LFSQualityMeasure {

	private double[][] Unit_ID;

	private double Map_ID = 0.0;

	public LFSKLDistance(LFSGrowingLayer layer, String nmap) {
		Map_ID = Double.valueOf(nmap);
	}

	public LFSKLDistance(LFSGrowingLayer layer, LFSData data) {

		int xSize1 = layer.getXSize();
		int ySize1 = layer.getYSize();

		/** *********Summand 1: Aequivalent zum (einfachen) Quantization Error */

		try {
			Map_ID = new LFSQuantizationError(layer, data).getMapQuality("mqe");
		} catch (LFSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/** *********Summand 2: Dist BMU 2ndBMU ueber den shortest path */

		HexMapDistancer distan = new HexMapDistancer(xSize1, ySize1, true);
		int[][][][] distancias = distan.map();

		int samplecount = data.numVectors();

		for (int s = 0; s < samplecount; s++) {
			LFSUnit[] winners = ((LFSGrowingLayer) layer).getWinners(
					data.getInputDatum(s), 2);

			LFSUnit bmu = winners[0];
			LFSUnit sbmu = winners[1];

			Map_ID += distancias[bmu.getXPos()][bmu.getYPos()][sbmu.getXPos()][sbmu
					.getYPos()];

		}

		Map_ID = Map_ID / samplecount; // oder nonEmpty: Units mit assoziierten
										// Samples

	}

	/**
	 * *************************************************************************
	 * **********************************
	 */
	/*
	 * Ausgabe: ID_Sample-(nicht ID_Unit!)- Werte, Durchschnitt
	 */

	public double getMapQuality(String name) throws LFSException {
		if (name.equals("ID_Map")) {
			return Map_ID;
		} else {
			throw new LFSException("Quality measure with name " + name
					+ " not found.");
		}
	}

	/*
	 * Ausgabe: ID_Sample- Werte, Mapping auf die entsprechende Unit
	 */

	public double[][] getUnitQualities(String name) throws LFSException {
		if (name.equals("ID_Unit")) {
			return Unit_ID;
		} else {
			throw new LFSException("Quality measure with name " + name
					+ " not found.");
		}
	}

}
