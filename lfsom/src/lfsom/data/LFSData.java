/*
 * 
 * This class is a specialized class of "InputData" class from:
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
package lfsom.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;

import lfsom.util.LFSPCA;

import org.math.array.StatisticSample;

import weka.core.Instances;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.ArffLoader;
import weka.core.converters.C45Loader;
import weka.core.converters.CSVLoader;
import weka.core.converters.JSONLoader;
import weka.core.converters.MatlabLoader;
import weka.core.converters.XRFFLoader;
import cern.colt.Sorting;
import cern.colt.function.IntComparator;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.math.Functions;

/**
 * Reads data from a csv. Rows are separated by newlines, and columns by commas.
 * First line contains attribute names.
 * 
 * @author Vicente Buendia
 * 
 * @author Rudolf Mayer (Original "InputData" class)
 * @version $Id: SimpleMatrixInputData.java 3358 2010-02-11 14:35:07Z mayer $
 *          (Original)
 */
public class LFSData {

	private LFSPCA pca;

	/**
	 * The label/name of the vector.
	 */
	private String[] dataNames = null;

	/**
	 * The dimension of the input vectors, i.e. the number of attributes
	 */
	private int dim = 0;

	/**
	 * The mean of all the input vectors.
	 */
	private DenseDoubleMatrix1D meanVector = null;

	/**
	 * The number of vectors in this input data collection.
	 */
	private int numVectors = 0;

	private Random rand = new Random();

	/** holds the computed results of {@link #getDataIntervals()} */
	private double[][] intervals;

	/**
	 * holds the computed results of {@link #getMinValue()} and
	 * {@link #getMaxValue()}
	 */
	private double[] extremes;

	/**
	 * Matrix which holds the data
	 * 
	 */
	private double[][] matrix;

	/**
	 * Stored to normalize and denormalize data
	 */
	private double[] maxValues;
	private double[] minValues;

	/**
	 * Attribute names
	 */
	private String[] labels;

	public LFSData() {
	}

	/**
	 * Gets the data from a csv file.
	 * 
	 * @param fileName
	 */

