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

import junit.framework.Assert;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.h5.H5File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.simulator.external.converters.ConvertDATToRecording;
import org.junit.Test;

public class TestConvertDATToRecordingClass {

	private static Log _logger = LogFactory.getLog(TestConvertDATToRecordingClass.class);

	@Test
	public void datToHDF5(){
		try {
			ConvertDATToRecording datConverter = new ConvertDATToRecording("sample.h5");
			String[] a = {"time","a"};
			datConverter.addDATFile("src/test/resources/sample/results/ex5_v.dat",a);
			
			String[] b = {"time","b","c","d"};
			datConverter.addDATFile("src/test/resources/sample/results/ex5_vars.dat",b);
			datConverter.convert();
			
			assertNotNull(datConverter.getRecordingsFile());
			
			H5File file = datConverter.getRecordingsFile();
			file.open();
			Dataset dataset = (Dataset) file.findObject(file, "/a");
			float[] value =  (float[])dataset.read();
			Assert.assertEquals(0.596121f,value[0]);
			Assert.assertEquals(0.596119f,value[1]);

			Dataset dataset2 = (Dataset) file.findObject(file, "/b");
			float[] value2 =  (float[])dataset2.read();
			Assert.assertEquals(0.052932f,value2[0]);
			Assert.assertEquals(0.052941f,value2[1]);
			
			Dataset dataset3 = (Dataset) file.findObject(file, "/c");
			float[] value3 =  (float[])dataset3.read();
			Assert.assertEquals(0.317677f,value3[0]);
			Assert.assertEquals(0.317678f,value3[1]);
			
			
			Dataset dataset4 = (Dataset) file.findObject(file, "/d");
			float[] value4 =  (float[])dataset4.read();
			Assert.assertEquals(-0.065000f,value4[0]);
			Assert.assertEquals(-0.064968f,value4[1]);
			
			file.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
