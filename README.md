<p align="center">  
  <img src="https://github.com/tarelli/bucket/blob/master/geppetto%20logo.png?raw=true" alt="Geppetto logo"/>
</p>

org.geppetto.simulator.external
======================================

Geppetto Simulator Bundle for External Services

[OpenWorm Project](http://openworm.org)

Installation
------------

* Install NEURON (check the [.travis.yml](.travis.yml) for detailed instructions on this and other dependencies)
* Don't forget to set the NEURON_HOME environment variable to the bin directory.  The final location may be under a directory like x86_64 after build has completed.
* Install the latest versions of NetPyNE, libNeuroML and pyNeuroML, e.g. `pip install libneuroml --update`.
* Install jNeuroML (`svn checkout svn://svn.code.sf.net/p/neuroml/code/jNeuroMLJar`) and set JNML_HOME.
* Fill in paths for your system pointing at the location of NEURON's bin directory and the directory containing jNeuroML's jnml in the [app-config.xml file](https://github.com/openworm/org.geppetto.simulator.external/blob/556c4ee4a1520a50be7fda9b827399c6be2d6faa/src/main/java/META-INF/spring/app-config.xml#L24) (multiple places)
* Set the [aws.credentials file up correctly](http://docs.geppetto.org/en/latest/persistence.html#setting-up-for-amazon-s3-support)
