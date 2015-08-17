/*
 * 
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
public class LFSSelProps {

	// Properties selected by the user, help to interface

	private boolean EnableCell = true;

	private boolean SelRadius = true;

	private int Radius = 2;

	private boolean SelCustom = false;

	private boolean SelCluster = false;

	private boolean SelSubnet = false;

	private String[] variables = { "setEnableCell", "setRadius", "setSel" };

	private String[] variablesval = { "getStrEnableCell", "getStrRadius",
			"getStrSel" };

	public void setEnableCell(String ena) {
		EnableCell = Boolean.valueOf(ena);
	}

	public boolean getEnableCell() {
		return isEnableCell();
	}

	public String getStrEnableCell() {
		return String.valueOf(isEnableCell());
	}

	public void setRadius(int rad) {
		Radius = rad;
	}

	public void setRadius(String rad) {
		Radius = Integer.valueOf(rad);
	}

	public String getStrRadius() {
		return String.valueOf(Radius);
	}

	public void setSel(String selec) {
		SelCustom = false;
		SelCluster = false;
		SelRadius = false;
		if (selec.equals("SelCustom")) {
			SelCustom = true;
		}
		if (selec.equals("SelCluster")) {
			SelCluster = true;
		}
		if (selec.equals("SelRadius")) {
			SelRadius = true;
		}

	}

	public String getStrSel() {
		String sel = "";

		if (SelCustom) {
			sel = "SelCustom";
		}
		if (SelRadius) {
			sel = "SelRadius";
		}
		if (SelCluster) {
			sel = "SelCluster";
		}
		return sel;
	}

	public String[] getVariables() {
		return variables;
	}

	public String getVariables(int k) {
		return variables[k];
	}

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

	public void EscribeXML(String output) {
		try {
			XMLOutputter salida = new XMLOutputter();
			salida.createXML(this, output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public LFSSelProps() {
	}

	public LFSSelProps(String fName) throws ParserConfigurationException,
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
	 * @return Returns the selRadius.
	 */
	public boolean isSelRadius() {
		return SelRadius;
	}

	/**
	 * @param selRadius
	 *            The selRadius to set.
	 */
	public void setSelRadius() {
		SelRadius = true;
		SelCustom = false;
		SelCluster = false;
	}

	public void setSelSubnet(boolean sSub) {
		SelSubnet = sSub;
	}

	public boolean isSelSubnet() {
		return SelSubnet;
	}

	/**
	 * @return Returns the selCustom.
	 */
	public boolean isSelCustom() {
		return SelCustom;
	}

	/**
	 * @param selCustom
	 *            The selCustom to set.
	 */
	public void setSelCustom() {
		SelRadius = false;
		SelCustom = true;
		SelCluster = false;
	}

	/**
	 * @return Returns the selCluster.
	 */
	public boolean isSelCluster() {
		return SelCluster;
	}

	/**
	 * @param selCluster
	 *            The selCluster to set.
	 */
	public void setSelCluster() {
		SelRadius = false;
		SelCustom = false;
		SelCluster = true;
	}

	/**
	 * @return Returns the enableCell.
	 */
	public boolean isEnableCell() {
		return EnableCell;
	}

	/**
	 * @param enableCell
	 *            The enableCell to set.
	 */
	public void setEnableCell(boolean enableCell) {
		EnableCell = enableCell;
	}

}
