/*
 * 
 * This class is a specialized class of "Cluster" class from:
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
package lfsom.visualization.clustering;

import java.util.Vector;

import lfsom.layers.metrics.LFSL2Metric;
import lfsom.util.LFSException;

public class LFSCluster {

	private Vector<Integer> indices;

	private double[] centroid;

	public LFSCluster() {
		indices = new Vector<Integer>();

	}

	LFSCluster(double[] centroid) {
		this();
		this.centroid = centroid;
	}

	/**
	 * Calculate the centroid of this cluster. This is done by summing up all
	 * individual values divided by the number of instances assigned to it.
	 * 
	 * @param data
	 *            the data set.
	 */
	void calculateCentroid(double[][] data) {
		for (int instanceIndex = 0; instanceIndex < indices.size(); instanceIndex++) {
			for (int attributeIndex = 0; attributeIndex < data[indices
					.elementAt(instanceIndex)].length; attributeIndex++) {
				if (instanceIndex == 0) {
					centroid[attributeIndex] = 0;
				}
				centroid[attributeIndex] += data[indices
						.elementAt(instanceIndex)][attributeIndex]
						/ indices.size();
			}
		}
	}

	/** Removes the instance according to the given index. */
	void removeInstanceIndex(int instanceIndex) {
		indices.remove(new Integer(instanceIndex));
	}

	/**
	 * Add the index of a data point to this cluster.
	 * 
	 * @param index
	 *            to add.
	 */
	void addIndex(int index) {
		indices.add(new Integer(index));
	}

	/**
	 * Set the centroid of this cluster.
	 * 
	 * @param centroid
	 *            to set.
	 */
	public void setCentroid(double[] centroid) {
		this.centroid = centroid;
	}

	public double[] getCentroid() {
		return centroid.clone();
	}

	public Vector<Integer> getIndices() {
		return this.indices;
	}

	public int getNumberOfInstances() {
		return indices.size();
	}

	/**
	 * Returns all the instances belonging to this cluster according to the
	 * given data set.
	 * 
	 * @param data
	 *            instances.
	 * @return plain matrix of all assigned instances.
	 */
	double[][] getInstances(double[][] data) {
		double[][] instances = new double[indices.size()][data[0].length];
		for (int i = 0; i < indices.size(); i++) {
			instances[i] = data[indices.elementAt(i)];
		}
		return instances;
	}

	/**
	 * Calculate the sum of the squared error (SSE) for this cluster. This is
	 * the distances of the cluster's centroid to all units assigned.
	 * 
	 * @param data
	 *            matrix to compute the SSE for.
	 * @return the SSE value for this cluster.
	 */
	double SSE(double[][] data) {
		double sse = 0d;
		for (int i = 0; i < indices.size(); i++) {
			try {
				sse += LFSL2Metric.distance(data[indices.elementAt(i)],
						centroid);
			} catch (LFSException e) {
				e.printStackTrace();
			}
		}
		return sse;
	}

	/**
	 * Get the distance of a given instance to this cluster's centroid.
	 * 
	 * @param instance
	 *            some instance.
	 * @return the distance according to the used distance function.
	 */
	double getDistanceToCentroid(double[] instance) {
		try {
			return LFSL2Metric.distance(centroid, instance);
		} catch (LFSException e) {
			e.printStackTrace();
		}
		return 0d;
	}

	/**
	 * Get the instance with the maximum SSE of all instances assigned to this
	 * cluster.
	 */
	int getInstanceIndexWithMaxSSE(double[][] data) {
		int index = -1;
		double maxSSE = Double.NEGATIVE_INFINITY;
		double currentSSE = 0;

		for (int i = 0; i < indices.size(); i++) {
			try {
				currentSSE = LFSL2Metric.distance(data[indices.elementAt(i)],
						centroid);

				if (currentSSE > maxSSE) {
					maxSSE = currentSSE;
					index = indices.elementAt(i);
				}
			} catch (LFSException e) {
				e.printStackTrace();
			}
		}
		return index;
	}
}