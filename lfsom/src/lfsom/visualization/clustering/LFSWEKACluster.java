package lfsom.visualization.clustering;

import java.util.ArrayList;

import weka.clusterers.EM;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class LFSWEKACluster {

	int numberOfInstances;
	int[] instancesInClusters;

	public LFSWEKACluster(int k, double[][] data, int nparalels) {
		// Calcula el cluster empleando la clase de WEKA

		numberOfInstances = data.length;
		this.instancesInClusters = new int[numberOfInstances];

		EM clusterer = new EM();

		ArrayList<Attribute> listaAt = new ArrayList<Attribute>();

		for (int r = 0; r < data[0].length; r++)
			listaAt.add(r, new Attribute("At" + r));

		Instances data1 = new Instances("Cluster", listaAt, 0);

		for (int r = 0; r < numberOfInstances; r++)
			data1.add(new DenseInstance(1.0, data[r]));

		clusterer.setNumExecutionSlots(nparalels);
		try {
			if (k > 0) {
				clusterer.setNumClusters(k);
				clusterer.setMaximumNumberOfClusters(k);
			}

			clusterer.buildClusterer(data1);

			for (int r = 0; r < numberOfInstances; r++) {
				this.instancesInClusters[r] = clusterer
						.clusterInstance(new DenseInstance(1.0, data[r]));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public int[] getResultados() {
		int resultados[] = new int[this.numberOfInstances];

		for (int k = 0; k < this.numberOfInstances; k++) {
			resultados[k] = this.instancesInClusters[k];
		}

		return resultados;
	}
}
