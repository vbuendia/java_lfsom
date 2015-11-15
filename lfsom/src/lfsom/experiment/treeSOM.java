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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.tuwien.ifs.somtoolbox.util.FileUtils;

/**
 * Reads folders structure and generates an object which contains all nets
 * information. It's needed for the client, to navegate trough the nets
 * 
 * @author vicente
 * @version $Id: $
 */
public class treeSOM {

	/**
	 * It maintains a list of nets, each with indexes to its father.
	 */
	private ArrayList<structNet> listaNets;

	public int size() {
		return listaNets.size();
	}

	public ArrayList<structNet> getLista() {
		return listaNets;
	}

	/**
	 * Gets the net indexed by i
	 * 
	 * @param i
	 * @return
	 */
	public structNet get(int i) {
		return listaNets.get(i);
	}

	/**
	 * Returns a string list containing only a field: "campo" of all the nets
	 * 
	 * @param campo
	 * @return
	 */
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

	/**
	 * Returns a list containing all the net names.
	 * 
	 * @return
	 */
	public String[] lista_nombres() {
		return lista("netName");
	}

	/**
	 * Returns a list containing all the folders of the nets
	 * 
	 * @return
	 */

	public String[] lista_folders() {
		return lista("netFolder");
	}

	/**
	 * Gives a list of indexes referencing each father, for each net.
	 * 
	 * @return
	 */

	public int[] lista_father() {
		String[] listaNum = lista("netFatherNumber");
		int[] listaF = new int[listaNum.length];
		for (int k = 0; k < listaNum.length; k++) {
			listaF[k] = Integer.valueOf(listaNum[k]);
		}
		return listaF;
	}

	/**
	 * Returns a list of all nets which don't have a father (root nets).
	 * 
	 * @return
	 */
	public ArrayList<structNet> redesIniciales() {

		ArrayList<structNet> sRedes = new ArrayList<structNet>();

		for (int i = 0; i < listaNets.size(); i++) {
			if (listaNets.get(i).netFatherFolder.equals("")) {
				sRedes.add(listaNets.get(i));
			}
		}

		return sRedes;
	}

	/**
	 * Gives a list of all the nets whose father is "k"
	 * 
	 * @param k
	 * @return
	 */
	public ArrayList<structNet> subredesFrom(int k) {

		ArrayList<structNet> sRedes = new ArrayList<structNet>();

		for (int i = 0; i < listaNets.size(); i++) {
			if (listaNets.get(i).netFatherNumber.equals(String.valueOf(k))) {
				sRedes.add(listaNets.get(i));
			}
		}

		return sRedes;
	}

	/**
	 * 
	 * Returns a net whose father is this and trained from sX, sY neuron
	 * 
	 * @param actualSOM
	 * @param xmlActual
	 * @param pos
	 * @param sY
	 * @param sX
	 * @return
	 */
	public int subRedFromPos(int actualSOM, String xmlActual, int pos, int sY,
			int sX) {
		int[] layerSubnets = layerSubRedesFrom(actualSOM, xmlActual, sY, sX);
		return layerSubnets[pos];
	}

	/**
	 * Returns an entire layer whose neuron colors indicate different sons, to
	 * be able to navigate to sons
	 * 
	 * @param actualSOM
	 * @param xmlActual
	 * @param sY
	 * @param sX
	 * @return
	 */

	public int[] layerSubRedesFrom(int actualSOM, String xmlActual, int sY,
			int sX) {
		int[] layer = new int[sY * sX];
		for (int k = 0; k < layer.length; k++) {
			layer[k] = 0;
		}

		// Now it gets a list with all subnets and every subnet is painted with
		// a different color

		ArrayList<structNet> sFrom = subredesFrom(actualSOM, xmlActual);
		for (int k = 0; k < sFrom.size(); k++) {
			int[] lista = sFrom.get(k).cellsIntFrom();
			for (int w = 0; w < lista.length; w++) {
				layer[lista[w]] = sFrom.get(k).getIndice();
			}

		}

		return layer;
	}

	/**
	 * Returns an entire layer whose neuron colors indicate different sons, to
	 * be able to navigate to sons
	 * 
	 * @param actualSOM
	 * @param xmlActual
	 * @param sY
	 * @param sX
	 * @return
	 */
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

	/**
	 * Runs over all the folders structure to insert the nets saved into memory.
	 * 
	 * @param direc
	 * @param padre
	 * @param padreFolder
	 */

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

