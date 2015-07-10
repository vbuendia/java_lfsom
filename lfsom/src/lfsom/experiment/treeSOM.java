/*
 * Copyright 2004-2010 Institute of Software Technology and Interactive Systems, Vienna University of Technology
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
 */
package lfsom.experiment;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Reads folders structure and generates an object which contains all nets
 * information. It's needed for the client, to navegate trough the nets
 * 
 * @author vicente
 * @version $Id: $
 */
public class treeSOM {

	private ArrayList<structNet> listaNets;

	public int size() {
		return listaNets.size();
	}

	public ArrayList<structNet> getLista() {
		return listaNets;
	}

	public structNet get(int i) {
		return listaNets.get(i);
	}

	public String[] lista(String campo) {
		String[] salida = new String[listaNets.size()];
		try {
			Field field = structNet.class.getDeclaredField(campo);
			for (int k = 0; k < listaNets.size(); k++) {
				salida[k] = (String) field.get(listaNets.get(k));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return salida;
	}

	public String[] lista_nombres() {
		return lista("netName");
	}

	public String[] lista_folders() {
		return lista("netFolder");
	}

	public int[] lista_father() {
		String[] listaNum = lista("netFatherNumber");
		int[] listaF = new int[listaNum.length];
		for (int k = 0; k < listaNum.length; k++) {
			listaF[k] = Integer.valueOf(listaNum[k]);
		}
		return listaF;
	}

	public ArrayList<structNet> redesIniciales() {

		ArrayList<structNet> sRedes = new ArrayList<structNet>();

		for (int i = 0; i < listaNets.size(); i++) {
			if (listaNets.get(i).netFatherFolder.equals("")) {
				sRedes.add(listaNets.get(i));
			}
		}

		return sRedes;
	}

	public ArrayList<structNet> subredesFrom(int k) {

		ArrayList<structNet> sRedes = new ArrayList<structNet>();

		for (int i = 0; i < listaNets.size(); i++) {
			if (listaNets.get(i).netFatherNumber.equals(String.valueOf(k))) {
				sRedes.add(listaNets.get(i));
			}
		}

		return sRedes;
	}

	public int subRedFromPos(int actualSOM, String xmlActual, int pos, int sY,
			int sX) {
		int[] layerSubnets = layerSubRedesFrom(actualSOM, xmlActual, sY, sX);
		return layerSubnets[pos];
	}

	public int[] layerSubRedesFrom(int actualSOM, String xmlActual, int sY,
			int sX) {
		int[] layer = new int[sY * sX];
		for (int k = 0; k < layer.length; k++) {
			layer[k] = 0;
		}

		// Ahora se saca una lista con todas las subredes y se pinta con un
		// color diferente para cada subred

		ArrayList<structNet> sFrom = subredesFrom(actualSOM, xmlActual);
		for (int k = 0; k < sFrom.size(); k++) {
			int[] lista = sFrom.get(k).cellsIntFrom();
			for (int w = 0; w < lista.length; w++) {
				layer[lista[w]] = sFrom.get(k).getIndice();
			}

		}

		return layer;
	}

	public float[] layerDibuSubRedesFrom(int actualSOM, String xmlActual,
			int sY, int sX) {
		float[] layer = new float[sY * sX];
		for (int k = 0; k < layer.length; k++) {
			layer[k] = 0;
		}
		int actColor = 1;
		// Ahora se saca una lista con todas las subredes y se pinta con un
		// color diferente para cada subred

		ArrayList<structNet> sFrom = subredesFrom(actualSOM, xmlActual);
		for (int k = 0; k < sFrom.size(); k++) {
			int[] lista = sFrom.get(k).cellsIntFrom();
			for (int w = 0; w < lista.length; w++) {
				layer[lista[w]] = actColor;
			}
			actColor++;
		}

		return layer;
	}

	public ArrayList<structNet> subredesFrom(int k, String fPadre) {

		ArrayList<structNet> sRedes = new ArrayList<structNet>();

		for (int i = 0; i < listaNets.size(); i++) {
			if (listaNets.get(i).netFatherNumber.equals(String.valueOf(k))
					&& listaNets.get(i).netFatherFile.equals(fPadre)) {
				sRedes.add(listaNets.get(i));
			}
		}

		return sRedes;
	}

	public void recorre(String direc, int padre, String padreFolder) {

		try {
			// Ahora se cargan los datos, si existe, del experimento
			File expProps = new File(direc + "/" + "ExpProps.xml");

			if (expProps.exists()) {

				File fXmlFile = new File(direc + "/" + "ExpProps.xml");
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder dBuilder;

				dBuilder = dbFactory.newDocumentBuilder();

				Document docu = dBuilder.parse(fXmlFile);
				docu.getDocumentElement().normalize();

				NodeList nList = docu.getElementsByTagName("atrib");
				structNet newNet = new structNet();

				// Se procesan todos los campos
				for (int temp = 0; temp < nList.getLength(); temp++) {

					Node nNode = nList.item(temp);

					if (nNode.getNodeType() == Node.ELEMENT_NODE) {

						Element eElement = (Element) nNode;

						String funcion = eElement
								.getElementsByTagName("funcion").item(0)
								.getTextContent();
						if (funcion.equals("setSubredOrigen")) {
							String valor = eElement
									.getElementsByTagName("valor").item(0)
									.getTextContent();
							newNet.cellsFrom = valor;
						}

						if (funcion.equals("setFPadre")) {
							String valor = eElement
									.getElementsByTagName("valor").item(0)
									.getTextContent();
							newNet.netFatherFile = valor;
						}

						if (funcion.equals("setExpName")) {
							String valor = eElement
									.getElementsByTagName("valor").item(0)
									.getTextContent();
							newNet.netName = valor;

						}

					}
				}

				newNet.netFolder = direc;
				newNet.netFatherNumber = String.valueOf(padre);
				newNet.netFatherFolder = padreFolder;

				int pos = listaNets.size();
				newNet.indice = pos;
				listaNets.add(newNet);
				File f = new File(direc);
				File[] list = f.listFiles();

				for (File element : list) {
					if (element.isDirectory()) {
						recorre(element.getAbsolutePath(), pos, direc);
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public treeSOM(String dataPath) {

		File f = new File(dataPath);
		File[] list = f.listFiles();
		listaNets = new ArrayList<structNet>();

		for (File element : list) {
			if (element.isDirectory()) {
				recorre(element.getAbsolutePath(), -1, "");
			}
		}

	}

}
