package lfsom.test;

import lfsom.data.LFSData;
import lfsom.experiment.TrainSelector;
import lfsom.properties.LFSExpProps;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestTrainSelector {

	static LFSData datos;
	static LFSExpProps expProps;

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

		expProps.setDataPath("./");
		expProps.setRootPath("./");

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
		expProps.setInitVector(true);

		// Neighbour functions
		expProps.setNeighCutGauss(true);
		expProps.setNeighGauss(false);
		expProps.setNeighBobble(false);

		// Parallel?
		expProps.setNumCPUs(4);

		expProps.setFicheroEntrada("noMatter");

		datos = LFSDataTest.generateTest(8, 1000);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testLanzaExperimentoLFSDataLFSExpProps() {
		TrainSelector trainSel = new TrainSelector();
		trainSel.LanzaExperimento(datos, expProps);
	}

}
