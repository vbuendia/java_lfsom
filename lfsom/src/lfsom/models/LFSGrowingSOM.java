/*
 * 
 * This class is a variation of "GrowingSOM" class from:
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
package lfsom.models;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lfsom.data.LFSData;
import lfsom.layers.LFSGrowingLayer;
import lfsom.layers.LFSUnit;
import lfsom.output.XMLOutputter;
import lfsom.properties.LFSSOMProperties;
import lfsom.util.LFSException;
import lfsom.visualization.clustering.LFSKMeans;
import lfsom.visualization.clustering.LFSWEKACluster;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class implements the Growing Self-Organizing Map. It is basically a
 * wrapper for the {@link at.tuwien.ifs.somtoolbox.layers.GrowingLayer} and
 * mainly handles command line execution and parameters. It implements the
 * {@link at.tuwien.ifs.somtoolbox.models.NetworkModel} interface wich is
 * currently not used, but may be used in the future.
 * 
 * @author Michael Dittenbach
 * @version $Id: GrowingSOM.java 4015 2011-01-26 16:07:49Z mayer $
 */
public class LFSGrowingSOM {

	private boolean Calculando = false;

	private boolean subnet = false;

	// 1
	private String[] labelAtrib;

	// 2
	private int[] labelAgrupados;

	private LFSGrowingLayer layer = null;

	private String expName = "SOM";

	private String[] variables = { "setLabelAtrib", "setLabelAgrupados",
			"setError", "setExpName", "setIsSubnet" };

	private String[] variablesval = { "getStrLabelAtrib",
			"getStrLabelAgrupados", "getStrError", "getExpName",
			"getStrIsSubnet" };

	private LFSSOMProperties propi = null;

	/**
	 * Method for stand-alone execution of map training. Options are:<br/>
	 * <ul>
	 * <li>-h toggles HTML output</li>
	 * <li>-l name of class implementing the labeling algorithm</li>
	 * <li>-n number of labels to generate</li>
	 * <li>-w name of weight vector file in case of training an already trained
	 * map</li>
	 * <li>-m name of map description file in case of training an already
	 * trained map</li>
	 * <li>--noDWM switch to not write the data winner mapping file</li>
	 * <li>properties name of properties file, mandatory</li>
	 * </ul>
	 * 
	 * @param args
	 *            the execution arguments as stated above.
	 */
	public boolean getCalculando() {
		return Calculando;
	}

	public int getDimenData() {
		return labelAtrib.length;
	}

	public String getLabel(int i) {
		return labelAtrib[i];
	}

	public String[] getLabel() {
		return labelAtrib;
	}

	public void saveMapCSVParcial(LFSData datum, ArrayList<Integer> listaCells,
			String dataPath, String nomFich) {

		layer.saveMapCSVParcial(datum, labelAtrib, listaCells, dataPath,
				nomFich);

	}

	public void initLayer(boolean norm, LFSSOMProperties props, LFSData data,
			LFSUnit[][] units) {

		layer = new LFSGrowingLayer(norm, data, props, units, this);

		this.setIsSubnet(props.isSubred());

	}

	/** only used for subclassing */
	public LFSGrowingSOM() {
	}

	public LFSGrowingSOM(String expName) {
		this.setExpName(expName);

	}

	public LFSGrowingSOM(String fName, LFSData datos1)
			throws ParserConfigurationException, SAXException, IOException,
			Exception, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {

		LFSSOMProperties props = new LFSSOMProperties(fName + "props");
		this.initLayer(true, props, datos1, null);

		File fXmlFile = new File(fName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("atrib");

		Method[] metodClase = this.getClass().getMethods();
		ArrayList<String> metodName = new ArrayList<String>();
		for (Method element : metodClase) {
			metodName.add(element.getName());
		}

		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				String funcion = eElement.getElementsByTagName("funcion")
						.item(0).getTextContent();
				String valor = eElement.getElementsByTagName("valor").item(0)
						.getTextContent();

				if (metodName.contains(funcion)) {
					Method metodo = this.getClass().getDeclaredMethod(funcion,
							String.class);
					metodo.invoke(this, valor);
				}

			}
		}

