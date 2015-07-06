/*
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
package lfsom.experiment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import lfsom.data.LFSData;
import lfsom.data.LFSDataCSVWriter;
import lfsom.layers.LFSGrowingLayer;
import lfsom.layers.LFSUnit;
import lfsom.layers.quality.LFSQuantizationError;
import lfsom.models.LFSGrowingSOM;
import lfsom.properties.LFSExpProps;
import lfsom.properties.LFSSOMProperties;
import lfsom.util.LFSPCA;

/**
 * @author vicente
 * @version $Id: $
 */
public class TrainSelector {

	private boolean Calculando = false;

	private int Progreso = 0;

	private double MedidaKaski = Double.POSITIVE_INFINITY;

	private double MedidaTopo = Double.POSITIVE_INFINITY;

	private double MedidaQuan = Double.POSITIVE_INFINITY;

	private CountDownLatch doneSignal;

	private int numIter = 0;

	private LFSData datosResul = null;

	private double[][] matrixResults;

	private LFSUnit[][] unitsPCA, unitsVector, unitsInterval;

	private LFSUnit[][] unitsPCABatch, unitsVectorBatch, unitsIntervalBatch;

	private int indiceResul = 0;

	// private String fTopo;

	// private String fData;

	private String expName = "SOM";

	private boolean cancelado = false;

	private long iteact = 0;

	private LFSGrowingSOM mapaMejorKaski;
	private LFSGrowingSOM mapaMejorQuan;
	private LFSGrowingSOM mapaMejorTopo;

	private final Semaphore sema = new Semaphore(2);

	private int minDatosExp = 300;

	private int prof = 1;

	private int maxProf = 6;

	public TrainSelector() {
	}

	private TrainSelector(int profun) {
		prof = profun;
	}

	private int[] calculaDimen(LFSData data, int x, int y, boolean isHier,
			boolean isBatch, int nWidth) {
		int[] dimen = new int[2];

		if (x == 0 || y == 0) {
			if (isHier) {
				dimen[1] = 2;
				dimen[0] = 2;
			} else {
				double numceldas = Math.pow(data.getData().length, 0.54321);

				double ratio = Math.sqrt(data.getPCA().getFirstAxisIndex()
						/ data.getPCA().getSecondAxisIndex());
				dimen[1] = (int) Math.min(numceldas,
						Math.round(Math.sqrt(numceldas / ratio)));
				dimen[0] = (int) (numceldas / dimen[1]);
			}
		} else {
			dimen[1] = y;
			dimen[0] = x;
		}

		if (isBatch && dimen[1] < 2 * nWidth) {
			dimen[1] = 2 * nWidth;
		}
		if (isBatch && dimen[0] < 2 * nWidth) {
			dimen[0] = 2 * nWidth;
		}
		return dimen;
	}

	private void calculaInitLayers(LFSData data, int xSize, int ySize) {

		Random rand = new Random();
		unitsInterval = new LFSUnit[xSize][ySize];
		unitsVector = new LFSUnit[xSize][ySize];

		for (int j = 0; j < ySize; j++) {
			for (int i = 0; i < xSize; i++) {
				unitsInterval[i][j] = new LFSUnit(data, i, j, data.dim(), rand,
						true, LFSUnit.INIT_INTERVAL_INTERPOLATE);
				unitsVector[i][j] = new LFSUnit(data, i, j, data.dim(), rand,
						true, LFSUnit.INIT_VECTOR);

			}
		}
	}

	private void calculaInitLayersBatch(LFSData data, int xSize, int ySize) {

		Random rand = new Random();
		unitsIntervalBatch = new LFSUnit[xSize][ySize];
		unitsVectorBatch = new LFSUnit[xSize][ySize];

		for (int j = 0; j < ySize; j++) {
			for (int i = 0; i < xSize; i++) {
				unitsIntervalBatch[i][j] = new LFSUnit(data, i, j, data.dim(),
						rand, true, LFSUnit.INIT_INTERVAL_INTERPOLATE);
				unitsVectorBatch[i][j] = new LFSUnit(data, i, j, data.dim(),
						rand, true, LFSUnit.INIT_VECTOR);

			}
		}
	}

