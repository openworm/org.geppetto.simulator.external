package org.geppetto.simulator.remote.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.beans.PathConfiguration;
import org.geppetto.core.beans.SimulatorConfig;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.data.model.IAspectConfiguration;
import org.geppetto.core.data.model.ResultsFormat;
import org.geppetto.core.manager.Scope;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.recordings.ConvertDATToRecording;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.core.simulator.RemoteSimulatorConfig;
import org.geppetto.core.utilities.Zipper;
import org.geppetto.model.DomainModel;
import org.geppetto.model.ExperimentState;
import org.geppetto.model.ExternalDomainModel;
import org.geppetto.model.ModelFormat;
import org.geppetto.simulator.external.services.AExternalProcessNeuronalSimulator;
import org.ngbw.directclient.CiCipresException;
import org.ngbw.directclient.CiClient;
import org.ngbw.directclient.CiJob;
import org.ngbw.restdatatypes.ErrorData;
import org.ngbw.restdatatypes.LimitStatus;
import org.ngbw.restdatatypes.ParamError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Wrapper class for NEURON at Neuroscience Gateway
 * 
 * @author Adrian Quintana (adrianquintana@gmail.com)
 *
 */
public class NSGSimulatorService extends AExternalProcessNeuronalSimulator
{

	protected Path filePath = null;

	private static Log logger = LogFactory.getLog(NSGSimulatorService.class);

	@Autowired
	private RemoteSimulatorConfig NSGExternalSimulatorConfig;

	private CiClient myClient;
	private CiJob jobStatus;
    
    private int numberProcessors = 1;

