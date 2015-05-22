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
package org.geppetto.simulator.external.converters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ncsa.hdf.object.h5.H5File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.recordings.GeppettoRecordingCreator;
import org.geppetto.core.recordings.GeppettoRecordingCreator.MetaType;

/**
 * Converts a DAT file into a recording HDF5 file
 * 
 * @author Jesus R Martinez (jesus@metacell.us)
 *
 */
public class ConvertDATToRecording
{

	private static Log logger = LogFactory.getLog(ConvertDATToRecording.class);

	private HashMap<String, String[]> datFilePaths = new HashMap<String, String[]>();
	private GeppettoRecordingCreator recordingCreator;

	/**
	 * @param recordingFile
	 */
	public ConvertDATToRecording(String recordingFile)
	{
		// call the class in charge of creating the hdf5
		recordingCreator = new GeppettoRecordingCreator(recordingFile);
	}

	/**
	 * Convert DAT to HDF5, does it for all .dat on the map
	 * 
	 * @throws Exception
	 */
	public void convert() throws Exception
	{
		// loop through map of DAT files
		Set<String> mapSet = datFilePaths.keySet();
		Iterator<String> iterator = mapSet.iterator();
		while(iterator.hasNext())
		{
			// Read each DAT file
			String datFilePath = iterator.next();
			String[] variables = datFilePaths.get(datFilePath);
			read(datFilePath, variables);
		}

		// Create HDF5 after reading all DAT files
		recordingCreator.create();
	}

	public void addDATFile(String datFile, String[] variables)
	{
		this.datFilePaths.put(datFile, variables);
	}

	/**
	 * Reads DAT file and extract's values. Then adds them to HDF5 file by calling one of its commands.
	 * 
	 * @param fileName
	 *            - Name of DAT file stored in map
	 * @param variables2
	 *            - Path to DAT file
	 * @throws GeppettoExecutionException
	 */
	public void read(String fileName, String[] variables) throws GeppettoExecutionException
	{
		BufferedReader input = null;
		// will store values and variables found in DAT
		HashMap<String, List<Float>> dataValues = new HashMap<String, List<Float>>();
		try
		{
			// read DAT into a buffered reader
			input = new BufferedReader(new FileReader(fileName));

			for(int i = 0; i < variables.length; i++)
			{
				dataValues.put(variables[i], new ArrayList<Float>());
			}

			// read rest of DAT file and extract values
			while(input.read() != -1)
			{
				String line = input.readLine();
				String[] columns = line.split("\\s+");
				for(int i = 0; i < columns.length; i++)
				{
					String key = variables[i];
					dataValues.get(key).add(Float.valueOf(columns[i]));
				}
			}

			// Add recording variables in map to hdf5 file
			Set<String> mapSet = dataValues.keySet();
			Iterator<String> iterator = mapSet.iterator();
			while(iterator.hasNext())
			{
				String variable = iterator.next();
				List<Float> floatValues = dataValues.get(variable);
				float[] target = new float[floatValues.size()];
				for(int i = 0; i < target.length; i++)
				{
					target[i] = floatValues.get(i);
				}
				recordingCreator.addValues(variable, target, "ms", MetaType.Variable_Node, false);
			}
		}
		catch(Exception e)
		{
			logger.error("An IOException was caught: " + e.getMessage());
			throw new GeppettoExecutionException(e);
		}
		// handles End of file exception
		finally
		{
			try
			{
				input.close();
			}
			catch(IOException ex)
			{
				logger.error("An IOException was caught: " + ex.getMessage());
				throw new GeppettoExecutionException(ex);
			}
		}

	}

	public H5File getRecordingsFile()
	{
		return this.recordingCreator.getRecordingsFile();
	}
}