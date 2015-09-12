/*
 * 
 * This class is a specialized class of "SOMProperties" class from:
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
package lfsom.properties;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lfsom.output.XMLOutputter;
import lfsom.util.LFSException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Properties for SOM training.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: SOMProperties.java 4252 2012-01-25 11:29:28Z mayer $
 */
public class LFSSOMProperties extends Properties {

	/**
	 * Properties of a single SOM
	 */

	private boolean growing = false;

	private boolean isSubred = false;

	private String expName = "SOM";

	private ArrayList<Integer> subRedOrigen = new ArrayList<Integer>();

	// 14
	private boolean batchSom = false;

	private boolean hier = false;

	private boolean gchsom = false;

	// 15
	private int neighbourWidth = 15;

	private float pcNeighbourWidth = 0.5f;

	// 16
	private double learnrate = 0;

	// 17
	private int neighbourFunc = 10;

	// 18
	private String metricName = null;

	// 20
	private int numCycles = 0;

	// 21
	private int numIterations = 0;

	// 22

	private String[] variables = { "setInitializationMode", "setUsePCA",
			"setBatchSom", "setNeighbourWidth", "setNeighbourFunc",
			"setLearnrate", "setMetricName", "setNumCycles",
			"setNumIterations", "setRandomSeed", "setSigma", "setTau",
			"setExpName", "setQRef", "setGrowing", "setHier", "setIsSubred",
			"setSubredOrigen", "setGCHSOM", "setPcNeighbourWidth", "setXYSize" };

	private String[] variablesval = { "getStrInitializationMode",
			"getStrUsePCA", "getStrBatchSom", "getStrNeighbourWidth",
			"getStrNeighbourFunc", "getStrLearnrate", "getStrMetricName",
			"getStrNumCycles", "getStrNumIterations", "getStrRandomSeed",
			"getStrSigma", "getStrTau", "getExpName", "getStrQRef",
			"getStrGrowing", "getStrHier", "getStrIsSubred",
			"getStrSubredOrigen", "getStrGCHSOM", "getStrPcNeighbourWidth",
			"getStrXYSize" };

	// 23
	private long randomSeed = -1;

	// 24
	private double sigma = -1;

	// 25
	public double tau = 1;

	// 26
	private int initializationMode = 10;

	// 1
	private int xSize = 0;

	// 2
	private int ySize = 0;

	// 6
	private boolean usePCA = false;

	private String dataPath;

	// Quality reference for growing
	public double qRef;

	/**
	 * Although we made our self serialization method
	 */
	private static final long serialVersionUID = 1L;

	public void setNumIterations(int iterations) {
		this.numIterations = iterations;
	}

	/******************************************************************************/
	public LFSSOMProperties(int xSize, int ySize, long seed,
			int trainingCycles, int trainingIterations, double lernrate,
			double sigma, double tau, String metric, boolean usePCA,
			boolean usebatch, int initializationMode, int neighbourFunc,
			float pcNighWidth, String expName, boolean growing, double qRef,
			boolean isSub, boolean hiera, boolean gchs) throws LFSException {
		this(xSize, ySize, seed, trainingCycles, trainingIterations, lernrate,
				sigma, tau, metric, usePCA);

		this.batchSom = usebatch;
		this.setExpName(expName);
		this.initializationMode = initializationMode;
		this.setNeighbourFunc(neighbourFunc);
		this.pcNeighbourWidth = pcNighWidth;
		this.growing = growing;
		this.hier = hiera;
		this.setGCHSOM(gchs);
		this.qRef = qRef;
		this.isSubred = isSub;
	}

	private LFSSOMProperties(int xSize, int ySize, int numIterations,
			double lernrate) throws LFSException {
		this.setxSize(xSize);
		this.setySize(ySize);
		this.numIterations = numIterations;
		this.learnrate = lernrate;
	}

	private LFSSOMProperties(int xSize, int ySize, long seed, int numCycles,
			int numIterations, double learnrate, double sigma, double tau,
			String metricName, boolean usePCA) throws LFSException {
		this(xSize, ySize, numIterations, learnrate);

		this.tau = tau;
		this.metricName = metricName;
		this.numCycles = numCycles;
		this.sigma = sigma;
		this.randomSeed = -1;
		this.usePCA = usePCA;
		validatePropertyValues();
	}

