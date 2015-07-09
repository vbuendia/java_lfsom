package lfsom.test;

import lfsom.data.LFSData;

//A class to generate test data to train
public  class LFSDataTest {
	public static LFSData generateTest(int dim,int nVec)
	{
		// Generate random data
		LFSData datos = new LFSData();
		String[] label = new String[dim];
		double[][] matrix2 = new double[nVec][dim];

		for (int j = 0; j < dim; j++) {
			label[j] = "Attrib-" + j;
			for (int i = 0; i < nVec; i++) {

				matrix2[i][j] = i * j+i; // It's needed to have std dev.
			}
		}
		datos.setLabels(label);
		datos.setMatrix(matrix2);
	return datos;
	}
	

}
