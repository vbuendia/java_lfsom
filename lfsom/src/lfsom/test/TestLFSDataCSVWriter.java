/**
 * 
 */
package lfsom.test;

import java.io.File;
import java.io.IOException;

import lfsom.data.LFSData;
import lfsom.data.LFSDataCSVWriter;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Vicente Buendia
 * 
 */
public class TestLFSDataCSVWriter {

	static LFSData datos;
	static String testFile = "test.csv";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
      datos = LFSDataTest.generateTest(5,100);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// Delete test csv file
		File fich = new File(testFile);
		fich.delete();
	}

	/**
	 * Test method for
	 * {@link lfsom.data.LFSDataCSVWriter#writeAsCSV(lfsom.data.LFSData, long, java.lang.String)}
	 * .
	 */
	@Test
	public void testWriteAsCSV() {
		// Save data, then load data and compare to initial data

		try {
			LFSDataCSVWriter.writeAsCSV(datos, datos.numVectors(), testFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Then, testFile is read and compared to "datos"
		LFSData datos2 = new LFSData(testFile);

		Assert.assertArrayEquals("Data matrix saved", datos2.getMatrix(),
				datos.getMatrix());
		Assert.assertArrayEquals("Labels saved",datos2.getLabels(),datos.getLabels());

	}

}
