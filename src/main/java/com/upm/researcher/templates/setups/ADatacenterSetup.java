package com.upm.researcher.templates.setups;

import java.util.LinkedList;

import com.upm.researcher.utils.NumberMixer.RandomStyle;

import org.cloudbus.cloudsim.Storage;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public class ADatacenterSetup 
{
    /**Number of machines with the same setup.*/
    public int clones;
    /**Name of the datacenter setup.*/
    public String name;
    /**Machine build architecture; x86 or x64.*/
    public String architecture;
    /**operating system.*/
    public String os;
    /**VIrtual machine monitor.*/
	public String vmm;
	
    /**Time zone.*/
	public double minTimeZone, maxTimeZone;
    /**Second cost.*/
	public double minSecCost, maxSecCost;
    /**Memory cost.*/
	public double minMemCost, maxMemCost;
    /**Storage cost.*/
	public double minStorageCost, maxStorageCost;
    /**Bandwidth cost.*/
	public double minBwCost, maxBwCost;
    /**Scheduling interval.*/
	public double minInterval, maxInterval;
    /**The random number generation style if any*/
	public RandomStyle randStyle;
    
    /**Time zone array filled during runtime.*/
    public double[] timeZones;
    /**Second cost array filled during runtime.*/
    public double[] secCosts;
    /**Memory cost array filled during runtime.*/
    public double[] memCosts;
    /**Storage cost array filled during runtime.*/
    public double[] storageCosts;
    /**Bandwidth cost array filled during runtime.*/
    public double[] BwCosts;
    /**Scheduling interval array filled during runtime.*/
    public double[] intervals;
    public LinkedList<Storage> storageList;
    
    /** Host Ids to be deployed on this datacenter.*/
    public IntArrayList hostIndeces;
}