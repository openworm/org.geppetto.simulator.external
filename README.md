<p align="center">
  <img src="https://dl.dropboxusercontent.com/u/7538688/geppetto%20logo.png?dl=1" alt="Geppetto logo"/>
</p>

org.geppetto.simulator.external
======================================

Geppetto Simulator Bundle for External Services

[OpenWorm Project](http://openworm.org)

Installation
------------

* Install NEURON (check the .travis.yml for instructions)
 * Don't forget to set the NEURON_HOME environment variable to the bin directory.  The final location may be under a directory like x86_64 after build has completed.
* Fill in paths for your system pointing at the location of NEURON's bin directory in the [app-config.xml file](https://github.com/openworm/org.geppetto.simulator.external/blob/556c4ee4a1520a50be7fda9b827399c6be2d6faa/src/main/java/META-INF/spring/app-config.xml#L24) (two places).
* Set the [aws.credentials file up correctly](http://docs.geppetto.org/en/latest/persistence.html#setting-up-for-amazon-s3-support)
