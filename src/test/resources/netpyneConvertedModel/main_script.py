'''
NetPyNE simulator compliant export for:

Components:
    passiveChan (Type: ionChannelHH:  conductance=1.0E-11 (SI conductance))
    naChan (Type: ionChannelHH:  conductance=1.0E-11 (SI conductance))
    kChan (Type: ionChannelHH:  conductance=1.0E-11 (SI conductance))
    hhcell (Type: cell)
    pulseGen1 (Type: pulseGenerator:  delay=0.1 (SI time) duration=0.1 (SI time) amplitude=8.000000000000001E-11 (SI current))
    net1 (Type: network)
    sim1 (Type: Simulation:  length=0.3 (SI time) step=1.0E-5 (SI time))


    This NetPyNE file has been generated by org.neuroml.export (see https://github.com/NeuroML/org.neuroml.export)
         org.neuroml.export  v1.5.2
         org.neuroml.model   v1.5.2
         jLEMS               v0.9.8.9

'''
# Main NetPyNE script for: net1

# See https://github.com/Neurosim-lab/netpyne

from netpyne import specs  # import netpyne specs module
from netpyne import sim    # import netpyne sim module

from neuron import h


###############################################################################
# NETWORK PARAMETERS
###############################################################################

nml2_file_name = 'NET_NML2_Ex5_DetCell.net.nml'

###############################################################################
# SIMULATION PARAMETERS
###############################################################################

simConfig = specs.SimConfig()   # object of class SimConfig to store the simulation configuration

# Simulation parameters
simConfig.duration = simConfig.tstop = 300.0 # Duration of the simulation, in ms
simConfig.dt = 0.01 # Internal integration timestep to use

# Seeds for randomizers (connectivity, input stimulation and cell locations)
# Note: locations and connections should be fully specified by the structure of the NeuroML,
# so seeds for conn & loc shouldn't affect networks structure/behaviour
simConfig.seeds = {'conn': 0, 'stim': 123456789, 'loc': 0} 

simConfig.createNEURONObj = 1  # create HOC objects when instantiating network
simConfig.createPyStruct = 1  # create Python structure (simulator-independent) when instantiating network
simConfig.verbose = False  # show detailed messages 

# Recording 
simConfig.recordCells = ['all']  
simConfig.recordTraces = {}

# For saving to file: results/ex5_v.dat (ref: of0)
# Column: v: Pop: hhpop; cell: 0; segment id: 0; segment name: soma; Neuron loc: soma(0.5); value: v (v)
simConfig.recordTraces['of0_hhpop_0_soma_v'] = {'sec':'soma','loc':0.5,'var':'v','conds':{'popLabel':'hhpop','cellLabel':0}}
# For saving to file: results/ex5_vars.dat (ref: of1)
# Column: m: Pop: hhpop; cell: 0; segment id: 0; segment name: soma; Neuron loc: soma(0.5); value: bioPhys1_membraneProperties_naChans_naChan_m_q (m_q_naChan)
simConfig.recordTraces['of1_hhpop_0_soma_m_q_naChan'] = {'sec':'soma','loc':0.5,'var':'m_q_naChan','conds':{'popLabel':'hhpop','cellLabel':0}}
# Column: h: Pop: hhpop; cell: 0; segment id: 0; segment name: soma; Neuron loc: soma(0.5); value: bioPhys1_membraneProperties_naChans_naChan_h_q (h_q_naChan)
simConfig.recordTraces['of1_hhpop_0_soma_h_q_naChan'] = {'sec':'soma','loc':0.5,'var':'h_q_naChan','conds':{'popLabel':'hhpop','cellLabel':0}}
# Column: n: Pop: hhpop; cell: 0; segment id: 0; segment name: soma; Neuron loc: soma(0.5); value: bioPhys1_membraneProperties_kChans_kChan_n_q (n_q_kChan)
simConfig.recordTraces['of1_hhpop_0_soma_n_q_kChan'] = {'sec':'soma','loc':0.5,'var':'n_q_kChan','conds':{'popLabel':'hhpop','cellLabel':0}}


