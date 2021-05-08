package tech.cypherskar.cloudex.templates.infos;

import java.time.Instant;

import tech.cypherskar.cloudex.components.cloudsim.ASimulation;
import tech.cypherskar.cloudex.components.window.Progress;

import org.cloudbus.cloudsim.Cloudlet;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**Responsible for carrying the simulations' results*/
public class ASimulationInfo
{
    /**Name of the simulation*/
    public String Name;

    /**Referred to as the last VM to finish a task*/
    public double Makespan;
    /**The benchmarking of the algorithm*/
    public long Algorithm_Time;
    /**The collective finish time of all VMs*/
    public double Vms_Makespan;
    /**How further from the mean this group of machines are.*/
    public double Standard_Deviation;
    /**Imbalance measure across all machines.*/
    public double Degree_Of_Imbalance;
	
    /**The benchmarking variable.*/
    public Instant lastTime;
    /**The algorithm/simulation to be run.*/
    public ASimulation algorithm;
    /**The progress bar displaying algorithm's updates.*/
    public Progress progress;
    /**The list that keeps received tasks to calculate all measures mentioned in this class.*/
    public ObjectArrayList<Cloudlet> recievedCloudlets;
}