	public LFSData(String fileName) {
		Class claseCargador = CSVLoader.class;

		if (fileName.endsWith(ArffLoader.FILE_EXTENSION)) {
			claseCargador = ArffLoader.class;
		} else {
			if (fileName.endsWith(JSONLoader.FILE_EXTENSION)) {
				claseCargador = JSONLoader.class;
			} else {
				if (fileName.endsWith(MatlabLoader.FILE_EXTENSION)) {
					claseCargador = MatlabLoader.class;
				} else {
					if (fileName.endsWith(XRFFLoader.FILE_EXTENSION)) {
						claseCargador = XRFFLoader.class;
					} else {
						if (fileName.endsWith(C45Loader.FILE_EXTENSION)) {
							claseCargador = C45Loader.class;
						}
					}
				}
			}
		}

		try {
			AbstractFileLoader cargador = (AbstractFileLoader) claseCargador
					.getConstructor().newInstance();
			boolean cambio_col = false;

			cargador.setSource(new File(fileName));

			Instances data1 = cargador.getDataSet();

			double[][] matrix2 = new double[data1.size()][data1.numAttributes()];

			for (int i = 0; i < data1.size(); i++) {
				matrix2[i] = data1.get(i).toDoubleArray();
			}

			// Ahora se comprueba si todas las columnas son ok

			Integer[] colVale;
			dim = 0;

			if (data1.size() > 0) {
				colVale = new Integer[matrix2[0].length];
				double[] stdevX = StatisticSample.stddeviation(matrix2);

				for (int k = 0; k < matrix2[0].length; k++) {
					if (Math.abs(stdevX[k]) >= 0.000000001) {
						colVale[k] = dim;
						dim++;
					} else {
						colVale[k] = -1;
						cambio_col = true;
					}
				}

			} else {
				dim = data1.numAttributes();
				colVale = new Integer[dim];
				for (int k = 0; k < dim; k++) {
					colVale[k] = k;
				}
			}

			double[][] matrixAssign = new double[matrix2.length][dim];

			if (cambio_col) {
				for (int k = 0; k < matrix2.length; k++) {
					for (int w = 0; w < matrix2[0].length; w++) {
						if (colVale[w] != -1) {
							matrixAssign[k][colVale[w]] = matrix2[k][w];
						}
					}

				}

			} else {
				matrixAssign = matrix2;
			}

			// Fin de la comprobacion

			setLabels(new String[dim]);
			for (int i = 0; i < data1.numAttributes(); i++) {
				if (colVale[i] != -1) {
					getLabels()[colVale[i]] = data1.attribute(i).name();
				}
			}

			BufferedWriter br = new BufferedWriter(new FileWriter(
					"d:/tmp/fich.csv"));
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < matrixAssign.length; i++) {
				String cad = String.valueOf(matrixAssign[i][0]);
				for (int k = 1; k < matrixAssign[i].length; k++)
					cad += "," + matrixAssign[i][k];
				sb.append(cad + "\n");
			}

			br.write(sb.toString());
			br.close();

			setMatrix(matrixAssign);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// Create LFSData to store the training results
	public LFSData(String[] labelsTrain) {
		setLabels(labelsTrain);
		dim = getLabels().length;
		numVectors = 0;

	}

	/**
	 * Get a row
	 * 
	 * @param d
	 * @return
	 */
	public LFSInputDatum getInputDatum(int d) {
		return new LFSInputDatum(dataNames[d], matrix[d]);
	}

	/**
	 * Get a single value
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	double getValue(int x, int y) {
		return matrix[x][y];
	}

	/**
	 * Assigns a data matrix
	 * 
	 * @param matrizEntrada
	 */
	public void setMatrix(double[][] matrizEntrada) {
		setMatrix(matrizEntrada, true);
	}

	public void setMatrix(double[][] matrizEntrada, boolean norm) {

		double[][] matrixD = matrizEntrada;
		dim = matrixD[0].length;
		numVectors = matrixD.length;

		dataNames = new String[numVectors];
		for (int i = 0; i < numVectors; i++) {
			dataNames[i] = String.valueOf(i);
		}

		// Se normaliza la matriz
		if (norm) {
			matrix = normalize(matrixD);

			meanVector = new DenseDoubleMatrix1D(dim);

			if (matrix.length > 0) {
				LFSInputDatum[] vectors = getInputDatum(dataNames);
				for (int i = 0; i < dataNames.length; i++) {
					meanVector.assign(vectors[i].getVector(), Functions.plus); // add
																				// to
																				// mean
																				// vector
				}
				meanVector.assign(Functions.div(dataNames.length)); // calculating
																	// mean
																	// vector

				// pca = new PCA(matrix);
				if (matrix.length > 1) {
					pca = new LFSPCA(matrix);
				}
			}
		} else
			matrix = matrixD;

	}

	public double[][] normalize(double[][] matrizD) {
		double[][] matrizN = new double[matrizD.length][matrizD[0].length];
		setMaxValues(new double[matrizD[0].length]);
		setMinValues(new double[matrizD[0].length]);

		// First take max and min values
		for (int k = 0; k < matrizD[0].length; k++) {
			getMaxValues()[k] = Double.NEGATIVE_INFINITY;
			getMinValues()[k] = Double.POSITIVE_INFINITY;
			for (int r = 0; r < matrizD.length; r++) {
				getMaxValues()[k] = getMaxValues()[k] < matrizD[r][k] ? matrizD[r][k]
						: getMaxValues()[k];
				getMinValues()[k] = getMinValues()[k] > matrizD[r][k] ? matrizD[r][k]
						: getMinValues()[k];
			}

		}

		// Then normalize
		for (int k = 0; k < matrizD[0].length; k++)
			for (int r = 0; r < matrizD.length; r++) {
				matrizN[r][k] = (matrizD[r][k] - getMinValues()[k])
						/ (getMaxValues()[k] - getMinValues()[k]);
			}

		return matrizN;
	}

	public double[][] getMatrix() {
		return matrix;
	}

	public int dim() {
		return dim;
	}

	public DoubleMatrix1D getMeanVector() {
		return meanVector;
	}

	public int numVectors() {
		return numVectors;
	}

	public LFSInputDatum getRandomInputDatum() {
		// Get a random number
		int randIndex = rand.nextInt(numVectors);
		return this.getInputDatum(randIndex);
	}

	private LFSInputDatum[] getInputDatum(String[] labels) {
		if (labels == null) {
			return null;
		} else {
			LFSInputDatum[] res = new LFSInputDatum[labels.length];

			int[] indices = new int[labels.length];

			for (int i = 0; i < labels.length; i++) {
				// indices[i] = nameCache.get(labels[i]).intValue()
				indices[i] = i;
			}

			IntComparator comp = new IntComparator() {
				/**
				 * @see cern.colt.function.IntComparator#compare(int, int)
				 */
				@Override
				public int compare(int o1, int o2) {
					return o1 < o2 ? -1 : o1 == o2 ? 0 : 1;
				}
			};
			Sorting.quickSort(indices, 0, indices.length - 1, comp);

			for (int i = 0; i < labels.length; i++) {
				res[i] = this.getInputDatum(i);
			}
			return res;
		}
	}

	public double[][] getData() {
		double[][] result = new double[numVectors][dim];
		for (int i = 0; i < numVectors; i++) {
			DoubleMatrix1D v = getInputDatum(i).getVector();
			for (int j = 0; j < v.size(); j++) {
				result[i][j] = v.get(j);
			}
		}
		return result;
	}

	public double[][] getDataIntervals() {
		if (intervals == null) {
			intervals = new double[dim()][2];
			double globalMin = Double.MAX_VALUE;
			double globalMax = -Double.MAX_VALUE;
			for (int i = 0; i < intervals.length; i++) {
				double featureMin = Double.MAX_VALUE;
				double featureMax = -Double.MAX_VALUE;
				for (int j = 0; j < numVectors(); j++) {
					double value = getValue(j, i);
					if (value > featureMax) {
						featureMax = value;
						if (value > globalMax) {
							globalMax = value;
						}
					}
					if (value < featureMin) {
						featureMin = value;
						if (value < globalMin) {
							globalMin = value;
						}
					}
				}
				intervals[i][0] = featureMin;
				intervals[i][1] = featureMax;
			}

			extremes = new double[] { globalMin, globalMax };
		}
		return intervals;
	}

	public double getMinValue() {
		if (extremes == null) {
			getDataIntervals();
		}
		return extremes[0];
	}

	public double getMaxValue() {
		if (extremes == null) {
			getDataIntervals();
		}
		return extremes[1];
	}

	public String getLabel(int index) {
		return getLabels()[index];
	}

	/**
	 * @return Returns the pca.
	 */
	public LFSPCA getPCA() {
		return pca;
	}

	/**
	 * @return the attribute names
	 */
	public String[] getLabels() {
		return labels;
	}

	/**
	 * @param labels
	 *            the attribute names to set
	 */
	public void setLabels(String[] labels) {
		this.labels = labels;
	}

	/**
	 * @return the maxValues
	 */
	public double[] getMaxValues() {
		return maxValues;
	}

	/**
	 * @param maxValues
	 *            the maxValues to set
	 */
	public void setMaxValues(double[] maxValues) {
		this.maxValues = maxValues;
	}

	/**
	 * @return the minValues
	 */
	public double[] getMinValues() {
		return minValues;
	}

	/**
	 * @param minValues
	 *            the minValues to set
	 */
	public void setMinValues(double[] minValues) {
		this.minValues = minValues;
	}

}
