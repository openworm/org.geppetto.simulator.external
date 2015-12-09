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
package org.geppetto.simulator.external.tests;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Arrays;

import junit.framework.Assert;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.h5.H5File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.emf.ecore.EClass;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.manager.SharedLibraryManager;
import org.geppetto.model.ExperimentState;
import org.geppetto.model.GeppettoFactory;
import org.geppetto.model.GeppettoLibrary;
import org.geppetto.model.VariableValue;
import org.geppetto.model.types.StateVariableType;
import org.geppetto.model.types.Type;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.values.Pointer;
import org.geppetto.model.values.PointerElement;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.VariablesFactory;
import org.geppetto.simulator.external.converters.ConvertDATToRecording;
import org.junit.AfterClass;
import org.junit.Test;

public class TestConvertDATToRecordingClass
{

	private static Log _logger = LogFactory.getLog(TestConvertDATToRecordingClass.class);

	@Test
	public void datToHDF5()
	{
		try
		{
			ExperimentState experimentState = GeppettoFactory.eINSTANCE.createExperimentState();
			addVariableValue(experimentState, "a");
			addVariableValue(experimentState, "b");
			addVariableValue(experimentState, "c");
			addVariableValue(experimentState, "d");

			ConvertDATToRecording datConverter = new ConvertDATToRecording("sample.h5");
			String[] a = { "time(StateVariable)", "d(StateVariable)" };
			datConverter.addDATFile("src/test/resources/sample/results/ex5_v.dat", a);

			String[] b = { "time(StateVariable)", "a(StateVariable)", "b(StateVariable)", "c(StateVariable)" };
			datConverter.addDATFile("src/test/resources/sample/results/ex5_vars.dat", b);
			datConverter.convert(experimentState);

			assertNotNull(datConverter.getRecordingsFile());

			H5File file = datConverter.getRecordingsFile();
			file.open();
			Dataset dataset = (Dataset) file.findObject(file, "/b(StateVariable)");
			float[] value = (float[]) dataset.read();
			Assert.assertEquals(0.596121f, value[0]);
			Assert.assertEquals(0.596119f, value[1]);

			Dataset dataset2 = (Dataset) file.findObject(file, "/a(StateVariable)");
			float[] value2 = (float[]) dataset2.read();
			Assert.assertEquals(0.052932f, value2[0]);
			Assert.assertEquals(0.052941f, value2[1]);

			Dataset dataset3 = (Dataset) file.findObject(file, "/c(StateVariable)");
			float[] value3 = (float[]) dataset3.read();
			Assert.assertEquals(0.317677f, value3[0]);
			Assert.assertEquals(0.317678f, value3[1]);

			Dataset dataset4 = (Dataset) file.findObject(file, "/d(StateVariable)");
			float[] value4 = (float[]) dataset4.read();
			Assert.assertEquals(-0.065000f, value4[0]);
			Assert.assertEquals(-0.064968f, value4[1]);

			file.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void addVariableValue(ExperimentState experimentState, String variable) throws GeppettoVisitingException, GeppettoInitializationException
	{
		VariableValue vv = GeppettoFactory.eINSTANCE.createVariableValue();
		Pointer p = ValuesFactory.eINSTANCE.createPointer();
		StateVariableType stateVariableType=(StateVariableType) getType(SharedLibraryManager.getSharedCommonLibrary(),TypesPackage.Literals.STATE_VARIABLE_TYPE);
		Variable v=VariablesFactory.eINSTANCE.createVariable();
		v.setId(variable);
		v.setName(variable);
		PointerElement pelem=ValuesFactory.eINSTANCE.createPointerElement();
		pelem.setVariable(v);
		pelem.setType(stateVariableType);
		p.getElements().add(pelem);
		vv.setPointer(p);
		experimentState.getRecordedVariables().add(vv);
	}
	
	
	/**
	 * Usage commonLibraryAccess.getType(TypesPackage.Literals.PARAMETER_TYPE);
	 * 
	 * @return
	 * @throws GeppettoVisitingException
	 */
	public Type getType(GeppettoLibrary library,EClass eclass) throws GeppettoVisitingException
	{
		for(Type type : library.getTypes())
		{
			if(type.eClass().equals(eclass))
			{
				return type;
			}
		}
		throw new GeppettoVisitingException("Type for eClass " + eclass + " not found in common library.");
	}

	@AfterClass
	public static void teardown() throws Exception
	{
		File sampleFile = new File(System.getProperty("user.dir") + "/sample.h5");
		if(sampleFile.exists())
		{
			sampleFile.delete();
			_logger.info("Deleting sample h5");
		}
	}

}
