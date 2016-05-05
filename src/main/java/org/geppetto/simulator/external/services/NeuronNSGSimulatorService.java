package org.geppetto.simulator.external.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.geppetto.core.externalprocesses.ExternalProcess;
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
import org.ngbw.directclient.CiCipresException;
import org.ngbw.directclient.CiClient;
import org.ngbw.directclient.CiJob;
import org.ngbw.directclient.CiResultFile;
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
@Service
public class NeuronNSGSimulatorService extends AExternalProcessNeuronalSimulator
{

	protected Path filePath = null;

	private static Log logger = LogFactory.getLog(NeuronNSGSimulatorService.class);

	@Autowired
	private SimulatorConfig neuronSimulatorConfig;

	@Autowired
	private RemoteSimulatorConfig neuronNSGExternalSimulatorConfig;
	
	private CiClient myClient;
	private CiJob jobStatus;

	@Override
	public void initialize(DomainModel model, IAspectConfiguration aspectConfiguration, ExperimentState experimentState, ISimulatorCallbackListener listener, GeppettoModelAccess modelAccess)
			throws GeppettoInitializationException, GeppettoExecutionException
	{
		super.initialize(model, aspectConfiguration, experimentState, listener, modelAccess);

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
			//this.createCommands(this.originalFileName);
			File originalFilePath = new File(originalFileName);
			directoryToExecuteFrom = originalFilePath.getParentFile().getAbsolutePath();
			outputFolder = directoryToExecuteFrom;
			
			File renamedfilePath = new File(directoryToExecuteFrom + "/init.py");
			Files.copy(originalFilePath.toPath(), renamedfilePath.toPath(), StandardCopyOption.REPLACE_EXISTING);
			
			// Zip folder
			Zipper zipper = new Zipper(directoryToExecuteFrom + "/input.zip");
			filePath = zipper.getZipFromDirectory(new File(directoryToExecuteFrom));
			
//			InputStream is = NeuronNSGSimulatorService.class.getResourceAsStream("/input.zip");
//			File dest = File.createTempFile("Example", ".txt"); 
//			dest.deleteOnExit();
//
//			CiResultFile.copyInputStreamToFile(is, dest);
//			
//			inputParams.put("infile_", dest.getAbsolutePath());


			
			
			myClient = new CiClient(neuronNSGExternalSimulatorConfig.getSimulatorParameters().get("appId"), neuronNSGExternalSimulatorConfig.getUsername(), neuronNSGExternalSimulatorConfig.getPassword(), neuronNSGExternalSimulatorConfig.getUrl());
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
			throw new GeppettoExecutionException("Error creating commands for Neuron NSG Simulator service");
		}
	}

	
	@Override
	public void simulate() throws GeppettoExecutionException
	{
		// send command, directory where execution is happening, and path to original file script to execute
		if(!started)
		{
			try
			{
				//this.runExternalProcess(commands, directoryToExecuteFrom, originalFileName);
				sendJob("", false);
				started = true;
			}
			catch(CiCipresException ce)
			{
				ErrorData ed = ce.getErrorData();
				System.out.println("Cipres error code=" + ed.code + ", message=" + ed.displayMessage);
				if (ed.code == ErrorData.FORM_VALIDATION)
				{
					for (ParamError pe : ed.paramError)
					{
						System.out.println(pe.param + ": " + pe.error);
					}
				} else if (ed.code == ErrorData.USAGE_LIMIT)
				{
					LimitStatus ls = ed.limitStatus;
					System.out.println("Usage Limit Error, type=" + ls.type + ", ceiling=" + ls.ceiling);
				}
			}
			catch (Exception e)
			{
				System.out.println(e.toString());
			}
		}
		else
		{
			throw new GeppettoExecutionException("Simulate has been called again");
		}
	}
	
	private void sendJob(String jobName, boolean validateOnly) throws CiCipresException, IOException, GeppettoExecutionException, InterruptedException
	{
		Map<String, Collection<String>> vParams = new HashMap<String, Collection<String>>();
		HashMap<String, String> inputParams = new HashMap<String, String>();
		HashMap<String, String> metadata = new HashMap<String, String>();
		
		inputParams.put("infile_", filePath.toString());
		
		// See https://www.phylo.org/restusers/docs/guide.html#UseOptionalMetadata for list of available
		// metadata keys.   
		metadata.put("statusEmail", "true");
		//metadata.put("clientJobName", jobName);
		metadata.put("clientJobId", "1234546");

		if (validateOnly)
		{
			jobStatus = myClient.validateJob("CLUSTALW", vParams, inputParams, metadata);
		} else
		{
			//jobStatus = myClient.submitJob("CLUSTALW", vParams, inputParams, metadata);
			jobStatus = myClient.submitJob("PY_TG", vParams, inputParams, metadata);
		}
		jobStatus.show(true);
		
		checkJobStatus();
	} 
	
	public void listJobs() throws CiCipresException
	{
		System.out.println("List all jobs");
		int count = 0;
		Collection<CiJob> jobs = myClient.listJobs(); 
		for (CiJob job : jobs)
		{
			count += 1;
			System.out.print("\n" + count + ". ");
			job.show(true);
		}
	}
	
	public void deleteAllJobs() throws CiCipresException
	{
		Collection<CiJob> jobs = myClient.listJobs(); 
		for (CiJob job : jobs)
		{
			job.delete();
		}
	}

	private void checkJobStatus() throws CiCipresException, GeppettoExecutionException, InterruptedException{
		jobStatus.update();
		if (jobStatus.isDone() || jobStatus.isError()){
			jobStatus.getJobStage();
			processDone();
		}
		else{
			
			listJobs();
			
			System.out.println("Current job status");
			jobStatus.show(true);
			Thread.sleep(5000);
			checkJobStatus();
		}
	}
	
	public void processDone() throws GeppettoExecutionException
	{
		try
		{
			// Retrieve from the server
			jobStatus.downloadResults(new File(outputFolder), true);
			
			// Convert to h5
			List<String> variableNames = new ArrayList<String>();

			ConvertDATToRecording datConverter = new ConvertDATToRecording(PathConfiguration.createProjectTmpFolder(Scope.RUN, projectId, PathConfiguration.getName("results", true)+ ".h5"),this.geppettoModelAccess);

			Map<File,ResultsFormat> results=new HashMap<File,ResultsFormat>();
			
			File mappingResultsFile = new File(outputFolder + "/outputMapping.dat");
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

			this.getListener().endOfSteps(this.aspectConfiguration, results);
		}
		catch(Exception e)
		{
			//The HDF5 library throws a generic Exception :/
			throw new GeppettoExecutionException(e);
		}
	}
	
	@Override
	public void registerGeppettoService()
	{
		List<ModelFormat> modelFormats = new ArrayList<ModelFormat>(Arrays.asList(ServicesRegistry.registerModelFormat("NEURON")));
		ServicesRegistry.registerSimulatorService(this, modelFormats);
	}

	@Override
	public String getName()
	{
		return this.neuronSimulatorConfig.getSimulatorName();
	}

	@Override
	public String getId()
	{
		return this.neuronSimulatorConfig.getSimulatorID();
	}

	@Override
	public String getSimulatorPath()
	{
		return this.neuronNSGExternalSimulatorConfig.getSimulatorPath();
	}
	

	/**
	 * @param neuronSimulatorConfig
	 * @deprecated for test purposes only, the configuration is autowired
	 */
	public void setNeuronSimulatorConfig(SimulatorConfig neuronSimulatorConfig)
	{
		this.neuronSimulatorConfig = neuronSimulatorConfig;
	}

	/**
	 * @param neuronNSGExternalSimulatorConfig
	 * @deprecated for test purposes only, the configuration is autowired
	 */
	public void setNeuronNSGExternalSimulatorConfig(RemoteSimulatorConfig neuronNSGExternalSimulatorConfig)
	{
		this.neuronNSGExternalSimulatorConfig = neuronNSGExternalSimulatorConfig;
	}

}
