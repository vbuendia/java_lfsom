/*
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
 * 
 */
package lfsom.properties;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lfsom.layers.LFSGrowingLayer;
import lfsom.layers.LFSUnit;
import lfsom.output.XMLOutputter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author vicente
 * @version $Id: $
 */
public class LFSExpProps {

	// 1
	private double[] bucleLearnRate = { 0.2 };

	private String strLearnRate = "0.2";

	// 2

	// private boolean[] bucleUseBatch = { true, false };

	private boolean useOnline = true;

	private boolean useBatch = true;

	// 3
	private int[] bucleSigma = { 3 };

	private String strBucleSigma = "3";

	// 4
	private int widthSOM = 0;

	private int heightSOM = 0;

	// 5
	private int numCPUs = 8;

	// 6
	private int nClusters = 5;

	private double mqeIni = -1;

	// 7
	private String ficheroEntrada = "(Select a CSV file to train)";

	private String dataPath = "";

	private String rootPath = "";

	// 9
	private int numRepe = 10;

	// 10
	private int[] bucleInitializationMode = { LFSUnit.INIT_PCA,
			LFSUnit.INIT_INTERVAL_INTERPOLATE, LFSUnit.INIT_RANDOM,
			LFSUnit.INIT_VECTOR };

	private int[] initPCA = { 1, LFSUnit.INIT_PCA };

	private int[] initInterval = { 1, LFSUnit.INIT_INTERVAL_INTERPOLATE };

	private int[] initRandom = { 1, LFSUnit.INIT_RANDOM };

	private int[] initVector = { 1, LFSUnit.INIT_VECTOR };

	// 11
	private int[] bucleNeighFunc = { LFSGrowingLayer.NEIGH_GAUSS,
			LFSGrowingLayer.NEIGH_CUTGAUSS, LFSGrowingLayer.NEIGH_BUBBLE };

	private int[] bucleNeighWidth = { 8 };

	private float[] buclePcNeighWidth = { 0.5f };

	private int[] neighGauss = { 1, LFSGrowingLayer.NEIGH_GAUSS };

	private int[] neighCutGauss = { 1, LFSGrowingLayer.NEIGH_CUTGAUSS };

	private int[] neighBobble = { 1, LFSGrowingLayer.NEIGH_BUBBLE };

	// 12
	private boolean sizeAut = true;

	private boolean growing = false;

	private boolean hier = false;

	private boolean gchsom = false;

	private String expName = "SOM";

	private ArrayList<String> netNames = new ArrayList<String>();

	private ArrayList<String> netFiles = new ArrayList<String>();

	private ArrayList<Integer> subRedOrigen = new ArrayList<Integer>();

	private boolean isSubred = false;

	private String fPadre = "-", fDatosPadre = "-";

	private String[] variables = { "setBucleLearnRate", "setBucleUseBatch",
			"setSigma", "setXYSOM", "setNumCPUs", "setnClusters",
			"setFicheroEntrada", "setNumRepe", "setBucleInitializationMode",
			"setBucleNeighFunc", "setSizeAut", "setExpName", "setNetNames",
			"setNetFiles", "setBucleNeighWidth", "setGrowing", "setIsSubred",
			"setSubredOrigen", "setFPadre", "setFDatosPadre", "setRootPath",
			"setHier", "setGCHSOM", "setBuclePcNeighWidth" };

	private String[] variablesval = { "getStrBucleLearnRate",
			"getStrBucleUseBatch", "getStrBucleSigma", "getStrXYSOM",
			"getStrNumCPUs", "getStrnClusters", "getFicheroEntrada",
			"getStrNumRepe", "getStrBucleInitializationMode",
			"getStrBucleNeighFunc", "isStrSizeAut", "getExpName",
			"getStrNetNames", "getStrNetFiles", "getStrBucleNeighWidth",
			"isStrGrowing", "getStrIsSubred", "getStrSubredOrigen",
			"getFPadre", "getFDatosPadre", "getRootPath", "isStrHier",
			"getStrGCHSOM", "getStrBuclePcNeighWidth" };

	private String strBucleNeighWidth = "8";

	private String strBuclePcNeighWidth = "0.5";

	/**
	 * @return Returns the bucleLearnRate.
	 */
	public double[] getBucleLearnRate() {
		return bucleLearnRate;
	}