	private LFSUnit[][] calculaUnitsPCA(LFSData data, int xSize, int ySize) {

		double[][] dataArray = data.getData();
		double[][] projectedDataArray = new double[data.numVectors()][2];
		int dim = dataArray[0].length;

		LFSUnit[][] uPCA = new LFSUnit[xSize][ySize];

		//
		// project the data points
		//
		LFSPCA pca = data.getPCA();

		for (int i = 0; i < data.numVectors(); i++) {
			float xProj = 0.f;
			for (int j = 0; j < dim; j++) {
				xProj += dataArray[i][j] * pca.U[pca.getFirstAxisIndex()][j];
			}

			projectedDataArray[i][0] = xProj;

			float yProj = 0.f;
			for (int j = 0; j < dim; j++) {
				yProj += dataArray[i][j] * pca.U[pca.getSecondAxisIndex()][j];
			}

			projectedDataArray[i][1] = yProj;
		}

		// find minX,minY,maxX,maxY
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;
		for (int i = 0; i < data.numVectors(); i++) {
			if (projectedDataArray[i][0] < minX) {
				minX = projectedDataArray[i][0];
			}
			if (projectedDataArray[i][1] < minY) {
				minY = projectedDataArray[i][1];
			}

			if (projectedDataArray[i][0] > maxX) {
				maxX = projectedDataArray[i][0];
			}
			if (projectedDataArray[i][1] > maxY) {
				maxY = projectedDataArray[i][1];
			}
		}

		double diffX = maxX - minX;
		double diffY = maxY - minY;
		double cellSizeX = diffX / xSize;
		double cellSizeY = diffY / ySize;

		// System.out.println("");
		// System.out.println("  *** diffX: " + diffX + " diffY: " + diffY);

		// ...

		for (int j = 0; j < ySize; j++) {
			for (int i = 0; i < xSize; i++) {
				// find the closes point in the data point cloud
				int closestPointIndex = -1;
				double closesPointDist = Double.MAX_VALUE;

				for (int curPoint = 0; curPoint < data.numVectors(); curPoint++) {
					double[] curCellCoords = new double[2];
					curCellCoords[0] = i * cellSizeX + cellSizeX / 2;
					curCellCoords[1] = j * cellSizeY + cellSizeY / 2;

					double curPointDist = Math.sqrt(Math.pow(
							projectedDataArray[curPoint][0] - curCellCoords[0],
							2)
							+ Math.pow(projectedDataArray[curPoint][1]
									- curCellCoords[1], 2));

					if (curPointDist < closesPointDist) {
						closesPointDist = curPointDist;
						closestPointIndex = curPoint;
					}
				}

				double[] closesPointVec = new double[dim];
				for (int l = 0; l < dim; l++) {
					closesPointVec[l] = dataArray[closestPointIndex][l];
				}

				uPCA[i][j] = new LFSUnit(null, i, j, closesPointVec);
			}
		}

		return uPCA;
	}

