/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2011 - 2015 OpenWorm.
 * http://openworm.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/MIT
 *
 * Contributors:
 *     	OpenWorm - http://openworm.org/people.html
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE 
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.geppetto.simulator.external.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geppetto.core.beans.PathConfiguration;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.data.model.ResultsFormat;
import org.geppetto.core.externalprocesses.ExternalProcess;
import org.geppetto.core.manager.Scope;
import org.geppetto.core.simulator.AExternalProcessSimulator;
import org.geppetto.simulator.external.converters.ConvertDATToRecording;

/**
 * @author matteocantarelli
 *
 */
public abstract class AExternalProcessNeuronalSimulator extends AExternalProcessSimulator
{
	

	@Override
	public void processDone(String[] processCommand) throws GeppettoExecutionException
	{
		super.processDone(processCommand);
		try
		{
			ExternalProcess process = this.getExternalProccesses().get(processCommand);

			List<String> variableNames = new ArrayList<String>();

			ConvertDATToRecording datConverter = new ConvertDATToRecording(PathConfiguration.createProjectTmpFolder(Scope.RUN, projectId, PathConfiguration.getName("results", true)+ ".h5"));

			Map<File,ResultsFormat> results=new HashMap<File,ResultsFormat>();
			
			File mappingResultsFile = new File(process.getOutputFolder() + "/outputMapping.dat");
			results.put(mappingResultsFile,ResultsFormat.RAW);
			
			BufferedReader input;

			input = new BufferedReader(new FileReader(mappingResultsFile));

			// read rest of DAT file and extract values
			String filePath = "";
			String line = "";
			while((line = input.readLine()) != null)
			{
				if(filePath.equals(""))
				{
					filePath = line;
				}
				else
				{
					String[] variables = line.split("\\s+");
					for(String s : variables)
					{
						variableNames.add(s);
					}
					String fileName=mappingResultsFile.getParent() + "/" + filePath;
					datConverter.addDATFile(fileName, variables);
					results.put(new File(fileName),ResultsFormat.RAW);
					filePath = "";
				}
			}
			input.close();
			
			datConverter.convert(experimentState);
			
			results.put(datConverter.getRecordingsFile(),ResultsFormat.GEPPETTO_RECORDING);

			this.getListener().endOfSteps(pointer, results);
		}
		catch(Exception e)
		{
			//The HDF5 library throws a generic Exception :/
			throw new GeppettoExecutionException(e);
		}
	}

}