	public void EscribeXML(String output) {
		try {
			XMLOutputter salida = new XMLOutputter();
			salida.createXML(this, output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void clearNets() {
		netNames.clear();
		netFiles.clear();
	}

	public void addNet(String nom, String fich) {
		netNames.add(nom);
		netFiles.add(fich);
	}

	public LFSExpProps() {

	}

	public LFSExpProps(String fName) throws ParserConfigurationException,
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

	/**
	 * @param bucleLearnRate
	 *            The bucleLearnRate to set.
	 */
	public String getStrBucleLearnRate() {
		return strLearnRate;
	}

	public void setBucleLearnRate(String strLearnRate) {
		this.strLearnRate = strLearnRate;
		String[] strbl = strLearnRate.split(",");
		if (strbl.length > 0) {
			this.bucleLearnRate = new double[strbl.length];

			for (int i = 0; i < strbl.length; i++) {
				this.bucleLearnRate[i] = Double.parseDouble(strbl[i]);
			}
		} else {
			this.bucleLearnRate = new double[1];
			this.bucleLearnRate[0] = 0.75;
		}
	}

	/**
	 * @return Returns the bucleUseBatch.
	 */
	public boolean[] getBucleUseBatch() {
		int suma = 0;
		if (useOnline) {
			suma++;
		}
		if (useBatch) {
			suma++;
		}
		boolean[] bucle = new boolean[suma];
		int i = 0;
		if (useOnline) {
			bucle[i++] = false;
		}
		if (useBatch) {
			bucle[i++] = true;
		}
		return bucle;
	}

	/**
	 * @param bucleUseBatch
	 *            The bucleUseBatch to set.
	 */
	public boolean getUseBatch() {
		return useBatch;
	}

	public String getStrBucleUseBatch() {

		String salida = "";
		int k = 0;
		if (useOnline) {
			salida = "false";
			k = 1;
		}
		if (useBatch) {
			if (k == 1) {
				salida = salida + ",";
			}
			salida = salida + "true";
		}

		return salida;

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

	public String getStrNetFiles() {
		String salida = "";
		for (int i = 0; i < netFiles.size(); i++) {
			salida = salida + netFiles.get(i);
			if (i < netFiles.size() - 1) {
				salida = salida + ",";
			}
		}
		return salida;
	}

	public String getStrNetNames() {
		String salida = "";
		for (int i = 0; i < netNames.size(); i++) {
			salida = salida + netNames.get(i);
			if (i < netNames.size() - 1) {
				salida = salida + ",";
			}
		}
		return salida;
	}

	public void useBatch(boolean use) {
		this.useBatch = use;
	}

	/**
	 * @return Returns the bucleSigma.
	 */
	public int[] getBucleSigma() {
		return bucleSigma;
	}

	public String getStrBucleSigma() {
		return strBucleSigma;
	}

	public void setNetNames(java.lang.String names) {
		String[] strbl = names.split(",");
		for (String element : strbl) {
			if (!element.equals("")) {
				netNames.add(element);
			}
		}
	}

	public void setNetFiles(java.lang.String names) {
		String[] strbl = names.split(",");
		for (String element : strbl) {
			if (!element.equals("")) {
				netFiles.add(element);
			}
		}
	}

	public String getStrBucleNeighWidth() {
		return strBucleNeighWidth;
	}

	/**
	 * @param bucleSigma
	 *            The bucleSigma to set.
	 */
	public int[] getBucleNeighWidth() {
		return bucleNeighWidth;
	}

	public float[] getBuclePcNeighWidth() {
		return buclePcNeighWidth;
	}

	public void setBucleNeighWidth(String strNWidth) {
		this.strBucleNeighWidth = strNWidth;
		String[] strbl = strNWidth.split(",");
		if (strbl.length > 0) {
			this.bucleNeighWidth = new int[strbl.length];
			for (int i = 0; i < strbl.length; i++) {
				this.bucleNeighWidth[i] = Integer.parseInt(strbl[i]);
			}
		} else {
			this.bucleNeighWidth = new int[1];
			this.bucleNeighWidth[0] = 15;
		}
	}

	public void setBuclePcNeighWidth(String strNWidth) {
		this.strBuclePcNeighWidth = strNWidth;
		String[] strbl = strNWidth.split(",");
		if (strbl.length > 0) {
			this.buclePcNeighWidth = new float[strbl.length];
			for (int i = 0; i < strbl.length; i++) {
				this.buclePcNeighWidth[i] = Float.parseFloat(strbl[i]);
			}
		} else {
			this.buclePcNeighWidth = new float[1];
			this.buclePcNeighWidth[0] = 0.5f;
		}
	}

	public String getStrPcBucleNeighWidth() {
		return strBuclePcNeighWidth;
	}

	public void setSigma(String strSigma) {
		this.strBucleSigma = strSigma;
		String[] strbl = strSigma.split(",");
		if (strbl.length > 0) {
			this.bucleSigma = new int[strbl.length];
			for (int i = 0; i < strbl.length; i++) {
				this.bucleSigma[i] = (int) Float.parseFloat(strbl[i]);
			}
		} else {
			this.bucleSigma = new int[1];
			this.bucleSigma[0] = 4;
		}
	}

	/**
	 * @return Returns the initPCA.
	 */
	public boolean getInitPCA() {
		return initPCA[0] == 1;
	}

	/**
	 * @param initPCA
	 *            The initPCA to set.
	 */
	public void setInitPCA(boolean initPCA) {
		if (initPCA) {
			this.initPCA[0] = 1;
		} else {
			this.initPCA[0] = 0;
		}

	}

	/**
	 * @return Returns the initInterval.
	 */
	public boolean getInitInterval() {
		return initInterval[0] == 1;
	}

	/**
	 * @param initInterval
	 *            The initInterval to set.
	 */
	public void setInitInterval(boolean initInterval) {
		if (initInterval) {
			this.initInterval[0] = 1;
		} else {
			this.initInterval[0] = 0;
		}
	}

	/**
	 * @return Returns the initRandom.
	 */
	public boolean getInitRandom() {
		return initRandom[0] == 1;
	}

	/**
	 * @param initRandom
	 *            The initRandom to set.
	 */
	public void setInitRandom(boolean initRandom) {
		if (initRandom) {
			this.initRandom[0] = 1;
		} else {
			this.initRandom[0] = 0;
		}
	}

	/**
	 * @return Returns the initVector.
	 */
	public boolean getInitVector() {
		return initVector[0] == 1;
	}

	/**
	 * @param initVector
	 *            The initVector to set.
	 */
	public void setInitVector(boolean initVector) {
		if (initVector) {
			this.initVector[0] = 1;
		} else {
			this.initVector[0] = 0;
		}
	}

	public void setBucleInitializationMode(String bucle) {

		String[] strbl = bucle.split(",");
		initPCA[0] = 0;
		initInterval[0] = 0;
		initRandom[0] = 0;
		initVector[0] = 0;

		for (String element : strbl) {
			int valor = Integer.valueOf(element);
			if (valor == initPCA[1]) {
				initPCA[0] = 1;
			}
			if (valor == initInterval[1]) {
				initInterval[0] = 1;
			}
			if (valor == initRandom[1]) {
				initRandom[0] = 1;
			}
			if (valor == initVector[1]) {
				initVector[0] = 1;
			}

		}

	}

	public String getStrBucleInitializationMode() {
		int[] bucle = getBucleInitializationMode();
		String salida = "";
		for (int k = 0; k < bucle.length; k++) {
			if (k > 0) {
				salida = salida + ",";
			}
			salida = salida + String.valueOf(bucle[k]);
		}
		return salida;
	}

	/**
	 * @return Returns the bucleInitializationMode.
	 */
	public int[] getBucleInitializationMode() {
		int suma = initPCA[0] + initInterval[0] + initRandom[0] + initVector[0];
		bucleInitializationMode = new int[suma];
		int actual = 0;
		if (initPCA[0] > 0) {
			bucleInitializationMode[actual++] = initPCA[1];
		}
		if (initInterval[0] > 0) {
			bucleInitializationMode[actual++] = initInterval[1];
		}
		if (initRandom[0] > 0) {
			bucleInitializationMode[actual++] = initRandom[1];
		}
		if (initVector[0] > 0) {
			bucleInitializationMode[actual++] = initVector[1];
		}

		return bucleInitializationMode;
	}

	/**
	 * @return Returns the neighGauss.
	 */
	public boolean getNeighGauss() {
		return neighGauss[0] == 1;
	}

	/**
	 * @param neighGauss
	 *            The neighGauss to set.
	 */
	public void setNeighGauss(boolean neighGauss) {
		if (neighGauss) {
			this.neighGauss[0] = 1;
		} else {
			this.neighGauss[0] = 0;
		}
	}

	/**
	 * @return Returns the neighCutGauss.
	 */
	public boolean getNeighCutGauss() {
		return neighCutGauss[0] == 1;
	}

	/**
	 * @param neighCutGauss
	 *            The neighCutGauss to set.
	 */
	public void setNeighCutGauss(boolean neighCutGauss) {
		if (neighCutGauss) {
			this.neighCutGauss[0] = 1;
		} else {
			this.neighCutGauss[0] = 0;
		}
	}

	/**
	 * @return Returns the neighBobble.
	 */
	public boolean getNeighBobble() {
		return neighBobble[0] == 1;
	}

	/**
	 * @param neighBobble
	 *            The neighBobble to set.
	 */
	public void setNeighBobble(boolean neighBobble) {
		if (neighBobble) {
			this.neighBobble[0] = 1;
		} else {
			this.neighBobble[0] = 0;
		}
	}

	public String getStrBucleNeighFunc() {
		int[] bucle = getBucleNeighFunc();
		String salida = "";
		for (int k = 0; k < bucle.length; k++) {
			if (k > 0) {
				salida = salida + ",";
			}
			salida = salida + String.valueOf(bucle[k]);
		}
		return salida;
	}

	public void setBucleNeighFunc(String bucle) {

		neighGauss[0] = 0;
		neighCutGauss[0] = 0;
		neighBobble[0] = 0;

		String[] strbl = bucle.split(",");

		for (String element : strbl) {
			int valor = Integer.valueOf(element);
			if (valor == neighGauss[1]) {
				neighGauss[0] = 1;
			}
			if (valor == neighCutGauss[1]) {
				neighCutGauss[0] = 1;
			}
			if (valor == neighBobble[1]) {
				neighBobble[0] = 1;
			}

		}
	}

	public void setBucleUseBatch(String bucle) {

		useBatch = false;
		useOnline = false;

		String[] strbl = bucle.split(",");

		for (String element : strbl) {
			if (element.equals("true")) {
				useBatch = true;
			}
			if (element.equals("false")) {
				useOnline = true;
			}
		}

	}

	/**
	 * @return Returns the bucleNeighFunc.
	 */
	public int[] getBucleNeighFunc() {
		int suma = neighGauss[0] + neighCutGauss[0] + neighBobble[0];
		bucleNeighFunc = new int[suma];
		int actual = 0;
		if (neighGauss[0] == 1) {
			bucleNeighFunc[actual++] = neighGauss[1];
		}
		if (neighCutGauss[0] == 1) {
			bucleNeighFunc[actual++] = neighCutGauss[1];
		}
		if (neighBobble[0] == 1) {
			bucleNeighFunc[actual++] = neighBobble[1];
		}
		return bucleNeighFunc;
	}

	/**
	 * @return Returns the numRepe.
	 */
	public int getNumRepe() {
		return numRepe;
	}

	public String getStrNumRepe() {
		return String.valueOf(numRepe);
	}

	/**
	 * @param numRepe
	 *            The numRepe to set.
	 */
	public void setNumRepe(int numRepe) {
		this.numRepe = numRepe;
	}

	public void setNumRepe(String numRepe) {
		setNumRepe(Integer.parseInt(numRepe));
	}

	/**
	 * @return Returns the nClusters.
	 */
	public int getnClusters() {
		return nClusters;
	}

	public String getStrnClusters() {
		return String.valueOf(nClusters);
	}

	/**
	 * @param nClusters
	 *            The nClusters to set.
	 */
	public void setnClusters(int nClusters) {
		this.nClusters = nClusters;
	}

	public void setnClusters(String nClusters) {
		setnClusters(Integer.parseInt(nClusters));
	}

	/**
	 * @return Returns the ficheroEntrada.
	 */
	public String getFicheroEntrada() {
		return ficheroEntrada;
	}

	/**
	 * @param ficheroEntrada
	 *            The ficheroEntrada to set.
	 */
	public void setFicheroEntrada(String ficheroEntrada) {
		this.ficheroEntrada = ficheroEntrada;
	}

	/**
	 * @return Returns the widthSOM.
	 */
	public int getWidthSOM() {
		return widthSOM;
	}

	public void setXYSOM(int widthSOM, int heightSOM) {
		if (!this.isSizeAut()) {
			this.widthSOM = widthSOM;
			this.heightSOM = heightSOM;
		} else {
			this.widthSOM = 0;
			this.heightSOM = 0;
		}
	}

	public void setXYSOM(String xy) {
		String[] strbl = xy.split(",");
		setXYSOM(Integer.parseInt(strbl[0]), Integer.parseInt(strbl[1]));
	}

	public String getStrXYSOM() {
		return String.valueOf(this.widthSOM) + ","
				+ String.valueOf(this.heightSOM);
	}

	/**
	 * @param widthSOM
	 *            The widthSOM to set.
	 */
	public void setWidthSOM(int widthSOM) {
		if (!this.isSizeAut()) {
			this.widthSOM = widthSOM;
		} else {
			this.widthSOM = 0;
		}
	}

	/**
	 * @return Returns the heightSOM.
	 */
	public int getHeightSOM() {
		return heightSOM;
	}

	/**
	 * @param heightSOM
	 *            The heightSOM to set.
	 */
	public void setHeightSOM(int heightSOM) {
		if (!this.isSizeAut()) {
			this.heightSOM = heightSOM;
		} else {
			this.heightSOM = 0;
		}
	}

	/**
	 * @return Returns the numCPUs.
	 */
	public int getNumCPUs() {
		return numCPUs;
	}

	public String getStrNumCPUs() {
		return String.valueOf(getNumCPUs());
	}

	/**
	 * @param numCPUs
	 *            The numCPUs to set.
	 */
	public void setNumCPUs(int numCPUs) {
		this.numCPUs = numCPUs;
	}

	public void setNumCPUs(String nCPU) {
		setNumCPUs(Integer.parseInt(nCPU));
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
	 * @param variablesval
	 *            The variablesval to set.
	 */
	public void setVariablesval(String[] variablesval) {
		this.variablesval = variablesval;
	}

	/**
	 * @return Returns the sizeAut.
	 */
	public boolean isSizeAut() {
		return sizeAut;
	}

	public String isStrSizeAut() {
		return new Boolean(this.sizeAut).toString();
	}

	/**
	 * @param sizeAut
	 *            The sizeAut to set.
	 */
	public void setSizeAut(boolean sizeAut) {
		this.sizeAut = sizeAut;
		if (sizeAut) {
			setXYSOM(0, 0);
		}
	}

	public void setSizeAut(String ssizeAut) {
		setSizeAut(ssizeAut.equals("true"));
	}

	public boolean isGrowing() {
		return growing;
	}

	public boolean isHier() {
		return hier;
	}

	public String getFPadre() {
		return fPadre;
	}

	public String getFDatosPadre() {
		return fDatosPadre;
	}

	public void setFDatosPadre(String dp) {
		fDatosPadre = dp;
	}

	public void setFPadre(String dp) {
		fPadre = dp;
	}

	public String isStrGrowing() {
		return new Boolean(this.growing).toString();
	}

	public String isStrHier() {
		return new Boolean(this.hier).toString();
	}

	public void setGrowing(boolean grow) {
		this.growing = grow;
	}

	public void setGrowing(String sgrow) {
		setGrowing(sgrow.equals("true"));
	}

	public void setHier(boolean grow) {
		this.hier = grow;
	}

	public void setHier(String sgrow) {
		setHier(sgrow.equals("true"));
	}

	/**
	 * @return Returns the useOnline.
	 */
	public boolean getUseOnline() {
		return useOnline;
	}

	/**
	 * @param useOnline
	 *            The useOnline to set.
	 */

	public void useOnline(String use) {
		useOnline(use.equals("true"));
	}

	public void useOnline(boolean use) {
		this.useOnline = use;
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
	 * @return Returns the dataPath.
	 */
	public String getDataPath() {
		return dataPath;
	}

	public String getRootPath() {
		return rootPath;
	}

	/**
	 * @param dataPath
	 *            The dataPath to set.
	 */
	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public void setIsSubred(String subRed) {
		this.isSubred = Boolean.valueOf(subRed);
	}

	public void setIsSubred(boolean subRed) {
		this.isSubred = subRed;
	}

	public String getStrIsSubred() {
		return String.valueOf(isSubred);
	}

	public boolean isSubred() {
		return isSubred;
	}

	/**
	 * @return Returns the mqeIni.
	 */
	public double getMqeIni() {
		return mqeIni;
	}

	/**
	 * @param mqeIni
	 *            The mqeIni to set.
	 */
	public void setMqeIni(double mqeIni) {
		this.mqeIni = mqeIni;
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

	public String getStrBuclePcNeighWidth() {
		return this.strBuclePcNeighWidth;
	}

}
