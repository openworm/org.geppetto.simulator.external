package org.geppetto.simulator.external.services;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.beans.SimulatorConfig;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.core.simulator.AExternalProcessSimulator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Neuron Simulator 
 * 
 * @author Jesus R Martinez (jesus@metacell.us)
 *
 */
public class NeuronSimulatorService extends AExternalProcessSimulator{

	private static Log _logger = LogFactory.getLog(NeuronSimulatorService.class);

	@Autowired
	private SimulatorConfig neuronSimulatorConfig;

	private String NEURON_HOME = "/usr/bin/nrn";
	
	@Override
	public void initialize(List<IModel> models, ISimulatorCallbackListener listener) throws GeppettoInitializationException, GeppettoExecutionException
	{
		/**
		 * Creates command from model wrapper's neuron script
		 */
		for(IModel m : models){
			ModelWrapper wrapper = (ModelWrapper) m;
			this.processCommand(wrapper.getModel("process").toString());
		}
	}
	
	@Override
	public String getName() {
		return this.neuronSimulatorConfig.getSimulatorName();
	}

	@Override
	public String getId() {
		return this.neuronSimulatorConfig.getSimulatorID();
	}

	/**
	 * Creates command to be executed by an external process
	 * 
	 * @param originalFileName
	 */
	public void processCommand(String originalFileName){
		_logger.info("Creating command to run " + originalFileName);

		try{
			File filePath = new File(originalFileName);
			String command = null;

			String directoryToExecuteFrom = filePath.getCanonicalPath();

			if(filePath.isDirectory()){
				command = NEURON_HOME
						+ System.getProperty("file.separator")
						+ "bin"
						+ System.getProperty("file.separator")
						+ "nrnivmodl";
			}else{
				String extension = "";

				int i = filePath.getAbsolutePath().lastIndexOf('.');
				if (i > 0) {
					extension = filePath.getAbsolutePath().substring(i+1);
				}

				_logger.info("File with extension " + extension + " detected");

				directoryToExecuteFrom = filePath.getParentFile().getAbsolutePath();
				if(extension.equals("hoc")){
					command = NEURON_HOME
							+ System.getProperty("file.separator")
							+ "bin"
							+ System.getProperty("file.separator")
							+ "nrngui " + filePath.getAbsolutePath();
				}
				else if(extension.equals("py")){
					command = "python " + filePath.getAbsolutePath();
				}

				_logger.info("Command to Execute: " + command + " ...");
				_logger.info("From directory : " + directoryToExecuteFrom);
			}

			//send command, directory where execution is happening, and path 
			//to original file script to exceture
			this.runExternalProcess(command, directoryToExecuteFrom, originalFileName);
		}
		catch(IOException e){

		}
	}
}
