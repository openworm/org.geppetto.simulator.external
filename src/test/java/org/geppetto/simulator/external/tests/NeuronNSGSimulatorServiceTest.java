/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2011, 2013 OpenWorm.
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
package org.geppetto.simulator.external.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.geppetto.core.beans.SimulatorConfig;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.data.model.IAspectConfiguration;
import org.geppetto.core.data.model.ResultsFormat;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.core.simulator.RemoteSimulatorConfig;
import org.geppetto.model.ExternalDomainModel;
import org.geppetto.model.GeppettoFactory;
import org.geppetto.simulator.remote.services.NSGUtilities;
import org.geppetto.simulator.remote.services.NeuronNSGSimulatorService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ngbw.directclient.CiCipresException;
import org.ngbw.directclient.CiClient;

public class NeuronNSGSimulatorServiceTest implements ISimulatorCallbackListener
{

	//AQP Review all these libs
	private static String dirToExecute;
	private static String fileToExecute;
	private static NeuronNSGSimulatorService simulator;
	private static String resultsDir;
	private static boolean done = false;
    
    // Set to true for testing on localhost with NSGR locally configured
    private static boolean localTest = false;

	@BeforeClass
	public static void setup() throws IOException
	{
        if (localTest)
        {
            System.out.println("setup NeuronNSGSimulatorServiceTest...");
            File dir = new File(NeuronNSGSimulatorServiceTest.class.getResource("/neuronConvertedModel/").getFile());
            dirToExecute = dir.getAbsolutePath();
            fileToExecute = "/main_script.py";

            simulator = new NeuronNSGSimulatorService();
            simulator.registerGeppettoService();
            
			FileReader fileReader = new FileReader(new File("src/main/java/META-INF/spring/app-config.xml"));
			BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            RemoteSimulatorConfig remoteSimulatorConfig = new RemoteSimulatorConfig();
            Map<String, String> simulatorParameters = new HashMap<String, String>();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
                System.out.println("Checking: "+line);
                String[] w = line.split("\"");
                
                if (line.indexOf("<entry key=\"appId\"")>0)
                    simulatorParameters.put("appId", w[3]);
                if (line.indexOf("<entry key=\"appName\"")>0)
                    simulatorParameters.put("appName", w[3]);
                if (line.indexOf("<entry key=\"internalUserName\"")>0)
                    simulatorParameters.put("internalUserName", w[3]);
                if (line.indexOf("<entry key=\"internalEmail\"")>0)
                    simulatorParameters.put("internalEmail", w[3]);
                
                if (line.indexOf("<property name=\"simulatorPath\"")>0)
                    remoteSimulatorConfig.setSimulatorPath(w[3]);
                if (line.indexOf("<property name=\"username\"")>0)
                    remoteSimulatorConfig.setUsername(w[3]);
                if (line.indexOf("<property name=\"password\"")>0)
                    remoteSimulatorConfig.setPassword(w[3]);
                
            }
            System.out.println("sp: "+simulatorParameters);
            
            remoteSimulatorConfig.setSimulatorParameters(simulatorParameters);
            Assert.assertNotNull(remoteSimulatorConfig.getSimulatorPath());
            simulator.setNeuronNSGExternalSimulatorConfig(remoteSimulatorConfig);
            SimulatorConfig simulatorConfig = new SimulatorConfig();
            simulatorConfig.setSimulatorID("neuronNSGSimulator");
            simulatorConfig.setSimulatorName("neuronNSGSimulator");
            simulator.setNeuronSimulatorConfig(simulatorConfig);

            System.out.println("SC: "+remoteSimulatorConfig.getSimulatorParameters().keySet());
            System.out.println("Setup; "+dirToExecute);
        }
	}

	/**
	 * Test method for {@link org.geppetto.simulator.external.services.NeuronSimulatorService}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNeuronExecution() throws Exception
	{
        if (localTest)
        {
            System.out.println("testNeuronExecution...");
            ExternalDomainModel model = GeppettoFactory.eINSTANCE.createExternalDomainModel();
            model.setFormat(ServicesRegistry.getModelFormat("NEURON"));
            model.setDomainModel(dirToExecute + fileToExecute);
            simulator.initialize(model, null, null, this, null);
            simulator.simulate();
            System.out.println("Sleeping...");
            Thread.sleep(6000);
            Assert.assertTrue(done);
            System.out.println("Done testNeuronExecution...");
        }
	}
	
	/**
	 * Test method for {@link org.geppetto.simulator.external.services.NeuronSimulatorService}.
	 * @throws GeppettoExecutionException 
	 * @throws GeppettoInitializationException 
	 * @throws CiCipresException 
	 * @throws InterruptedException 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNeuronList() throws GeppettoInitializationException, GeppettoExecutionException, CiCipresException, InterruptedException
	{
        if (localTest)
        {
            System.out.println("testNeuronList...");
            ExternalDomainModel model = GeppettoFactory.eINSTANCE.createExternalDomainModel();
            model.setFormat(ServicesRegistry.getModelFormat("NEURON"));
            model.setDomainModel(dirToExecute + fileToExecute);
    //        File localCompModFiledir = new File(dirToExecute,"x86_64");
    //        System.out.println("Checking "+localCompModFiledir.getAbsolutePath() );
    //        if (localCompModFiledir.exists())
    //            localCompModFiledir.

            simulator.initialize(model, null, null, this, null);
            NSGUtilities.listJobs(simulator.getCiClient());
            Thread.sleep(2000);
            System.out.println("Done testNeuronList...");
        }
	}
	
	/**
	 * Test method for {@link org.geppetto.simulator.external.services.NeuronSimulatorService}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNeuronDeleteJobs() throws Exception
	{
//		ExternalDomainModel model = GeppettoFactory.eINSTANCE.createExternalDomainModel();
//		model.setFormat(ServicesRegistry.getModelFormat("NEURON"));
//		model.setDomainModel(dirToExecute + fileToExecute);
//		simulator.initialize(model, null, null, this, null);
//		simulator.deleteAllJobs();
	}
	
	/**
	 * Test method for {@link org.geppetto.simulator.external.services.NeuronSimulatorService}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNeuronProcessDone() throws Exception
	{
//		ExternalDomainModel model = GeppettoFactory.eINSTANCE.createExternalDomainModel();
//		model.setFormat(ServicesRegistry.getModelFormat("NEURON"));
//		model.setDomainModel(dirToExecute + fileToExecute);
//		simulator.initialize(model, null, null, this, null);
//		simulator.processDone();
	}
	
	@Override
	public void endOfSteps(IAspectConfiguration aspectConfiguration, Map<File, ResultsFormat> results)
	{
        
        System.out.println("endOfSteps; have recieved "+results.size()+" results...");
        
        if (results.size()==2)
            done = true;
		
	}

	@AfterClass
	public static void doYourOneTimeTeardown() throws IOException
	{
	}

	@Override
	public void externalProcessFailed(String message, Exception e) {
		// TODO Auto-generated method stub
		
	}

}
