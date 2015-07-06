/*
 * 
 * This class is a specialized class of "PCA" class from:
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
package lfsom.util;

import org.math.array.DoubleArray;
import org.math.array.StatisticSample;

/**
 * Principal Component Analysis
 * 
 * @version $Id: PCA.java 3587 2010-05-21 10:35:33Z mayer $
 */
public class LFSPCA {

	private double[] meanX, stdevX;

	private double[][] Z; // X centered reduced

	private double[][] cov; // Z covariance matrix

	public double[][] U; // projection matrix

	private double[] info; // information matrix

	private int firstAxisIndex = 0;

	private int secondAxisIndex = 0;

	public LFSPCA(double[][] X2) {

		stdevX = StatisticSample.stddeviation(X2);

		Integer[] colVale = new Integer[X2[0].length];
		int dim = 0;
		for (int k = 0; k < X2[0].length; k++) {
			if (Math.abs(stdevX[k]) >= 0.000000001) {
				colVale[k] = dim;
				dim++;
			} else {
				colVale[k] = -1;
			}
		}

		double[][] X = new double[X2.length][dim];

		if (dim != X2[1].length) {
			for (int k = 0; k < X2.length; k++) {
				for (int w = 0; w < X2[0].length; w++) {
					if (colVale[w] != -1) {
						X[k][colVale[w]] = X2[k][w];
					}
				}

			}

		} else {
			X = X2;
		}

		stdevX = StatisticSample.stddeviation(X);
		meanX = StatisticSample.mean(X);

		Z = centerReduce(X);

		cov = StatisticSample.covariance(Z);

		Jama.EigenvalueDecomposition e = org.math.array.LinearAlgebra
				.eigen(cov);
		U = DoubleArray.transpose(e.getV().getArray());
		info = e.getRealEigenvalues(); // covariance matrix is symetric, so only
										// real eigenvalues...

		// Se calculan el firstAxis y el secondAxis
		double firstAxisVar = Double.MIN_VALUE;
		double secondAxisVar = Double.MIN_VALUE;
		setFirstAxisIndex(-1);
		setSecondAxisIndex(-1);

		for (int curAxis = 0; curAxis < dim; curAxis++) {
			if (info[curAxis] > firstAxisVar) {
				secondAxisVar = firstAxisVar;
				setSecondAxisIndex(getFirstAxisIndex());

				firstAxisVar = info[curAxis];
				setFirstAxisIndex(curAxis);
			} else if (info[curAxis] > secondAxisVar) {
				secondAxisVar = info[curAxis];
				setSecondAxisIndex(curAxis);
			}
		}

		System.out.println("");
		System.out.println("  *** firstAxisIndex: " + getFirstAxisIndex()
				+ " secondAxisIndex: " + getSecondAxisIndex());
	}

	// normalization of x relatively to X mean and standard deviation
	private double[][] centerReduce(double[][] x) {
		double[][] y = new double[x.length][x[0].length];
		for (int i = 0; i < y.length; i++) {
			for (int j = 0; j < y[i].length; j++) {
				y[i][j] = (x[i][j] - meanX[j]) / stdevX[j];
			}
		}
		return y;
	}

	/**
	 * @return Returns the firstAxisIndex.
	 */
	public int getFirstAxisIndex() {
		return firstAxisIndex;
	}

	/**
	 * @param firstAxisIndex
	 *            The firstAxisIndex to set.
	 */
	private void setFirstAxisIndex(int firstAxisIndex) {
		this.firstAxisIndex = firstAxisIndex;
	}

	/**
	 * @return Returns the secondAxisIndex.
	 */
	public int getSecondAxisIndex() {
		return secondAxisIndex;
	}

	/**
	 * @param secondAxisIndex
	 *            The secondAxisIndex to set.
	 */
	private void setSecondAxisIndex(int secondAxisIndex) {
		this.secondAxisIndex = secondAxisIndex;
	}

}