		nList = doc.getElementsByTagName("layer");

		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;
				if (eElement.getAttribute("name").toString().equals("(Hits)")) {
					// Es el momento de calcular mapeo
					this.getLayer().mapCompleteDataAfterTraining(datos1);

				} else if (eElement.getAttribute("name").toString()
						.equals("(Cluster)")) {
					this.setListClusters(eElement.getTextContent());

				} else {
					this.getLayer().chargeWeights(temp,
							eElement.getTextContent());
				}

			}
		}

	}

	public void EscribeXML(String output) {
		try {
			XMLOutputter salida = new XMLOutputter();
			salida.createXML(this, output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void escribeProps(String output) {
		this.propi.escribeXML(output);
	}

	public LFSSOMProperties getProps() {
		return propi;
	}

	public void setLayer(LFSGrowingLayer lay) {
		try {
			this.layer = (LFSGrowingLayer) lay.clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void clusteriza(int nclusters) {
		if (nclusters > 0) {
			LFSKMeans kmedias = new LFSKMeans(nclusters, this.getCodebook());
			setLabelAgrupados(kmedias.getResultados());
		} else {
			LFSWEKACluster em = new LFSWEKACluster(this.getCodebook());
			setLabelAgrupados(em.getResultados());
		}
	}

	public void clusterSelec(ArrayList<Integer> lista) {

		int liAg[] = new int[this.getLayer().getXSize()
				* this.getLayer().getYSize()];
		for (int w = 0; w < liAg.length; w++) {
			liAg[w] = 1;
		}

		for (int w = 0; w < lista.size(); w++) {
			liAg[lista.get(w)] = 0;
		}

		setLabelAgrupados(liAg);

	}

	private String getListLabels(int[] labels) {
		String lista = "";
		int xsize = this.getLayer().getXSize();
		int ysize = this.getLayer().getYSize();
		for (int j = 0; j < ysize; j++) {
			for (int i = 0; i < xsize; i++) {

				lista = lista + " " + String.valueOf(labels[j + i * ysize]);
			}
		}

		return lista.trim();
	}

	public String getListClusters() {
		return getListLabels(getLabelAgrupados());
	}

	private int[] listLabels(String lista) {
		String[] strbl = lista.split(" ");
		int temp = 0;
		int xsize = this.getLayer().getXSize();
		int ysize = this.getLayer().getYSize();

		int[] labels = new int[xsize * ysize];

		for (int j = 0; j < ysize; j++) {
			for (int i = 0; i < xsize; i++) {

				labels[j + i * ysize] = Integer.valueOf(strbl[temp++]);
			}
		}
		return labels;

	}

	public void setListClusters(String lista) {
		setLabelAgrupados(listLabels(lista));
	}

	public double[][] getCodebook() {
		int xsize = this.getLayer().getXSize();
		int ysize = this.getLayer().getYSize();
		int numLayers = this.getDimenData();

		double matrix[][] = new double[xsize * ysize][numLayers];
		for (int j = 0; j < ysize; j++) {
			for (int i = 0; i < xsize; i++) {
				LFSUnit u = null;
				try {
					u = this.getLayer().getUnit(i, j);
				} catch (LFSException e) {
					Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
							e.getMessage());
					System.exit(-1);
				}
				matrix[j + i * ysize] = u.getWeightVector();
			}
		}

		return matrix;

	}

	/**
	 * Returns the actual map layer.
	 * 
	 * @return the actual map layer
	 */
	public LFSGrowingLayer getLayer() {
		return layer;
	}

	/**
	 * Trains the map with the input data and training parameters specified in
	 * the properties provided by argument <code>props</code>. If the value of
	 * property <code>tau</code> is 1, a fix-sized layer is trained, otherwise
	 * the layer grows until a certain quality criterion determined by
	 * <code>tau</code> and the mean quantization error of the data (which is
	 * automatically calculated) is reached.
	 * 
	 * @param data
	 *            input data to train the map with.
	 * @param props
	 *            the training properties
	 */
	public void train(LFSData data, LFSSOMProperties props) {
		propi = props;
		// call training function depending on the properties (iterations ||
		// cycles)
		labelAtrib = new String[data.dim()];
		for (int i = 0; i < data.dim(); i++) {
			labelAtrib[i] = data.getLabel(i);
		}

		int cycles = props.numCycles();
		if (cycles < 1)
			cycles = 1;
		int iterationsToTrain = cycles * data.numVectors();

		layer.trainNormal(data, iterationsToTrain, 0, props, props.learnrate(),
				props.sigma());
	}

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

	public boolean isSubnet() {
		return this.subnet;
	}

	public void setIsSubnet(String sub) {
		this.subnet = Boolean.valueOf(sub);
	}

	public void setIsSubnet(boolean sub) {
		this.subnet = sub;
	}

	public String getStrIsSubnet() {
		return String.valueOf(this.subnet);
	}

	public void setVariablesval(String[] variablesval) {
		this.variablesval = variablesval;
	}

	public void setLabelAtrib(String atr) {
		String[] strbl = atr.split(",");
		labelAtrib = new String[strbl.length];
		for (int k = 0; k < strbl.length; k++) {
			labelAtrib[k] = strbl[k];
		}
	}

	public String getStrLabelAtrib() {
		String atr = "";
		for (int k = 0; k < labelAtrib.length; k++) {
			if (k > 0) {
				atr = atr + ",";
			}
			atr = atr + labelAtrib[k];
		}

		return atr;
	}

	public void setLabelAgrupados(String atr) {
		String[] strbl = atr.split(",");
		labelAgrupados = new int[strbl.length];
		for (int k = 0; k < strbl.length; k++) {
			labelAgrupados[k] = Integer.parseInt(strbl[k]);
		}
	}

	private String getStrLabel(int[] labels) {
		String atr = "";
		for (int k = 0; k < labels.length; k++) {
			if (k > 0) {
				atr = atr + ",";
			}
			atr = atr + String.valueOf(labels[k]);
		}

		return atr;

	}

	public String getStrLabelAgrupados() {

		return getStrLabel(getLabelAgrupados());
	}

	public void setError(String strError) {
		this.getLayer().setError(strError);
	}

	public String getStrError() {
		double errActualKaski = 0;
		double errActualQuan = 0;
		double errActualTopo = 0;

		try {
			if (this.getLayer().getQualityMeasure("KError") != null) {
				errActualKaski = this.getLayer().getQualityMeasure("KError")
						.getMapQuality("ID_Map");
				errActualQuan = this.getLayer().getQualityMeasure("QError")
						.getMapQuality("mqe");
				errActualTopo = this.getLayer().getQualityMeasure("TError")
						.getMapQuality("TE_Map");
			}
		} catch (LFSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return String.valueOf(errActualQuan) + " "
				+ String.valueOf(errActualTopo) + " "
				+ String.valueOf(errActualKaski);
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

	public void grabaTMP() {

		this.clusteriza(5);

		this.EscribeXML("d:/somtmp/fich.xml");
		this.escribeProps("d:/somtmp/fich.xmlprops");

	}

	/**
	 * @return Returns the labelAgrupados.
	 */
	public int[] getLabelAgrupados() {
		return labelAgrupados;
	}

	/**
	 * @param labelAgrupados
	 *            The labelAgrupados to set.
	 */
	public void setLabelAgrupados(int[] labelAgrupados) {
		this.labelAgrupados = labelAgrupados;
	}

	public void setGridTopology(String gt) {
	}

}
