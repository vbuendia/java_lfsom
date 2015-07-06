/*
 * 
 * This class is a specialized class of "L2Metric" class from:
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
package lfsom.layers.metrics;

import lfsom.util.LFSException;
import cern.colt.matrix.DoubleMatrix1D;

/**
 * Implements the L2 or Euclidean metric. Though this class could us
 * at.tuwien.ifs.somtoolbox.layers.metrics.LNMetric, for performance issues this
 * less complex computation should be used.
 * 
 * @author Michael Dittenbach
 * @version $Id: L2Metric.java 3883 2010-11-02 17:13:23Z frank $
 */
public class LFSL2Metric {

	/**
	 * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(double[],
	 *      double[])
	 */

	public static double distance(double[] vector1, double[] vector2)
			throws LFSException {
		checkDimensions(vector1, vector2);
		double dist = 0;
		for (int i = 0; i < vector1.length; i++) {
			dist += (vector1[i] - vector2[i]) * (vector1[i] - vector2[i]);
		}
		return Math.sqrt(dist);
	}

	@Override
	public String toString() {
		return "L2";
	}

	/**
	 * Instantiates a certain distance metric class specified by argument
	 * <code>mName</code>.<br/>
	 * Note: for backwards compatibility, if the metric name contains the
	 * package <code>prefix at.ec3.somtoolbox</code>, this will be replaced by
	 * <code>at.tuwien.ifs.somtoolbox</code>.
	 * 
	 * @param mName
	 *            the name of the metric.
	 * @return a distance metric object of class <code>mName</code>.
	 * @throws ClassNotFoundException
	 *             if class denoted by argument <code>mName</code> is not found.
	 * @throws InstantiationException
	 *             if if this Class represents an abstract class, an interface,
	 *             an array class, a primitive type, or void; or if the class
	 *             has no nullary constructor; or if the instantiation fails for
	 *             some other reason.
	 * @throws IllegalAccessException
	 *             if the class or its nullary constructor is not accessible.
	 */

	/**
	 * Can be used to do some performance testing to compare colt vs. direct
	 * distance implementations.
	 */

	/**
	 * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(double[],
	 *      cern.colt.matrix.DoubleMatrix1D)
	 */

	public static double distance(double[] vector1, DoubleMatrix1D vector2)
			throws LFSException {
		return distance(vector1, vector2.toArray());
	}

	/**
	 * Calculates the mean vector of two double array vectors.
	 * 
	 * @param vector1
	 *            first vector.
	 * @param vector2
	 *            second vector.
	 * @return the mean vector.
	 * @throws LFSException
	 *             if the dimensionalities of the two vectors differ.
	 */
	public static double[] meanVector(double[] vector1, double[] vector2)
			throws LFSException {
		if (vector1.length != vector2.length) {
			throw new LFSException(
					"Oops ... tried to calculate the mean vector of two vectors with different dimensionalities.");
		}
		// initialize mean vector
		double[] meanVector = new double[vector1.length];
		for (int ve = 0; ve < vector1.length; ve++) { // calculating mean vector
			meanVector[ve] = (vector1[ve] + vector2[ve]) / 2;
		}
		return meanVector;
	}

	/**
	 * @see at.tuwien.ifs.somtoolbox.layers.metrics.DistanceMetric#distance(cern.colt.matrix.DoubleMatrix1D,
	 *      double[])
	 */

	public static double distance(DoubleMatrix1D vector1, double[] vector2)
			throws LFSException {
		return distance(vector1.toArray(), vector2);
	}

	/**
	 * Performs a check on wether the given vectors have the same dimension.
	 * 
	 * @throws LFSException
	 *             If the given vectors have different dimensions.
	 */
	private static void checkDimensions(double[] vector1, double[] vector2)
			throws LFSException {
		if (vector1.length != vector2.length) {
			throw new LFSException(
					"Tried to calculate distance between two vectors with different dimensionalities.");
		}
	}

}
