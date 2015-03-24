package org.geppetto.simulator.external.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.beans.SimulatorConfig;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.externalprocesses.ExternalProcess;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.services.IModelFormat;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.core.simulation.IRunConfiguration;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.core.simulator.AExternalProcessSimulator;
import org.geppetto.simulator.external.converters.ConvertDATToRecording;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Neuron Simulator 
 * 
 * @author Jesus R Martinez (jesus@metacell.us)
 *
 */
@Service
public class NeuronSimulatorService extends AExternalProcessSimulator{

	private static Log _logger = LogFactory.getLog(NeuronSimulatorService.class);

	@Autowired
	private SimulatorConfig neuronSimulatorConfig;
	
	@Autowired
	private ExternalSimulatorConfig neuronExternalSimulatorConfig;
	
	@Override
	public void initialize(List<IModel> models, ISimulatorCallbackListener listener) throws GeppettoInitializationException, GeppettoExecutionException
	{
		super.initialize(models, listener);
		/**
		 * Creates command from model wrapper's neuron script
		 */
		for(IModel m : models){
			ModelWrapper wrapper = (ModelWrapper) m;
			this.processCommand(wrapper.getModel(ModelFormat.NEURON).toString());
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

	@Override
	public String getSimulatorPath(){
		return this.neuronExternalSimulatorConfig.getSimulatorPath();
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
			String[] commands = null;

			String directoryToExecuteFrom = filePath.getCanonicalPath();

			if(filePath.isDirectory()){
				if(Utilities.isWindows()){
					commands = new String[]{getSimulatorPath()+ "rxvt.exe -e "+
							getSimulatorPath()+ "sh " +getSimulatorPath().replace("/bin/","/lib/")+ "mknrndll.sh"};
				}else{
					commands = new String[]{getSimulatorPath() + "nrnivmodl"};
				}
				
			}else{
				String extension = Utilities.extension(filePath);

				_logger.info("File with extension " + extension + " detected");

				directoryToExecuteFrom = filePath.getParentFile().getAbsolutePath();
				if(extension.equals("hoc")){
					commands =  new String[]{getSimulatorPath() + "nrngui " + filePath.getAbsolutePath()};
				}
				else if(extension.equals("py")){
					if(Utilities.isWindows()){
						commands = new String[]{getSimulatorPath()+ "rxvt.exe -e "+
								getSimulatorPath()+ "sh " +getSimulatorPath().replace("/bin/","/lib/")+ "mknrndll.sh",
								getSimulatorPath()+"mkdir.exe results", getSimulatorPath() + "nrniv.exe -python " + filePath.getAbsolutePath()};
					}else{
						commands = new String[]{getSimulatorPath() + "nrnivmodl", "mkdir results", getSimulatorPath() + "nrniv -python " + filePath.getAbsolutePath()};
					}
				}

				_logger.info("Command to Execute: " + commands + " ...");
				_logger.info("From directory : " + directoryToExecuteFrom);
			}

			//send command, directory where execution is happening, and path 
			//to original file script to exceture
			this.runExternalProcess(commands, directoryToExecuteFrom, originalFileName);
		}
		catch(IOException e){

		}
	}
	
	@Override
	public void simulate(IRunConfiguration runConfiguration, AspectNode aspect)
			throws GeppettoExecutionException {
		
	}

	@Override
	public boolean populateVisualTree(AspectNode aspectNode)
			throws ModelInterpreterException, GeppettoExecutionException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void registerGeppettoService()
	{
		List<IModelFormat> modelFormatList = new ArrayList<IModelFormat>();
		modelFormatList.add(ModelFormat.NEURON);
		ServicesRegistry.registerSimulatorService(this, modelFormatList);
	}

	@Override
	public void processDone(String[] processCommand) throws GeppettoExecutionException {
		super.processDone(processCommand);
		ExternalProcess process = this.getExternalProccesses().get(processCommand);
		File results = new File(process.getExecutionDirectoryPath()+"/results");
		if(results.exists()){
			File[] resultFiles = results.listFiles();
			ConvertDATToRecording datConverter;
			try {
				datConverter = new ConvertDATToRecording("results.h5");
				for(File f : resultFiles){
					String extension = Utilities.extension(f);
					if(extension.equals("dat")){
						//FIXME: Remove hack to assign variables
						String[] s = {"time","b","c","d"};
						if(f.getName().equals("ex5_v.dat")){
							s = new String[2];
							s[0] = "time";
							s[1]= "a";
						}
						datConverter.addDATFile(f.getAbsolutePath(),s);
					}
				}
				datConverter.convert();
			} catch (Exception e) {
				throw new GeppettoExecutionException(e);
			}
		}
	}

}
