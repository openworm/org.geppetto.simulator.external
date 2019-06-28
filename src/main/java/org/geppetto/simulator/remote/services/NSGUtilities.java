package org.geppetto.simulator.remote.services;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.geppetto.core.common.GeppettoExecutionException;
import org.ngbw.directclient.CiCipresException;
import org.ngbw.directclient.CiClient;
import org.ngbw.directclient.CiJob;

/**
 * Utility class for services. 
 * 
 * @author jrmartin
 *
 */
public class NSGUtilities {

	public static CiJob sendJob(CiClient myClient, long jobId, Path filePath, int numberProcessors, boolean validateOnly, String info) throws CiCipresException, IOException, GeppettoExecutionException, InterruptedException
	{
		CiJob jobStatus;
		
		Map<String, Collection<String>> vParams = new HashMap<String, Collection<String>>();
		HashMap<String, String> inputParams = new HashMap<String, String>();
		HashMap<String, String> metadata = new HashMap<String, String>();
        
        String tool = "OSBPYNEURON74";
        
        int numCores = numberProcessors;
        int numNodes = 1;
        int maxPerNode = 16;
        if (numberProcessors>maxPerNode)
        {
            numCores = maxPerNode;
            numNodes = (int)Math.round(numberProcessors/(float)maxPerNode);
        }
            
		vParams.put("number_cores_", Arrays.asList(numCores+""));
		vParams.put("number_nodes_", Arrays.asList(numNodes+""));

		inputParams.put("infile_", filePath.toString());

		metadata.put("statusEmail", "true");
		// metadata.put("clientJobName", jobName);
		metadata.put("clientJobId", info);

		if(validateOnly)
		{
			jobStatus = myClient.validateJob(tool, vParams, inputParams, metadata);
		}
		else
		{
			jobStatus = myClient.submitJob(tool, vParams, inputParams, metadata);
		}
		jobStatus.show(true);
		
		return jobStatus;
	}

	public static void listJobs(CiClient myClient) throws CiCipresException
	{
		System.out.println("List all jobs");
		int count = 0;
		Collection<CiJob> jobs = myClient.listJobs();
		for(CiJob job : jobs)
		{
			count += 1;
			System.out.print("\n" + count + ". ");
			job.show(true);
		}
	}

	public static void deleteAllJobs(CiClient myClient) throws CiCipresException
	{
		Collection<CiJob> jobs = myClient.listJobs();
		for(CiJob job : jobs)
		{
			job.delete();
		}
	}

	public static void checkJobStatus(CiJob jobStatus) throws CiCipresException, GeppettoExecutionException, InterruptedException
	{
		jobStatus.update();
		jobStatus.show(true);
		if(jobStatus.isDone())
		{
			jobStatus.getJobStage();
		}
		else if (jobStatus.isError()) {
			jobStatus.getJobStage();
			throw new GeppettoExecutionException("Error executing job");
		}
		else
		{
			System.out.println("Current job status");
			Thread.sleep(5000);
			checkJobStatus(jobStatus);
		}
	}
	
	public static void extractResults(File outputFileFolder, File resultsFileFolder) throws FileNotFoundException, IOException
	{
		// Extracting output folder
		TarArchiveInputStream tarInput = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(outputFileFolder + "/output.tar.gz")));
		TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
		
		File currentFolder = null;
		boolean isOutput = false;
		while(currentEntry != null)
		{
			if(!currentEntry.isDirectory() && currentEntry.getName().contains("input/results/")){
				currentFolder = resultsFileFolder;
				isOutput = true;
			}
			else if (currentEntry.getName().contains("time.dat"))
			{
				currentFolder = outputFileFolder;
				isOutput = true;
			}
			
			if (isOutput){
				File destPath = new File(currentFolder, currentEntry.getName().substring(currentEntry.getName().lastIndexOf("/")+1));
				destPath.createNewFile();
				byte[] btoRead = new byte[1024];
				BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(destPath));
				int len = 0;

				while((len = tarInput.read(btoRead)) != -1)
				{
					bout.write(btoRead, 0, len);
				}

				bout.close();
				btoRead = null;
			}
			
			isOutput = false;
			currentEntry = tarInput.getNextTarEntry();
		}
		tarInput.close();

	}
    
    
}