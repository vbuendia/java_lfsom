/*
 * 
 * This class is a variation of class of "IntrinsicDistance" class from:
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
package lfsom.layers.quality;

import java.util.ArrayList;

import lfsom.data.LFSData;
import lfsom.layers.LFSGrowingLayer;
import lfsom.layers.LFSUnit;
import lfsom.layers.metrics.HexMapDistancer;
import lfsom.layers.metrics.LFSL2Metric;
import lfsom.util.LFSException;

/**
 * Implementation of Intrinsic Distance Quality
 * 
 * @author Gerd Platzgummer
 * @version $Id: IntrinsicDistance.java 3883 2010-11-02 17:13:23Z frank $
 * 
 *          Modified for LFS by Vicente Buendia
 */
public class LFSKLDistance implements LFSQualityMeasure {

	// Whole map quality
	private double Map_Q = 0.0;

	// Quality previously assigned
	public LFSKLDistance(LFSGrowingLayer layer, String nmap) {
		Map_Q = Double.valueOf(nmap);
	}

	private double[] iniciaDijkstra(int size) {
		double[] dDijkstra = new double[size];

		for (int x0 = 0; x0 < size; x0++)
			dDijkstra[x0] = Double.POSITIVE_INFINITY;
		return dDijkstra;
	}

	private boolean[] iniciaVisto(int size) {
		boolean[] visto = new boolean[size];
		for (int x0 = 0; x0 < size; x0++)
			visto[x0] = false;
		return visto;
	}

	private boolean todoVisto(boolean[] visto) {
		boolean estaVisto = false;

		for (int y = 0; y < visto.length && !estaVisto; y++)
			if (visto[y])
				estaVisto = true;
		return estaVisto;
	}

	// Returns index of min Dij map
	private int minDij(ArrayList<Integer> cola, double[] dDijkstra) {
		int mini = -1;
		if (cola.size() > 0) {
			int minimo = 0;
			double minDist = dDijkstra[cola.get(0)];

			int act = 1;
			while (act < cola.size()) {
				if (minDist > dDijkstra[cola.get(act)]) {
					minimo = act;
					minDist = dDijkstra[cola.get(act)];
				}
				act++;
			}

			mini = cola.get(minimo);
			cola.remove(minimo);
		}
		return mini;
	}

	private int[] iniciaPadre(int tam) {
		int[] pa = new int[tam];
		for (int k = 0; k < tam; k++)
			pa[k] = -1;
		return pa;
	}

	public LFSKLDistance(LFSGrowingLayer layer, LFSData data) {

		double cam = 0;
		int xSize = layer.getXSize();
		int ySize = layer.getYSize();

		HexMapDistancer distan = new HexMapDistancer(xSize, ySize, true);

		double[] dDijkstra = new double[xSize * ySize];// Array which will
														// store Dijkstra
														// distances
		boolean[] visto = new boolean[xSize * ySize];
		int padre[] = new int[xSize * ySize];

		int samplecount = data.numVectors();
		try {
			for (int s = 0; s < samplecount; s++) {

				dDijkstra = iniciaDijkstra(xSize * ySize);
				padre = iniciaPadre(xSize * ySize);
				visto = iniciaVisto(xSize * ySize);

				LFSUnit[] winners = ((LFSGrowingLayer) layer).getWinners(
						data.getInputDatum(s), 2);

				LFSUnit bmu = winners[0];
				LFSUnit sbmu = winners[1];

				dDijkstra[bmu.getPos(xSize)] = 0;

				ArrayList<Integer> cola = new ArrayList<Integer>();

				cola.add(bmu.getPos(xSize));
				// Ahora se calcula el coste de la ruta entre bmu y sbmu
				while (cola.size() > 0 && !visto[sbmu.getPos(xSize)]) {
					int minim = minDij(cola, dDijkstra);
					visto[minim] = true;
					// Del minimo se toman sus vecinos, se calculan las
					// distancias y
					// se incluyen en la cola
					distan.listaProximos(minim, 1);
					ArrayList<Integer> verTratar = distan.getIncluidos();
					for (int k = 0; k < verTratar.size(); k++) {
						int uActual = verTratar.get(k);
						if (!visto[uActual] && uActual != minim) {

							double[] vec1 = layer.getUnit(uActual)
									.getWeightVector();
							double[] vec2 = layer.getUnit(minim)
									.getWeightVector();
							double distancia = LFSL2Metric.distance(vec1, vec2);

							if (dDijkstra[uActual] > dDijkstra[minim]
									+ distancia) {
								dDijkstra[uActual] = dDijkstra[minim]
										+ distancia;
								cola.add(uActual);
								padre[uActual] = minim;
							}
						}
					}
				}

				cam += dDijkstra[sbmu.getPos(xSize)]
						+ LFSL2Metric.distance(bmu.getWeightVector(), data
								.getInputDatum(s).getVector().toArray());
			}

		} catch (LFSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map_Q = cam / samplecount;
	}

	/**
	 * Calculate KL Index
	 * 
	 * @param layer
	 * @param data
	 */
	public void LFSKLDistance_ant(LFSGrowingLayer layer, LFSData data) {

		int xSize1 = layer.getXSize();
		int ySize1 = layer.getYSize();

		/** *********Sum1: Quantization Error */

		try {
			Map_Q = new LFSQuantizationError(layer, data).getMapQuality("mqe");
		} catch (LFSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/** *********Sum 2: Shortest path between BMU2 and BMU2 */

		HexMapDistancer distan = new HexMapDistancer(xSize1, ySize1, true);
		int[][][][] distancias = distan.map();

		int samplecount = data.numVectors();

		for (int s = 0; s < samplecount; s++) {
			LFSUnit[] winners = ((LFSGrowingLayer) layer).getWinners(
					data.getInputDatum(s), 2);

			LFSUnit bmu = winners[0];
			LFSUnit sbmu = winners[1];

			Map_Q += distancias[bmu.getXPos()][bmu.getYPos()][sbmu.getXPos()][sbmu
					.getYPos()];

		}

		Map_Q = Map_Q / samplecount;

	}

	/**
	 * Get map quality
	 */

	public double getMapQuality(String name) throws LFSException {
		if (name.equals("ID_Map")) {
			return Map_Q;
		} else {
			throw new LFSException("Quality measure with name " + name
					+ " not found.");
		}
	}

	/**
	 * Compatibility function
	 */

	public double[][] getUnitQualities(String name) throws LFSException {

		throw new LFSException("UnitQualities not implemented for KLDistance.");

	}

}
