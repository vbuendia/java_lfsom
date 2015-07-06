/*
 * 
 * This class is a specialized class of "InputDatum" class from:
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
package lfsom.data;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

/**
 * Class representing a specific input datum.
 * 
 * @author Michael Dittenbach
 * @author Rudolf Mayer
 * @version $Id: InputDatum.java 3583 2010-05-21 10:07:41Z mayer $
 */
public class LFSInputDatum {

	private String label;

	private DoubleMatrix1D vector;

	private int dim;

	/**
	 * Constructs a new InputDatum.
	 * 
	 * @param label
	 *            The label of the input datum. Basically this should be a sort
	 *            of unique id.
	 * @param vector
	 *            The vector holding the values, this time as a double[].
	 */
	LFSInputDatum(String label, double[] vector) {
		// DoubleMatrix1D = new DoubleMatri
		this.label = label;
		dim = vector.length;
		this.vector = new DenseDoubleMatrix1D(dim);
		this.vector.assign(vector);
	}

	/**
	 * Returns the label of the InputDatum.
	 * 
	 * @return the label of the InputDatum.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Returns the vector of the InputDatum.
	 * 
	 * @return the vector of the InputDatum.
	 */
	public DoubleMatrix1D getVector() {
		return vector;
	}

	/**
	 * Returns the dimensionality of the vector.
	 * 
	 * @return the dimensionality of the vector.
	 */
	public int getDim() {
		return dim;
	}

	/**
	 * Returns a String representation of this {@link LFSInputDatum} as
	 * <code><i>labelName</i>[<i>vector</i>]</code>.
	 */
	@Override
	public String toString() {
		return getLabel() + " [" + getVector() + "]";
	}

	/**
	 * Compares two {@link LFSInputDatum} by both comparing the labels and
	 * vectors.
	 */
	@Override
	public boolean equals(Object obj) {
		return obj instanceof LFSInputDatum
				&& ((LFSInputDatum) obj).getLabel().equals(getLabel())
				&& ((LFSInputDatum) obj).getVector().equals(getVector());
	}

}
