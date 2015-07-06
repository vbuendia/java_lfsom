/*
 * 
 * This class is a variation of "HTMLOutputter" class from:
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
package lfsom.output;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import lfsom.layers.LFSUnit;
import lfsom.models.LFSGrowingSOM;
import lfsom.properties.LFSExpProps;
import lfsom.properties.LFSSOMProperties;
import lfsom.properties.LFSSelProps;
import lfsom.util.LFSException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLOutputter {

	/**
	 * Writes the XML representation of a GHSOM to a file.
	 * 
	 * @param ghsom
	 *            The GHSOM to be written.
	 * @param fDir
	 *            Directory where to write the file to.
	 * @param fName
	 *            Filename without suffix. Usually the name of the training run.
	 * @throws TransformerException
	 */

	public void createXML(LFSSelProps propi, String fName) throws IOException {

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("expprops");
			doc.appendChild(rootElement);

			for (int k = 0; k < propi.getVariables().length; k++) {
				Element config = doc.createElement("atrib");

				Element funci = doc.createElement("funcion");
				funci.appendChild(doc.createTextNode(propi.getVariables(k)));
				config.appendChild(funci);

				Element valo = doc.createElement("valor");
				valo.appendChild(doc.createTextNode(propi.getVariablesval(k)));
				config.appendChild(valo);

				// config.setAttribute(propi.getVariables(k),
				// propi.getVariablesval(k));

				rootElement.appendChild(config);
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(fName));

			transformer.transform(source, result);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void createXML(LFSExpProps propi, String fName) throws IOException {

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("expprops");
			doc.appendChild(rootElement);

			for (int k = 0; k < propi.getVariables().length; k++) {
				Element config = doc.createElement("atrib");

				Element funci = doc.createElement("funcion");
				funci.appendChild(doc.createTextNode(propi.getVariables(k)));
				config.appendChild(funci);

				Element valo = doc.createElement("valor");
				valo.appendChild(doc.createTextNode(propi.getVariablesval(k)));
				config.appendChild(valo);

				// config.setAttribute(propi.getVariables(k),
				// propi.getVariablesval(k));

				rootElement.appendChild(config);
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(fName));

			transformer.transform(source, result);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void createXML(LFSSOMProperties propi, String fName)
			throws IOException {

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("expprops");
			doc.appendChild(rootElement);

			for (int k = 0; k < propi.getVariables().length; k++) {
				Element config = doc.createElement("atrib");

				Element funci = doc.createElement("funcion");
				funci.appendChild(doc.createTextNode(propi.getVariables(k)));
				config.appendChild(funci);

				Element valo = doc.createElement("valor");
				valo.appendChild(doc.createTextNode(propi.getVariablesval(k)));
				config.appendChild(valo);

				// config.setAttribute(propi.getVariables(k),
				// propi.getVariablesval(k));

				rootElement.appendChild(config);
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(fName));

			transformer.transform(source, result);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void createXMLsimple(LFSGrowingSOM gsom, String fName)
			throws IOException {

		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("som_xml");
			doc.appendChild(rootElement);

			// STRUCT
			// *************************************************************
			Element structComp = doc.createElement("struct");

			// comp_names
			String strcomp_names = "";

			// Falta por poner los atributos
			structComp.setAttribute("comp_names", strcomp_names);
			// num_layers, topolx, topoly
			int numLayers = gsom.getDimenData();
			structComp
					.setAttribute("num_layers", String.valueOf(numLayers + 2));
			structComp.setAttribute("topolx",
					String.valueOf(gsom.getLayer().getXSize()));
			structComp.setAttribute("topoly",
					String.valueOf(gsom.getLayer().getYSize()));
			structComp.setAttribute("QError", "0");
			structComp.setAttribute("KError", "0");
			structComp.setAttribute("TError", "0");
			rootElement.appendChild(structComp);

			List<Element> layerSom = new ArrayList<Element>();
			String[] strValoresDen = new String[numLayers];
			for (int i = 0; i < numLayers; i++) {
				layerSom.add(doc.createElement("layer"));
				layerSom.get(i).setAttribute("name", gsom.getLabel(i));
				layerSom.get(i).setAttribute("show_cuad", "true");
				strValoresDen[i] = "";
			}
			for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
				for (int i = 0; i < gsom.getLayer().getXSize(); i++) {

					LFSUnit u = null;

					try {
						u = gsom.getLayer().getUnit(i, j);
					} catch (LFSException e) {
						Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
								e.getMessage());
						System.exit(-1);
					}

					double[] vectorw = u.getWeightVector();
					// LAYERS
					// **************************************************************
					for (int k = 0; k < numLayers; k++) {
						strValoresDen[k] = strValoresDen[k] + " "
								+ String.valueOf(vectorw[k]);
					}

				}
			}

			for (int i = 0; i < numLayers; i++) {
				layerSom.get(i).appendChild(
						doc.createTextNode(strValoresDen[i].trim()));
				rootElement.appendChild(layerSom.get(i));
			}

			// HIT LAYER
			// ************************************************************************
			Element layerHitSom = doc.createElement("layer");
			layerHitSom.setAttribute("name", "(Hits)");
			layerHitSom.setAttribute("show_cuad", "true");

			String numHits = "";
			for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
				for (int i = 0; i < gsom.getLayer().getXSize(); i++) {
					numHits = numHits + " 0";
				}
			}

			layerHitSom.appendChild(doc.createTextNode(numHits.trim()));
			rootElement.appendChild(layerHitSom);

			// CLUSTER LAYER

			Element layerClusterSom = doc.createElement("layer");
			layerClusterSom.setAttribute("name", "(Cluster)");
			layerClusterSom.setAttribute("show_cuad", "false");

			layerClusterSom.appendChild(doc.createTextNode(numHits.trim()));
			rootElement.appendChild(layerClusterSom);

			for (int k = 0; k < gsom.getVariables().length; k++) {
				if (k != 3) {
					Element config = doc.createElement("atrib");

					Element funci = doc.createElement("funcion");
					funci.appendChild(doc.createTextNode(gsom.getVariables(k)));
					config.appendChild(funci);

					Element valo = doc.createElement("valor");
					valo.appendChild(doc.createTextNode(gsom.getVariablesval(k)));
					config.appendChild(valo);

					// config.setAttribute(propi.getVariables(k),
					// propi.getVariablesval(k));

					rootElement.appendChild(config);
				}
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(fName));

			transformer.transform(source, result);

		} catch (Exception e) {
			System.out.println(e);
		}

	}

	/**
	 * Creates the string containing the HTML representation of a map.
	 * 
	 * @param gsom
	 *            The GrowingSOM to be written.
	 * @param fDir
	 *            Directory where to write the file.
	 * @param fName
	 *            Filename without suffix. Usually the name of the training run.
	 * @param minmax
	 *            Array of double containing the minima and maxima of distances
	 *            between data items and weight vectors, and label values
	 *            respectively. These values are used for coloring. [0] minimum
	 *            distance, [1] maximum distance, [2] minimum label value, [3]
	 *            maximum label value.
	 * @param dataNames
	 *            Array of strings containing data items to highlight on the map
	 * @return String containing the HTML representation.
	 */
	public void createXML(LFSGrowingSOM gsom, String fName) throws IOException {

		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("som_xml");
			doc.appendChild(rootElement);

			// STRUCT
			// *************************************************************
			Element structComp = doc.createElement("struct");

			// comp_names
			String strcomp_names = "";

			// Falta por poner los atributos
			structComp.setAttribute("comp_names", strcomp_names);
			// num_layers, topolx, topoly
			int numLayers = gsom.getDimenData();
			structComp
					.setAttribute("num_layers", String.valueOf(numLayers + 2));
			structComp.setAttribute("topolx",
					String.valueOf(gsom.getLayer().getXSize()));
			structComp.setAttribute("topoly",
					String.valueOf(gsom.getLayer().getYSize()));
			try {
				structComp.setAttribute(
						"QError",
						String.valueOf(gsom.getLayer()
								.getQualityMeasure("QError")
								.getMapQuality("mqe")));
				structComp.setAttribute(
						"KError",
						String.valueOf(gsom.getLayer()
								.getQualityMeasure("KError")
								.getMapQuality("ID_Map")));
				structComp.setAttribute(
						"TError",
						String.valueOf(gsom.getLayer()
								.getQualityMeasure("TError")
								.getMapQuality("TE_Map")));
			} catch (Exception e) {
				e.printStackTrace();
			}
			rootElement.appendChild(structComp);

			List<Element> layerSom = new ArrayList<Element>();
			String[] strValoresDen = new String[numLayers];
			for (int i = 0; i < numLayers; i++) {
				layerSom.add(doc.createElement("layer"));
				layerSom.get(i).setAttribute("name", gsom.getLabel(i));
				layerSom.get(i).setAttribute("show_cuad", "true");
				strValoresDen[i] = "";
			}
			for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
				for (int i = 0; i < gsom.getLayer().getXSize(); i++) {

					LFSUnit u = null;

					try {
						u = gsom.getLayer().getUnit(i, j);
					} catch (LFSException e) {
						Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(
								e.getMessage());
						System.exit(-1);
					}

					double[] vectorw = u.getWeightVector();
					// LAYERS
					// **************************************************************
					for (int k = 0; k < numLayers; k++) {
						strValoresDen[k] = strValoresDen[k] + " "
								+ String.valueOf(vectorw[k]);
					}

				}
			}

			for (int i = 0; i < numLayers; i++) {
				layerSom.get(i).appendChild(
						doc.createTextNode(strValoresDen[i].trim()));
				rootElement.appendChild(layerSom.get(i));
			}

			// HIT LAYER
			// ************************************************************************
			Element layerHitSom = doc.createElement("layer");
			layerHitSom.setAttribute("name", "(Hits)");
			layerHitSom.setAttribute("show_cuad", "true");

			String numHits = "";
			for (int j = 0; j < gsom.getLayer().getYSize(); j++) {
				for (int i = 0; i < gsom.getLayer().getXSize(); i++) {

					LFSUnit u = null;
					u = gsom.getLayer().getUnit(i, j);

					numHits = numHits + " "
							+ String.valueOf(u.getNumberOfMappedInputs());
				}
			}

			layerHitSom.appendChild(doc.createTextNode(numHits.trim()));
			rootElement.appendChild(layerHitSom);

			// CLUSTER LAYER

			Element layerClusterSom = doc.createElement("layer");
			layerClusterSom.setAttribute("name", "(Cluster)");
			layerClusterSom.setAttribute("show_cuad", "false");

			layerClusterSom.appendChild(doc.createTextNode(gsom
					.getListClusters()));
			rootElement.appendChild(layerClusterSom);

			for (int k = 0; k < gsom.getVariables().length; k++) {
				Element config = doc.createElement("atrib");

				Element funci = doc.createElement("funcion");
				funci.appendChild(doc.createTextNode(gsom.getVariables(k)));
				config.appendChild(funci);

				Element valo = doc.createElement("valor");
				valo.appendChild(doc.createTextNode(gsom.getVariablesval(k)));
				config.appendChild(valo);

				// config.setAttribute(propi.getVariables(k),
				// propi.getVariablesval(k));

				rootElement.appendChild(config);
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(fName));

			transformer.transform(source, result);

		} catch (Exception e) {
			System.out.println(e);
		}

	}
	// public void write(GrowingSOM gsom, String fName, Labeller label) throws
	// IOException { // daten bereits gemappt
	// }

	// public void write(GrowingSOM gsom, InputData data, String fName, Labeller
	// label) throws IOException { // daten
	// mappen und schreiben
	// }

}