simConfig.plotCells = ['all']


simConfig.recordStim = True  # record spikes of cell stims
simConfig.recordStep = simConfig.dt # Step size in ms to save data (eg. V traces, LFP, etc)



# Analysis and plotting 
simConfig.plotRaster = True # Whether or not to plot a raster
simConfig.plotLFPSpectrum = False # plot power spectral density
simConfig.maxspikestoplot = 3e8 # Maximum number of spikes to plot
simConfig.plotConn = False # whether to plot conn matrix
simConfig.plotWeightChanges = False # whether to plot weight changes (shown in conn matrix)
#simConfig.plot3dArch = True # plot 3d architecture

# Saving
simConfig.filename = 'net1.txt'  # Set file output name
simConfig.saveFileStep = simConfig.dt # step size in ms to save data to disk
# simConfig.saveDat = True # save to dat file


###############################################################################
# IMPORT & RUN
###############################################################################

print("Running a NetPyNE based simulation for %sms (dt: %sms) at %s degC"%(simConfig.duration, simConfig.dt, h.celsius))

gids = sim.importNeuroML2SimulateAnalyze(nml2_file_name,simConfig)

print("Finished simulation")


###############################################################################
#   Saving data (this ensures the data gets saved in the format/files 
#   as specified in the LEMS <Simulation> element)
###############################################################################


if sim.rank==0: 
    print("Saving to file: results/ex5_v.dat (ref: of0)")

 
    # Column: t
    col_of0_t = [i*simConfig.dt for i in range(int(simConfig.duration/simConfig.dt))]

    # Column: v: Pop: hhpop; cell: 0; segment id: 0; segment name: soma; value: v
    col_of0_v = sim.allSimData['of0_hhpop_0_soma_v']['cell_%s'%gids['hhpop'][0]]

    dat_file_of0 = open('results/ex5_v.dat', 'w')
    for i in range(len(col_of0_t)):
        dat_file_of0.write( '%s\t'%(col_of0_t[i]/1000.0) +  '%s\t'%(col_of0_v[i]/1000.0) +  '\n')
    dat_file_of0.close()

    print("Saving to file: results/ex5_vars.dat (ref: of1)")

 
    # Column: t
    col_of1_t = [i*simConfig.dt for i in range(int(simConfig.duration/simConfig.dt))]

    # Column: m: Pop: hhpop; cell: 0; segment id: 0; segment name: soma; value: bioPhys1_membraneProperties_naChans_naChan_m_q
    col_of1_m = sim.allSimData['of1_hhpop_0_soma_m_q_naChan']['cell_%s'%gids['hhpop'][0]]

    # Column: h: Pop: hhpop; cell: 0; segment id: 0; segment name: soma; value: bioPhys1_membraneProperties_naChans_naChan_h_q
    col_of1_h = sim.allSimData['of1_hhpop_0_soma_h_q_naChan']['cell_%s'%gids['hhpop'][0]]

    # Column: n: Pop: hhpop; cell: 0; segment id: 0; segment name: soma; value: bioPhys1_membraneProperties_kChans_kChan_n_q
    col_of1_n = sim.allSimData['of1_hhpop_0_soma_n_q_kChan']['cell_%s'%gids['hhpop'][0]]

    dat_file_of1 = open('results/ex5_vars.dat', 'w')
    for i in range(len(col_of1_t)):
        dat_file_of1.write( '%s\t'%(col_of1_t[i]/1000.0) +  '%s\t'%(col_of1_m[i]/1.0) +  '%s\t'%(col_of1_h[i]/1.0) +  '%s\t'%(col_of1_n[i]/1.0) +  '\n')
    dat_file_of1.close()


    print("Saved all data.")

    quit()
