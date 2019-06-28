
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
import org.geppetto.core.recordings.ConvertDATToRecording;
import org.geppetto.core.simulator.AExternalProcessSimulator;

/**
 * @author matteocantarelli
 *
 */
public abstract class AExternalProcessNeuronalSimulator extends AExternalProcessSimulator
{
	private Map<File,ResultsFormat> results;

	public Map<File,ResultsFormat> getResults(){
		return this.results;
	}
	
	@Override
	public void processDone(String[] processCommand) throws GeppettoExecutionException
	{
		ExternalProcess process = this.getExternalProccesses().get(processCommand);

		try
		{
			List<String> variableNames = new ArrayList<String>();

			ConvertDATToRecording datConverter = new ConvertDATToRecording(PathConfiguration.createExperimentTmpPath(Scope.RUN, projectId, getExperiment().getId(), aspectConfiguration.getInstance(), PathConfiguration.getName("results", true)+ ".h5"),this.geppettoModelAccess);

			results=new HashMap<File,ResultsFormat>();
			
			File mappingResultsFile = new File(process.getOutputFolder() + "/outputMapping.dat");
			results.put(mappingResultsFile,ResultsFormat.RAW);
            
			File reportFile = new File(process.getOutputFolder() + "/report.txt");
			results.put(reportFile,ResultsFormat.RAW);
			
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

			this.getListener().endOfSteps(this.aspectConfiguration, results);			
		}
		catch(Exception e)
		{
			//The HDF5 library throws a generic Exception :/
			throw new GeppettoExecutionException(e);
		}
	}
	
	@Override
	public void processFailed(String errorMessage, Exception e){
		this.getListener().externalProcessFailed(errorMessage, e);
	}

}
