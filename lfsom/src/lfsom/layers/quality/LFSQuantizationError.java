/*
 * 
 * This class is a specialized class of "QuantizationError" class from:
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
import lfsom.util.LFSException;

/**
 * Calculates the <i>Quantization Error</i>, defined as the average distance
 * between and input data vector and the weight-vector of its
 * best-matching-unit.<br>
 * Calculates the following values:
 * <ul>
 * <li>Unit
 * <ul>
 * <li>Quantisation error (qe). Calculated for each unit as the sum of the
 * distances between the input vectors mapped to this unit to the unit's weight
 * vector.</li>
 * <li>Mean quantisation error (mqe). Calculated for each unit as the
 * (qe/|mapped vectors|), i.e. the qe divided by the number of input data
 * vectors mapped on this unit.</li>
 * </ul>
 * </li>
 * <li>Map
 * <ul>
 * <li>Mean quantisation error (mqe), caluclated as the sum of all unit qe's
 * divided by the number of units with at least one mapped input data vector.</li>
 * <li>Mean mean quantisation error (mmqe), calculated as the sum of all unit
 * mqe's divided by the number of units with at least one mapped input data
 * vector.</li>
 * </ul>
 * </ul>
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: QuantizationError.java 3883 2010-11-02 17:13:23Z frank $
 */
public class LFSQuantizationError implements LFSQualityMeasure {

	private double mmqe;

	private double mqe;

	private double[][] unitMqe;

	private double[][] unitQe;

	public LFSQuantizationError(LFSGrowingLayer layer, String errorInit) {

		mqe = Double.valueOf(errorInit);
	}

	/**
	 * Calculates mqe and mmqe
	 * 
	 * @param layer
	 * @param data
	 */
	public LFSQuantizationError(LFSGrowingLayer layer, LFSData data) {

		int xSize = layer.getXSize();
		int ySize = layer.getYSize();

		mqe = 0;
		mmqe = 0;
		int nonEmpty = 0;

		unitQe = new double[xSize][ySize];
		unitMqe = new double[xSize][ySize];

		for (int y = 0; y < ySize; y++) {
			for (int x = 0; x < xSize; x++) {
				double quantErr = 0;
				LFSUnit u = null;
				try {
					u = layer.getUnit(x, y);
				} catch (LFSException e) {
					// TODO: this does not happen
				}
				// added to deal with mnemonic (sparse) SOMs
				if (u != null && u.getNumberOfMappedInputs() > 0) {
					double[] dists = u.getMappedInputDistances();
					for (int i = 0; i < u.getNumberOfMappedInputs(); i++) {
						quantErr += dists[i];
					}
					unitQe[x][y] = quantErr;
					unitMqe[x][y] = quantErr / u.getNumberOfMappedInputs();
					nonEmpty++;
					mqe += unitQe[x][y];
					mmqe += unitMqe[x][y];

				} else {
					unitQe[x][y] = 0;
					unitMqe[x][y] = 0;
				}
			}
		}
		mqe = mqe / nonEmpty;
		mmqe = mmqe / nonEmpty;
	}

	@Override
	public double[][] getUnitQualities(String name) throws LFSException {
		if (name.equals("qe")) {
			return unitQe;
		} else if (name.equals("mqe")) {
			return unitMqe;
		} else {
			throw new LFSException("Quality measure with name " + name
					+ " not found.");
		}
	}

	@Override
	public double getMapQuality(String name) throws LFSException {
		if (name.equals("mqe")) {
			return mqe;
		} else if (name.equals("mmqe")) {
			return mmqe;
		} else {
			throw new LFSException("Quality measure with name " + name
					+ " not found.");
		}
	}

}
