TITLE Mod file for component: Component(id=pulseGen1 type=pulseGenerator)

COMMENT

    This NEURON file has been generated by org.neuroml.export (see https://github.com/NeuroML/org.neuroml.export)
         org.neuroml.export  v1.4.2
         org.neuroml.model   v1.4.2
         jLEMS               v0.9.7.3

ENDCOMMENT

NEURON {
    POINT_PROCESS pulseGen1
    ELECTRODE_CURRENT i
    RANGE delay                             : parameter
    RANGE duration                          : parameter
    RANGE amplitude                         : parameter
    
}

UNITS {
    
    (nA) = (nanoamp)
    (uA) = (microamp)
    (mA) = (milliamp)
    (A) = (amp)
    (mV) = (millivolt)
    (mS) = (millisiemens)
    (uS) = (microsiemens)
    (molar) = (1/liter)
    (kHz) = (kilohertz)
    (mM) = (millimolar)
    (um) = (micrometer)
    (S) = (siemens)
    
}

PARAMETER {
    
    delay = 0 (ms)
    duration = 9.9999998E10 (ms)
    amplitude = 0.08 (nA)
}

STATE {
    i (nA)
    
}

INITIAL {
    rates()
    
}

BREAKPOINT {
    
    rates()
    if (t <=  delay) {
        i = 0 ? standard OnCondition
    }
    
    if (t >=  delay  && t <=  duration  +  delay) {
        i = amplitude ? standard OnCondition
    }
    
    if (t >=  duration  +  delay) {
        i = 0 ? standard OnCondition
    }
    
    
}

PROCEDURE rates() {
    
    
     
    
}
