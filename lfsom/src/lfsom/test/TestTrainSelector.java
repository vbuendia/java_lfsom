package lfsom.test;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import lfsom.data.LFSData;
import lfsom.experiment.TrainSelector;
import lfsom.properties.LFSExpProps;

public class TestTrainSelector {

	static LFSData datos;
	static LFSExpProps expProps;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//An expprops object is created
		expProps = new LFSExpProps();
		//Configure expProps
		expProps.useOnline(true);
		expProps.useBatch(true);
		//Autocalc width and height
		expProps.setWidthSOM(0);
		expProps.setHeightSOM(0);
		
		datos = LFSDataTest.generateTest(8, 1000);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//Delete net files and results.csv
		
	}

	@Test
	public void testLanzaExperimento() {
		TrainSelector trainSel = new TrainSelector();
		trainSel.LanzaExperimento(datos, expProps);
		
	}

}
