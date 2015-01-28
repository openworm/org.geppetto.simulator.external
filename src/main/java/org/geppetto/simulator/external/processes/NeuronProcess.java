package org.geppetto.simulator.external.processes;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;

/**
 * Threaded class used to run a Neuron process
 * 
 * @author Jesus R Martinez (jesus@metacell.us)
 *
 */
public class NeuronProcess extends Thread{
	
	private static Log _logger = LogFactory.getLog(NeuronProcess.class);
	private File _file;
	private boolean _recompile;
	public volatile boolean run = true;
	
	public NeuronProcess(File file, boolean recompile){
		this._recompile = recompile;
		this._file = file;
	}
	@Override
	public void run(){
		if(run){
			run = false;
			try {
				this.compile(_file, _recompile);
			} catch (GeppettoExecutionException e) {
				_logger.error("Geppetto Exectuion Exception error : " + e.getMessage());
			}
		}
	}
	
	/*
     * Compliles all of the mod files at the specified location using NEURON's nrnivmodl/mknrndll.sh
     */
	public boolean compile(File filePath, boolean recompile) throws GeppettoExecutionException {
		_logger.info("Going to compile the mod files in: " + filePath.getAbsolutePath() + ", forcing recompile: " + recompile);

		Runtime runtime = Runtime.getRuntime();

		File neuronHome = null;
		try {

			neuronHome = Utilities.findNeuronHome();

			String command = null;

			String directoryToExecuteIn = filePath.getCanonicalPath();

			if(filePath.isDirectory()){
				command = neuronHome.getCanonicalPath()
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
				
				directoryToExecuteIn = filePath.getParentFile().getAbsolutePath();
				if(extension.equals("hoc")){
					command = neuronHome.getCanonicalPath()
							+ System.getProperty("file.separator")
							+ "bin"
							+ System.getProperty("file.separator")
							+ "nrngui " + filePath.getAbsolutePath();
				}
				else if(extension.equals("py")){
					command = "python " + filePath.getAbsolutePath();
				}
			}
			
			_logger.info("Command to Execute: " + command + " ...");
			_logger.info("From directory : " + directoryToExecuteIn);
			
			Process currentProcess = runtime.exec(command, null, new File(directoryToExecuteIn));
			NeuronProcessWatcher procOutputMain = new NeuronProcessWatcher(currentProcess.getInputStream(),  "NMODL Compile >> ");
			procOutputMain.start();

			NeuronProcessWatcher procOutputError = new NeuronProcessWatcher(currentProcess.getErrorStream(), "NMODL Error   >> ");
			procOutputError.start();

			_logger.info("Successfully executed command: " + command);
			
			currentProcess.waitFor();
		} catch (InterruptedException e) {
			_logger.error("Interrupted Exception " + e.getMessage());
		} catch (GeppettoInitializationException e) {
			_logger.error("Initialization error" + e.getMessage());
		} catch (IOException e) {
			_logger.error("Initialization error " + e.getMessage());
		}
        return true;
    }
}