				// Se cargan los datos del kaski

				fXmlFile = new File(direc + "/" + "kaski.xml");
				dbFactory = DocumentBuilderFactory.newInstance();

				dBuilder = dbFactory.newDocumentBuilder();

				docu = dBuilder.parse(fXmlFile);
				docu.getDocumentElement().normalize();

				nList = docu.getElementsByTagName("struct");
				NamedNodeMap atri = nList.item(0).getAttributes();
				String qErr = atri.getNamedItem("QError").getNodeValue()
						.toString();
				String topoly = atri.getNamedItem("topoly").getNodeValue()
						.toString();
				String topolx = atri.getNamedItem("topolx").getNodeValue()
						.toString();
				int dimen = Integer.valueOf(topoly) * Integer.valueOf(topolx);

				newNet.mQ = String.valueOf(Double.valueOf(qErr));
				newNet.numCells = dimen;
				// ///////

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

	/**
	 * When inited, runs all folder structure and gets a list with all nets,
	 * containing information of fathers and cells
	 * 
	 * @param dataPath
	 */

	public treeSOM(String dataPath) {

		File f = new File(dataPath);
		File[] list = f.listFiles();
		listaNets = new ArrayList<structNet>();

		for (File element : list) {
			if (element.isDirectory()) {
				recorre(element.getAbsolutePath(), -1, "");
			}
		}
		// escribeDatos();

	}

	public void escribeDatos() {
		// Escribe todos los datos en un fichero
		PrintWriter writer;
		try {
			writer = FileUtils.openFileForWriting("CVS", "comparacsv.csv",
					false);

			writer.print("nombre" + ";" + "Indice" + ";" + "padre" + ";"
					+ "Nro. neuronas" + ";" + "Err");
			writer.println();
			for (int i = 0; i < listaNets.size(); i++) {
				structNet tS = listaNets.get(i);
				String cad = tS.netName + ";" + tS.indice + ";"
						+ tS.netFatherNumber + ";" + tS.numCells + ";" + tS.mQ
						+ ";" + tS.netFolder;
				writer.print(cad);
				writer.println();
			}

			writer.close();

			writer = FileUtils.openFileForWriting("CVS", "capas.csv", false);

			// Ahora calcula un poco mejor...
			int etapa = -1;
			boolean fin = false;

			ArrayList<Integer> listSig = new ArrayList<Integer>();
			ArrayList<Integer> listAct = new ArrayList<Integer>();
			listAct.add(-1);
			writer.print("Etapa" + ";" + "Num Capas" + ";" + "Num Neuro" + ";"
					+ "MQErr" + ";" + "Num Datos");
			writer.println();
			while (!fin) {

				// Se recorre toda la red y se aumenta la etapa
				fin = true;
				etapa++;

				int nRedes = 0;
				int nCells = 0;
				double qErr = 0;
				int nDatos = 0;

				for (int i = 0; i < listaNets.size(); i++) {
					structNet tS = listaNets.get(i);
					if (listAct.contains(Integer.valueOf(tS.netFatherNumber))) {
						fin = false;
						nRedes++;
						qErr += Double.valueOf(tS.mQ);
						nCells += Integer.valueOf(tS.numCells);
						listSig.add(tS.indice);

						// Se accede a su carpeta, a data.csv y se comprueba el
						// nro. de lineas

						BufferedReader lector = FileUtils.openFile("CSV",
								tS.getNetFolder() + "/data.csv");
						String sCurrentLine;
						int tmpNDatos = -1;
						while ((sCurrentLine = lector.readLine()) != null) {
							tmpNDatos++;
						}
						nDatos += tmpNDatos;

					}

				}

				if (!fin) { // Si no ha terminado, que ponga resultados
					Double MQErr = qErr / nRedes;

					// Termina de procesar una capa. Se escribe y se actualiza
					// la
					// lista de indices de la siguiente capa
					writer.print(String.valueOf(etapa) + ";"
							+ String.valueOf(nRedes) + ";"
							+ String.valueOf(nCells) + ";"
							+ String.valueOf(MQErr).replace(".", ",") + ";"
							+ String.valueOf(nDatos));
					writer.println();

					// Actualiza las listas.
					listAct.clear();
					for (int i = 0; i < listSig.size(); i++)
						listAct.add(listSig.get(i));
					listSig.clear();
				}

			}
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
