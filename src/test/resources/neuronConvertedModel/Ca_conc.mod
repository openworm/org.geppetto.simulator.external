TITLE Mod file for component: Component(id=Ca_conc type=fixedFactorConcentrationModel)

COMMENT

    This NEURON file has been generated by org.neuroml.export (see https://github.com/NeuroML/org.neuroml.export)
         org.neuroml.export  v1.4.4
         org.neuroml.model   v1.4.4
         jLEMS               v0.9.8.4

ENDCOMMENT

NEURON {
    SUFFIX Ca_conc
    USEION ca READ cai, cao, ica WRITE cai VALENCE 2
    RANGE cai
    RANGE cao
    GLOBAL initialConcentration
    GLOBAL initialExtConcentration
    RANGE restingConc                       : parameter
    RANGE decayConstant                     : parameter
    RANGE rho                               : parameter
    
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
    (umol) = (micromole)
    (S) = (siemens)
    
}

PARAMETER {
    surfaceArea (um2)
    iCa (nA)
    initialConcentration (mM)
    initialExtConcentration (mM)
    
    restingConc = 0 (mM)
    decayConstant = 100 (ms)
    rho = 9543.15 (mM m2 /A /s)
}

ASSIGNED {
    cai (mM)
    cao (mM)
    ica (mA/cm2)
    diam (um)
    area (um2)
    rate_concentration (mM/ms)
    
}

STATE {
    concentration (mM) 
    extConcentration (mM) 
    
}

INITIAL {
    initialConcentration = cai
    initialExtConcentration = cao
    rates()
    rates() ? To ensure correct initialisation.
    
    concentration = initialConcentration
    
    extConcentration = initialExtConcentration
    
}

BREAKPOINT {
    
    SOLVE states METHOD cnexp
    
    if (concentration  <= 0) {
        concentration = 0 ? standard OnCondition
    }
    
    
}

DERIVATIVE states {
    rates()
    concentration' = rate_concentration
    cai = concentration 
    
}

PROCEDURE rates() {
    
    surfaceArea = area   : surfaceArea has units (um2), area (built in to NEURON) is in um^2...
    
    iCa = -1 * (0.01) * ica * surfaceArea :   iCa has units (nA) ; ica (built in to NEURON) has units (mA/cm2)...
    
    rate_concentration = (iCa/surfaceArea) *  rho  - ((  concentration   -   restingConc  ) /   decayConstant  ) ? Note units of all quantities used here need to be consistent!
    
     
    
}

