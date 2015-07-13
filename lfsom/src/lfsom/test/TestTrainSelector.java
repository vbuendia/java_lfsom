package lfsom.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import lfsom.data.LFSData;
import lfsom.experiment.TrainSelector;
import lfsom.properties.LFSExpProps;
import lfsom.properties.LFSSOMProperties;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestTrainSelector {

	static LFSData datos;
	static LFSExpProps expProps;
	static String dataPath = "./";
	static String rootPath = "./";
	static TrainSelector trainSel;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// An expprops object is created
		expProps = new LFSExpProps();
		// Configure expProps
		expProps.useOnline(true);
		expProps.useBatch(true);
		// Autocalc width and height
		expProps.setWidthSOM(0);
		expProps.setHeightSOM(0);
		expProps.setSizeAut(true);

		expProps.setDataPath(dataPath);
		expProps.setRootPath(rootPath);

		expProps.setExpName("Test");

		expProps.setSigma("4");
		expProps.setBucleLearnRate("0.4");
		expProps.setBuclePcNeighWidth("8");

		// Type of SOM
		expProps.setGCHSOM(false);
		expProps.setGrowing(false);
		expProps.setHier(false);

		// Initializations
		expProps.setInitPCA(true);
		expProps.setInitRandom(false);
		expProps.setInitInterval(false);
		expProps.setInitVector(false);

		expProps.setBuclePcNeighWidth("0.1");
		// Neighbour functions
		expProps.setNeighCutGauss(true);
		expProps.setNeighGauss(false);
		expProps.setNeighBobble(false);

		// Parallel?
		expProps.setNumCPUs(1);

		expProps.setFicheroEntrada("noMatter");

		datos = LFSDataTest.generateTest(8, 1000);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// delete all files
		String[] fichs = new String[] { "ExpProps.xml", "kaski.xml",
				"kaski.xmlprops", "quan.xml", "quan.xmlprops", "topo.xml",
				"topo.xmlprops", "results.csv" };
		for (int i = 0; i < fichs.length; i++) {
			File fich = new File(dataPath + fichs[i]);
			fich.delete();
		}
	}

	@Test
	public void testLanzaExperimentoLFSDataLFSExpProps() throws IOException {

		LFSSOMProperties[] listaProps = expProps.generateLFSSOMProperties();

		trainSel = new TrainSelector();
		trainSel.LanzaExperimento(datos, expProps);

		// Now, read results.csv to see if there are results for all the nets
		File fich = new File(dataPath + "results.csv");
		Assert.assertTrue("Generated results file", fich.exists());

		int nLines = 0;
		if (fich.exists()) {
			try (BufferedReader br = new BufferedReader(new FileReader(dataPath
					+ "results.csv"))) {
				String line = br.readLine();
				while (line != null) {
					line = br.readLine();
					nLines++;
				}
			}
		}
		Assert.assertEquals(nLines - 1,
				listaProps.length * expProps.getNumRepe());

	}
}
