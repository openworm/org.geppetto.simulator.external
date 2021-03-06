TITLE Mod file for component: Component(id=Kdr_pyr type=ionChannelHH)

COMMENT

    This NEURON file has been generated by org.neuroml.export (see https://github.com/NeuroML/org.neuroml.export)
         org.neuroml.export  v1.4.4
         org.neuroml.model   v1.4.4
         jLEMS               v0.9.8.4

ENDCOMMENT

NEURON {
    SUFFIX Kdr_pyr
    USEION k WRITE ik VALENCE 1 ? Assuming valence = 1; TODO check this!!
    
    RANGE gion                           
    RANGE gmax                              : Will be changed when ion channel mechanism placed on cell!
    RANGE conductance                       : parameter
    
    RANGE g                                 : exposure
    
    RANGE fopen                             : exposure
    RANGE n_instances                       : parameter
    
    RANGE n_alpha                           : exposure
    
    RANGE n_beta                            : exposure
    
    RANGE n_tau                             : exposure
    
    RANGE n_inf                             : exposure
    
    RANGE n_rateScale                       : exposure
    
    RANGE n_fcond                           : exposure
    RANGE n_reverseRate_rate                : parameter
    RANGE n_reverseRate_midpoint            : parameter
    RANGE n_reverseRate_scale               : parameter
    
    RANGE n_reverseRate_r                   : exposure
    RANGE n_forwardRate_rate                : parameter
    RANGE n_forwardRate_midpoint            : parameter
    RANGE n_forwardRate_scale               : parameter
    
    RANGE n_forwardRate_r                   : exposure
    RANGE n_forwardRate_x                   : derived variable
    RANGE conductanceScale                  : derived variable
    RANGE fopenHHrates                      : derived variable
    RANGE fopenHHtauInf                     : derived variable
    RANGE fopenHHratesTau                   : derived variable
    RANGE fopenHHratesInf                   : derived variable
    RANGE fopenHHratesTauInf                : derived variable
    RANGE fopenHHInstantaneous              : derived variable
    
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
    
    gmax = 0  (S/cm2)                       : Will be changed when ion channel mechanism placed on cell!
    
    conductance = 1.0E-5 (uS)
    n_instances = 1 
    n_reverseRate_rate = 0.125 (kHz)
    n_reverseRate_midpoint = -40 (mV)
    n_reverseRate_scale = -40 (mV)
    n_forwardRate_rate = 0.040000003 (kHz)
    n_forwardRate_midpoint = -24.9 (mV)
    n_forwardRate_scale = 5 (mV)
}

ASSIGNED {
    
    gion   (S/cm2)                          : Transient conductance density of the channel? Standard Assigned variables with ionChannel
    v (mV)
    celsius (degC)
    temperature (K)
    ek (mV)
    ik (mA/cm2)
    
    
    n_reverseRate_r (kHz)                  : derived variable
    
    n_forwardRate_x                        : derived variable
    
    n_forwardRate_r (kHz)                  : conditional derived var...
    
    n_rateScale                            : derived variable
    
    n_alpha (kHz)                          : derived variable
    
    n_beta (kHz)                           : derived variable
    
    n_fcond                                : derived variable
    
    n_inf                                  : derived variable
    
    n_tau (ms)                             : derived variable
    
    conductanceScale                       : derived variable
    
    fopenHHrates                           : derived variable
    
    fopenHHtauInf                          : derived variable
    
    fopenHHratesTau                        : derived variable
    
    fopenHHratesInf                        : derived variable
    
    fopenHHratesTauInf                     : derived variable
    
    fopenHHInstantaneous                   : derived variable
    
    fopen                                  : derived variable
    
    g (uS)                                 : derived variable
    rate_n_q (/ms)
    
}

STATE {
    n_q  
    
}

INITIAL {
    ek = -75.0
    
    temperature = celsius + 273.15
    
    rates()
    rates() ? To ensure correct initialisation.
    
    n_q = n_inf
    
}

BREAKPOINT {
    
    SOLVE states METHOD cnexp
    
    ? DerivedVariable is based on path: conductanceScaling[*]/factor, on: Component(id=Kdr_pyr type=ionChannelHH), from conductanceScaling; null
    ? Path not present in component, using factor: 1
    
    conductanceScale = 1 
    
    ? DerivedVariable is based on path: gatesHHrates[*]/fcond, on: Component(id=Kdr_pyr type=ionChannelHH), from gatesHHrates; Component(id=n type=gateHHrates)
    ? multiply applied to all instances of fcond in: <gatesHHrates> ([Component(id=n type=gateHHrates)]))
    fopenHHrates = n_fcond ? path based
    
    ? DerivedVariable is based on path: gatesHHtauInf[*]/fcond, on: Component(id=Kdr_pyr type=ionChannelHH), from gatesHHtauInf; null
    ? Path not present in component, using factor: 1
    
    fopenHHtauInf = 1 
    
    ? DerivedVariable is based on path: gatesHHratesTau[*]/fcond, on: Component(id=Kdr_pyr type=ionChannelHH), from gatesHHratesTau; null
    ? Path not present in component, using factor: 1
    
    fopenHHratesTau = 1 
    
    ? DerivedVariable is based on path: gatesHHratesInf[*]/fcond, on: Component(id=Kdr_pyr type=ionChannelHH), from gatesHHratesInf; null
    ? Path not present in component, using factor: 1
    
    fopenHHratesInf = 1 
    
    ? DerivedVariable is based on path: gatesHHratesTauInf[*]/fcond, on: Component(id=Kdr_pyr type=ionChannelHH), from gatesHHratesTauInf; null
    ? Path not present in component, using factor: 1
    
    fopenHHratesTauInf = 1 
    
    ? DerivedVariable is based on path: gateHHInstantaneous[*]/fcond, on: Component(id=Kdr_pyr type=ionChannelHH), from gateHHInstantaneous; null
    ? Path not present in component, using factor: 1
    
    fopenHHInstantaneous = 1 
    
    fopen = conductanceScale  *  fopenHHrates  *  fopenHHtauInf  *  fopenHHratesTau  *  fopenHHratesInf  *  fopenHHratesTauInf  *  fopenHHInstantaneous ? evaluable
    g = conductance  *  fopen ? evaluable
    gion = gmax * fopen 
    
    ik = gion * (v - ek)
    
}