	private void cuerpoTrain(LFSData datos1, LFSSOMProperties propsMapa,
			double xmulti) {

		try {
			LFSSOMProperties props = propsMapa.copia();

			if (!this.cancelado) {

				LFSGrowingSOM mapaActivo2 = new LFSGrowingSOM(
						props.getExpName());

				if (!propsMapa.batchSom()) {
					switch (props.getInitializationMode()) {

					case LFSUnit.INIT_INTERVAL_INTERPOLATE:
						mapaActivo2.initLayer(true, props, datos1,
								unitsInterval);
						break;
					case LFSUnit.INIT_VECTOR:
						mapaActivo2.initLayer(true, props, datos1, unitsVector);
						break;
					case LFSUnit.INIT_PCA:
						mapaActivo2.initLayer(true, props, datos1, unitsPCA);

						break;
					default:
						mapaActivo2.initLayer(true, props, datos1, null);
					}
				} else {
					switch (props.getInitializationMode()) {

					case LFSUnit.INIT_INTERVAL_INTERPOLATE:
						mapaActivo2.initLayer(true, props, datos1,
								unitsIntervalBatch);
						break;
					case LFSUnit.INIT_VECTOR:
						mapaActivo2.initLayer(true, props, datos1,
								unitsVectorBatch);
						break;
					case LFSUnit.INIT_PCA:
						mapaActivo2.initLayer(true, props, datos1,
								unitsPCABatch);

						break;
					default:
						mapaActivo2.initLayer(true, props, datos1, null);
					}
				}

				mapaActivo2.train(datos1, props);

				// InputData subMuestra = datos1.getSubMuestra(3000);
				mapaActivo2.getLayer().mapCompleteDataAfterTraining(datos1);
				mapaActivo2.getLayer().calcQuality(datos1);

				double errActualQuan = mapaActivo2.getLayer()
						.getQualityMeasure("QError").getMapQuality("mqe");
				double errActualTopo = mapaActivo2.getLayer()
						.getQualityMeasure("TError").getMapQuality("TE_Map");
				double errActualKaski = mapaActivo2.getLayer()
						.getQualityMeasure("KError").getMapQuality("ID_Map");

				try {
					sema.acquire();

					double[] arrRes = new double[] { errActualTopo,
							errActualQuan, errActualKaski, props.learnrate(),
							props.batchSom() ? 1 : 0, props.sigma(),
							props.getInitializationMode(),
							props.getNeighbourFunc(), props.pcNeighbourWidth(),
							props.isGrowing() ? 1 : 0 };
					matrixResults[indiceResul++] = arrRes;

					if (MedidaKaski > errActualKaski
							&& 2 * MedidaQuan > errActualQuan) {
						MedidaKaski = errActualKaski;
						mapaMejorKaski = mapaActivo2;
					}

					if (MedidaQuan > errActualQuan) {
						MedidaQuan = errActualQuan;
						mapaMejorQuan = mapaActivo2;
					}
					if (MedidaTopo > errActualTopo
							&& 2 * MedidaQuan > errActualQuan) {
						MedidaTopo = errActualTopo;
						mapaMejorTopo = mapaActivo2;
					}
				} finally {
					sema.release();
				}
			}

			iteact++;
			Progreso = (int) (iteact / xmulti);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private class UpdaterThread implements Runnable {

		private LFSData datos1;

		private LFSSOMProperties props;

		private double xmulti;

		@Override
		public void run() {
			try {
				System.out.println("PROPS: useBatch: " + props.batchSom()
						+ " bucleLearnRate " + props.learnrate()
						+ " NeighFunc " + props.getNeighbourFunc()
						+ " Init Func " + props.getInitializationMode()
						+ " Sigma " + props.sigma());
				cuerpoTrain(datos1, props, xmulti);

			} finally {
				doneSignal.countDown();
			}
		}

		private UpdaterThread(LFSData datos1, LFSSOMProperties props,
				double xmulti) {
			this.datos1 = datos1;
			this.props = props;
			this.xmulti = xmulti;

		}
	}

	public long getIteAct() {
		return iteact;
	}

	public void LanzaExperimento(LFSExpProps exprops) throws Exception {

		LFSData datos1 = new LFSData(exprops.getFicheroEntrada());
		LanzaExperimento(datos1, exprops);
	}

	public void LanzaExperimento(LFSData datos1, LFSExpProps exprops) {

		String ficheroEntrada = exprops.getFicheroEntrada();
		int widthSOM = exprops.getWidthSOM();
		int heightSOM = exprops.getHeightSOM();
		boolean isHier = exprops.isHier();
		boolean isGCHSOM = exprops.isGCHSOM();
		boolean isGrowing = exprops.isGrowing() || isGCHSOM;
		int[] dimSOM = calculaDimen(datos1, widthSOM, heightSOM, isHier
				|| isGrowing, false, 0);

		widthSOM = dimSOM[0];
		heightSOM = dimSOM[1];

		// int nClusters = exprops.getnClusters();
		int numRepe = exprops.getNumRepe();
		int numCPUs = exprops.getNumCPUs();
		double[] bucleLearnRate = exprops.getBucleLearnRate();
		boolean[] bucleUseBatch = exprops.getBucleUseBatch();
		int[] bucleSigma = exprops.getBucleSigma();
		int[] bucleInitializationMode = exprops.getBucleInitializationMode();
		int[] bucleNeighFunc = exprops.getBucleNeighFunc();
		float[] buclePcNeighWidth = exprops.getBuclePcNeighWidth();
		String nameExp = exprops.getExpName();
		String dataPath = exprops.getDataPath();
		String rootPath = exprops.getRootPath();

		boolean isSub = exprops.isSubred();
		// String celdasSubnet = exprops.getStrSubredOrigen();
		String fPadre = exprops.getFPadre();
		// String fDatosPadre = exprops.getFDatosPadre();

		try {

			MedidaKaski = Double.POSITIVE_INFINITY;
			MedidaTopo = Double.POSITIVE_INFINITY;
			MedidaQuan = Double.POSITIVE_INFINITY;

			Calculando = true;
			cancelado = false;
			Progreso = 0;

			// Se carga la estructura de datos de los resultados

			// Inicializacion de variables
			double tau = 0.001;
			double tau2 = 0.1;

			int zSize = 1;
			long seed = 1;
			int trainingCycles = 0;
			int trainingIterations = 1;

			String metric = null;

			long startTime = System.currentTimeMillis();
			int nThreads = numCPUs;

			double mqeRef = Double.POSITIVE_INFINITY;

			// Parametros que se pueden cambiar:
			// -> batch
			// -> inicializaciones
			// -> metrica?
			// -> sigma (radio vecindad)
			// -> funciones de vecindad

			if (isHier || isGCHSOM) {
				// Si es hierarchical, se calcula el mqeRef de una red con 1
				// casilla

				// Se calcula el mqeRef de una red con 1 casilla, si no ven�a
				// dado
				if (exprops.getMqeIni() == -1) {
					LFSSOMProperties props = new LFSSOMProperties(1, 1, 1,
							seed, trainingCycles, trainingIterations, 1, 1,
							tau, metric, false, false, LFSUnit.INIT_RANDOM,
							LFSGrowingLayer.NEIGH_GAUSS, 1, nameExp, false,
							0.0, false, false, false);
					props.setDataPath(dataPath);
					LFSGrowingSOM mapa1Celda = new LFSGrowingSOM(
							props.getExpName());
					LFSUnit[][] units1Celda = new LFSUnit[1][1];
					units1Celda[0][0] = new LFSUnit(null, 0, 0, datos1
							.getMeanVector().toArray());
					mapa1Celda.initLayer(true, props, datos1, units1Celda);
					mapa1Celda.getLayer().mapCompleteDataAfterTraining(datos1);
					mapa1Celda.getLayer().setQError(
							new LFSQuantizationError(mapa1Celda.getLayer(),
									datos1));
					mqeRef = mapa1Celda.getLayer().getQualityMeasure("QError")
							.getMapQuality("mqe");
				} else {
					mqeRef = exprops.getMqeIni();
				}

			}

			// Se prepara un unitsPCA para asignar y no tener que calcular cada
			// vez

			unitsPCA = calculaUnitsPCA(datos1, widthSOM, heightSOM);
			calculaInitLayers(datos1, widthSOM, heightSOM);

			// Calculo de total de redes a entrenar
			int numItera = 0;
			for (int element : bucleNeighFunc) {
				if (element == LFSGrowingLayer.NEIGH_GAUSS) { // GAUSS no
																// utiliza
																// neighwidth
					numItera += numRepe * bucleLearnRate.length
							* bucleUseBatch.length * bucleSigma.length
							* bucleInitializationMode.length;
				}
				if (element == LFSGrowingLayer.NEIGH_BUBBLE) { // Bubble no
																// utiliza sigma
					numItera += numRepe * bucleLearnRate.length
							* bucleUseBatch.length
							* bucleInitializationMode.length
							* buclePcNeighWidth.length;
				}
				if (element == LFSGrowingLayer.NEIGH_EP
						|| element == LFSGrowingLayer.NEIGH_CUTGAUSS) {
					numItera += numRepe * bucleLearnRate.length
							* bucleUseBatch.length * bucleSigma.length
							* bucleInitializationMode.length
							* buclePcNeighWidth.length;
				}
			}

			setNumIter(numItera);
			// Fin del calculo del total de redes a entrenar

			double xmulti = getNumIter() / 100;
			datosResul = new LFSData(rootPath + "/structTrain.csv");
			matrixResults = new double[getNumIter() + nThreads][datosResul
					.dim()];

			ExecutorService e = null;
			if (nThreads > 1) {
				e = Executors.newFixedThreadPool(nThreads);
				doneSignal = new CountDownLatch(getNumIter());
			}

			int nbSigma = 0;
			int nbNeighWidth = 0;
			for (boolean bBatch : bucleUseBatch) {
				nbNeighWidth = 0;
				for (float bNeighWidth : buclePcNeighWidth) {
					int wSOM = widthSOM;
					int hSOM = heightSOM;
					if (bBatch) {
						int maxSize = wSOM > hSOM ? wSOM : hSOM;
						int neighWidth = (int) (maxSize * bNeighWidth);
						int[] dimSOMBatch = calculaDimen(datos1, wSOM, hSOM,
								isHier, true, neighWidth);
						wSOM = dimSOMBatch[0];
						hSOM = dimSOMBatch[1];
						unitsPCABatch = calculaUnitsPCA(datos1, wSOM, hSOM);
						calculaInitLayersBatch(datos1, wSOM, hSOM);
					}
					for (int bNeighFunc : bucleNeighFunc) {
						for (double bLearnRate : bucleLearnRate) {

							for (int bInitializationMode : bucleInitializationMode) {
								boolean usePCA = bInitializationMode == LFSUnit.INIT_PCA;
								nbSigma = 0;
								for (int bSigma : bucleSigma) {

									boolean ejecuta = true;
									// Si es Bubble, solo se ejecuta para el
									// primer Sigma
									if (nbSigma > 0
											&& bNeighFunc == LFSGrowingLayer.NEIGH_BUBBLE) {
										ejecuta = false;
									}

									// Si es Gauss, solo se ejecuta una vez para
									// un Neighwidth
									if (nbNeighWidth > 0
											&& bNeighFunc == LFSGrowingLayer.NEIGH_GAUSS) {
										ejecuta = false;
									}

									if (ejecuta) {
										LFSSOMProperties props = new LFSSOMProperties(
												wSOM, hSOM, zSize, seed,
												trainingCycles,
												trainingIterations, bLearnRate,
												bSigma, tau, metric, usePCA,
												bBatch, bInitializationMode,
												bNeighFunc, bNeighWidth,
												nameExp, isGrowing, mqeRef,
												isSub, isHier, isGCHSOM);
										props.setDataPath(dataPath);

										if (nThreads > 1) {

											for (int i = 0; i < numRepe; i++) {
												e.execute(new UpdaterThread(
														datos1, props, xmulti));
											}

										} else {
											for (int i = 0; i < numRepe; i++) {
												cuerpoTrain(datos1, props,
														xmulti);
											}
										}
									}

									nbSigma++;
								}
							}
						}
					}
					nbNeighWidth++;
				}
			}
			if (nThreads > 1) {
				doneSignal.await();
				e.shutdown();
			}

			if (!this.cancelado) {

				// Ya tenemos los mejores mapas. Ahora, si es hierarchical, que
				// se generen sus hijos

				new File(dataPath).mkdirs();
				String fiche = null;
				int numClusters = isGCHSOM ? 10 : 5;

				mapaMejorTopo.getLayer().mapCompleteDataAfterTraining(datos1);
				mapaMejorTopo.clusteriza(numClusters);
				// String fich = getfTopo();
				fiche = dataPath + "/topo.xml";
				mapaMejorTopo.EscribeXML(fiche);
				mapaMejorTopo.escribeProps(fiche + "props");

				mapaMejorQuan.getLayer().mapCompleteDataAfterTraining(datos1);
				mapaMejorQuan.clusteriza(numClusters);
				// String fich = getfTopo();
				fiche = dataPath + "/quan.xml";
				mapaMejorQuan.EscribeXML(fiche);
				mapaMejorQuan.escribeProps(fiche + "props");

				mapaMejorKaski.getLayer().mapCompleteDataAfterTraining(datos1);
				mapaMejorKaski.clusteriza(numClusters);
				// String fich = getfTopo();
				fiche = dataPath + "/kaski.xml";
				mapaMejorKaski.EscribeXML(fiche);
				mapaMejorKaski.escribeProps(fiche + "props");

				exprops.clearNets();
				exprops.setFPadre(fPadre);
				// Se incluyen los datos de las redes calculadas para ese
				// experimento.
				exprops.addNet("Best Kaski - Lagus Index", "kaski.xml");
				exprops.addNet("Best Quantization Error", "quan.xml");
				exprops.addNet("Best Topographic Error", "topo.xml");

				exprops.EscribeXML(dataPath + "/ExpProps.xml");
				if (!ficheroEntrada.equals(dataPath + "/data.csv")) {
					copyFile(new File(ficheroEntrada), new File(dataPath
							+ "/data.csv"));
				}

				datosResul.setMatrix(matrixResults);
				LFSDataCSVWriter.writeAsCSV(datosResul, indiceResul, dataPath
						+ "/results.csv");

				// Si es GHSOM, que se generen los hijos que corresponda

				if ((isHier || isGCHSOM) && prof < maxProf) {
					// Se crea la lista de hijos

					LFSGrowingLayer mMejor = mapaMejorKaski.getLayer();
					mqeRef = mMejor.getQualityMeasure("QError").getMapQuality(
							"mqe");
					if (isGCHSOM) {
						lanza_experimento_clusters(tau, mMejor, dataPath,
								rootPath, datos1, mqeRef);
					} else if (isHier) {
						lanza_experimento_units(tau2, mMejor, dataPath,
								rootPath, datos1, mqeRef);
					}

				}

			}
			Progreso = 100;
			System.out.println("Tiempo "
					+ java.lang.String.valueOf(System.currentTimeMillis()
							- startTime));

			Progreso = 101;
			Calculando = false;
			cancelado = false;

			System.out.println("FIN ");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void lanza_experimento_clusters(double tau, LFSGrowingLayer mMejor,
			String dataPath, String rootPath, LFSData datos1, Double mqeRef) {

		// Se calcula el numero de clusters que hay

		int nClusters = mMejor.getNClusterMax();

		long iteini = iteact;
		// Para cada cluster se comprueba si en promedio cumple con tau2xmqeRef
		for (int z = 0; z <= nClusters; z++) {
			int numDatos = mMejor.getNDatosCluster(z);
			if (numDatos > minDatosExp) {
				ArrayList<Integer> arrInc = mMejor.getLabelCluster(z);
				double quality = mMejor.getMqeCluster(z);

				if (quality > tau * mqeRef) {
					// No cumple, se envia a entrenar el cluster completo
					generaExp(mMejor, dataPath, rootPath, mqeRef, datos1,
							arrInc, iteini, z);
				}

			}
		}

	}

	private void generaExp(LFSGrowingLayer mMejor, String dataPath,
			String rootPath, Double mqeRef, LFSData datos1,
			ArrayList<Integer> arrInc, long iteini, int z) {
		try {
			LFSExpProps nexprops = new LFSExpProps(dataPath + "/ExpProps.xml");
			String directorio = dataPath + "/n" + z;
			mMejor.getgSOM().saveMapCSVParcial(datos1, arrInc, directorio,
					directorio + "/data.csv");
			nexprops.setRootPath(rootPath);
			nexprops.setIsSubred(true);
			nexprops.setMqeIni(mqeRef);
			nexprops.setFicheroEntrada(directorio + "/data.csv");
			nexprops.setSubredOrigen(arrInc);
			nexprops.setFPadre("kaski.xml");
			nexprops.setDataPath(directorio);
			nexprops.setExpName(nexprops.getExpName() + "-" + z);
			TrainSelector nuevoExp = new TrainSelector(prof + 1);
			iteact = iteini + z;

			nuevoExp.LanzaExperimento(nexprops);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void lanza_experimento_units(double tau2, LFSGrowingLayer mMejor,
			String dataPath, String rootPath, LFSData datos1, Double mqeRef) {

		ArrayList<LFSUnit> ExpUnits = mMejor.getExpandedUnits(
				mMejor.getQualityMeasure("QError"), "qe", tau2, mqeRef,
				minDatosExp, datos1.numVectors());

		// Se lanza un experimento para cada uno de ellos
		numIter = numIter + ExpUnits.size();
		long iteini = iteact;
		try {
			for (int z = 0; z < ExpUnits.size(); z++) {
				ArrayList<Integer> arrInc = new ArrayList<Integer>();
				LFSUnit unidad = ExpUnits.get(z);

				arrInc.add(unidad.getYPos() * mMejor.getXSize()
						+ unidad.getXPos());
				generaExp(mMejor, dataPath, rootPath, mqeRef, datos1, arrInc,
						iteini, z);

			}
		} catch (Exception e) {
		}
	}

	public void sendCancel() {
		cancelado = true;
	}

	/**
	 * @return Returns the progreso.
	 */
	public int getProgreso() {
		return Progreso;
	}

	/**
	 * @param progreso
	 *            The progreso to set.
	 */
	public void setProgreso(int progreso) {
		Progreso = progreso;
	}

	/**
	 * @return Returns the calculando.
	 */
	public boolean isCalculando() {
		return Calculando;
	}

	/**
	 * @param calculando
	 *            The calculando to set.
	 */
	public void setCalculando(boolean calculando) {
		Calculando = calculando;
	}

	private static void copyFile(File source, File dest) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			is.close();
			os.close();
		}
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
	 * @return Returns the numIter.
	 */
	public int getNumIter() {
		return numIter;
	}

	/**
	 * @param numIter
	 *            The numIter to set.
	 */
	public void setNumIter(int numIter) {
		this.numIter = numIter;
	}

}
