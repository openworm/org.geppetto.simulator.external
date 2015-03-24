package org.geppetto.simulator.external.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Utility class for services. 
 * 
 * @author jrmartin
 *
 */
public class Utilities {

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
    }
    
    public static String extension(File f){
    	String extension = "";

		int i = f.getAbsolutePath().lastIndexOf('.');
		if (i > 0) {
			extension = f.getAbsolutePath().substring(i+1);
		}
		
		return extension;
    }
    
    public static void delete(File f) throws IOException {
    	if (f.isDirectory()) {
    	    for (File c : f.listFiles())
    	      delete(c);
    	  }
    	  if (!f.delete())
    	    throw new FileNotFoundException("Failed to delete file: " + f);
    }
    
    
}