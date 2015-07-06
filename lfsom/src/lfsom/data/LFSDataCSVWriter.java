/*
 * 
 * This class is a specialized class of "InputDataWriter" class from:
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.data.SOMLibTemplateVector;
import at.tuwien.ifs.somtoolbox.data.TemplateVector;
import at.tuwien.ifs.somtoolbox.util.FileUtils;
import at.tuwien.ifs.somtoolbox.util.StringUtils;

/**
 * This class gathers methods to write certain {@link LFSInputData},
 * {@link TemplateVector} and {@link LFSSOMLibClassInformation} in a certain
 * number of file formats, such as SOMLib, WEKA ARFF, SOMPak and ESOM.
 * 
 * @author Rudolf Mayer
 * @version $Id: InputDataWriter.java 4272 2012-04-21 02:22:36Z mayer $
 */
public class LFSDataCSVWriter {

	/**
	 * Writes the class information to a file in CSV format; elements will be
	 * sorted by label name.
	 */

	public static void writeAsCSV(LFSData data, long numReg, String fileName)
			throws IOException {
		Logger.getLogger("lfsom").info(
				"Writing input data as CVS to '" + fileName + "'.");
		PrintWriter writer = FileUtils.openFileForWriting("CVS", fileName,
				false);

		TemplateVector tv = data.templateVector();
		if (tv == null) {
			Logger.getLogger("at.tuwien.ifs.somtoolbox").info(
					"Template vector not loaded - creating a generic one.");
			tv = new SOMLibTemplateVector(data.numVectors(), data.dim());
		}

		String separator = ",";

		// header: tab-separated label names
		writer.print(StringUtils.toString(tv.getLabels(), "", "", separator,
				"\""));
		writer.println();

		// data: tab separated, optionally with the class assignment
		for (int i = 0; i < numReg; i++) {
			for (int j = 0; j < data.dim(); j++) {
				String datoprint = String.valueOf(data.getValue(i, j));
				writer.print(datoprint);
				if (j + 1 < data.dim()) {
					writer.print(separator);
				}
			}

			writer.println();
		}
		writer.close();
	}

}