	@Override
	public void initialize(DomainModel model, IAspectConfiguration aspectConfiguration, ExperimentState experimentState, ISimulatorCallbackListener listener, GeppettoModelAccess modelAccess)
			throws GeppettoInitializationException, GeppettoExecutionException
	{
		super.initialize(model, aspectConfiguration, experimentState, listener, modelAccess);
        
        if (aspectConfiguration!=null && aspectConfiguration.getSimulatorConfiguration()!=null)
        {
            if (aspectConfiguration.getSimulatorConfiguration().getParameters().get("numberProcessors")!=null &&
                aspectConfiguration.getSimulatorConfiguration().getParameters().get("numberProcessors").length()>0)
            {
                numberProcessors = Integer.parseInt(aspectConfiguration.getSimulatorConfiguration().getParameters().get("numberProcessors"));
            }
        }

		if(model instanceof ExternalDomainModel)
		{
			originalFileName = (String) model.getDomainModel();
		}
		else
		{
			throw new GeppettoExecutionException("Unexpected domain model inside NEURON NSG Simulator service");
		}

		try
		{
			// Set directory to execute from and output folder
			File originalFilePath = new File(originalFileName);
			directoryToExecuteFrom = originalFilePath.getParentFile().getAbsolutePath();
			outputFolder = directoryToExecuteFrom;

			// Rename main script to input.py (NSG requirement)
			File renamedfilePath = new File(directoryToExecuteFrom + "/init.py");
			Files.copy(originalFilePath.toPath(), renamedfilePath.toPath(), StandardCopyOption.REPLACE_EXISTING);

			// Create Results Folder (it is needed because of the way the neuron code is generated in the export library)
			File resultsFolder = new File(directoryToExecuteFrom + "/results");
			resultsFolder.mkdir();
			File emptyFile = new File(resultsFolder, "empty");
			emptyFile.createNewFile();

			// Zip the folder creating an inner folder input
			Zipper zipper = new Zipper(originalFilePath.getParentFile().getParentFile().getAbsolutePath() + "/input.zip", "input");
			filePath = zipper.getZipFromDirectory(new File(directoryToExecuteFrom));

			//Initialise client for remote execution
			if (NSGExternalSimulatorConfig.getSimulatorParameters().containsKey("appName")){
				Map<String, String> endUserHeaders = new HashMap<String, String>();
				endUserHeaders.put("cipres-eu", NSGExternalSimulatorConfig.getSimulatorParameters().get("internalUserName"));
				endUserHeaders.put("cipres-eu-email", NSGExternalSimulatorConfig.getSimulatorParameters().get("internalEmail"));
				
				myClient = new CiClient(NSGExternalSimulatorConfig.getSimulatorParameters().get("appId"), NSGExternalSimulatorConfig.getSimulatorParameters().get("appName"), NSGExternalSimulatorConfig.getUsername(),
						NSGExternalSimulatorConfig.getPassword(), NSGExternalSimulatorConfig.getSimulatorPath(), endUserHeaders);
			}
			else{
				myClient = new CiClient(NSGExternalSimulatorConfig.getSimulatorParameters().get("appId"), NSGExternalSimulatorConfig.getUsername(),
						NSGExternalSimulatorConfig.getPassword(), NSGExternalSimulatorConfig.getSimulatorPath());
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
			throw new GeppettoExecutionException("Error creating commands for Neuron NSG Simulator service", e);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new GeppettoExecutionException("Error creating commands for Neuron NSG Simulator service", e);
		}
	}

	@Override
	public void simulate() throws GeppettoExecutionException
	{
		// Send job to remote server
		if(!started)
		{
			try
			{
				//AQP: Should this be executed in a different thread?
				// this.runExternalProcess(commands, directoryToExecuteFrom, originalFileName);
                long jobId = System.currentTimeMillis();
                if (this.experimentState!=null)
                {
                    jobId = this.experimentState.getExperimentId();
                }
				jobStatus = NSGUtilities.sendJob(myClient, jobId, filePath, numberProcessors, false);
				started = true;
				
				try{
					// AQP: Should we execute this in a different thread
					// Let's wait until the job is done
					NSGUtilities.checkJobStatus(jobStatus);
					// Let's download the results and process them
					processDone();
				}
				catch(GeppettoExecutionException e){
					//AQP we should return something about the job status
					logger.error("Error executing job in remote server");
					throw new GeppettoExecutionException("Error executing job");
				}
				
			}
			catch(CiCipresException ce)
			{
				ErrorData ed = ce.getErrorData();
				logger.error("Cipres error code=" + ed.code + ", message=" + ed.displayMessage);
				logger.error("Cipres error code=" + ed.message);
				logger.error("Cipres error code=" + ed.paramError);
				logger.error("Cipres error code=" + ed.limitStatus);
				if (ed.paramError!=null)
				{
				    for (ParamError pe: ed.paramError)
                    {
                        logger.error(pe.param + " = " + pe.error); 
                    }
                }
                
				if(ed.code == ErrorData.FORM_VALIDATION)
				{
					for(ParamError pe : ed.paramError)
					{
						logger.error(pe.param + ": " + pe.error);
					}
					throw new GeppettoExecutionException("Parameter no valids. See logs for more information.");
				}
				else if(ed.code == ErrorData.USAGE_LIMIT)
				{
					LimitStatus ls = ed.limitStatus;
					throw new GeppettoExecutionException("Usage Limit Error, type=" + ls.type + ", ceiling=" + ls.ceiling);
				}
			}
			catch(javax.ws.rs.InternalServerErrorException e)
			{
				e.printStackTrace();
                logger.error("- Response: "+e.getResponse());
                logger.error("- Response: "+e.getResponse().serverError());
				throw new GeppettoExecutionException("Error executing simulation for Neuron NSG Simulator Service: "+e.getMessage(), e);
			}
			catch(Exception e)
			{
				//e.printStackTrace();
				throw new GeppettoExecutionException("Error executing simulation for Neuron NSG Simulator Service: "+e.getMessage(), e);
			}
		}
		else
		{
			throw new GeppettoExecutionException("Simulate has been called again");
		}
	}


	public void processDone() throws GeppettoExecutionException
	{
		try
		{
			// Prepare output and results folder
			File outputFileFolder = new File(outputFolder);
			File resultsFileFolder = new File(outputFolder + "/results");
			
			// Retrieve results from the server
			jobStatus.downloadResults(outputFileFolder, true);

			//AQP: What if we don't have results
			//AQP: We are not controlling errors
			//Extract results from zip file
			NSGUtilities.extractResults(outputFileFolder, resultsFileFolder);
			
			//AQP: This is redundant code
			// Convert to h5
			List<String> variableNames = new ArrayList<String>();

			ConvertDATToRecording datConverter = new ConvertDATToRecording(PathConfiguration.createProjectTmpFolder(Scope.RUN, projectId, PathConfiguration.getName("results", true) + ".h5"),
					this.geppettoModelAccess);

			Map<File, ResultsFormat> results = new HashMap<File, ResultsFormat>();

			File mappingResultsFile = new File(outputFolder + "/outputMapping.dat");
			results.put(mappingResultsFile, ResultsFormat.RAW);

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
					String fileName = mappingResultsFile.getParent() + "/" + filePath;
					datConverter.addDATFile(fileName, variables);
					results.put(new File(fileName), ResultsFormat.RAW);
					filePath = "";
				}
			}
			input.close();

			datConverter.convert(experimentState);

			results.put(datConverter.getRecordingsFile(), ResultsFormat.GEPPETTO_RECORDING);

			this.getListener().endOfSteps(this.aspectConfiguration, results);
		}
		catch(FileNotFoundException e) {
			throw new GeppettoExecutionException("Error extracting results");
		}
		catch(Exception e)
		{
			// The HDF5 library throws a generic Exception :/
			throw new GeppettoExecutionException(e);
		}
	}

	@Override
	public String getName()
	{
		return "";
	}

	@Override
	public String getId()
	{
		return "";
	}

	@Override
	public String getSimulatorPath()
	{
		return this.NSGExternalSimulatorConfig.getSimulatorPath();
	}
	
	@Override
	public void registerGeppettoService()
	{
	}

	/**
	 * @param nsgExternalSimulatorConfig
	 * @deprecated for test purposes only, the configuration is autowired
	 */
	public void setNSGExternalSimulatorConfig(RemoteSimulatorConfig nsgExternalSimulatorConfig)
	{
		this.NSGExternalSimulatorConfig = nsgExternalSimulatorConfig;
	}
	
	/**
	 * @deprecated for test purposes only
	 */
	public CiClient getCiClient()
	{
		return myClient;
	}

}