	/**
	 * Loads and encapsulated properties for the SOM training process.
	 * 
	 * @param fname
	 *            name of the properties file.
	 * @throws LFSException
	 *             thrown if properties file could not be opened or the values
	 *             of the properties are illegal.
	 */
	public LFSSOMProperties(String fName) throws ParserConfigurationException,
			SAXException, IOException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		// propi = this();
		// ExpProps propi = new ExpProps();

		File fXmlFile = new File(fName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("atrib");

		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				String funcion = eElement.getElementsByTagName("funcion")
						.item(0).getTextContent();
				String valor = eElement.getElementsByTagName("valor").item(0)
						.getTextContent();

				Method metodo = this.getClass().getDeclaredMethod(funcion,
						String.class);
				metodo.invoke(this, valor);

			}
		}

	}

	public LFSSOMProperties() {
	}

	public void validatePropertyValues() throws LFSException {

		if (getxSize() < 0 || getySize() < 0) {
			throw new LFSException("Either x or y "
					+ " is less than or equal zero.");
		}
		if (learnrate <= 0) {
			throw new LFSException(
					"Learn Rate is less than or equal zero or missing.");
		}

		if (sigma <= 0) {

			if (getxSize() == 1 || getySize() == 1) {
				sigma = Math.min(getxSize(), getySize()) / 2d;
			} else {
				sigma = Math.max(getxSize(), getySize()) / 2d;
			}
			Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
					" Sigma is missing or negative. Defaulting to " + sigma
							+ " for a map of size " + getxSize() + "x"
							+ getySize());
		}
		if (tau == 1) {

		} else if (tau <= 0 || tau > 1) {
			Logger.getLogger("at.tuwien.ifs.somtoolbox")
					.warning(
							" Tau less than or equal zero or greater than 1. Fixing to 1 (fix-sized layer)");
			tau = 1;
		}

		if (numIterations <= 0 && numCycles <= 0) {
			throw new LFSException(
					" Num. Iterations and Cycles are less than or equal zero or missing.\n");
		}
	}

	/**
	 * Returns the batch_som status.
	 * 
	 * @return the batch_som status.
	 */
	public boolean batchSom() {
		return batchSom;
	}

	public void escribeXML(String output) {
		try {
			XMLOutputter salida = new XMLOutputter();
			salida.createXML(this, output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public LFSSOMProperties copia() {

		LFSSOMProperties propsRet = new LFSSOMProperties();

		try {

			for (int k = 0; k < this.getVariables().length; k++) {

				String funcion = this.getVariables(k);
				String valor = this.getVariablesval(k);

				Method metodo;

				metodo = propsRet.getClass().getDeclaredMethod(funcion,
						String.class);

				metodo.invoke(propsRet, valor);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return propsRet;
	}

	public String getStrSubredOrigen() {
		String salida = "";
		for (int i = 0; i < subRedOrigen.size(); i++) {
			salida = salida + subRedOrigen.get(i);
			if (i < subRedOrigen.size() - 1) {
				salida = salida + ",";
			}
		}
		if (salida.equals("")) {
			salida = "-1";
		}
		return salida;
	}

	public void setSubredOrigen(String subRed) {

		String[] strbl = subRed.split(",");
		for (String element : strbl) {
			this.subRedOrigen.add(Integer.parseInt(element));
		}

	}

	public void setSubredOrigen(ArrayList<Integer> subRed) {
		this.subRedOrigen = subRed;
	}

	public void setIsSubred(String subRed) {
		this.isSubred = Boolean.valueOf(subRed);
	}

	public String getStrIsSubred() {
		return String.valueOf(isSubred);
	}

	public boolean isSubred() {
		return isSubred;
	}

	public float pcNeighbourWidth() {
		return pcNeighbourWidth;
	}

	/**
	 * Returns the learnrate.
	 * 
	 * @return the learnrate.
	 */
	public double learnrate() {
		return learnrate;
	}

	/**
	 * Returns the name of the used metric.
	 * 
	 * @return the name of the used metric.
	 */
	public String getStrMetricName() {
		return metricName();
	}

	public String metricName() {
		if (metricName == null) {
			metricName = "lfsom.layers.metrics.LFSL2Metric";
		}
		return metricName;
	}

	public void setMetricName(String met) {
		metricName = met;
	}

	/**
	 * Returns the number of training cycles.
	 * 
	 * @return the number of training cycles.
	 */
	public int numCycles() {
		return numCycles;
	}

	/**
	 * Returns the number of training iterations.
	 * 
	 * @return the number of training iterations.
	 */
	public int numIterations() {
		return numIterations;
	}

	/**
	 * Returns the random seed.<br/>
	 * FIXME: this is a duplicate to {@link LFSFileProperties#randomSeed()}
	 * 
	 * @return the random seed.
	 */
	public long randomSeed() {
		return randomSeed;
	}

	/**
	 * Returns sigma determining the neighbourhood radius.
	 * 
	 * @return sigma determining the neighbourhood radius.
	 */
	public double sigma() {
		return sigma;
	}

	public void setXYSize(int x, int y) {
		this.setxSize(x);
		this.setySize(y);
	}

	public void setXYSize(String xy) {
		String[] strbl = xy.split(",");
		xSize = Integer.parseInt(strbl[0]);
		ySize = Integer.parseInt(strbl[1]);

	}

	public String getStrXYSize() {
		return "" + xSize + "," + ySize;
	}

	/**
	 * Returns the number of units in horizontal direction.
	 * 
	 * @return the number of units in horizontal direction.
	 */
	public int xSize() {
		return getxSize();
	}

	/**
	 * Returns the number of units in vertical direction.
	 * 
	 * @return the number of units in vertical direction.
	 */
	public int ySize() {
		return getySize();
	}

	public boolean pca() {
		return usePCA;
	}

	/**
	 * @return Returns the initializationMode.
	 */
	public int getInitializationMode() {
		return initializationMode;
	}

	public String getStrInitializationMode() {
		return String.valueOf(initializationMode);
	}

	public void setInitializationMode(String iMode) {
		initializationMode = Integer.valueOf(iMode);
	}

	/**
	 * @return Returns the neighbourFunc.
	 */
	public int getNeighbourFunc() {
		return neighbourFunc;
	}

	/**
	 * @param neighbourFunc
	 *            The neighbourFunc to set.
	 */
	public void setNeighbourFunc(int neighbourFunc) {
		this.neighbourFunc = neighbourFunc;
	}

	public void setUsePCA(String usa) {
		this.usePCA = Boolean.valueOf(usa);
	}

	public String getStrUsePCA() {
		return String.valueOf(this.usePCA);
	}

	public void setBatchSom(String batch) {
		this.batchSom = Boolean.valueOf(batch);
	}

	public String getStrBatchSom() {
		return String.valueOf(this.batchSom);
	}

	public void setNeighbourWidth(String neig) {
		this.neighbourWidth = Integer.valueOf(neig);
	}

	public void setNeighbourWidth(int neig) {
		this.neighbourWidth = neig;
	}

	public String getStrNeighbourWidth() {
		return String.valueOf(this.neighbourWidth);
	}

	public void setLearnrate(String learn) {
		this.learnrate = Double.parseDouble(learn);
	}

	public String getStrLearnrate() {
		return String.valueOf(this.learnrate);

	}

	public void setNeighbourFunc(String neig) {
		this.neighbourFunc = Integer.parseInt(neig);
	}

	public String getStrNeighbourFunc() {
		return String.valueOf(this.neighbourFunc);
	}

	public void setNumCycles(String nc) {
		this.numCycles = Integer.parseInt(nc);
	}

	public String getStrNumCycles() {
		return String.valueOf(this.numCycles);
	}

	public void setNumIterations(String ni) {
		this.numIterations = Integer.parseInt(ni);
	}

	public String getStrNumIterations() {
		return String.valueOf(this.numIterations());
	}

	public void setRandomSeed(String rs) {
		this.randomSeed = Long.parseLong(rs);
	}

	public String getStrRandomSeed() {
		return String.valueOf(this.randomSeed);
	}

	public void setSigma(String sig) {
		this.sigma = Double.valueOf(sig);
	}

	public String getStrSigma() {
		return String.valueOf(this.sigma);
	}

	public void setTau(String ta) {
		this.tau = Double.valueOf(ta);
	}

	public String getStrTau() {
		return String.valueOf(this.tau);
	}

	/**
	 * @return Returns the variables.
	 */
	public String[] getVariables() {
		return variables;
	}

	public String getVariables(int k) {
		return variables[k];
	}

	/**
	 * @param variables
	 *            The variables to set.
	 */
	public void setVariables(String[] variables) {
		this.variables = variables;
	}

	/**
	 * @return Returns the variablesval.
	 */
	public String[] getVariablesval() {
		return variablesval;
	}

	public String getVariablesval(int k) throws NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Method funcion = this.getClass().getMethod(this.variablesval[k]);
		String salida = (String) funcion.invoke(this);
		return salida;
	}

	/**
	 * @return Returns the expName.
	 */
	public String getExpName() {
		return expName;
	}

	/**
	 * @param expName
	 *            The expName to set.
	 */
	public void setExpName(String expName) {
		this.expName = expName;
	}

	/**
	 * @return Returns the xSize.
	 */
	private int getxSize() {
		return xSize;
	}

	/**
	 * @param xSize
	 *            The xSize to set.
	 */
	private void setxSize(int xSize) {
		this.xSize = xSize;
	}

	/**
	 * @return Returns the ySize.
	 */
	private int getySize() {
		return ySize;
	}

	/**
	 * @param ySize
	 *            The ySize to set.
	 */
	private void setySize(int ySize) {
		this.ySize = ySize;
	}

	/**
	 * @return Returns the dataPath.
	 */
	public String getDataPath() {
		return dataPath;
	}

	/**
	 * @param dataPath
	 *            The dataPath to set.
	 */
	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	/**
	 * @return Returns the growing.
	 */
	public boolean isGrowing() {
		return growing;
	}

	public boolean isHier() {
		return hier;
	}

	/**
	 * @param growing
	 *            The growing to set.
	 */
	public void setGrowing(boolean growing) {
		this.growing = growing;
	}

	public void setGrowing(String growing) {
		this.growing = Boolean.valueOf(growing);
	}

	public String getStrGrowing() {
		return String.valueOf(this.growing);
	}

	public void setHier(boolean hiera) {
		this.hier = hiera;
	}

	public void setHier(String hiera) {
		this.hier = Boolean.valueOf(hiera);
	}

	public String getStrHier() {
		return String.valueOf(this.hier);
	}

	public void setQRef(String ref) {
		this.qRef = Double.valueOf(ref);
	}

	public Double getQRef() {
		return this.qRef;
	}

	public String getStrQRef() {
		return String.valueOf(this.qRef);
	}

	/**
	 * @return Returns the gchsom.
	 */
	public boolean isGCHSOM() {
		return gchsom;
	}

	/**
	 * @param gchsom
	 *            The gchsom to set.
	 */
	public void setGCHSOM(boolean gchsom) {
		this.gchsom = gchsom;
	}

	public void setGCHSOM(String gchsom) {
		this.gchsom = Boolean.valueOf(gchsom);
	}

	public String getStrGCHSOM() {
		return String.valueOf(this.gchsom);
	}

	public String getStrPcNeighbourWidth() {
		return String.valueOf(this.pcNeighbourWidth);
	}

	/**
	 * @return Returns the pcNeighbourWidth.
	 */
	public float getPcNeighbourWidth() {
		return pcNeighbourWidth;
	}

	/**
	 * @param pcNeighbourWidth
	 *            The pcNeighbourWidth to set.
	 */
	public void setPcNeighbourWidth(float pcNeighbourWidth) {
		this.pcNeighbourWidth = pcNeighbourWidth;
	}

	public void setPcNeighbourWidth(String pcNeighbourWidth) {
		this.pcNeighbourWidth = Float.valueOf(pcNeighbourWidth);
	}

}