DERIVATIVE states {
    rates()
    n_q' = rate_n_q 
    
}

PROCEDURE rates() {
    
    n_reverseRate_r = n_reverseRate_rate  * exp((v -  n_reverseRate_midpoint )/ n_reverseRate_scale ) ? evaluable
    n_forwardRate_x = (v -  n_forwardRate_midpoint ) /  n_forwardRate_scale ? evaluable
    if (n_forwardRate_x  != 0)  { 
        n_forwardRate_r = n_forwardRate_rate  *  n_forwardRate_x  / (1 - exp(0 -  n_forwardRate_x )) ? evaluable cdv
    } else if (n_forwardRate_x  == 0)  { 
        n_forwardRate_r = n_forwardRate_rate ? evaluable cdv
    }
    
    ? DerivedVariable is based on path: q10Settings[*]/q10, on: Component(id=n type=gateHHrates), from q10Settings; null
    ? Path not present in component, using factor: 1
    
    n_rateScale = 1 
    
    ? DerivedVariable is based on path: forwardRate/r, on: Component(id=n type=gateHHrates), from forwardRate; Component(id=null type=HHExpLinearRate)
    n_alpha = n_forwardRate_r ? path based
    
    ? DerivedVariable is based on path: reverseRate/r, on: Component(id=n type=gateHHrates), from reverseRate; Component(id=null type=HHExpRate)
    n_beta = n_reverseRate_r ? path based
    
    n_fcond = n_q ^ n_instances ? evaluable
    n_inf = n_alpha /( n_alpha + n_beta ) ? evaluable
    n_tau = 1/(( n_alpha + n_beta ) *  n_rateScale ) ? evaluable
    
     
    
     
    
     
    
     
    
     
    
     
    
     
    
     
    
     
    
     
    
     
    
     
    rate_n_q = ( n_inf  -  n_q ) /  n_tau ? Note units of all quantities used here need to be consistent!
    
     
    
     
    
     
    
}

