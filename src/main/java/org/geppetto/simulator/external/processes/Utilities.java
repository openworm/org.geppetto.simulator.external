package org.geppetto.simulator.external.processes;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.geppetto.core.common.GeppettoInitializationException;

public class Utilities {

	public static final String ARCH_I686 = "i686";
    public static final String ARCH_I386 = "i386";
    public static final String ARCH_64BIT = "amd64";
    public static final String ARCH_POWERPC = "ppc";
    public static final String ARCH_UMAC = "umac";

    public static final String DIR_I386 = "i386";
    public static final String DIR_I686 = "i686";
    public static final String DIR_64BIT = "x86_64";
    public static final String DIR_POWERPC = "powerpc";
    public static final String DIR_UMAC = "umac";
    
	public static String NEURON_HOME = "NEURON_HOME";

	public static boolean isWindowsBasedPlatform()
    {
        return System.getProperty("os.name").toLowerCase().indexOf("indows") > 0;
    }
    
    public static boolean isLinuxBasedPlatform()
    {
        return System.getProperty("os.name").toLowerCase().indexOf("nix") >= 0 ||
            System.getProperty("os.name").toLowerCase().indexOf("linux") >= 0;
    }


    public static boolean isMacBasedPlatform()
    {
        if (isWindowsBasedPlatform()) return false;
        if (isLinuxBasedPlatform()) return false;

        return System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
    }

	public static String getArchSpecificDir() {
		if (!isMacBasedPlatform() &&
	            (System.getProperty("os.arch").equals(ARCH_64BIT) ||
	            System.getProperty("os.arch").indexOf("64")>=0))
	        {
	            return DIR_64BIT;
	        }
	        else if (isMacBasedPlatform() && System.getProperty("os.arch").indexOf(ARCH_POWERPC)>=0)
	        {
	            return DIR_POWERPC;
	        }
	        else if (isMacBasedPlatform() && System.getProperty("os.arch").indexOf(ARCH_I386)>=0)
	        {
	            return DIR_I686;
	        }
	        else
	        {
	            return DIR_I686;
	        }
	}

	public static File findNeuronHome() throws GeppettoInitializationException {

        ArrayList<String> options = new ArrayList<String>();
        String nrnEnvVar = System.getenv(Utilities.NEURON_HOME);
        if (nrnEnvVar != null) {
            options.add(nrnEnvVar);
        } else {

            if (Utilities.isWindowsBasedPlatform()) {
                Collections.addAll(options, "C://nrn73/bin/nrniv.exe", "C://nrn72/bin/nrniv.exe", "C://nrn71/bin/nrniv.exe", "C://nrn70/bin/nrniv.exe", "C://nrn62/bin/nrniv.exe", "C://nrn61/bin/nrniv.exe", "C://nrn60/bin/nrniv.exe");

            } else if (Utilities.isMacBasedPlatform()) {
                String[] vers = new String[]{"7.3", "7.2", "7.1", "6.2", "6.1", "6.0"};
                for (String ver : vers) {
                    options.add("/Applications/NEURON-" + ver + "/nrn/powerpc");
                    options.add("/Applications/NEURON-" + ver + "/nrn/umac");
                    options.add("/Applications/NEURON-" + ver + "/nrn/i386");
                }

            } else if (Utilities.isLinuxBasedPlatform()) {
                options.add("/usr/local/nrn/x86_64");
            }
        }

        for (String option : options) {
            File f = new File(option);
            if (f.exists()) {
                return f;
            }
        }

        throw new GeppettoInitializationException("Could not find NEURON home directory! Options tried: " + options +"\n\n"
            + "Try setting the environment variable "+ NEURON_HOME +" to the location of your NEURON installation (containing bin), e.g.\n\n"
            + "  export "+NEURON_HOME+"=/home/myuser/nrn7/x86_64\n");
    }
}
