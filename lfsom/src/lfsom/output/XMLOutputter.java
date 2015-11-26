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
	 * XML serialization of different classes: - LFSSelProps - LFSExpProps -
	 * LFSSOMProperties - LFSGrowingSOM
	 * 
	 * 
	 * 
	 */

	public static void createXML(LFSSelProps propi, String fName)
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

	public void createXML(LFSGrowingSOM gsom, String fName, double[] maxValues,
			double[] minValues) throws IOException {

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

			String strcomp_names = "";

			structComp.setAttribute("comp_names", strcomp_names);

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

					double[] vectorw = null;
					if (maxValues != null)
						vectorw = u.getWeightDVector(maxValues, minValues);
					else
						vectorw = u.getWeightVector();

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

}
