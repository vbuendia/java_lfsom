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

import java.io.File;
import java.util.Random;

import lfsom.util.LFSPCA;

import org.math.array.StatisticSample;

import weka.core.Instances;
import weka.core.converters.CSVLoader;
import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.data.TemplateVector;
import cern.colt.Sorting;
import cern.colt.function.IntComparator;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.math.Functions;

/**
 * Reads data from a simple matrix file. Rows are separated by newlines, and
 * columns by spaces or tabs.
 * 
 * @author Rudolf Mayer
 * @version $Id: SimpleMatrixInputData.java 3358 2010-02-11 14:35:07Z mayer $
 */
public class LFSData {

	private LFSPCA pca;

	/**
	 * The label/name of the vector.
	 */
	private String[] dataNames = null;

	/**
	 * Column dimension of the feature matrix before having been vectorized to
	 * input vector.
	 */
	// protected int featureMatrixCols = -1;

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

	/**
	 * A {@link TemplateVector} attached to this input data.
	 */
	private TemplateVector templateVector = null;

	/** holds the computed results of {@link #getDataIntervals()} */
	private double[][] intervals;

	/**
	 * holds the computed results of {@link #getMinValue()} and
	 * {@link #getMaxValue()}
	 */
	private double[] extremes;

	private double[][] matrix;

	private String[] labels;

	public LFSData() {
	}

	public LFSData(String fileName) {

		setFichOrig(fileName);
		CSVLoader cargador = new CSVLoader();
		try {
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

			matrix = new double[matrix2.length][dim];

			if (cambio_col) {
				for (int k = 0; k < matrix2.length; k++) {
					for (int w = 0; w < matrix2[0].length; w++) {
						if (colVale[w] != -1) {
							matrix[k][colVale[w]] = matrix2[k][w];
						}
					}

				}

			} else {
				matrix = matrix2;
			}

			// Fin de la comprobacion

			labels = new String[dim];
			for (int i = 0; i < data1.numAttributes(); i++) {
				if (colVale[i] != -1) {
					labels[colVale[i]] = data1.attribute(i).name();
				}
			}

			dataNames = new String[data1.size()];
			for (int i = 0; i < data1.size(); i++) {
				dataNames[i] = String.valueOf(i);
			}

			numVectors = data1.size();
			// dim = data1.numAttributes();

			templateVector = new SOMLibTemplateVector(numVectors(), labels,
					dim());

			meanVector = new DenseDoubleMatrix1D(dim);

			if (data1.size() > 0) {
				LFSInputDatum[] vectors = getInputDatum(dataNames);
				for (int i = 0; i < dataNames.length; i++) {
					meanVector.assign(vectors[i].getVector(), Functions.plus); // add
																				// to
																				// mean
																				// vector
				}
				meanVector.assign(Functions.div(labels.length)); // calculating
																	// mean
																	// vector

				// pca = new PCA(matrix);
				if (matrix.length > 1) {
					pca = new LFSPCA(matrix);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public LFSInputDatum getInputDatum(int d) {
		return new LFSInputDatum(dataNames[d], matrix[d]);
	}

	double getValue(int x, int y) {
		return matrix[x][y];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.tuwien.ifs.somtoolbox.data.InputData#pushInputDatum(at.tuwien.ifs.
	 * somtoolbox.data.InputDatum)
	 */

	public void setMatrix(double[][] matrizEntrada) {
		matrix = matrizEntrada;
		dim = matrix[0].length;
		numVectors = matrix.length;

		templateVector = new SOMLibTemplateVector(numVectors(), labels, dim());

		meanVector = new DenseDoubleMatrix1D(dim);

		pca = new LFSPCA(matrizEntrada);
	}

	/**
	 * @param fichOrig
	 *            The fichOrig to set.
	 */
	private void setFichOrig(String fichOrig) {
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

	TemplateVector templateVector() {
		return templateVector;
	}

	public void setTemplateVector(TemplateVector templateVector) {
		this.templateVector = templateVector;
	}

	public LFSInputDatum getRandomInputDatum(int iteration, int numIterations) {
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
		return labels[index];
	}

	/**
	 * @return Returns the pca.
	 */
	public LFSPCA getPCA() {
		return pca;
	}

}
